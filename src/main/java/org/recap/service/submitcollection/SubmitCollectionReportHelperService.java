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
     * Set submit collection rejection info.
     *
     * @param itemEntityList            the item entity list
     * @param submitCollectionRejectionInfos the submit collection rejection infos
     */
    public void setSubmitCollectionRejectionInfo(List<ItemEntity> itemEntityList, List<SubmitCollectionReportInfo> submitCollectionRejectionInfos){
        for(ItemEntity itemEntity : itemEntityList){
            ItemStatusEntity itemStatusEntity = repositoryService.getItemStatusDetailsRepository().findByItemStatusId(itemEntity.getItemAvailabilityStatusId());
            if(!itemStatusEntity.getStatusCode().equalsIgnoreCase(ReCAPConstants.ITEM_STATUS_AVAILABLE)){
                SubmitCollectionReportInfo submitCollectionRejectionInfo = new SubmitCollectionReportInfo();
                submitCollectionRejectionInfo.setItemBarcode(itemEntity.getBarcode());
                submitCollectionRejectionInfo.setCustomerCode(itemEntity.getCustomerCode());
                submitCollectionRejectionInfo.setOwningInstitution((String) setupDataService.getInstitutionEntityMap().get(itemEntity.getOwningInstitutionId()));
                submitCollectionRejectionInfo.setMessage(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_RECORD);
                submitCollectionRejectionInfos.add(submitCollectionRejectionInfo);
            }
        }
    }

    public void setSubmitCollectionReportInfo(List<ItemEntity> itemEntityList, List<SubmitCollectionReportInfo> submitCollectionExceptionInfos, String message) {
        for (ItemEntity itemEntity : itemEntityList) {
            logger.info("Report data for item {}",itemEntity.getBarcode());
            SubmitCollectionReportInfo submitCollectionExceptionInfo = new SubmitCollectionReportInfo();
            submitCollectionExceptionInfo.setItemBarcode(itemEntity.getBarcode());
            submitCollectionExceptionInfo.setCustomerCode(itemEntity.getCustomerCode());
            submitCollectionExceptionInfo.setOwningInstitution((String) setupDataService.getInstitutionEntityMap().get(itemEntity.getOwningInstitutionId()));
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

    public void setSubmitCollectionReportInfoForInvalidXml(List<SubmitCollectionReportInfo> submitCollectionExceptionInfos, String message) {
        SubmitCollectionReportInfo submitCollectionExceptionInfo = new SubmitCollectionReportInfo();
        submitCollectionExceptionInfo.setItemBarcode("");
        submitCollectionExceptionInfo.setCustomerCode("");
        submitCollectionExceptionInfo.setOwningInstitution("");
        submitCollectionExceptionInfo.setMessage(message);
        submitCollectionExceptionInfos.add(submitCollectionExceptionInfo);
    }

    public Map<String,List<SubmitCollectionReportInfo>> buildSubmitCollectionReportInfo(Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, BibliographicEntity fetchedBibliographicEntity, BibliographicEntity incomingBibliographicEntity){
        List<SubmitCollectionReportInfo> successSubmitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST)!=null?submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST):new ArrayList<>();
        List<SubmitCollectionReportInfo> rejectedSubmitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST)!=null?submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST):new ArrayList<>();
        List<SubmitCollectionReportInfo> failureSubmitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST)!=null?submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST):new ArrayList<>();
        Map<String,Map<String,ItemEntity>> fetchedHoldingItemMap = getHoldingItemIdMap(fetchedBibliographicEntity);
        Map<String,Map<String,ItemEntity>> incomingHoldingItemMap = getHoldingItemIdMap(incomingBibliographicEntity);
        String owningInstitution = (String) setupDataService.getInstitutionEntityMap().get(fetchedBibliographicEntity.getOwningInstitutionId());
        for (Map.Entry<String,Map<String,ItemEntity>> incomingHoldingItemMapEntry : incomingHoldingItemMap.entrySet()) {
            Map<String,ItemEntity> incomingOwningItemIdBarcodeMap = incomingHoldingItemMapEntry.getValue();
            Map<String,ItemEntity> fetchedOwningItemIdBarcodeMap = fetchedHoldingItemMap.get(incomingHoldingItemMapEntry.getKey());
            if (fetchedOwningItemIdBarcodeMap != null && !fetchedHoldingItemMap.isEmpty()) {
                for(Map.Entry<String,ItemEntity> incomingOwningItemIdBarcodeMapEntry:incomingOwningItemIdBarcodeMap.entrySet()){
                    ItemEntity incomingItemEntity = incomingOwningItemIdBarcodeMapEntry.getValue();
                    ItemEntity fetchedItemEntity = fetchedOwningItemIdBarcodeMap.get(incomingOwningItemIdBarcodeMapEntry.getKey());
                    if(fetchedItemEntity!=null && incomingItemEntity.getBarcode().equals(fetchedItemEntity.getBarcode())){
                        ItemStatusEntity itemStatusEntity = repositoryService.getItemStatusDetailsRepository().findByItemStatusId(fetchedItemEntity.getItemAvailabilityStatusId());
                        if(!itemStatusEntity.getStatusCode().equalsIgnoreCase(ReCAPConstants.ITEM_STATUS_AVAILABLE)){
                            SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
                            submitCollectionReportInfo.setItemBarcode(fetchedItemEntity.getBarcode());
                            submitCollectionReportInfo.setCustomerCode(fetchedItemEntity.getCustomerCode());
                            submitCollectionReportInfo.setOwningInstitution(owningInstitution);
                            submitCollectionReportInfo.setMessage("Rejection record - only use restriction and cgd not updated because the item is unavailable");
                            rejectedSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
                        } else {
                            SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
                            submitCollectionReportInfo.setItemBarcode(fetchedItemEntity.getBarcode());
                            submitCollectionReportInfo.setCustomerCode(fetchedItemEntity.getCustomerCode());
                            submitCollectionReportInfo.setOwningInstitution(owningInstitution);
                            submitCollectionReportInfo.setMessage("Success record");
                            successSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
                        }
                    } else{
                        SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
                        submitCollectionReportInfo.setItemBarcode(incomingItemEntity.getBarcode());
                        submitCollectionReportInfo.setCustomerCode(incomingItemEntity.getCustomerCode()!=null?incomingItemEntity.getCustomerCode():"");
                        submitCollectionReportInfo.setOwningInstitution(owningInstitution);
                        ItemEntity misMatchedItemEntity = getMismatchedItemEntity(incomingItemEntity,fetchedOwningItemIdBarcodeMap);
                        submitCollectionReportInfo.setMessage("Failed - Item "+incomingItemEntity.getBarcode()+", owning institution item id "+incomingItemEntity.getOwningInstitutionItemId()
                                +" mismatched with the existing item "+misMatchedItemEntity.getBarcode()+ ", owning institution item id "+misMatchedItemEntity.getOwningInstitutionItemId()
                                +", owning institution holding id "+misMatchedItemEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId()+", owning institution bib id "
                                +misMatchedItemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                        failureSubmitCollectionReportInfoList.add(submitCollectionReportInfo);
                    }
                }
            } else {
                for(Map.Entry<String,ItemEntity> incomingOwningItemIdBarcodeMapEntry:incomingOwningItemIdBarcodeMap.entrySet()) {
                    ItemEntity incomingItemEntity = incomingOwningItemIdBarcodeMapEntry.getValue();
                    SubmitCollectionReportInfo submitCollectionReportInfo = new SubmitCollectionReportInfo();
                    submitCollectionReportInfo.setItemBarcode(incomingItemEntity.getBarcode());
                    submitCollectionReportInfo.setCustomerCode(incomingItemEntity.getCustomerCode());
                    submitCollectionReportInfo.setOwningInstitution(owningInstitution);
                    submitCollectionReportInfo.setMessage("Failed - Owning institution holding id "+incomingOwningItemIdBarcodeMapEntry.getKey()+" for the barcode "+incomingItemEntity.getBarcode()
                            +" is unavailable in the existing bib - owning institution bib id - "+incomingBibliographicEntity.getOwningInstitutionBibId());
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

}
