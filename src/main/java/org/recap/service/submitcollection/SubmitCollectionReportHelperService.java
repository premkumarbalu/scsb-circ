package org.recap.service.submitcollection;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.recap.ReCAPConstants;
import org.recap.model.BibliographicEntity;
import org.recap.model.HoldingsEntity;
import org.recap.model.ItemEntity;
import org.recap.model.ItemStatusEntity;
import org.recap.model.report.SubmitCollectionReportInfo;
import org.recap.service.common.RepositoryService;
import org.recap.service.common.SetupDataService;
import org.recap.util.MarcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by premkb on 11/6/17.
 */
@Service
public class SubmitCollectionReportHelperService {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionReportHelperService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SetupDataService setupDataService;

    @Value("${nonholdingid.institution}")
    private String nonHoldingIdInstitution;

    /**
     * This method sets submit collection report information based on the given information.
     *
     * @param itemEntityList                 the item entity list
     * @param submitCollectionExceptionInfos the submit collection exception infos
     * @param message                        the message
     */
    public void setSubmitCollectionExceptionReportInfo(List<ItemEntity> itemEntityList, List<SubmitCollectionReportInfo> submitCollectionExceptionInfos, String message) {
        for (ItemEntity itemEntity : itemEntityList) {
            logger.info("Report data for item {}",itemEntity.getBarcode());
            StringBuilder sbMessage = new StringBuilder();
            sbMessage.append(message);
            if(itemEntity.getCatalogingStatus() != null && itemEntity.getCatalogingStatus().equals(ReCAPConstants.INCOMPLETE_STATUS)){
                if(StringUtils.isEmpty(itemEntity.getUseRestrictions())){
                    sbMessage.append("-").append(ReCAPConstants.RECORD_INCOMPLETE).append(ReCAPConstants.USE_RESTRICTION_UNAVAILABLE);
                }
            }
            setSubmitCollectionReportInfo(submitCollectionExceptionInfos,itemEntity,sbMessage.toString());
        }
    }

    /**
     * Set submit collection report info for invalid dummy record.
     *
     * @param incomingBibliographicEntity    the incoming bibliographic entity
     * @param submitCollectionReportInfoList the submit collection report info list
     * @param fetchedCompleteItem            the fetched complete item
     */
    public void setSubmitCollectionReportInfoForInvalidDummyRecordBasedOnBarcode(BibliographicEntity incomingBibliographicEntity, List<SubmitCollectionReportInfo> submitCollectionReportInfoList, List<ItemEntity> fetchedCompleteItem){
        Map<String,ItemEntity> incomingBarcodeItemEntityMap = getBarcodeItemEntityMap(incomingBibliographicEntity.getItemEntities());
        Map<String,ItemEntity> fetchedBarcodeItemEntityMap = getBarcodeItemEntityMap(fetchedCompleteItem);
        for(String barcode:incomingBarcodeItemEntityMap.keySet()){
            ItemEntity incomingEntity = incomingBarcodeItemEntityMap.get(barcode);
            ItemEntity fetchedItemEntity = fetchedBarcodeItemEntityMap.get(barcode);
            String message;
            if(fetchedItemEntity!=null){
                message = "Failed record - Incoming item barcode "+barcode+ ", incoming owning institution bib id "+
                        incomingBibliographicEntity.getOwningInstitutionBibId()+", is already attached with existing bib, owning institution bib id "+
                        fetchedItemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId()+", owning institution item id "+
                        fetchedItemEntity.getOwningInstitutionItemId();
            } else {
                message = ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD;
            }
            setSubmitCollectionReportInfo(submitCollectionReportInfoList, incomingEntity, message);
        }
    }

