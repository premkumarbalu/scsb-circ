package org.recap.service.submitcollection;

import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.model.BibliographicEntity;
import org.recap.model.HoldingsEntity;
import org.recap.model.ItemEntity;
import org.recap.model.ItemStatusEntity;
import org.recap.model.report.SubmitCollectionReportInfo;
import org.recap.service.common.RepositoryService;
import org.recap.service.common.SetupDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    /**
     * This method sets submit collection report information based on the given information.
     *
     * @param itemEntityList                 the item entity list
     * @param submitCollectionExceptionInfos the submit collection exception infos
     * @param message                        the message
     */
    public void setSubmitCollectionReportInfo(List<ItemEntity> itemEntityList, List<SubmitCollectionReportInfo> submitCollectionExceptionInfos, String message) {
        for (ItemEntity itemEntity : itemEntityList) {
            logger.info("Report data for item {}",itemEntity.getBarcode());
            SubmitCollectionReportInfo submitCollectionExceptionInfo = new SubmitCollectionReportInfo();
            submitCollectionExceptionInfo.setItemBarcode(itemEntity.getBarcode());
            submitCollectionExceptionInfo.setCustomerCode(itemEntity.getCustomerCode());
            submitCollectionExceptionInfo.setOwningInstitution((String) setupDataService.getInstitutionIdCodeMap().get(itemEntity.getOwningInstitutionId()));
            StringBuilder sbMessage = new StringBuilder();
            sbMessage.append(message);
            if(itemEntity.getCatalogingStatus() != null && itemEntity.getCatalogingStatus().equals(ReCAPConstants.INCOMPLETE_STATUS)){
                if(StringUtils.isEmpty(itemEntity.getUseRestrictions())){
                    sbMessage.append("-").append(ReCAPConstants.RECORD_INCOMPLETE).append(ReCAPConstants.USE_RESTRICTION_UNAVAILABLE);
                }
            }
            submitCollectionExceptionInfo.setMessage(sbMessage.toString());
            submitCollectionExceptionInfos.add(submitCollectionExceptionInfo);
        }
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
        for (Map.Entry<String,Map<String,ItemEntity>> incomingHoldingItemMapEntry : incomingHoldingItemMap.entrySet()) {
            Map<String,ItemEntity> incomingOwningItemIdBarcodeMap = incomingHoldingItemMapEntry.getValue();
            Map<String,ItemEntity> fetchedOwningItemIdBarcodeMap = fetchedHoldingItemMap.get(incomingHoldingItemMapEntry.getKey());
            if (fetchedOwningItemIdBarcodeMap != null && !fetchedHoldingItemMap.isEmpty()) {
                for(Map.Entry<String,ItemEntity> incomingOwningItemIdBarcodeMapEntry:incomingOwningItemIdBarcodeMap.entrySet()){
                    ItemEntity incomingItemEntity = incomingOwningItemIdBarcodeMapEntry.getValue();
                    ItemEntity fetchedItemEntity = fetchedOwningItemIdBarcodeMap.get(incomingOwningItemIdBarcodeMapEntry.getKey());
                    if(fetchedItemEntity!=null && incomingItemEntity.getBarcode().equals(fetchedItemEntity.getBarcode())){
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
                    } else{//Failure report - item id mismatch
                        SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
                        submitCollectionReportInfo.setItemBarcode(incomingItemEntity.getBarcode());
                        submitCollectionReportInfo.setCustomerCode(incomingItemEntity.getCustomerCode()!=null?incomingItemEntity.getCustomerCode():"");
                        submitCollectionReportInfo.setOwningInstitution(owningInstitution);
                        ItemEntity misMatchedItemEntity = getMismatchedItemEntity(incomingItemEntity,fetchedOwningItemIdBarcodeMap);
                        if (misMatchedItemEntity != null) {
                            submitCollectionReportInfo.setMessage("Failed record - Incoming item "+incomingItemEntity.getBarcode()+", owning institution item id "+incomingItemEntity.getOwningInstitutionItemId()
                                    +" is not matched with the existing item "+misMatchedItemEntity.getBarcode()+ ", owning institution item id "+misMatchedItemEntity.getOwningInstitutionItemId()
                                    +", owning institution holding id "+misMatchedItemEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId()+", owning institution bib id "
                                    +misMatchedItemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                            failureSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
                        }
                    }
                }
            } else {//Failure report - holding id mismatch
                for(Map.Entry<String,ItemEntity> incomingOwningItemIdBarcodeMapEntry:incomingOwningItemIdBarcodeMap.entrySet()) {
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

}
