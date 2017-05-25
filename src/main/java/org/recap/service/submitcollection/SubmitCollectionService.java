package org.recap.service.submitcollection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
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
import org.recap.repository.*;
import org.recap.util.MarcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.ResourceAccessException;
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
    public List<SubmitCollectionResponse> process(String inputRecords, List<Integer> processedBibIdList,Map<String,String> idMapToRemoveIndex,String xmlFileName) {
        logger.info("Input record processing started");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String reponse = null;
        String escapedString = StringEscapeUtils.escapeXml10(inputRecords);
        String finalXmlString = replaceEscapedTags(escapedString);
        List<SubmitCollectionResponse> submitColletionResponseList = new ArrayList<>();
        Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap = getSubmitCollectionReportMap();
        try {
            if (!"".equals(inputRecords)) {
                if (inputRecords.contains(ReCAPConstants.BIBRECORD_TAG)) {
                    reponse = processSCSB(finalXmlString, processedBibIdList, submitCollectionReportInfoMap, idMapToRemoveIndex);
                } else {
                    reponse = processMarc(finalXmlString, processedBibIdList, submitCollectionReportInfoMap, idMapToRemoveIndex);
                }
                if (reponse != null){//This happens when there is a failure
                    setResponse(reponse, submitColletionResponseList);
                    setSubmitCollectionReportInfoForInvalidXml(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),reponse);
                    generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_FAILURE_REPORT, xmlFileName);
                    return submitColletionResponseList;
                }
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_REPORT, xmlFileName);
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_FAILURE_REPORT, xmlFileName);
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_REJECTION_REPORT, xmlFileName);
                generateSubmitCollectionReport(submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_LIST), ReCAPConstants.SUBMIT_COLLECTION_REPORT, ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_REPORT, xmlFileName);
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

    private List<SubmitCollectionResponse> getResponseMessage(Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap,List<SubmitCollectionResponse> submitColletionResponseList){
        for (Map.Entry<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMapEntry : submitCollectionReportInfoMap.entrySet()) {
            List<SubmitCollectionReportInfo> submitCollectionReportInfoList = submitCollectionReportInfoMapEntry.getValue();
            for(SubmitCollectionReportInfo submitCollectionReportInfo:submitCollectionReportInfoList){
                SubmitCollectionResponse submitCollectionResponse = new SubmitCollectionResponse();
                setSubmitCollectionResponse(submitCollectionReportInfo,submitColletionResponseList,submitCollectionResponse);
            }
        }
        return submitColletionResponseList;
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

    private void setSubmitCollectionReportInfo(List<ItemEntity> itemList,List<SubmitCollectionReportInfo> submitCollectionExceptionInfos, String message) {
        for (ItemEntity itemEntity : itemList) {
            logger.info("Report data for item {}",itemEntity.getBarcode());
            SubmitCollectionReportInfo submitCollectionExceptionInfo = new SubmitCollectionReportInfo();
            submitCollectionExceptionInfo.setItemBarcode(itemEntity.getBarcode());
            submitCollectionExceptionInfo.setCustomerCode(itemEntity.getCustomerCode());
            submitCollectionExceptionInfo.setOwningInstitution((String) getInstitutionEntityMap().get(itemEntity.getOwningInstitutionId()));
            StringBuilder sbMessage = new StringBuilder();
            sbMessage.append(message);
            if(itemEntity.getCatalogingStatus() != null && itemEntity.getCatalogingStatus().equals(ReCAPConstants.INCOMPLETE_STATUS)){
                if(StringUtils.isEmpty(itemEntity.getUseRestrictions())){
                    sbMessage.append("-").append(ReCAPConstants.RECORD_INCOMPLETE).append(ReCAPConstants.USE_RESTRICTION_UNAVAILABLE);
                }
                if(itemEntity.getCollectionGroupEntity().getCollectionGroupCode().equals(ReCAPConstants.NOT_AVAILABLE_CGD)){
                    sbMessage.append(ReCAPConstants.CGD_NA);
                }
            }
            submitCollectionExceptionInfo.setMessage(sbMessage.toString());
            submitCollectionExceptionInfos.add(submitCollectionExceptionInfo);
        }
    }

    private void setSubmitCollectionReportInfoForInvalidXml(List<SubmitCollectionReportInfo> submitCollectionExceptionInfos, String message) {
            SubmitCollectionReportInfo submitCollectionExceptionInfo = new SubmitCollectionReportInfo();
            submitCollectionExceptionInfo.setItemBarcode("");
            submitCollectionExceptionInfo.setCustomerCode("");
            submitCollectionExceptionInfo.setOwningInstitution("");
            submitCollectionExceptionInfo.setMessage(message);
            submitCollectionExceptionInfos.add(submitCollectionExceptionInfo);
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
     * This method updates the Bib, Holding and Item information for the given input xml
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
                logger.info("Delete dummy record - barcode - {}",fetchBibliographicEntity.getItemEntities().get(0).getBarcode());
                updateCustomerCode(fetchBibliographicEntity,bibliographicEntity);
                getBibliographicDetailsRepository().delete(fetchBibliographicEntity);
                getBibliographicDetailsRepository().flush();
                setItemAvailabilityStatusForDummyRecord(bibliographicEntity.getItemEntities().get(0));
                updateCatalogingStatusForItem(bibliographicEntity);
                updateCatalogingStatusForBib(fetchBibliographicEntity);
                savedBibliographicEntity = getBibliographicDetailsRepository().saveAndFlush(bibliographicEntity);
                saveItemChangeLogEntity(ReCAPConstants.SUBMIT_COLLECTION,ReCAPConstants.SUBMIT_COLLECTION_INCOMPLETE_RECORD_UPDATE,savedBibliographicEntity.getItemEntities());
                entityManager.refresh(savedBibliographicEntity);
                setSubmitCollectionReportInfo(savedBibliographicEntity.getItemEntities(),submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST),ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD);
            }
        } else {//if no record found to update, generate exception info
            savedBibliographicEntity = bibliographicEntity;
            setSubmitCollectionReportInfo(bibliographicEntity.getItemEntities(),submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_LIST),ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD);
        }
        return savedBibliographicEntity;
    }

    private void updateCustomerCode(BibliographicEntity dummyBibliographicEntity,BibliographicEntity updatedBibliographicEntity) {
        updatedBibliographicEntity.getItemEntities().get(0).setCustomerCode(dummyBibliographicEntity.getItemEntities().get(0).getCustomerCode());
    }

    private BibliographicEntity updateCatalogingStatusForItem(BibliographicEntity bibliographicEntity) {
        for(ItemEntity itemEntity:bibliographicEntity.getItemEntities()){
            if(itemEntity.getUseRestrictions()==null || itemEntity.getCollectionGroupId()==null){
                itemEntity.setCatalogingStatus(ReCAPConstants.INCOMPLETE_STATUS);
            }
        }
        return bibliographicEntity;
    }

    private BibliographicEntity updateCatalogingStatusForBib(BibliographicEntity fetchBibliographicEntity) {
        fetchBibliographicEntity.setCatalogingStatus(ReCAPConstants.INCOMPLETE_STATUS);
        for(ItemEntity itemEntity:fetchBibliographicEntity.getItemEntities()){
            if(itemEntity.getCatalogingStatus().equals(ReCAPConstants.COMPLETE_STATUS)){
                fetchBibliographicEntity.setCatalogingStatus(ReCAPConstants.COMPLETE_STATUS);
                return fetchBibliographicEntity;
            }
        }
        return fetchBibliographicEntity;
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

    private void setSubmitCollectionResponse(SubmitCollectionReportInfo submitCollectionReportInfo,List<SubmitCollectionResponse> submitColletionResponseList, SubmitCollectionResponse submitCollectionResponse){
        submitCollectionResponse.setItemBarcode(submitCollectionReportInfo.getItemBarcode());
        submitCollectionResponse.setMessage(submitCollectionReportInfo.getMessage());
        submitColletionResponseList.add(submitCollectionResponse);
    }

    private BibliographicEntity updateCompleteRecord(BibliographicEntity fetchBibliographicEntity,BibliographicEntity bibliographicEntity,
                                                     Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap) {
        BibliographicEntity savedOrUnsavedBibliographicEntity;
        setSubmitCollectionRejectionInfo(fetchBibliographicEntity, submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST));
        copyBibliographicEntity(fetchBibliographicEntity, bibliographicEntity);
        List<HoldingsEntity> fetchHoldingsEntities = fetchBibliographicEntity.getHoldingsEntities();
        List<HoldingsEntity> holdingsEntities = new ArrayList<>(bibliographicEntity.getHoldingsEntities());
        List<ItemEntity> updatedItemEntityList = new ArrayList<>();
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
                    copyItemEntity(fetchItem, itemEntity,updatedItemEntityList);
                    iItems.remove();
                }
            }
        }
        fetchItemsEntities.addAll(itemsEntities);
        fetchBibliographicEntity.setHoldingsEntities(fetchHoldingsEntities);
        fetchBibliographicEntity.setItemEntities(fetchItemsEntities);
        try {
            updateCatalogingStatusForBib(fetchBibliographicEntity);
            savedOrUnsavedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(fetchBibliographicEntity);
            setSubmitCollectionReportInfo(updatedItemEntityList,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST),ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD);
            return savedOrUnsavedBibliographicEntity;
        } catch (Exception e) {
            setSubmitCollectionReportInfo(updatedItemEntityList,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),ReCAPConstants.SUBMIT_COLLECTION_FAILED_RECORD);
            logger.error(ReCAPConstants.LOG_ERROR,e);
            return null;
        }
    }

    private BibliographicEntity copyBibliographicEntity(BibliographicEntity fetchBibliographicEntity,BibliographicEntity bibliographicEntity){
        fetchBibliographicEntity.setContent(bibliographicEntity.getContent());
        fetchBibliographicEntity.setDeleted(bibliographicEntity.isDeleted());
        fetchBibliographicEntity.setLastUpdatedBy(bibliographicEntity.getLastUpdatedBy());
        fetchBibliographicEntity.setLastUpdatedDate(bibliographicEntity.getLastUpdatedDate());
        logger.info("updating existing bib - owning inst bibid - "+fetchBibliographicEntity.getOwningInstitutionBibId());
        return fetchBibliographicEntity;
    }

    private HoldingsEntity copyHoldingsEntity(HoldingsEntity fetchHoldingsEntity, HoldingsEntity holdingsEntity){
        fetchHoldingsEntity.setContent(holdingsEntity.getContent());
        fetchHoldingsEntity.setDeleted(holdingsEntity.isDeleted());
        fetchHoldingsEntity.setLastUpdatedBy(holdingsEntity.getLastUpdatedBy());
        fetchHoldingsEntity.setLastUpdatedDate(holdingsEntity.getLastUpdatedDate());
        return fetchHoldingsEntity;
    }

    private ItemEntity copyItemEntity(ItemEntity fetchItemEntity, ItemEntity itemEntity,List<ItemEntity> updatedItemEntityList) {
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
            if (itemEntity.getCollectionGroupId() != null) {
                fetchItemEntity.setCollectionGroupId(itemEntity.getCollectionGroupId());
            }
            fetchItemEntity.setUseRestrictions(itemEntity.getUseRestrictions());
        }
        fetchItemEntity.setCopyNumber(itemEntity.getCopyNumber());
        fetchItemEntity.setVolumePartYear(itemEntity.getVolumePartYear());
        if((fetchItemEntity.getUseRestrictions() == null && itemEntity.getUseRestrictions() == null )
                || (fetchItemEntity.getCollectionGroupEntity().getCollectionGroupCode().equals(ReCAPConstants.NOT_AVAILABLE_CGD)
                && itemEntity.getCollectionGroupId()==null)){
            fetchItemEntity.setCatalogingStatus(ReCAPConstants.INCOMPLETE_STATUS);
        } else{
            fetchItemEntity.setCatalogingStatus(ReCAPConstants.COMPLETE_STATUS);
        }
        logger.info("updating existing barcode - "+fetchItemEntity.getBarcode());
        updatedItemEntityList.add(fetchItemEntity);
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
                    if(!StringUtils.isEmpty(xmlFileName)) {
                        reportEntity.setFileName(fileName + "-" + xmlFileName);
                    }else{
                        reportEntity.setFileName(fileName);
                    }
                    reportEntity.setType(reportType);
                    reportEntity.setCreatedDate(new Date());
                    reportEntity.setInstitutionName(owningInstitution);
                }
                int count = 1;
                for(SubmitCollectionReportInfo submitCollectionReportInfo : submitCollectionReportList){
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

    //added for escaping special character in the xml content
    private String replaceEscapedTags(String xmlString){
        String updatedString = xmlString.replaceAll(ReCAPConstants.ESCAPED_STARTING_COLLECTION_TAG,ReCAPConstants.UNESCAPED_STARTING_COLLECTION_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_COLLECTION_TAG,ReCAPConstants.UNESCAPED_ENDING_COLLECTION_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_BIBRECORDS_TAG,ReCAPConstants.UNESCAPED_STARTING_BIBRECORDS_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_BIBRECORDS_TAG,ReCAPConstants.UNESCAPED_ENDING_BIBRECORDS_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_BIBRECORD_TAG,ReCAPConstants.UNESCAPED_STARTING_BIBRECORD_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_BIBRECORD_TAG,ReCAPConstants.UNESCAPED_ENDING_BIBRECORD_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_BIB_TAG,ReCAPConstants.UNESCAPED_STARTING_BIB_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_BIB_TAG,ReCAPConstants.UNESCAPED_ENDING_BIB_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_OWNINSTID_TAG,ReCAPConstants.UNESCAPED_STARTING_OWNINSTID_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_OWNINSTID_TAG,ReCAPConstants.UNESCAPED_ENDING_OWNINSTID_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_OWNINSTID_BIBID_TAG,ReCAPConstants.UNESCAPED_STARTING_OWNINSTID_BIBID_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_OWNINSTID_BIBID_TAG,ReCAPConstants.UNESCAPED_ENDING_OWNINSTID_BIBID_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_CONTENT_TAG,ReCAPConstants.UNESCAPED_STARTING_CONTENT_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_CONTENT_TAG,ReCAPConstants.UNESCAPED_ENDING_CONTENT_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_INNER_COLLECTION_TAG,ReCAPConstants.UNESCAPED_STARTING_INNER_COLLECTION_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_HOLDINGS_TAG,ReCAPConstants.UNESCAPED_STARTING_HOLDINGS_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_HOLDINGS_TAG,ReCAPConstants.UNESCAPED_ENDING_HOLDINGS_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_HOLDING_TAG,ReCAPConstants.UNESCAPED_STARTING_HOLDING_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_HOLDING_TAG,ReCAPConstants.UNESCAPED_ENDING_HOLDING_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_OWNINGINST_HOLDID_TAG,ReCAPConstants.UNESCAPED_STARTING_OWNINGINST_HOLDID_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_OWNINGINST_HOLDID_TAG,ReCAPConstants.UNESCAPED_ENDING_OWNINGINST_HOLDID_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_ITEMS_HOLDID_TAG,ReCAPConstants.UNESCAPED_STARTING_ITEMS_HOLDID_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_ITEMS_HOLDID_TAG,ReCAPConstants.UNESCAPED_ENDING_ITEMS_HOLDID_TAG);

        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_RECORD_TAG,ReCAPConstants.UNESCAPED_STARTING_RECORD_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_RECORD_TAG,ReCAPConstants.UNESCAPED_ENDING_RECORD_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_LEADER_TAG,ReCAPConstants.UNESCAPED_STARTING_LEADER_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_LEADER_TAG,ReCAPConstants.UNESCAPED_ENDING_LEADER_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_CONTROLFIELD_TAG,ReCAPConstants.UNESCAPED_STARTING_CONTROLFIELD_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_CONTROLFIELD_TAG,ReCAPConstants.UNESCAPED_ENDING_CONTROLFIELD_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_DATAFIELD_TAG,ReCAPConstants.UNESCAPED_STARTING_DATAFIELD_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_DATAFIELD_TAG,ReCAPConstants.UNESCAPED_ENDING_DATAFIELD_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_SUBFIELD_TAG,ReCAPConstants.UNESCAPED_STARTING_SUBFIELD_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_ENDING_SUBFIELD_TAG,ReCAPConstants.UNESCAPED_ENDING_SUBFIELD_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_CLOSING_TAG_WITH_QUOTE,ReCAPConstants.UNESCAPED_CLOSING_TAG_WITH_QUOTE);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_CLOSING_TAG,ReCAPConstants.UNESCAPED_CLOSING_TAG);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_QUOTE,ReCAPConstants.UNESCAPED_QUOTE);
        updatedString = updatedString.replaceAll(ReCAPConstants.ESCAPED_STARTING_XML_TAG,ReCAPConstants.UNESCAPED_STARTING_XML_TAG);


        return updatedString;
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