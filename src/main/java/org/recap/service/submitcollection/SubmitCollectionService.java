package org.recap.service.submitcollection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.marc4j.MarcException;
import org.marc4j.marc.Record;
import org.recap.ReCAPConstants;
import org.recap.converter.MarcToBibEntityConverter;
import org.recap.converter.SCSBToBibEntityConverter;
import org.recap.converter.XmlToBibEntityConverterInterface;
import org.recap.model.*;
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
    private SubmitCollectionHelperService submitCollectionHelperService;

/*    @Autowired
    private SubmitCollectionDAOService submitCollectionDAOService;*/

/*    @Autowired
    private MarcToBibEntityConverter marcToBibEntityConverter;

    @Autowired
    private SCSBToBibEntityConverter scsbToBibEntityConverter;*/

    @Autowired
    private SubmitCollectionValidationService validationService;

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
     * @param processedBibIds the processed bib id list
     * @param idMapToRemoveIndexList the id map to remove index
     * @param xmlFileName        the xml file name
     * @param checkLimit
     * @return the string
     */
    @Transactional
    public List<SubmitCollectionResponse> process(String institutionCode, String inputRecords, Set<Integer> processedBibIds, List<Map<String, String>> idMapToRemoveIndexList, String xmlFileName, List<Integer> reportRecordNumberList, boolean checkLimit,boolean isCGDProtected) {
        logger.info("Submit Collection : Input record processing started");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String response = null;
        List<SubmitCollectionResponse> submitColletionResponseList = new ArrayList<>();
        boolean isValidToProcess = validationService.validateInstitution(institutionCode);
        if (isValidToProcess) {
            Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap = getSubmitCollectionReportMap();
            InstitutionEntity institutionEntity = repositoryService.getInstitutionDetailsRepository().findByInstitutionCode(institutionCode);
            try {
                if (!"".equals(inputRecords)) {
                    if (inputRecords.contains(ReCAPConstants.BIBRECORD_TAG)) {
                        response = processSCSB(inputRecords, processedBibIds, submitCollectionReportInfoMap, idMapToRemoveIndexList, checkLimit,isCGDProtected,institutionEntity);
                    } else {
                        response = processMarc(inputRecords, processedBibIds, submitCollectionReportInfoMap, idMapToRemoveIndexList, checkLimit,isCGDProtected,institutionEntity);
                    }
                    if (response != null){//This happens when there is a failure
                        setResponse(response, submitColletionResponseList);
                        submitCollectionReportHelperService.setSubmitCollectionReportInfoForInvalidXml(institutionCode,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),response);
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
                response = ReCAPConstants.SUBMIT_COLLECTION_INTERNAL_ERROR;
            }
            setResponse(response, submitColletionResponseList);
            stopWatch.stop();
            logger.info("Submit Collection : total time take for processing input record and saving to DB {}", stopWatch.getTotalTimeSeconds());
        } else {
            SubmitCollectionResponse submitCollectionResponse = new SubmitCollectionResponse();
            submitCollectionResponse.setItemBarcode("");
            submitCollectionResponse.setMessage("Please provide valid institution code");
            submitColletionResponseList.add(submitCollectionResponse);
        }
        return submitColletionResponseList;
    }

    private void setResponse(String response, List<SubmitCollectionResponse> submitColletionResponseList) {
        if(response != null){
            SubmitCollectionResponse submitCollectionResponse = new SubmitCollectionResponse();
            submitCollectionResponse.setMessage(response);
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

    private String processMarc(String inputRecords, Set<Integer> processedBibIds, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String, String>> idMapToRemoveIndexList, boolean checkLimit
            ,boolean isCGDProtected,InstitutionEntity institutionEntity) throws Exception{
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String format;
        format = ReCAPConstants.FORMAT_MARC;
        List<Record> records = null;
        try {
            records = getMarcUtil().convertMarcXmlToRecord(inputRecords);
            if(checkLimit && records.size() > inputLimit){
                return ReCAPConstants.SUBMIT_COLLECTION_LIMIT_EXCEED_MESSAGE + inputLimit;
            }
        } catch (Exception e) {
            logger.info(String.valueOf(e.getCause()));
            logger.error(ReCAPConstants.LOG_ERROR,e);
            return ReCAPConstants.INVALID_MARC_XML_FORMAT_MESSAGE;
        }
        if (CollectionUtils.isNotEmpty(records)) {
            int count = 1;
            Set<String> processedBarcodeSet = new HashSet<>();
/*            for (Record record : records) {
                logger.info("Processing record no: {}",count);
                BibliographicEntity bibliographicEntity = submitCollectionHelperService.loadData(record, format, submitCollectionReportInfoMap,idMapToRemoveIndexList,isCGDProtection,institutionEntity,processedBarcodeSet);
                if (null!=bibliographicEntity && null != bibliographicEntity.getBibliographicId()) {
                    processedBibIds.add(bibliographicEntity.getBibliographicId());
                }
                logger.info("Processing completed for record no: {}",count);
                count ++;
            }*/
            Set<String> processedBarcodeSetForDummyRecords = new HashSet<>();
            String response = loadRecord(processedBibIds, submitCollectionReportInfoMap, idMapToRemoveIndexList, isCGDProtected, institutionEntity, format, records, count, processedBarcodeSetForDummyRecords);
            if (response != null) return response;
        }
        stopWatch.stop();
        logger.info("Total time take {}",stopWatch.getTotalTimeSeconds());
        return null;
    }

    private String processSCSB(String inputRecords, Set<Integer> processedBibIds, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap,
                               List<Map<String, String>> idMapToRemoveIndexList, boolean checkLimit,boolean isCGDProtected,InstitutionEntity institutionEntity) throws Exception{
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String format;
        format = ReCAPConstants.FORMAT_SCSB;
        BibRecords bibRecords = null;
        try {
            bibRecords = (BibRecords) JAXBHandler.getInstance().unmarshal(inputRecords, BibRecords.class);
            logger.info("bibrecord size {}", bibRecords.getBibRecords().size());
            if (checkLimit && bibRecords.getBibRecords().size() > inputLimit) {
                return ReCAPConstants.SUBMIT_COLLECTION_LIMIT_EXCEED_MESSAGE + " " + inputLimit;
            }
        } catch (JAXBException e) {
            logger.info(String.valueOf(e.getCause()));
            logger.error(ReCAPConstants.LOG_ERROR, e);
            return ReCAPConstants.INVALID_SCSB_XML_FORMAT_MESSAGE;
        }
        int count = 1;
        Set<String> processedBarcodeSetForDummyRecords = new HashSet<>();
        String response = loadBibRecord(processedBibIds, submitCollectionReportInfoMap, idMapToRemoveIndexList, isCGDProtected, institutionEntity, format, bibRecords, count, processedBarcodeSetForDummyRecords);
        if (response != null) return response;
        logger.info("Total time take {}",stopWatch.getTotalTimeSeconds());
        return null;
    }

    public String loadBibRecord(Set<Integer> processedBibIds, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String, String>> idMapToRemoveIndexList, boolean isCGDProtected, InstitutionEntity institutionEntity, String format, BibRecords bibRecords, int count, Set<String> processedBarcodeSetForDummyRecords) throws Exception{
        for (BibRecord bibRecord : bibRecords.getBibRecords()) {
            logger.info("Processing Bib record no: {}",count);
            try {
                BibliographicEntity bibliographicEntity = submitCollectionHelperService.loadData(bibRecord, format, submitCollectionReportInfoMap, idMapToRemoveIndexList,isCGDProtected,institutionEntity,processedBarcodeSetForDummyRecords);
                if (null!=bibliographicEntity && null != bibliographicEntity.getBibliographicId()) {
                    processedBibIds.add(bibliographicEntity.getBibliographicId());
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
        return null;
    }

    public String loadRecord(Set<Integer> processedBibIds, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String, String>> idMapToRemoveIndexList, boolean isCGDProtected, InstitutionEntity institutionEntity, String format,
                                List<Record> recordList, int count, Set<String> processedBarcodeSetForDummyRecords) throws Exception{
        for (Record record : recordList) {
            logger.info("Processing Bib record no: {}",count);
            try {
                BibliographicEntity bibliographicEntity = submitCollectionHelperService.loadData(record, format, submitCollectionReportInfoMap, idMapToRemoveIndexList,isCGDProtected,institutionEntity,processedBarcodeSetForDummyRecords);
                if (null!=bibliographicEntity && null != bibliographicEntity.getBibliographicId()) {
                    processedBibIds.add(bibliographicEntity.getBibliographicId());
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
        return null;
    }

/*    public BibliographicEntity loadData(Object record, String format, Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String,String>> idMapToRemoveIndexList
            ,boolean isCGDProtected,InstitutionEntity institutionEntity,Set<String> processedBarcodeSetForDummyRecords){
        BibliographicEntity savedBibliographicEntity = null;
        BibliographicEntity bibliographicEntity = null;
        try {
            Map responseMap = getConverter(format).convert(record,institutionEntity);
            StringBuilder errorMessage = (StringBuilder)responseMap.get("errorMessage");
            bibliographicEntity = responseMap.get("bibliographicEntity") != null ? (BibliographicEntity) responseMap.get("bibliographicEntity"):null;
            if (errorMessage != null && errorMessage.length()==0) {
                setCGDProtectionForItems(bibliographicEntity,isCGDProtected);
                if (bibliographicEntity != null) {
                    savedBibliographicEntity = submitCollectionDAOService.updateBibliographicEntity(bibliographicEntity, submitCollectionReportInfoMap,idMapToRemoveIndexList,processedBarcodeSetForDummyRecords);
                }
            } else {
                logger.error("Error while parsing xml for a barcode in submit collection");
                submitCollectionReportHelperService.setSubmitCollectionFailureReportForUnexpectedException(bibliographicEntity,
                        submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),"Failed record - Item not updated - "+errorMessage.toString(),institutionEntity);
            }
        } catch (Exception e) {
            submitCollectionReportHelperService.setSubmitCollectionFailureReportForUnexpectedException(bibliographicEntity,
                    submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),"Failed record - Item not updated - "+e.getMessage(),institutionEntity);
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }
        return savedBibliographicEntity;
    }

    private void setCGDProtectionForItems(BibliographicEntity bibliographicEntity,boolean isCGDProtected){
        if(bibliographicEntity != null && bibliographicEntity.getHoldingsEntities() != null){
            for(HoldingsEntity holdingsEntity : bibliographicEntity.getHoldingsEntities()){
                if (holdingsEntity.getItemEntities() != null){
                    for (ItemEntity itemEntity : holdingsEntity.getItemEntities()){
                        itemEntity.setCgdProtection(isCGDProtected);
                    }
                }
            }
        }
    }*/

    /**
     * Index data string.
     *
     * @param bibliographicIdList the bibliographic id list
     * @return the string
     */
    public String indexData(Set<Integer> bibliographicIdList){
        return getRestTemplate().postForObject(scsbSolrClientUrl + "solrIndexer/indexByBibliographicId", bibliographicIdList, String.class);
    }

    /**
     * Remove solr index string.
     *
     * @param idMapToRemoveIndexList the id map to remove index
     * @return the string
     */
    public String removeSolrIndex(List<Map<String,String> >idMapToRemoveIndexList){
        return getRestTemplate().postForObject(scsbSolrClientUrl + "solrIndexer/deleteByBibHoldingItemId", idMapToRemoveIndexList, String.class);
    }

    private void setSubmitCollectionResponse(SubmitCollectionReportInfo submitCollectionReportInfo, List<SubmitCollectionResponse> submitColletionResponseList, SubmitCollectionResponse submitCollectionResponse){
        submitCollectionResponse.setItemBarcode(submitCollectionReportInfo.getItemBarcode());
        submitCollectionResponse.setMessage(submitCollectionReportInfo.getMessage());
        submitColletionResponseList.add(submitCollectionResponse);
    }

/*    private XmlToBibEntityConverterInterface getConverter(String format){
        if(format.equalsIgnoreCase(ReCAPConstants.FORMAT_MARC)){
            return marcToBibEntityConverter;
        } else if(format.equalsIgnoreCase(ReCAPConstants.FORMAT_SCSB)){
            return scsbToBibEntityConverter;
        }
        return null;
    }*/

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