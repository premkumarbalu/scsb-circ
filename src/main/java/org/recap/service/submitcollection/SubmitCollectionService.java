package org.recap.service.submitcollection;

import org.apache.commons.collections.CollectionUtils;
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
import org.recap.repository.*;
import org.recap.util.MarcUtil;
import org.recap.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private CustomerCodeDetailsRepository customerCodeDetailsRepository;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private MarcToBibEntityConverter marcToBibEntityConverter;

    @Autowired
    private SCSBToBibEntityConverter scsbToBibEntityConverter;

    @Autowired
    private ReportDetailRepository reportDetailRepository;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    @Autowired
    private MarcUtil marcUtil;

    @PersistenceContext
    private EntityManager entityManager;

    private RestTemplate restTemplate;

    private Map<Integer,String> itemStatusIdCodeMap;

    private Map<String,Integer> itemStatusCodeIdMap;

    private Map<Integer,String> institutionEntityMap;

    @Value("${server.protocol}")
    private String serverProtocol;

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
    public String process(String inputRecords, List<Integer> processedBibIdList,Map<String,String> idMapToRemoveIndex,String xmlFileName) {
        logger.info("Input record processing started");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String reponse = null;
        String finalXmlString = XMLUtils.escapeTextAroundXMLTags(inputRecords);
        Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap = getSubmitCollectionReportMap();
        try {
            if (!"".equals(inputRecords)) {
                if (inputRecords.contains(ReCAPConstants.BIBRECORD_TAG)) {
                    reponse = processSCSB(finalXmlString, processedBibIdList, submitCollectionReportInfoMap, idMapToRemoveIndex);
                    if (reponse != null)
                        return reponse;
                } else {
                    reponse = processMarc(finalXmlString, processedBibIdList, submitCollectionReportInfoMap, idMapToRemoveIndex);
                    if (reponse != null)
                        return reponse;
                }
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_REPORT, xmlFileName);
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_FAILURE_REPORT, xmlFileName);
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_REJECTION_REPORT, xmlFileName);
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_REPORT, xmlFileName);
                reponse = getResponseMessage(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST), submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_LIST), processedBibIdList);
            }
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR, e);
            reponse = ReCAPConstants.SUBMIT_COLLECTION_INTERNAL_ERROR;
        }
        stopWatch.stop();
        logger.info("total time take for processing input record {}", stopWatch.getTotalTimeSeconds());
        return reponse;
    }

    private String getResponseMessage(List<SubmitCollectionReportInfo> submitCollectionRejectionInfos,List<SubmitCollectionReportInfo> submitCollectionExceptionInfos,
                                      List<Integer> processedBibIdList){
        String responseMessage;
        if(!processedBibIdList.isEmpty()){
            responseMessage = ReCAPConstants.SUCCESS+", "+ReCAPConstants.SUMBIT_COLLECTION_UPDATE_MESSAGE;
        } else{
            responseMessage = ReCAPConstants.SUMBIT_COLLECTION_NOT_UPDATED_MESSAGE;
        }
        if(!submitCollectionRejectionInfos.isEmpty()){
            if (null != responseMessage) {
                responseMessage = responseMessage + ", " +ReCAPConstants.SUBMIT_COLLECTION_REJECTION_REPORT_MESSAGE;
            } else {
                responseMessage = ReCAPConstants.SUBMIT_COLLECTION_REJECTION_REPORT_MESSAGE;
            }
        }
        if(!submitCollectionExceptionInfos.isEmpty()){
            if(null != responseMessage){
                responseMessage = responseMessage + ", " +ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_REPORT_MESSAGE;
            } else{
                responseMessage = ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_REPORT_MESSAGE;
            }
        }
        return responseMessage;
    }

    private String processMarc(String inputRecords, List<Integer> processedBibIdList,Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap,Map<String,String> idMapToRemoveIndex) {
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
                logger.info("Process record no: {}",count);
                BibliographicEntity bibliographicEntity = loadData(record, format, submitCollectionReportInfoMap,idMapToRemoveIndex);
                if (null!=bibliographicEntity && null != bibliographicEntity.getBibliographicId()) {
                    processedBibIdList.add(bibliographicEntity.getBibliographicId());
                }
                logger.info("Process completed for record no: {}",count);
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
            logger.info("Process record no: {}",count);
            BibliographicEntity bibliographicEntity = loadData(bibRecord, format, submitCollectionReportInfoMap, idMapToRemoveIndex);
            if (null!=bibliographicEntity && null != bibliographicEntity.getBibliographicId()) {
                processedBibIdList.add(bibliographicEntity.getBibliographicId());
            }
            logger.info("Process completed for record no: {}",count);
            count ++;
        }
        logger.info("Total time take {}",stopWatch.getTotalTimeSeconds());
        return null;
    }


    private BibliographicEntity loadData(Object record, String format, Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap,Map<String,String> idMapToRemoveIndex){
        BibliographicEntity savedBibliographicEntity = null;
        Map responseMap = getConverter(format).convert(record);
        BibliographicEntity bibliographicEntity = (BibliographicEntity) responseMap.get("bibliographicEntity");
        List<ReportEntity> reportEntityList = (List<ReportEntity>) responseMap.get("reportEntities");
        if (CollectionUtils.isNotEmpty(reportEntityList)) {
            reportDetailRepository.save(reportEntityList);
        }
        if (bibliographicEntity != null) {
            savedBibliographicEntity = updateBibliographicEntity(bibliographicEntity, submitCollectionReportInfoMap,idMapToRemoveIndex);
        }
        return savedBibliographicEntity;
    }

    private void saveItemChangeLogEntity(String operationType, String message, List<ItemEntity> itemEntityList) {
        List<ItemChangeLogEntity> itemChangeLogEntityList = new ArrayList<>();
        for (ItemEntity itemEntity:itemEntityList) {
            ItemChangeLogEntity itemChangeLogEntity = new ItemChangeLogEntity();
            itemChangeLogEntity.setOperationType(ReCAPConstants.SUBMIT_COLLECTION);
            itemChangeLogEntity.setUpdatedBy(operationType);
            itemChangeLogEntity.setUpdatedDate(new Date());
            itemChangeLogEntity.setRecordId(itemEntity.getItemId());
            itemChangeLogEntity.setNotes(message);
            itemChangeLogEntityList.add(itemChangeLogEntity);
        }
        itemChangeLogDetailsRepository.save(itemChangeLogEntityList);
    }

    /**
     * Set submit collection rejection info.
     *
     * @param bibliographicEntity            the bibliographic entity
     * @param submitCollectionRejectionInfos the submit collection rejection infos
     */
    public void setSubmitCollectionRejectionInfo(BibliographicEntity bibliographicEntity,List<SubmitCollectionReportInfo> submitCollectionRejectionInfos){
        for(ItemEntity itemEntity : bibliographicEntity.getItemEntities()){
            ItemStatusEntity itemStatusEntity = getItemStatusDetailsRepository().findByItemStatusId(itemEntity.getItemAvailabilityStatusId());
            if(!itemStatusEntity.getStatusCode().equalsIgnoreCase(ReCAPConstants.ITEM_STATUS_AVAILABLE)){
                SubmitCollectionReportInfo submitCollectionRejectionInfo = new SubmitCollectionReportInfo();
                submitCollectionRejectionInfo.setItemBarcode(itemEntity.getBarcode());
                submitCollectionRejectionInfo.setCustomerCode(itemEntity.getCustomerCode());
                submitCollectionRejectionInfo.setOwningInstitution((String) getInstitutionEntityMap().get(itemEntity.getOwningInstitutionId()));
                submitCollectionRejectionInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_RECORD);
                submitCollectionRejectionInfos.add(submitCollectionRejectionInfo);
            }
        }
    }

    private void setSubmitCollectionReportInfo(BibliographicEntity bibliographicEntity, List<SubmitCollectionReportInfo> submitCollectionExceptionInfos, String message) {
        for (ItemEntity itemEntity : bibliographicEntity.getItemEntities()) {
            SubmitCollectionReportInfo submitCollectionExceptionInfo = new SubmitCollectionReportInfo();
            submitCollectionExceptionInfo.setItemBarcode(itemEntity.getBarcode());
            submitCollectionExceptionInfo.setCustomerCode(itemEntity.getCustomerCode());
            submitCollectionExceptionInfo.setOwningInstitution((String) getInstitutionEntityMap().get(itemEntity.getOwningInstitutionId()));
            submitCollectionExceptionInfo.setMessage(message);
            submitCollectionExceptionInfos.add(submitCollectionExceptionInfo);
        }
    }

    /**
     * Index data string.
     *
     * @param bibliographicIdList the bibliographic id list
     * @return the string
     */
    public String indexData(List<Integer> bibliographicIdList){
        return getRestTemplate().postForObject(serverProtocol + scsbSolrClientUrl + "solrIndexer/indexByBibliographicId", bibliographicIdList, String.class);
    }

    /**
     * Remove solr index string.
     *
     * @param idMapToRemoveIndex the id map to remove index
     * @return the string
     */
    public String removeSolrIndex(Map idMapToRemoveIndex){
        return getRestTemplate().postForObject(serverProtocol + scsbSolrClientUrl + "solrIndexer/deleteByBibHoldingItemId", idMapToRemoveIndex, String.class);
    }

    /**
     * Update bibliographic entity bibliographic entity.
     *
     * @param bibliographicEntity           the bibliographic entity
     * @param submitCollectionReportInfoMap the submit collection report info map
     * @param idMapToRemoveIndex            the id map to remove index
     * @return the bibliographic entity
     */
    public BibliographicEntity updateBibliographicEntity(BibliographicEntity bibliographicEntity,Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap,Map<String,String> idMapToRemoveIndex) {
        BibliographicEntity savedBibliographicEntity;
        BibliographicEntity fetchBibliographicEntity = getBibEntityUsingBarcode(bibliographicEntity);
        if(fetchBibliographicEntity != null ){//update exisiting complete record
            if(fetchBibliographicEntity.getOwningInstitutionBibId().equals(bibliographicEntity.getOwningInstitutionBibId())){
                savedBibliographicEntity = updateCompleteRecord(fetchBibliographicEntity,bibliographicEntity,submitCollectionReportInfoMap);
                saveItemChangeLogEntity(ReCAPConstants.SUBMIT_COLLECTION,ReCAPConstants.SUBMIT_COLLECTION_COMPLETE_RECORD_UPDATE,savedBibliographicEntity.getItemEntities());
            } else {//update existing dummy record if any
                idMapToRemoveIndex.put(ReCAPConstants.BIB_ID,String.valueOf(fetchBibliographicEntity.getBibliographicId()));
                idMapToRemoveIndex.put(ReCAPConstants.HOLDING_ID,String.valueOf(fetchBibliographicEntity.getHoldingsEntities().get(0).getHoldingsId()));
                idMapToRemoveIndex.put(ReCAPConstants.ITEM_ID,String.valueOf(fetchBibliographicEntity.getItemEntities().get(0).getItemId()));
                getBibliographicDetailsRepository().delete(fetchBibliographicEntity);
                getBibliographicDetailsRepository().flush();
                setItemAvailabilityStatusForDummyRecord(bibliographicEntity.getItemEntities().get(0));
                savedBibliographicEntity = getBibliographicDetailsRepository().saveAndFlush(bibliographicEntity);
                saveItemChangeLogEntity(ReCAPConstants.SUBMIT_COLLECTION,ReCAPConstants.SUBMIT_COLLECTION_INCOMPLETE_RECORD_UPDATE,savedBibliographicEntity.getItemEntities());
                entityManager.refresh(savedBibliographicEntity);
            }
        } else {//if no record found to update, generate exception info
            savedBibliographicEntity = bibliographicEntity;
            setSubmitCollectionReportInfo(bibliographicEntity,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_LIST),ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD);
        }
        return savedBibliographicEntity;
    }

    private void setItemAvailabilityStatusForDummyRecord(ItemEntity itemEntity){
        if(itemEntity.getItemAvailabilityStatusId()==null) {
            itemEntity.setItemAvailabilityStatusId((Integer) getItemStatusCodeIdMap().get("Available"));
        }
    }

    private BibliographicEntity getBibEntityUsingBarcode(BibliographicEntity bibliographicEntity) {
        List<String> itemBarcodeList = new ArrayList<>();
        for (ItemEntity itemEntity : bibliographicEntity.getItemEntities()) {
            itemBarcodeList.add(itemEntity.getBarcode());
        }
        List<ItemEntity> itemEntityList = getItemDetailsRepository().findByBarcodeIn(itemBarcodeList);
        BibliographicEntity fetchedBibliographicEntity = null;
        if (itemEntityList != null && !itemEntityList.isEmpty() && itemEntityList.get(0).getBibliographicEntities() != null) {
            boolean isBoundWith = isBoundWithItem(itemEntityList.get(0));
            if (isBoundWith) {//To handle boundwith item
                for (BibliographicEntity resultBibliographicEntity : itemEntityList.get(0).getBibliographicEntities()) {
                    if (bibliographicEntity.getOwningInstitutionBibId().equals(resultBibliographicEntity.getOwningInstitutionBibId())) {
                        fetchedBibliographicEntity = resultBibliographicEntity;
                    }
                }
            } else {
                fetchedBibliographicEntity = itemEntityList.get(0).getBibliographicEntities().get(0);
            }
        }
        return fetchedBibliographicEntity;
    }

    private boolean isBoundWithItem(ItemEntity itemEntity){
        if(itemEntity.getBibliographicEntities().size() > 1){
            return true;
        }
        return false;
    }

    private BibliographicEntity updateCompleteRecord(BibliographicEntity fetchBibliographicEntity,BibliographicEntity bibliographicEntity,
                                                     Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap) {
        BibliographicEntity savedOrUnsavedBibliographicEntity;
        setSubmitCollectionRejectionInfo(fetchBibliographicEntity, submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST));
        copyBibliographicEntity(fetchBibliographicEntity, bibliographicEntity);
        List<HoldingsEntity> fetchHoldingsEntities = fetchBibliographicEntity.getHoldingsEntities();
        List<HoldingsEntity> holdingsEntities = new ArrayList<>(bibliographicEntity.getHoldingsEntities());
        for (Iterator iholdings = holdingsEntities.iterator(); iholdings.hasNext(); ) {
            HoldingsEntity holdingsEntity = (HoldingsEntity) iholdings.next();
            for (int j = 0; j < fetchHoldingsEntities.size(); j++) {
                HoldingsEntity fetchHolding = fetchHoldingsEntities.get(j);
                if (fetchHolding.getOwningInstitutionHoldingsId().equalsIgnoreCase(holdingsEntity.getOwningInstitutionHoldingsId())) {
                    copyHoldingsEntity(fetchHolding, holdingsEntity);
                    iholdings.remove();
                } else {
                    List<ItemEntity> fetchedItemEntityList = fetchHolding.getItemEntities();
                    List<ItemEntity> itemEntityList = holdingsEntity.getItemEntities();
                    for (ItemEntity fetchedItemEntity : fetchedItemEntityList) {
                        for (ItemEntity itemEntity : itemEntityList) {
                            if (fetchedItemEntity.getOwningInstitutionItemId().equals(itemEntity.getOwningInstitutionItemId())) {
                                copyHoldingsEntity(fetchHolding, holdingsEntity);
                                iholdings.remove();
                            }
                        }
                    }
                }
            }
        }
        fetchHoldingsEntities.addAll(holdingsEntities);
        // Item
        List<ItemEntity> fetchItemsEntities = fetchBibliographicEntity.getItemEntities();
        List<ItemEntity> itemsEntities = new ArrayList<>(bibliographicEntity.getItemEntities());
        for (Iterator iItems = itemsEntities.iterator(); iItems.hasNext(); ) {
            ItemEntity itemEntity = (ItemEntity) iItems.next();
            for (Iterator ifetchItems = fetchItemsEntities.iterator(); ifetchItems.hasNext(); ) {
                ItemEntity fetchItem = (ItemEntity) ifetchItems.next();
                if (fetchItem.getOwningInstitutionItemId().equalsIgnoreCase(itemEntity.getOwningInstitutionItemId())) {
                    copyItemEntity(fetchItem, itemEntity);
                    iItems.remove();
                }
            }
        }
        fetchItemsEntities.addAll(itemsEntities);
        fetchBibliographicEntity.setHoldingsEntities(fetchHoldingsEntities);
        fetchBibliographicEntity.setItemEntities(fetchItemsEntities);
        try {
            savedOrUnsavedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(fetchBibliographicEntity);
            setSubmitCollectionReportInfo(fetchBibliographicEntity,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST),ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD);
            return savedOrUnsavedBibliographicEntity;
        } catch (Exception e) {
            setSubmitCollectionReportInfo(fetchBibliographicEntity,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),ReCAPConstants.SUBMIT_COLLECTION_FAILED_RECORD);
            logger.error(ReCAPConstants.LOG_ERROR,e);
            return null;
        }
    }

    private BibliographicEntity copyBibliographicEntity(BibliographicEntity fetchBibliographicEntity,BibliographicEntity bibliographicEntity){
        fetchBibliographicEntity.setContent(bibliographicEntity.getContent());
        fetchBibliographicEntity.setDeleted(bibliographicEntity.isDeleted());
        fetchBibliographicEntity.setLastUpdatedBy(bibliographicEntity.getLastUpdatedBy());
        fetchBibliographicEntity.setLastUpdatedDate(bibliographicEntity.getLastUpdatedDate());
        fetchBibliographicEntity.setCatalogingStatus(ReCAPConstants.COMPLETE_STATUS);
        return fetchBibliographicEntity;
    }

    private HoldingsEntity copyHoldingsEntity(HoldingsEntity fetchHoldingsEntity, HoldingsEntity holdingsEntity){
        fetchHoldingsEntity.setContent(holdingsEntity.getContent());
        fetchHoldingsEntity.setDeleted(holdingsEntity.isDeleted());
        fetchHoldingsEntity.setLastUpdatedBy(holdingsEntity.getLastUpdatedBy());
        fetchHoldingsEntity.setLastUpdatedDate(holdingsEntity.getLastUpdatedDate());
        return fetchHoldingsEntity;
    }

    private ItemEntity copyItemEntity(ItemEntity fetchItemEntity, ItemEntity itemEntity) {
        fetchItemEntity.setBarcode(itemEntity.getBarcode());
        fetchItemEntity.setDeleted(itemEntity.isDeleted());
        fetchItemEntity.setLastUpdatedBy(itemEntity.getLastUpdatedBy());
        fetchItemEntity.setLastUpdatedDate(itemEntity.getLastUpdatedDate());
        fetchItemEntity.setCallNumber(itemEntity.getCallNumber());
        fetchItemEntity.setCallNumberType(itemEntity.getCallNumberType());
        if(null != itemEntity.getCustomerCode()){
            fetchItemEntity.setCustomerCode(itemEntity.getCustomerCode());
        }
        if (isAvailableItem(fetchItemEntity.getItemAvailabilityStatusId())) {
            fetchItemEntity.setCollectionGroupId(itemEntity.getCollectionGroupId());
            fetchItemEntity.setUseRestrictions(itemEntity.getUseRestrictions());
        }
        fetchItemEntity.setCopyNumber(itemEntity.getCopyNumber());
        fetchItemEntity.setVolumePartYear(itemEntity.getVolumePartYear());
        fetchItemEntity.setCatalogingStatus(ReCAPConstants.COMPLETE_STATUS);
        return fetchItemEntity;
    }

    /**
     * Is available item boolean.
     *
     * @param itemAvailabilityStatusId the item availability status id
     * @return the boolean
     */
    public boolean isAvailableItem(Integer itemAvailabilityStatusId){
        String itemStatusCode = (String) getItemStatusIdCodeMap().get(itemAvailabilityStatusId);
        if (itemStatusCode.equalsIgnoreCase(ReCAPConstants.ITEM_STATUS_AVAILABLE)) {
            return true;
        }
        return false;
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
    public void generateSubmitCollectionReport(List<SubmitCollectionReportInfo> submitCollectionReportList,String fileName, String reportType,String xmlFileName){
        logger.info("Preparing report entities");
        if(submitCollectionReportList != null && !submitCollectionReportList.isEmpty()){
            try {
                ReportEntity reportEntity = new ReportEntity();
                List<ReportDataEntity> reportDataEntities = new ArrayList<>();
                String owningInstitution = submitCollectionReportList.get(0).getOwningInstitution();
                if(!submitCollectionReportList.isEmpty()){
                    reportEntity.setFileName(fileName+"-"+xmlFileName);
                    reportEntity.setType(reportType);
                    reportEntity.setCreatedDate(new Date());
                    reportEntity.setInstitutionName(owningInstitution);
                }
                int count = 1;
                for(SubmitCollectionReportInfo submitCollectionReportInfo : submitCollectionReportList){
                    logger.info("Processing report for record {}",count);
                    if(!StringUtils.isEmpty(submitCollectionReportInfo.getItemBarcode())){

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
                        reportDetailRepository.save(reportEntity);
                        count ++;

                    }
                    logger.info("Processed completed report for record {}",count);
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
    }

    /**
     * Gets bibliographic details repository.
     *
     * @return the bibliographic details repository
     */
    public BibliographicDetailsRepository getBibliographicDetailsRepository() {
        return bibliographicDetailsRepository;
    }

    /**
     * Sets bibliographic details repository.
     *
     * @param bibliographicDetailsRepository the bibliographic details repository
     */
    public void setBibliographicDetailsRepository(BibliographicDetailsRepository bibliographicDetailsRepository) {
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
    }

    /**
     * Gets customer code details repository.
     *
     * @return the customer code details repository
     */
    public CustomerCodeDetailsRepository getCustomerCodeDetailsRepository() {
        return customerCodeDetailsRepository;
    }

    /**
     * Sets customer code details repository.
     *
     * @param customerCodeDetailsRepository the customer code details repository
     */
    public void setCustomerCodeDetailsRepository(CustomerCodeDetailsRepository customerCodeDetailsRepository) {
        this.customerCodeDetailsRepository = customerCodeDetailsRepository;
    }

    /**
     * Gets marc util.
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
     * Gets item details repository.
     *
     * @return the item details repository
     */
    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    /**
     * Sets item details repository.
     *
     * @param itemDetailsRepository the item details repository
     */
    public void setItemDetailsRepository(ItemDetailsRepository itemDetailsRepository) {
        this.itemDetailsRepository = itemDetailsRepository;
    }

    /**
     * Gets item status details repository.
     *
     * @return the item status details repository
     */
    public ItemStatusDetailsRepository getItemStatusDetailsRepository() {
        return itemStatusDetailsRepository;
    }

    /**
     * Sets item status details repository.
     *
     * @param itemStatusDetailsRepository the item status details repository
     */
    public void setItemStatusDetailsRepository(ItemStatusDetailsRepository itemStatusDetailsRepository) {
        this.itemStatusDetailsRepository = itemStatusDetailsRepository;
    }

    /**
     * Gets institution details repository.
     *
     * @return the institution details repository
     */
    public InstitutionDetailsRepository getInstitutionDetailsRepository() {
        return institutionDetailsRepository;
    }

    /**
     * Sets institution details repository.
     *
     * @param institutionDetailsRepository the institution details repository
     */
    public void setInstitutionDetailsRepository(InstitutionDetailsRepository institutionDetailsRepository) {
        this.institutionDetailsRepository = institutionDetailsRepository;
    }

    /**
     * Gets entity manager.
     *
     * @return the entity manager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Sets entity manager.
     *
     * @param entityManager the entity manager
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
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


    private Map getItemStatusIdCodeMap() {
        if (null == itemStatusIdCodeMap) {
            itemStatusIdCodeMap = new HashMap();
            try {
                Iterable<ItemStatusEntity> itemStatusEntities = itemStatusDetailsRepository.findAll();
                for (Iterator iterator = itemStatusEntities.iterator(); iterator.hasNext(); ) {
                    ItemStatusEntity itemStatusEntity = (ItemStatusEntity) iterator.next();
                    itemStatusIdCodeMap.put(itemStatusEntity.getItemStatusId(), itemStatusEntity.getStatusCode());
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
        return itemStatusIdCodeMap;
    }

    private Map getItemStatusCodeIdMap() {
        if (null == itemStatusCodeIdMap) {
            itemStatusCodeIdMap = new HashMap();
            try {
                Iterable<ItemStatusEntity> itemStatusEntities = itemStatusDetailsRepository.findAll();
                for (Iterator iterator = itemStatusEntities.iterator(); iterator.hasNext(); ) {
                    ItemStatusEntity itemStatusEntity = (ItemStatusEntity) iterator.next();
                    itemStatusCodeIdMap.put(itemStatusEntity.getStatusCode(), itemStatusEntity.getItemStatusId());
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
        return itemStatusCodeIdMap;
    }

    /**
     * Gets institution entity map.
     *
     * @return the institution entity map
     */
    public Map getInstitutionEntityMap() {
        if (null == institutionEntityMap) {
            institutionEntityMap = new HashMap();
            try {
                Iterable<InstitutionEntity> institutionEntities = institutionDetailsRepository.findAll();
                for (Iterator iterator = institutionEntities.iterator(); iterator.hasNext(); ) {
                    InstitutionEntity institutionEntity = (InstitutionEntity) iterator.next();
                    institutionEntityMap.put(institutionEntity.getInstitutionId(), institutionEntity.getInstitutionCode());
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
        return institutionEntityMap;
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
}