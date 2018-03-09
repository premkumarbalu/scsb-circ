package org.recap.service.submitcollection;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.model.*;
import org.recap.model.report.SubmitCollectionReportInfo;
import org.recap.repository.ItemDetailsRepository;
import org.recap.service.common.RepositoryService;
import org.recap.service.common.SetupDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by premkb on 11/6/17.
 */
@Service
public class SubmitCollectionReportHelperService {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionReportHelperService.class);

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SetupDataService setupDataService;

    @Autowired
    private SubmitCollectionHelperService submitCollectionHelperService;

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
            setSubmitCollectionReportInfo(submitCollectionExceptionInfos,itemEntity,sbMessage.toString(),null);
        }
    }


    public void setSubmitCollectionReportInfoForOwningInstitutionBibIdMismatch(BibliographicEntity fetchedBibliographicEntity, BibliographicEntity incomingBibliographicEntity,
                                                                               Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap){
        Map<String,String> fetchedBarcodeOwningInstitutionBibIdMap = getBarcodeOwningInstitutionBibIdMap(fetchedBibliographicEntity);
        Map<String,String> incomingBarcodeOwningInstitutionBibIdMap = getBarcodeOwningInstitutionBibIdMap(incomingBibliographicEntity);
        Map<String,ItemEntity> incomingBarcodeItemEntityMap = getBarcodeItemEntityMap(incomingBibliographicEntity.getItemEntities());
        Map<String,ItemEntity> fetchedBarcodeItemEntityMap = getBarcodeItemEntityMap(fetchedBibliographicEntity.getItemEntities());
        List<SubmitCollectionReportInfo> submitCollectionFailureReportInfos = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST);

        String owningInstitution = (String) setupDataService.getInstitutionIdCodeMap().get(fetchedBibliographicEntity.getOwningInstitutionId());
        for(Map.Entry<String,String> incomingOwningInstitutionBibIdBarcodeMapEntry : incomingBarcodeOwningInstitutionBibIdMap.entrySet()){
            String existingOwningInstitutionBibId = fetchedBarcodeOwningInstitutionBibIdMap.get(incomingOwningInstitutionBibIdBarcodeMapEntry.getKey());
            boolean isBarcodeAlreadyAdded = isBarcodeAlreadyAdded(incomingOwningInstitutionBibIdBarcodeMapEntry.getKey(),submitCollectionReportInfoMap);
            if(!isBarcodeAlreadyAdded){
                if(!existingOwningInstitutionBibId.equals(incomingOwningInstitutionBibIdBarcodeMapEntry.getValue())){
                    ItemEntity incomingItemEntity = incomingBarcodeItemEntityMap.get(incomingOwningInstitutionBibIdBarcodeMapEntry.getKey());
                    ItemEntity fetchedItemEntity = fetchedBarcodeItemEntityMap.get(incomingOwningInstitutionBibIdBarcodeMapEntry.getKey());
                    SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
                    submitCollectionReportInfo.setOwningInstitution(owningInstitution);
                    submitCollectionReportInfo.setItemBarcode(incomingOwningInstitutionBibIdBarcodeMapEntry.getKey());
                    submitCollectionReportInfo.setCustomerCode(incomingItemEntity.getCustomerCode());
                    submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_FAILED_RECORD+" - Owning institution bib id mismatch - incoming owning institution"
                            +"bib id "+incomingBibliographicEntity.getOwningInstitutionBibId()+", existing owning institution bib id "+fetchedBibliographicEntity.getOwningInstitutionBibId()
                            +", existing owning institution holdings id "+fetchedItemEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId()+", existing owning"
                            +"institution item id "+fetchedItemEntity.getOwningInstitutionItemId());
                    submitCollectionFailureReportInfos.add(submitCollectionReportInfo);
                }
            }
        }
    }

    public void setSubmitCollectionReportInfoForOwningInstitutionBibIdMismatchForBoundWith(List<String> notMatchedIncomingOwnInstBibId,List<String> notMatchedFetchedOwnInstBibId,ItemEntity incomingItemEntity,ItemEntity fetchedItemEntity,
                                                                                           List<SubmitCollectionReportInfo> submitCollectionExceptionInfos){
        String owningInstitution = (String) setupDataService.getInstitutionIdCodeMap().get(fetchedItemEntity.getOwningInstitutionId());

        SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
        submitCollectionReportInfo.setOwningInstitution(owningInstitution);
        submitCollectionReportInfo.setItemBarcode(fetchedItemEntity.getBarcode());
        submitCollectionReportInfo.setCustomerCode(incomingItemEntity.getCustomerCode());
        String incomingOwningInstBibIds = notMatchedIncomingOwnInstBibId.stream().collect(Collectors.joining(","));
        String fetchedOwningInstBibIds = notMatchedFetchedOwnInstBibId.stream().collect(Collectors.joining(","));
        submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_FAILED_RECORD+" - Owning institution bib id mismatch for bound-with item - incoming owning institution"
                +"bib id "+incomingOwningInstBibIds+", existing owning institution bib id "+fetchedOwningInstBibIds
                +", existing owning institution holdings id "+fetchedItemEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId()+", existing owning"
                +"institution item id "+fetchedItemEntity.getOwningInstitutionItemId());
        submitCollectionExceptionInfos.add(submitCollectionReportInfo);
    }


    private Map<String,String> getBarcodeOwningInstitutionBibIdMap(BibliographicEntity bibliographicEntity){
        Map<String,String> owningInstitutionBibIdBarcodeMap = new HashMap<>();
        for(ItemEntity itemEntity:bibliographicEntity.getItemEntities()){
            owningInstitutionBibIdBarcodeMap.put(itemEntity.getBarcode(),bibliographicEntity.getOwningInstitutionBibId());
        }
        return owningInstitutionBibIdBarcodeMap;
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
                message = ReCAPConstants.SUBMIT_COLLECTION_FAILED_RECORD+" - Issue while updating dummy record, incoming owning institution item id "+owningInstitutionItemId
                        +", is already attached with existing barcode "+fetchedItemEntity.getBarcode()+", existing owning institution item id "+incomingEntity.getOwningInstitutionItemId()+", existing owning institution bib id "+fetchedItemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId()
                        +", existing owning institution holdings id "+fetchedItemEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId();
            } else {
                message = ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD;
            }
            setSubmitCollectionReportInfo(submitCollectionReportInfoList, incomingEntity, message,null);
        }
    }

    private void setSubmitCollectionReportInfo(List<SubmitCollectionReportInfo> submitCollectionReportInfoList, ItemEntity incomingItemEntity, String message,InstitutionEntity institutionEntity) {
        SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
        submitCollectionReportInfo.setMessage(message);
        if (incomingItemEntity != null) {
            submitCollectionReportInfo.setItemBarcode(incomingItemEntity.getBarcode());
            submitCollectionReportInfo.setCustomerCode(incomingItemEntity.getCustomerCode());
            submitCollectionReportInfo.setOwningInstitution((String) setupDataService.getInstitutionIdCodeMap().get(incomingItemEntity.getOwningInstitutionId()));
        } else {
            submitCollectionReportInfo.setItemBarcode("");
            submitCollectionReportInfo.setCustomerCode("");
            submitCollectionReportInfo.setOwningInstitution(institutionEntity !=null ? institutionEntity.getInstitutionCode():"");
        }
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
     * @param barcode                       the item barcode
     * @param submitCollectionReportInfoMap the submit collection report info map
     * @return the boolean
     */
    public boolean isBarcodeAlreadyAdded(String barcode,Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap){
        for (Map.Entry<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoIndividualMap : submitCollectionReportInfoMap.entrySet()) {
            List<SubmitCollectionReportInfo> submitCollectionReportInfoList = submitCollectionReportInfoIndividualMap.getValue();
            if(!submitCollectionReportInfoList.isEmpty()){
                for(SubmitCollectionReportInfo submitCollectionReportInfo : submitCollectionReportInfoList){
                    if(submitCollectionReportInfo.getItemBarcode().equals(barcode)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isBarcodeAlreadyAddedToReport(String barcode,List<SubmitCollectionReportInfo> submitCollectionReportInfoList){
            if(!submitCollectionReportInfoList.isEmpty()){
                for(SubmitCollectionReportInfo submitCollectionReportInfo : submitCollectionReportInfoList){
                    if(submitCollectionReportInfo.getItemBarcode().equals(barcode)){
                        return true;
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
        Map<String,Map<String,ItemEntity>> fetchedHoldingItemMap = submitCollectionHelperService.getHoldingItemIdMap(fetchedBibliographicEntity);
        Map<String,Map<String,ItemEntity>> incomingHoldingItemMap = submitCollectionHelperService.getHoldingItemIdMap(incomingBibliographicEntity);
        String owningInstitution = (String) setupDataService.getInstitutionIdCodeMap().get(fetchedBibliographicEntity.getOwningInstitutionId());
        String[] nonHoldingIdInstitutionArray = nonHoldingIdInstitution.split(",");
        String institutionCode = (String) setupDataService.getInstitutionIdCodeMap().get(incomingBibliographicEntity.getOwningInstitutionId());

        for (Map.Entry<String,Map<String,ItemEntity>> incomingHoldingItemMapEntry : incomingHoldingItemMap.entrySet()) {
            Map<String,ItemEntity> incomingOwningItemIdEntityMap = incomingHoldingItemMapEntry.getValue();
            Map<String,ItemEntity> fetchedOwningItemIdEntityMap = fetchedHoldingItemMap.get(incomingHoldingItemMapEntry.getKey());
            if(Arrays.asList(nonHoldingIdInstitutionArray).contains(institutionCode)) {//Report for non holding id institution eg:NYPL
                Map<String,ItemEntity> incomingItemEntityMap = getItemIdEntityMap(incomingBibliographicEntity);
                Map<String,ItemEntity> fetchedItemEntityMap = getItemIdEntityMap(fetchedBibliographicEntity);
                for(Map.Entry<String,ItemEntity> incomingItemEntityMapEntry:incomingItemEntityMap.entrySet()){
                    setReportForMatchedAndUnmatchedRecords(submitCollectionReportInfoMap, successSubmitCollectionReportInfoList, rejectedSubmitCollectionReportInfoList, failureSubmitCollectionReportInfoList, owningInstitution, fetchedItemEntityMap, incomingItemEntityMapEntry,incomingHoldingItemMapEntry.getKey());
                }
            } else if (fetchedOwningItemIdEntityMap != null && !fetchedHoldingItemMap.isEmpty()) {
                for(Map.Entry<String,ItemEntity> incomingOwningItemIdEntityMapEntry:incomingOwningItemIdEntityMap.entrySet()){
                    setReportForMatchedAndUnmatchedRecords(submitCollectionReportInfoMap, successSubmitCollectionReportInfoList, rejectedSubmitCollectionReportInfoList, failureSubmitCollectionReportInfoList, owningInstitution, fetchedOwningItemIdEntityMap, incomingOwningItemIdEntityMapEntry,incomingHoldingItemMapEntry.getKey());
                }
            } else {//Failure report - holding id mismatch and for dummy record not having CGD in the incoming data
                for(Map.Entry<String,ItemEntity> incomingOwningItemIdBarcodeMapEntry:incomingOwningItemIdEntityMap.entrySet()) {
                    ItemEntity incomingItemEntity = incomingOwningItemIdBarcodeMapEntry.getValue();
                    if (incomingItemEntity.getCollectionGroupId()==null) {
                        SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
                        submitCollectionReportInfo.setItemBarcode(incomingItemEntity.getBarcode());
                        submitCollectionReportInfo.setCustomerCode(incomingItemEntity.getCustomerCode());
                        submitCollectionReportInfo.setOwningInstitution(owningInstitution);
                        submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_FAILED_RECORD+" - "+"Unable to update dummy record, CGD is unavailable in the incoming xml record - incoming owning institution bib id - "+incomingBibliographicEntity.getOwningInstitutionBibId()
                                +", incoming owning institution item id - "+incomingItemEntity.getOwningInstitutionItemId());
                        failureSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
                    } else {
                        SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
                        submitCollectionReportInfo.setItemBarcode(incomingItemEntity.getBarcode());
                        submitCollectionReportInfo.setCustomerCode(incomingItemEntity.getCustomerCode());
                        submitCollectionReportInfo.setOwningInstitution(owningInstitution);
                        String existingOwningInstitutionHoldingsId = getExistingItemEntityOwningInstItemId(fetchedBibliographicEntity,incomingItemEntity);
                        submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_FAILED_RECORD+" - Owning institution holdings id mismatch - incoming owning institution holdings id " +incomingHoldingItemMapEntry.getKey()+ ", existing owning institution item id "+incomingItemEntity.getOwningInstitutionItemId()
                                +", existing owning institution holdings id "+existingOwningInstitutionHoldingsId+", existing owning institution bib id "+fetchedBibliographicEntity.getOwningInstitutionBibId());
                        failureSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
                    }
                }
            }
        }
        submitCollectionReportInfoMap.put(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST,successSubmitCollectionReportInfoList);
        submitCollectionReportInfoMap.put(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST,failureSubmitCollectionReportInfoList);
        submitCollectionReportInfoMap.put(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST,rejectedSubmitCollectionReportInfoList);
        return submitCollectionReportInfoMap;

    }

    private String getExistingItemEntityOwningInstItemId(BibliographicEntity fetchedBibliographicEntity,ItemEntity incomingItemEntity){
        for(ItemEntity fetchedItemEntity:fetchedBibliographicEntity.getItemEntities()){
            if(fetchedItemEntity.getOwningInstitutionItemId().equals(incomingItemEntity.getOwningInstitutionItemId())){
                return fetchedItemEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId();
            }
        }
        return "";
    }

    private void setReportForMatchedAndUnmatchedRecords(Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<SubmitCollectionReportInfo> successSubmitCollectionReportInfoList, List<SubmitCollectionReportInfo> rejectedSubmitCollectionReportInfoList, List<SubmitCollectionReportInfo> failureSubmitCollectionReportInfoList, String owningInstitution, Map<String, ItemEntity> fetchedOwningItemIdEntityMap, Map.Entry<String, ItemEntity> incomingOwningItemIdEntityMapEntry,String incomingOwningInstHoldingsId) {
        ItemEntity incomingItemEntity = incomingOwningItemIdEntityMapEntry.getValue();
        ItemEntity fetchedItemEntity = fetchedOwningItemIdEntityMap.get(incomingOwningItemIdEntityMapEntry.getKey());
        if(fetchedItemEntity!=null && incomingItemEntity.getBarcode().equals(fetchedItemEntity.getBarcode())){
            setReportInfoForMatchedRecord(submitCollectionReportInfoMap, successSubmitCollectionReportInfoList, rejectedSubmitCollectionReportInfoList, owningInstitution, incomingItemEntity, fetchedItemEntity);
        } else {//Failure report - item id mismatch
            boolean isBarcodeAlreadyAdded = isBarcodeAlreadyAddedToReport(incomingItemEntity.getBarcode(),submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST));
            if(!isBarcodeAlreadyAdded){
                SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
                submitCollectionReportInfo.setItemBarcode(incomingItemEntity.getBarcode());
                submitCollectionReportInfo.setCustomerCode(incomingItemEntity.getCustomerCode()!=null?incomingItemEntity.getCustomerCode():"");
                submitCollectionReportInfo.setOwningInstitution(owningInstitution);
                ItemEntity misMatchedItemEntity = getMismatchedItemEntity(incomingItemEntity,fetchedOwningItemIdEntityMap);
                if (misMatchedItemEntity != null) {
                    submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_FAILED_RECORD+" - Owning institution item id mismatch - incoming owning institution item id "+incomingItemEntity.getOwningInstitutionItemId()
                            +" , existing owning institution item id "+misMatchedItemEntity.getOwningInstitutionItemId()
                            +", existing owning institution holding id "+misMatchedItemEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId()+", existing owning institution bib id "
                            +misMatchedItemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                    failureSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
                } else if(fetchedItemEntity==null){ //Failure report - corresponding item's owningInstHoldingId mismatch
                    List<ItemEntity> existingBarcodeDetails = itemDetailsRepository.findByBarcode(incomingItemEntity.getBarcode());
                    ItemEntity existingItemEntity = existingBarcodeDetails.get(0);
                    submitCollectionReportInfo.setOwningInstitution(existingItemEntity.getInstitutionEntity().getInstitutionCode());
                        submitCollectionReportInfo.setCustomerCode(existingItemEntity.getCustomerCode());
                        submitCollectionReportInfo.setItemBarcode(existingItemEntity.getBarcode());
                        submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_FAILED_RECORD + " - Owning institution holdings id mismatch - incoming owning institution holdings id " + incomingOwningInstHoldingsId + ", existing owning institution item id " + existingItemEntity.getOwningInstitutionItemId()
                                + ", existing owning institution holdings id " + existingItemEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId() + ", existing owning institution bib id : " + existingItemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                        failureSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
                }
            }
        }
    }

    private void setReportInfoForMatchedRecord(Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<SubmitCollectionReportInfo> successSubmitCollectionReportInfoList,
                                               List<SubmitCollectionReportInfo> rejectedSubmitCollectionReportInfoList, String owningInstitution, ItemEntity incomingItemEntity, ItemEntity fetchedItemEntity) {
        if(!isAvailableItem(fetchedItemEntity.getItemAvailabilityStatusId()) && !fetchedItemEntity.isDeleted() && fetchedItemEntity.getCatalogingStatus().equals(ReCAPConstants.COMPLETE_STATUS)){//Rejection report
            boolean isMessageAlreadyAdded = false;
            for(SubmitCollectionReportInfo submitCollectionRejectionReportInfo:rejectedSubmitCollectionReportInfoList){
                if(submitCollectionRejectionReportInfo.getItemBarcode().equals(fetchedItemEntity.getBarcode())){
                    isMessageAlreadyAdded = true;
                }
            }
            if (!isMessageAlreadyAdded) {
                SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
                submitCollectionReportInfo.setItemBarcode(fetchedItemEntity.getBarcode());
                submitCollectionReportInfo.setCustomerCode(fetchedItemEntity.getCustomerCode());
                submitCollectionReportInfo.setOwningInstitution(owningInstitution);
                submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_RECORD);
                rejectedSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
            }

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
            boolean isBarcodeAlreadyAdded = isBarcodeAlreadyAdded(incomingItemEntity.getBarcode(),submitCollectionReportInfoMap);
            if (!isBarcodeAlreadyAdded) {//To avoid multiple response message for boundwith items
                successSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
            }
        }
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
    public List<ItemEntity> getIncomingItemIsComplete(List<ItemEntity> itemEntityList){
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
    public void setSubmitCollectionFailureReportForUnexpectedException(BibliographicEntity bibliographicEntity, List<SubmitCollectionReportInfo> submitCollectionReportInfoList, String message, InstitutionEntity institutionEntity) {
        if (bibliographicEntity != null) {
            for (ItemEntity itemEntity:bibliographicEntity.getItemEntities()) {
                setSubmitCollectionReportInfo(submitCollectionReportInfoList,itemEntity,message,null);
            }
        } else {
            setSubmitCollectionReportInfo(submitCollectionReportInfoList,null,message,institutionEntity);
        }
    }

    public boolean isAvailableItem(Integer itemAvailabilityStatusId){
        String itemStatusCode = (String) setupDataService.getItemStatusIdCodeMap().get(itemAvailabilityStatusId);
        if (itemStatusCode.equalsIgnoreCase(ReCAPConstants.ITEM_STATUS_AVAILABLE)) {
            return true;
        }
        return false;
    }

    public String updateSuccessMessageForAdditionalBibsAdded(List<BibliographicEntity> incomingBibliographicEntityList, List<BibliographicEntity> existingBibliographicEntityList,
                                                           ItemEntity existingItemEntity, String barcode, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap,boolean isItemAvailable) {
        Map<String,BibliographicEntity> incomingBibliographicEntityMap = incomingBibliographicEntityList.stream()
                .collect(Collectors.toMap(BibliographicEntity::getOwningInstitutionBibId,bibliographicEntity -> bibliographicEntity));
        Map<String,BibliographicEntity> existingBibliographicEntityMap = existingBibliographicEntityList.stream()
                .collect(Collectors.toMap(BibliographicEntity::getOwningInstitutionBibId,bibliographicEntity -> bibliographicEntity));
        List<String> newlyAddedOwningInstBibIdList = getNewlyAddedOwningInstBibIdList(incomingBibliographicEntityMap,existingBibliographicEntityMap);
        String newlyAddedOwningInstBibIdString = newlyAddedOwningInstBibIdList.stream().collect(Collectors.joining(","));
        StringBuilder message = new StringBuilder();
        message.append(" New bibs are attached to the item, newly attached owning institution bib id(s) - ")
                .append(newlyAddedOwningInstBibIdString).append(", bib count before update ").append(existingBibliographicEntityList.size())
                .append(", bib count after update ").append(incomingBibliographicEntityList.size());
        List<SubmitCollectionReportInfo> successSubmitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST);
        List<SubmitCollectionReportInfo> rejectionSubmitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST);
        boolean isSuccessMessageAdded = false;
        boolean isRejectedMessageAdded = false;
        for (SubmitCollectionReportInfo submitCollectionReportInfo : successSubmitCollectionReportInfoList) {//Added to update the success message with added bibs for bound-with items
            if (submitCollectionReportInfo.getItemBarcode().equals(barcode)) {
                submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD + ReCAPConstants.HYPHEN + message);
                isSuccessMessageAdded = true;
            }
        }
        for (SubmitCollectionReportInfo submitCollectionReportInfo : rejectionSubmitCollectionReportInfoList) {//Added to update the success message with added bibs for bound-with items
            if (submitCollectionReportInfo.getItemBarcode().equals(barcode)) {
                submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_RECORD + ReCAPConstants.HYPHEN + message);
                isRejectedMessageAdded = true;
            }
        }

        if(!isSuccessMessageAdded && !isRejectedMessageAdded){//Added to add the success message if there were no success message already for the bound-with item
            SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
            submitCollectionReportInfo.setItemBarcode(barcode);
            submitCollectionReportInfo.setOwningInstitution(existingItemEntity.getInstitutionEntity().getInstitutionCode());
            submitCollectionReportInfo.setCustomerCode(existingItemEntity.getCustomerCode());
            if(isItemAvailable) {
                submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD+ReCAPConstants.HYPHEN+message);
                successSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
            } else {
                submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_RECORD+", "+message);
                rejectionSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
            }
        }
        return message.toString();
    }

    public String updateSuccessMessageForRemovedBibs(List<BibliographicEntity> incomingBibliographicEntityList, List<BibliographicEntity> existingBibliographicEntityList,
                                                     ItemEntity existingItemEntity,String barcode, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap) {
        Map<String,BibliographicEntity> incomingBibliographicEntityMap = incomingBibliographicEntityList.stream()
                .collect(Collectors.toMap(BibliographicEntity::getOwningInstitutionBibId,bibliographicEntity -> bibliographicEntity));
        Map<String,BibliographicEntity> existingBibliographicEntityMap = existingBibliographicEntityList.stream()
                .collect(Collectors.toMap(BibliographicEntity::getOwningInstitutionBibId,bibliographicEntity -> bibliographicEntity));
        List<String> unlinkedOwningInstBibIdList = getUnlinkedOwningInstBibIdList(incomingBibliographicEntityMap,existingBibliographicEntityMap);
        String unlinkedOwningInstBibIdsString = unlinkedOwningInstBibIdList.stream().collect(Collectors.joining(","));
        StringBuilder message = new StringBuilder();
        message.append(" Bib(s) are unlinked from the item, unlinked owning institution bib id(s) - ")
                .append(unlinkedOwningInstBibIdsString).append(", bib count before update ").append(existingBibliographicEntityList.size())
                .append(", bib count after update ").append(incomingBibliographicEntityList.size());
        List<SubmitCollectionReportInfo> successSubmitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST);
        boolean isMessageAdded = false;
        for(SubmitCollectionReportInfo submitCollectionReportInfo:successSubmitCollectionReportInfoList){
            if(submitCollectionReportInfo.getItemBarcode().equals(barcode)){
                submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD+ReCAPConstants.HYPHEN+message);
                isMessageAdded = true;
            }
        }
        if(!isMessageAdded){//Added to add the success message if there were no success message already for the bound-with item
            SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
            submitCollectionReportInfo.setItemBarcode(barcode);
            submitCollectionReportInfo.setOwningInstitution(existingItemEntity.getInstitutionEntity().getInstitutionCode());
            submitCollectionReportInfo.setCustomerCode(existingItemEntity.getCustomerCode());
            submitCollectionReportInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD+ReCAPConstants.HYPHEN+message);
            successSubmitCollectionReportInfoList.add(submitCollectionReportInfo);

        }
        return message.toString();
    }

    private List<String> getNewlyAddedOwningInstBibIdList(Map<String, BibliographicEntity> incomingBibliographicEntityMap, Map<String, BibliographicEntity> existingBibliographicEntityMap) {
        List<String> newlyAddedOwningInstBibIdList = new ArrayList<>();
        for(Map.Entry<String,BibliographicEntity> incomingBibliographicEntityMapEntry:incomingBibliographicEntityMap.entrySet()){
            BibliographicEntity incomingBibliographicEntity = incomingBibliographicEntityMapEntry.getValue();
            if(!existingBibliographicEntityMap.containsKey(incomingBibliographicEntity.getOwningInstitutionBibId())){
                newlyAddedOwningInstBibIdList.add(incomingBibliographicEntity.getOwningInstitutionBibId());
            }
        }
        return newlyAddedOwningInstBibIdList;
    }

    private List<String> getUnlinkedOwningInstBibIdList(Map<String, BibliographicEntity> incomingBibliographicEntityMap, Map<String, BibliographicEntity> existingBibliographicEntityMap) {
        List<String> unlinkedOwningInstBibIdList = new ArrayList<>();
        for(Map.Entry<String,BibliographicEntity> existingBibliographicEntityMapEntry:existingBibliographicEntityMap.entrySet()){
            BibliographicEntity existingBibliographicEntity = existingBibliographicEntityMapEntry.getValue();
            if(!incomingBibliographicEntityMap.containsKey(existingBibliographicEntity.getOwningInstitutionBibId())){
                unlinkedOwningInstBibIdList.add(existingBibliographicEntity.getOwningInstitutionBibId());
            }
        }
        return unlinkedOwningInstBibIdList;
    }

    public void setSubmitCollectionReportInfo(List<SubmitCollectionReportInfo> submitCollectionReportInfoList,String barcode,String customerCode,String owningInstitution,String message){
        SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
        submitCollectionReportInfo.setItemBarcode(barcode);
        submitCollectionReportInfo.setCustomerCode(customerCode);
        submitCollectionReportInfo.setOwningInstitution(owningInstitution);
        submitCollectionReportInfo.setMessage(message);
        submitCollectionReportInfoList.add(submitCollectionReportInfo);
    }

}