    /**
     * Set submit collection report info for invalid dummy record.
     *
     * @param incomingBibliographicEntity    the incoming bibliographic entity
     * @param submitCollectionReportInfoList the submit collection report info list
     * @param fetchedCompleteItem            the fetched complete item
     */
    public void setSubmitCollectionReportInfoForInvalidDummyRecordBasedOnOwnInstItemId(BibliographicEntity incomingBibliographicEntity, List<SubmitCollectionReportInfo> submitCollectionReportInfoList, List<ItemEntity> fetchedCompleteItem){
        Map<String,ItemEntity> incomingOwningInstitutionItemIdItemEntityMap = getOwningInstitutionItemIdItemEntityMap(incomingBibliographicEntity.getItemEntities());
        Map<String,ItemEntity> fetchedOwningInstitutionItemIdItemEntityMap = getOwningInstitutionItemIdItemEntityMap(fetchedCompleteItem);
        for(String owningInstitutionItemId:incomingOwningInstitutionItemIdItemEntityMap.keySet()){
            ItemEntity incomingEntity = incomingOwningInstitutionItemIdItemEntityMap.get(owningInstitutionItemId);
            ItemEntity fetchedItemEntity = fetchedOwningInstitutionItemIdItemEntityMap.get(owningInstitutionItemId);
            String message;
            if(fetchedItemEntity!=null){
                message = "Failed record - Incoming item owning institution item id "+owningInstitutionItemId+ ", incoming owning institution bib id "+
                        incomingBibliographicEntity.getOwningInstitutionBibId()+", is already attached with existing barcode "+ fetchedItemEntity.getBarcode() +", owning institution bib id "+
                        fetchedItemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId();
            } else {
                message = ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD;
            }
            setSubmitCollectionReportInfo(submitCollectionReportInfoList, incomingEntity, message);
        }
    }

    private void setSubmitCollectionReportInfo(List<SubmitCollectionReportInfo> submitCollectionReportInfoList, ItemEntity incomingEntity, String message) {
        SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
        submitCollectionReportInfo.setMessage(message);
        submitCollectionReportInfo.setItemBarcode(incomingEntity.getBarcode());
        submitCollectionReportInfo.setCustomerCode(incomingEntity.getCustomerCode());
        submitCollectionReportInfo.setOwningInstitution((String) setupDataService.getInstitutionIdCodeMap().get(incomingEntity.getOwningInstitutionId()));
        submitCollectionReportInfoList.add(submitCollectionReportInfo);
    }

    private Map<String,ItemEntity> getBarcodeItemEntityMap(List<ItemEntity> itemEntityList){
        Map<String,ItemEntity> barcodeItemEntityMap = new HashedMap();
        for(ItemEntity itemEntity:itemEntityList){
            barcodeItemEntityMap.put(itemEntity.getBarcode(),itemEntity);
        }
        return  barcodeItemEntityMap;
    }

    private Map<String,ItemEntity> getOwningInstitutionItemIdItemEntityMap(List<ItemEntity> itemEntityList){
        Map<String,ItemEntity> owningInstitutionItemIdItemEntityMap = new HashedMap();
        for(ItemEntity itemEntity:itemEntityList){
            owningInstitutionItemIdItemEntityMap.put(itemEntity.getOwningInstitutionItemId(),itemEntity);
        }
        return  owningInstitutionItemIdItemEntityMap;
    }

