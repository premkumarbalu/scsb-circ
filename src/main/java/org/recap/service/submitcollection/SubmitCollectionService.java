package org.recap.service.submitcollection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.marc4j.MarcException;
import org.marc4j.marc.Record;
import org.recap.ReCAPConstants;
import org.recap.converter.MarcToBibEntityConverter;
import org.recap.converter.SCSBToBibEntityConverter;
import org.recap.converter.XmlToBibEntityConverterInterface;
import org.recap.model.BibliographicEntity;
import org.recap.model.ReportDataEntity;
import org.recap.model.ReportEntity;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.model.report.SubmitCollectionReportInfo;
import org.recap.model.submitcollection.SubmitCollectionResponse;
import org.recap.service.common.RepositoryService;
import org.recap.util.MarcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import java.util.*;

/**
 * Created by premkb on 20/12/16.
 */
@Service
public class SubmitCollectionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SubmitCollectionReportHelperService submitCollectionReportHelperService;

    @Autowired
    private SubmitCollectionDAOService submitCollectionDAOService;

    @Autowired
    private MarcToBibEntityConverter marcToBibEntityConverter;

    @Autowired
    private SCSBToBibEntityConverter scsbToBibEntityConverter;

    @Autowired
    private MarcUtil marcUtil;

    private RestTemplate restTemplate;

    @Value("${scsb.solr.client.url}")
    private String scsbSolrClientUrl;

    @Value("${submit.collection.input.limit}")
    private Integer inputLimit;

    /**
     * Process string.
     *
     * @param inputRecords       the input records
     * @param processedBibIdList the processed bib id list
     * @param idMapToRemoveIndex the id map to remove index
     * @param xmlFileName        the xml file name
     * @return the string
     */
    @Transactional
    public List<SubmitCollectionResponse> process(String inputRecords, List<Integer> processedBibIdList, Map<String,String> idMapToRemoveIndex, String xmlFileName, List<Integer> reportRecordNumberList) {
        logger.info("Input record processing started");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String reponse = null;
        List<SubmitCollectionResponse> submitColletionResponseList = new ArrayList<>();
        Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap = getSubmitCollectionReportMap();
        try {
            if (!"".equals(inputRecords)) {
                if (inputRecords.contains(ReCAPConstants.BIBRECORD_TAG)) {
                    reponse = processSCSB(inputRecords, processedBibIdList, submitCollectionReportInfoMap, idMapToRemoveIndex);
                } else {
                    reponse = processMarc(inputRecords, processedBibIdList, submitCollectionReportInfoMap, idMapToRemoveIndex);
                }
                if (reponse != null){//This happens when there is a failure
                    setResponse(reponse, submitColletionResponseList);
                    submitCollectionReportHelperService.setSubmitCollectionReportInfoForInvalidXml(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),reponse);
                    generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_FAILURE_REPORT, xmlFileName,reportRecordNumberList);
                    return submitColletionResponseList;
                }
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_REPORT, xmlFileName,reportRecordNumberList);
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_FAILURE_REPORT, xmlFileName,reportRecordNumberList);
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_REJECTION_REPORT, xmlFileName,reportRecordNumberList);
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_REPORT, xmlFileName,reportRecordNumberList);
                getResponseMessage(submitCollectionReportInfoMap,submitColletionResponseList);
            }
        }catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR, e);
            reponse = ReCAPConstants.SUBMIT_COLLECTION_INTERNAL_ERROR;
        }
        setResponse(reponse, submitColletionResponseList);
        stopWatch.stop();
        logger.info("total time take for processing input record {}", stopWatch.getTotalTimeSeconds());
        return submitColletionResponseList;
    }

    private void setResponse(String reponse, List<SubmitCollectionResponse> submitColletionResponseList) {
        if(reponse != null){
            SubmitCollectionResponse submitCollectionResponse = new SubmitCollectionResponse();
            submitCollectionResponse.setMessage(reponse);
            submitColletionResponseList.add(submitCollectionResponse);
        }
    }

    private List<SubmitCollectionResponse> getResponseMessage(Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<SubmitCollectionResponse> submitColletionResponseList){
        for (Map.Entry<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMapEntry : submitCollectionReportInfoMap.entrySet()) {
            List<SubmitCollectionReportInfo> submitCollectionReportInfoList = submitCollectionReportInfoMapEntry.getValue();
            for(SubmitCollectionReportInfo submitCollectionReportInfo:submitCollectionReportInfoList){
                SubmitCollectionResponse submitCollectionResponse = new SubmitCollectionResponse();
                setSubmitCollectionResponse(submitCollectionReportInfo,submitColletionResponseList,submitCollectionResponse);
            }
        }
        return submitColletionResponseList;
    }

    private String processMarc(String inputRecords, List<Integer> processedBibIdList, Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, Map<String,String> idMapToRemoveIndex) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String format;
        format = ReCAPConstants.FORMAT_MARC;
        List<Record> records = null;
        try {
            records = getMarcUtil().convertMarcXmlToRecord(inputRecords);
            if(records.size() > inputLimit){
                return ReCAPConstants.SUBMIT_COLLECTION_LIMIT_EXCEED_MESSAGE + inputLimit;
            }
        } catch (Exception e) {
            logger.info(String.valueOf(e.getCause()));
            logger.error(ReCAPConstants.LOG_ERROR,e);
            return ReCAPConstants.INVALID_MARC_XML_FORMAT_MESSAGE;
        }
        if (CollectionUtils.isNotEmpty(records)) {
            int count = 1;
            for (Record record : records) {
                logger.info("Processing record no: {}",count);
                BibliographicEntity bibliographicEntity = loadData(record, format, submitCollectionReportInfoMap,idMapToRemoveIndex);
                if (null!=bibliographicEntity && null != bibliographicEntity.getBibliographicId()) {
                    processedBibIdList.add(bibliographicEntity.getBibliographicId());
                }
                logger.info("Processing completed for record no: {}",count);
                count ++;
            }
        }
        stopWatch.stop();
        logger.info("Total time take {}",stopWatch.getTotalTimeSeconds());
        return null;
    }

    private String processSCSB(String inputRecords, List<Integer> processedBibIdList, Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, Map<String, String> idMapToRemoveIndex) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String format;
        format = ReCAPConstants.FORMAT_SCSB;
        BibRecords bibRecords = null;
        try {
            bibRecords = (BibRecords) JAXBHandler.getInstance().unmarshal(inputRecords, BibRecords.class);
            logger.info("bibrecord size {}", bibRecords.getBibRecords().size());
            if (bibRecords.getBibRecords().size() > inputLimit) {
                return ReCAPConstants.SUBMIT_COLLECTION_LIMIT_EXCEED_MESSAGE + " " + inputLimit;
            }
        } catch (JAXBException e) {
            logger.info(String.valueOf(e.getCause()));
            logger.error(ReCAPConstants.LOG_ERROR, e);
            return ReCAPConstants.INVALID_SCSB_XML_FORMAT_MESSAGE;
        }
        int count = 1;
        for (BibRecord bibRecord : bibRecords.getBibRecords()) {
            logger.info("Processing Bib record no: {}",count);
            try {
                BibliographicEntity bibliographicEntity = loadData(bibRecord, format, submitCollectionReportInfoMap, idMapToRemoveIndex);
                if (null!=bibliographicEntity && null != bibliographicEntity.getBibliographicId()) {
                    processedBibIdList.add(bibliographicEntity.getBibliographicId());
                }
            } catch (MarcException me) {
                logger.error(ReCAPConstants.LOG_ERROR,me);
                return ReCAPConstants.INVALID_MARC_XML_FORMAT_IN_SCSBXML_MESSAGE;
            } catch (ResourceAccessException rae){
                logger.error(ReCAPConstants.LOG_ERROR,rae);
                return ReCAPConstants.SCSB_SOLR_CLIENT_SERVICE_UNAVAILABLE;
            }
            logger.info("Process completed for Bib record no: {}",count);
            count ++;
        }
        logger.info("Total time take {}",stopWatch.getTotalTimeSeconds());
        return null;
    }

    private BibliographicEntity loadData(Object record, String format, Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, Map<String,String> idMapToRemoveIndex){
        BibliographicEntity savedBibliographicEntity = null;
        Map responseMap = getConverter(format).convert(record);
        BibliographicEntity bibliographicEntity = (BibliographicEntity) responseMap.get("bibliographicEntity");
        List<ReportEntity> reportEntityList = (List<ReportEntity>) responseMap.get("reportEntities");
        if (CollectionUtils.isNotEmpty(reportEntityList)) {
            repositoryService.getReportDetailRepository().save(reportEntityList);
        }
        if (bibliographicEntity != null) {
            savedBibliographicEntity = submitCollectionDAOService.updateBibliographicEntity(bibliographicEntity, submitCollectionReportInfoMap,idMapToRemoveIndex);
        }
        return savedBibliographicEntity;
    }

    /**
     * Index data string.
     *
     * @param bibliographicIdList the bibliographic id list
     * @return the string
     */
    public String indexData(List<Integer> bibliographicIdList){
        return getRestTemplate().postForObject(scsbSolrClientUrl + "solrIndexer/indexByBibliographicId", bibliographicIdList, String.class);
    }

    /**
     * Remove solr index string.
     *
     * @param idMapToRemoveIndex the id map to remove index
     * @return the string
     */
    public String removeSolrIndex(Map idMapToRemoveIndex){
        return getRestTemplate().postForObject(scsbSolrClientUrl + "solrIndexer/deleteByBibHoldingItemId", idMapToRemoveIndex, String.class);
    }

    private void setSubmitCollectionResponse(SubmitCollectionReportInfo submitCollectionReportInfo, List<SubmitCollectionResponse> submitColletionResponseList, SubmitCollectionResponse submitCollectionResponse){
        submitCollectionResponse.setItemBarcode(submitCollectionReportInfo.getItemBarcode());
        submitCollectionResponse.setMessage(submitCollectionReportInfo.getMessage());
        submitColletionResponseList.add(submitCollectionResponse);
    }

    private XmlToBibEntityConverterInterface getConverter(String format){
        if(format.equalsIgnoreCase(ReCAPConstants.FORMAT_MARC)){
            return marcToBibEntityConverter;
        } else if(format.equalsIgnoreCase(ReCAPConstants.FORMAT_SCSB)){
            return scsbToBibEntityConverter;
        }
        return null;
    }

    /**
     * Generate submit collection report.
     *
     * @param submitCollectionReportList the submit collection report list
     * @param fileName                   the file name
     * @param reportType                 the report type
     * @param xmlFileName                the xml file name
     */
    public void generateSubmitCollectionReport(List<SubmitCollectionReportInfo> submitCollectionReportList, String fileName, String reportType, String xmlFileName, List<Integer> reportRecordNumberList){
        logger.info("Preparing report entities");
        if(submitCollectionReportList != null && !submitCollectionReportList.isEmpty()){
            try {
                int count = 1;
                for(SubmitCollectionReportInfo submitCollectionReportInfo : submitCollectionReportList){
                    ReportEntity reportEntity = new ReportEntity();
                    List<ReportDataEntity> reportDataEntities = new ArrayList<>();
                    String owningInstitution = submitCollectionReportList.get(0).getOwningInstitution();
                    if(!submitCollectionReportList.isEmpty()){
                        if(!StringUtils.isEmpty(xmlFileName)) {
                            reportEntity.setFileName(fileName + "-" + xmlFileName);
                        }else{
                            reportEntity.setFileName(fileName);
                        }
                        reportEntity.setType(reportType);
                        reportEntity.setCreatedDate(new Date());
                        reportEntity.setInstitutionName(owningInstitution);
                    }
                    logger.info("Processing report for record {}",count);
                    if(submitCollectionReportInfo.getItemBarcode() != null){

                        ReportDataEntity itemBarcodeReportDataEntity = new ReportDataEntity();
                        itemBarcodeReportDataEntity.setHeaderName(ReCAPConstants.SUBMIT_COLLECTION_ITEM_BARCODE);
                        itemBarcodeReportDataEntity.setHeaderValue(submitCollectionReportInfo.getItemBarcode());
                        reportDataEntities.add(itemBarcodeReportDataEntity);

                        ReportDataEntity customerCodeReportDataEntity = new ReportDataEntity();
                        customerCodeReportDataEntity.setHeaderName(ReCAPConstants.SUBMIT_COLLECTION_CUSTOMER_CODE);
                        customerCodeReportDataEntity.setHeaderValue(submitCollectionReportInfo.getCustomerCode()!=null?submitCollectionReportInfo.getCustomerCode():"");
                        reportDataEntities.add(customerCodeReportDataEntity);

                        ReportDataEntity owningInstitutionReportDataEntity = new ReportDataEntity();
                        owningInstitutionReportDataEntity.setHeaderName(ReCAPConstants.OWNING_INSTITUTION);
                        owningInstitutionReportDataEntity.setHeaderValue(owningInstitution);
                        reportDataEntities.add(owningInstitutionReportDataEntity);

                        ReportDataEntity messageReportDataEntity = new ReportDataEntity();
                        messageReportDataEntity.setHeaderName(ReCAPConstants.MESSAGE);
                        messageReportDataEntity.setHeaderValue(submitCollectionReportInfo.getMessage());
                        reportDataEntities.add(messageReportDataEntity);

                        reportEntity.setReportDataEntities(reportDataEntities);
                        ReportEntity savedReportEntity = repositoryService.getReportDetailRepository().save(reportEntity);
                        count ++;
                        reportRecordNumberList.add(savedReportEntity.getRecordNumber());
                    }
                    logger.info("Processed completed report for record {}",count);
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
    }

    /**
     * Gets marc util object which is used to perform read, write, convert operation on marc object.
     *
     * @return the marc util
     */
    public MarcUtil getMarcUtil() {
        return marcUtil;
    }

    /**
     * Sets marc util.
     *
     * @param marcUtil the marc util
     */
    public void setMarcUtil(MarcUtil marcUtil) {
        this.marcUtil = marcUtil;
    }

    /**
     * Gets rest template.
     *
     * @return the rest template
     */
    public RestTemplate getRestTemplate() {
        if(restTemplate == null){
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }

    /**
     * Sets rest template.
     *
     * @param restTemplate the rest template
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private Map getSubmitCollectionReportMap(){
        List<SubmitCollectionReportInfo> submitCollectionSuccessInfoList = new ArrayList<>();
        List<SubmitCollectionReportInfo> submitCollectionFailureInfoList = new ArrayList<>();
        List<SubmitCollectionReportInfo> submitCollectionRejectionInfoList = new ArrayList<>();
        List<SubmitCollectionReportInfo> submitCollectionExceptionInfoList = new ArrayList<>();
        Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap = new HashMap<>();
        submitCollectionReportInfoMap.put(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST,submitCollectionSuccessInfoList);
        submitCollectionReportInfoMap.put(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST,submitCollectionFailureInfoList);
        submitCollectionReportInfoMap.put(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST,submitCollectionRejectionInfoList);
        submitCollectionReportInfoMap.put(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_LIST,submitCollectionExceptionInfoList);
        return submitCollectionReportInfoMap;
    }

    public void generateSubmitCollectionReportFile(List<Integer> reportRecordNumberList) {
        getRestTemplate().postForObject(scsbSolrClientUrl + "generateReportService/generateSubmitCollectionReport", reportRecordNumberList, String.class);

    }
}