    /**
     * This method is to check is barcode already added.
     *
     * @param itemEntity                    the item entity
     * @param submitCollectionReportInfoMap the submit collection report info map
     * @return the boolean
     */
    public boolean isBarcodeAlreadyAdded(ItemEntity itemEntity,Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap){

        for (Map.Entry<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoIndividualMap : submitCollectionReportInfoMap.entrySet()) {
            List<SubmitCollectionReportInfo> submitCollectionReportInfoList = submitCollectionReportInfoIndividualMap.getValue();
            if(!submitCollectionReportInfoList.isEmpty()){
                for(SubmitCollectionReportInfo submitCollectionReportInfo : submitCollectionReportInfoList){
                    if(submitCollectionReportInfo.getItemBarcode().equals(itemEntity.getBarcode())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sets submit collection report info for invalid xml.
     *
     * @param institutionCode                the institution code
     * @param submitCollectionExceptionInfos the submit collection exception infos
     * @param message                        the message
     */
    public void setSubmitCollectionReportInfoForInvalidXml(String institutionCode, List<SubmitCollectionReportInfo> submitCollectionExceptionInfos, String message) {
        SubmitCollectionReportInfo submitCollectionExceptionInfo = new SubmitCollectionReportInfo();
        submitCollectionExceptionInfo.setItemBarcode("");
        submitCollectionExceptionInfo.setCustomerCode("");
        submitCollectionExceptionInfo.setOwningInstitution(institutionCode);
        submitCollectionExceptionInfo.setMessage(message);
        submitCollectionExceptionInfos.add(submitCollectionExceptionInfo);
    }

    /**
     * Build submit collection report info map.
     *
     * @param submitCollectionReportInfoMap the submit collection report info map
     * @param fetchedBibliographicEntity    the fetched bibliographic entity
     * @param incomingBibliographicEntity   the incoming bibliographic entity
     * @return the map
     */
    public Map<String,List<SubmitCollectionReportInfo>> buildSubmitCollectionReportInfo(Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, BibliographicEntity fetchedBibliographicEntity, BibliographicEntity incomingBibliographicEntity){
        List<SubmitCollectionReportInfo> successSubmitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST);
        List<SubmitCollectionReportInfo> rejectedSubmitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST);
        List<SubmitCollectionReportInfo> failureSubmitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST);
        Map<String,Map<String,ItemEntity>> fetchedHoldingItemMap = getHoldingItemIdMap(fetchedBibliographicEntity);
        Map<String,Map<String,ItemEntity>> incomingHoldingItemMap = getHoldingItemIdMap(incomingBibliographicEntity);
        String owningInstitution = (String) setupDataService.getInstitutionIdCodeMap().get(fetchedBibliographicEntity.getOwningInstitutionId());
        String[] nonHoldingIdInstitutionArray = nonHoldingIdInstitution.split(",");
        String institutionCode = (String) setupDataService.getInstitutionIdCodeMap().get(incomingBibliographicEntity.getOwningInstitutionId());

        for (Map.Entry<String,Map<String,ItemEntity>> incomingHoldingItemMapEntry : incomingHoldingItemMap.entrySet()) {
            Map<String,ItemEntity> incomingOwningItemIdEntityMap = incomingHoldingItemMapEntry.getValue();
            Map<String,ItemEntity> fetchedOwningItemIdEntityMap = fetchedHoldingItemMap.get(incomingHoldingItemMapEntry.getKey());
            if (fetchedOwningItemIdEntityMap != null && !fetchedHoldingItemMap.isEmpty()) {
                for(Map.Entry<String,ItemEntity> incomingOwningItemIdEntityMapEntry:incomingOwningItemIdEntityMap.entrySet()){
                    setReportForMatchedAndUnmatchedRecords(submitCollectionReportInfoMap, successSubmitCollectionReportInfoList, rejectedSubmitCollectionReportInfoList, failureSubmitCollectionReportInfoList, owningInstitution, fetchedOwningItemIdEntityMap, incomingOwningItemIdEntityMapEntry);
                }
            } else if(Arrays.asList(nonHoldingIdInstitutionArray).contains(institutionCode)){//Report for non holding id institution eg:NYPL
                Map<String,ItemEntity> incomingItemEntityMap = getItemIdEntityMap(incomingBibliographicEntity);
                Map<String,ItemEntity> fetchedItemEntityMap = getItemIdEntityMap(fetchedBibliographicEntity);
                for(Map.Entry<String,ItemEntity> incomingItemEntityMapEntry:incomingItemEntityMap.entrySet()){
                    setReportForMatchedAndUnmatchedRecords(submitCollectionReportInfoMap, successSubmitCollectionReportInfoList, rejectedSubmitCollectionReportInfoList, failureSubmitCollectionReportInfoList, owningInstitution, fetchedItemEntityMap, incomingItemEntityMapEntry);
                }
            } else {//Failure report - holding id mismatch
                for(Map.Entry<String,ItemEntity> incomingOwningItemIdBarcodeMapEntry:incomingOwningItemIdEntityMap.entrySet()) {
                    ItemEntity incomingItemEntity = incomingOwningItemIdBarcodeMapEntry.getValue();
                    SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
                    submitCollectionReportInfo.setItemBarcode(incomingItemEntity.getBarcode());
                    submitCollectionReportInfo.setCustomerCode(incomingItemEntity.getCustomerCode());
                    submitCollectionReportInfo.setOwningInstitution(owningInstitution);
                    submitCollectionReportInfo.setMessage("Failed record - Owning institution holding id "+incomingHoldingItemMapEntry.getKey()+" for the incoming barcode "+incomingItemEntity.getBarcode()
                            +", owning institution item id "+incomingItemEntity.getOwningInstitutionItemId()+" is unavailable in the existing bib - owning institution bib id - "+incomingBibliographicEntity.getOwningInstitutionBibId());
                    failureSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
                }
            }
        }
        submitCollectionReportInfoMap.put(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST,successSubmitCollectionReportInfoList);
        submitCollectionReportInfoMap.put(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST,failureSubmitCollectionReportInfoList);
        submitCollectionReportInfoMap.put(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST,rejectedSubmitCollectionReportInfoList);
        return submitCollectionReportInfoMap;

    }

    private void setReportForMatchedAndUnmatchedRecords(Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<SubmitCollectionReportInfo> successSubmitCollectionReportInfoList, List<SubmitCollectionReportInfo> rejectedSubmitCollectionReportInfoList, List<SubmitCollectionReportInfo> failureSubmitCollectionReportInfoList, String owningInstitution, Map<String, ItemEntity> fetchedOwningItemIdEntityMap, Map.Entry<String, ItemEntity> incomingOwningItemIdEntityMapEntry) {
        ItemEntity incomingItemEntity = incomingOwningItemIdEntityMapEntry.getValue();
        ItemEntity fetchedItemEntity = fetchedOwningItemIdEntityMap.get(incomingOwningItemIdEntityMapEntry.getKey());
        if(fetchedItemEntity!=null && incomingItemEntity.getBarcode().equals(fetchedItemEntity.getBarcode())){
            setReportInfoForMatchedRecord(submitCollectionReportInfoMap, successSubmitCollectionReportInfoList, rejectedSubmitCollectionReportInfoList, owningInstitution, incomingItemEntity, fetchedItemEntity);
        } else {//Failure report - item id mismatch
            SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
            submitCollectionReportInfo.setItemBarcode(incomingItemEntity.getBarcode());
            submitCollectionReportInfo.setCustomerCode(incomingItemEntity.getCustomerCode()!=null?incomingItemEntity.getCustomerCode():"");
            submitCollectionReportInfo.setOwningInstitution(owningInstitution);
            ItemEntity misMatchedItemEntity = getMismatchedItemEntity(incomingItemEntity,fetchedOwningItemIdEntityMap);
            if (misMatchedItemEntity != null) {
                submitCollectionReportInfo.setMessage("Failed record - Incoming item "+incomingItemEntity.getBarcode()+", owning institution item id "+incomingItemEntity.getOwningInstitutionItemId()
                        +" is not matched with the existing item "+misMatchedItemEntity.getBarcode()+ ", owning institution item id "+misMatchedItemEntity.getOwningInstitutionItemId()
                        +", owning institution holding id "+misMatchedItemEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId()+", owning institution bib id "
                        +misMatchedItemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                failureSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
            }
        }
    }

    private void setReportInfoForMatchedRecord(Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<SubmitCollectionReportInfo> successSubmitCollectionReportInfoList, List<SubmitCollectionReportInfo> rejectedSubmitCollectionReportInfoList, String owningInstitution, ItemEntity incomingItemEntity, ItemEntity fetchedItemEntity) {
        ItemStatusEntity itemStatusEntity = repositoryService.getItemStatusDetailsRepository().findByItemStatusId(fetchedItemEntity.getItemAvailabilityStatusId());
        if(!itemStatusEntity.getStatusCode().equalsIgnoreCase(ReCAPConstants.ITEM_STATUS_AVAILABLE)){//Rejection report
            SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
            submitCollectionReportInfo.setItemBarcode(fetchedItemEntity.getBarcode());
            submitCollectionReportInfo.setCustomerCode(fetchedItemEntity.getCustomerCode());
            submitCollectionReportInfo.setOwningInstitution(owningInstitution);
            submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_RECORD);
            rejectedSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
        } else {//Success report
            SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
            submitCollectionReportInfo.setItemBarcode(fetchedItemEntity.getBarcode());
            submitCollectionReportInfo.setCustomerCode(fetchedItemEntity.getCustomerCode());
            submitCollectionReportInfo.setOwningInstitution(owningInstitution);
            StringBuilder sbMessage = new StringBuilder();
            sbMessage.append(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD);
            if(fetchedItemEntity.getCatalogingStatus() != null && fetchedItemEntity.getCatalogingStatus().equals(ReCAPConstants.INCOMPLETE_STATUS) &&
                    StringUtils.isEmpty(fetchedItemEntity.getUseRestrictions())){
                sbMessage.append("-").append(ReCAPConstants.RECORD_INCOMPLETE).append(ReCAPConstants.USE_RESTRICTION_UNAVAILABLE);
            }
            submitCollectionReportInfo.setMessage(sbMessage.toString());
            boolean isBarcodeAlreadyAdded = isBarcodeAlreadyAdded(incomingItemEntity,submitCollectionReportInfoMap);
            if (!isBarcodeAlreadyAdded) {//To avoid multiple response message for boundwith items
                successSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
            }
        }
    }

    private Map<String,Map<String,ItemEntity>> getHoldingItemIdMap(BibliographicEntity bibliographicEntity){
        Map<String,Map<String,ItemEntity>> holdingItemMap = new HashMap<>();
        Map<String,ItemEntity> itemEntityMap = new HashMap<>();
        for(HoldingsEntity holdingsEntity:bibliographicEntity.getHoldingsEntities()){
            for(ItemEntity itemEntity:holdingsEntity.getItemEntities()){
                itemEntityMap.put(itemEntity.getOwningInstitutionItemId(),itemEntity);
            }
            holdingItemMap.put(holdingsEntity.getOwningInstitutionHoldingsId(),itemEntityMap);
        }
        return holdingItemMap;
    }

    private Map<String,ItemEntity> getItemIdEntityMap(BibliographicEntity bibliographicEntity){
        Map<String,ItemEntity> itemEntityMap = new HashedMap();
        for(ItemEntity itemEntity:bibliographicEntity.getItemEntities()){
            itemEntityMap.put(itemEntity.getOwningInstitutionItemId(),itemEntity);
        }
        return itemEntityMap;
    }

    private ItemEntity getMismatchedItemEntity(ItemEntity incomingItemEntity, Map<String,ItemEntity> fetchedOwningItemIdBarcodeMap){
        for(Map.Entry<String,ItemEntity> fetchedOwningItemIdBarcodeMapEntry:fetchedOwningItemIdBarcodeMap.entrySet()){
            ItemEntity fetchedItemEntity = fetchedOwningItemIdBarcodeMapEntry.getValue();
            if(incomingItemEntity.getBarcode().equals(fetchedItemEntity.getBarcode())){
                return fetchedItemEntity;
            }
        }
        return null;
    }

    /**
     * Get items which are having complete cataloging status.
     *
     * @param itemEntityList the item entity list
     * @return the list
     */
    public List<ItemEntity> getIncomingItemIsIncomplete(List<ItemEntity> itemEntityList){
        List<String> barcodeList = new ArrayList<>();
        for(ItemEntity itemEntity:itemEntityList){
            barcodeList.add(itemEntity.getBarcode());
        }
        List<ItemEntity> fetchedItemEntityList = repositoryService.getItemDetailsRepository().findByBarcodeInAndComplete(barcodeList);
        return fetchedItemEntityList;
    }


    /**
     * Get item based on owning institution item id and list of owning institution id.
     *
     * @param itemEntityList the item entity list
     * @return the list
     */
    public List<ItemEntity> getItemBasedOnOwningInstitutionItemIdAndOwningInstitutionId(List<ItemEntity> itemEntityList){
        List<String> owningInstitutionItemIdList = new ArrayList<>();
        for(ItemEntity itemEntity:itemEntityList){
            owningInstitutionItemIdList.add(itemEntity.getOwningInstitutionItemId());
        }
        List<ItemEntity> fetchedItemEntityList = repositoryService.getItemDetailsRepository().findByOwningInstitutionItemIdInAndOwningInstitutionId(owningInstitutionItemIdList,itemEntityList.get(0).getOwningInstitutionId());
        return fetchedItemEntityList;
    }


    /**
     * Sets submit collection failure report for unexpected exception .
     *
     * @param bibliographicEntity            the bibliographic entity
     * @param submitCollectionReportInfoList the submit collection report info list
     * @param message                        the message
     */
    public void setSubmitCollectionFailureReportForUnexpectedException(BibliographicEntity bibliographicEntity, List<SubmitCollectionReportInfo> submitCollectionReportInfoList, String message ) {
        for (ItemEntity itemEntity:bibliographicEntity.getItemEntities()) {
            setSubmitCollectionReportInfo(submitCollectionReportInfoList,itemEntity,message);
        }
    }
}
