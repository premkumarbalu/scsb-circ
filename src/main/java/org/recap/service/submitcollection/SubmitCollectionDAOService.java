package org.recap.service.submitcollection;

import org.apache.commons.collections.map.HashedMap;
import org.recap.ReCAPConstants;
import org.recap.model.*;
import org.recap.model.report.SubmitCollectionReportInfo;
import org.recap.service.common.RepositoryService;
import org.recap.service.common.SetupDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

/**
 * Created by premkb on 11/6/17.
 */
@Service
public class SubmitCollectionDAOService {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionDAOService.class);
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SetupDataService setupDataService;

    @Autowired
    private SubmitCollectionReportHelperService submitCollectionReportHelperService;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${nonholdingid.institution}")
    private String nonHoldingIdInstitution;

    /**
     * This method updates the Bib, Holding and Item information for the given input xml
     *
     * @param bibliographicEntity           the bibliographic entity
     * @param submitCollectionReportInfoMap the submit collection report info map
     * @param idMapToRemoveIndexList            the id map to remove index
     * @return the bibliographic entity
     */
    public BibliographicEntity updateBibliographicEntity(BibliographicEntity bibliographicEntity, Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String,String>> idMapToRemoveIndexList,
                                                         Set<String> processedBarcodeSetForDummyRecords) {
        BibliographicEntity savedBibliographicEntity = null;
        BibliographicEntity fetchBibliographicEntity = getBibEntityUsingBarcode(bibliographicEntity);
        if(fetchBibliographicEntity != null ){//update existing record
            if(fetchBibliographicEntity.getOwningInstitutionBibId().equals(bibliographicEntity.getOwningInstitutionBibId())){//update existing complete record
                savedBibliographicEntity = updateExistingRecord(fetchBibliographicEntity,bibliographicEntity,submitCollectionReportInfoMap);
            } else {//update existing dummy record if any (Removes existing dummy record and creates new record for the same barcode based on the input xml)
                savedBibliographicEntity = updateDummyRecord(bibliographicEntity, submitCollectionReportInfoMap, idMapToRemoveIndexList, processedBarcodeSetForDummyRecords, savedBibliographicEntity, fetchBibliographicEntity);
            }
        } else {//if no record found to update, generate exception info
            savedBibliographicEntity = bibliographicEntity;
            addExceptionReport(bibliographicEntity.getItemEntities(), submitCollectionReportInfoMap,ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD);
        }
        return savedBibliographicEntity;
    }

    private void addExceptionReport(List<ItemEntity> itemEntityList, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap,String message) {
        boolean isBarcodeAlreadyAdded = submitCollectionReportHelperService.isBarcodeAlreadyAdded(itemEntityList.get(0),submitCollectionReportInfoMap);
        if (!isBarcodeAlreadyAdded) {//This is to avoid repeated error message for non-existing boundwith records
            submitCollectionReportHelperService.setSubmitCollectionExceptionReportInfo(itemEntityList,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_LIST), message);
        }
    }

    private BibliographicEntity updateDummyRecord(BibliographicEntity bibliographicEntity, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String, String>> idMapToRemoveIndexList, Set<String> processedBarcodeSet, BibliographicEntity savedBibliographicEntity, BibliographicEntity fetchBibliographicEntity) {
        List<ItemEntity> fetchedCompleteItem = submitCollectionReportHelperService.getIncomingItemIsIncomplete(bibliographicEntity.getItemEntities());//To verify the incoming barcode is complete for dummy record, if it is complete record and update will not happen.
        List<ItemEntity> fetchedItemBasedOnOwningInstitutionItemId = submitCollectionReportHelperService.getItemBasedOnOwningInstitutionItemIdAndOwningInstitutionId(bibliographicEntity.getItemEntities());
        boolean boundWith = isBoundWithItem(bibliographicEntity,processedBarcodeSet);
        if ((fetchedCompleteItem.isEmpty() && fetchedItemBasedOnOwningInstitutionItemId.isEmpty()) || boundWith) {
            boolean isCheckCGDNotNull = checkIsCGDNotNull(bibliographicEntity);
            if (isCheckCGDNotNull) {
                updateCustomerCode(fetchBibliographicEntity, bibliographicEntity);//Added to get customer code for existing dummy record, this value is used when the input xml dosent have the customer code in it, this happens mostly for CUL
                removeDummyRecord(idMapToRemoveIndexList, fetchBibliographicEntity);
                BibliographicEntity fetchedBibliographicEntity = repositoryService.getBibliographicDetailsRepository().findByOwningInstitutionIdAndOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionId(), bibliographicEntity.getOwningInstitutionBibId());
                BibliographicEntity bibliographicEntityToSave = bibliographicEntity;
                setItemAvailabilityStatus(bibliographicEntity.getItemEntities().get(0));
                updateCatalogingStatusForItem(bibliographicEntityToSave);
                updateCatalogingStatusForBib(bibliographicEntityToSave);
                if (fetchedBibliographicEntity != null) {//1Bib n holding n item
                    bibliographicEntityToSave = updateExistingRecordForDummy(fetchedBibliographicEntity, bibliographicEntity);
                }
                savedBibliographicEntity = repositoryService.getBibliographicDetailsRepository().saveAndFlush(bibliographicEntityToSave);
                saveItemChangeLogEntity(ReCAPConstants.SUBMIT_COLLECTION, ReCAPConstants.SUBMIT_COLLECTION_DUMMY_RECORD_UPDATE, savedBibliographicEntity.getItemEntities());
                entityManager.refresh(savedBibliographicEntity);
                setProcessedBarcode(bibliographicEntity, processedBarcodeSet);
                submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap, savedBibliographicEntity, bibliographicEntity);
            } else {
                    submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap,fetchBibliographicEntity,bibliographicEntity);
            }
        } else {
            if (!fetchedCompleteItem.isEmpty()) {
                submitCollectionReportHelperService.setSubmitCollectionReportInfoForInvalidDummyRecordBasedOnBarcode(bibliographicEntity,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),fetchedCompleteItem);
            } else if (!fetchedItemBasedOnOwningInstitutionItemId.isEmpty()) {
                submitCollectionReportHelperService.setSubmitCollectionReportInfoForInvalidDummyRecordBasedOnOwnInstItemId(bibliographicEntity,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),fetchedItemBasedOnOwningInstitutionItemId);
            }
        }
        return savedBibliographicEntity;
    }

    private boolean checkIsCGDNotNull(BibliographicEntity incomingBibliographicEntity){
        logger.info("item size--->{}",incomingBibliographicEntity.getItemEntities().size());
        for(ItemEntity itemEntity:incomingBibliographicEntity.getItemEntities()){
            if(itemEntity.getCollectionGroupId() == null){
                logger.info("item cgd is null");
                return false;
            }
            logger.info("item cgd is not null");
        }
        return true;
    }

    private boolean isBoundWithItem(BibliographicEntity bibliographicEntity,Set<String> processedBarcodeSet){
        for(String barcode:processedBarcodeSet){
            for(ItemEntity itemEntity:bibliographicEntity.getItemEntities()){
                if(itemEntity.getBarcode().equals(barcode)){
                    return true;
                }
            }
        }
        return false;
    }

    private void setProcessedBarcode(BibliographicEntity bibliographicEntity,Set<String> processedBarcodeSet){
        for(ItemEntity itemEntity:bibliographicEntity.getItemEntities()){
            processedBarcodeSet.add(itemEntity.getBarcode());
        }
    }

    private BibliographicEntity updateExistingRecord(BibliographicEntity fetchBibliographicEntity, BibliographicEntity incomingBibliographicEntity,
                                                     Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap) {
        BibliographicEntity savedOrUnsavedBibliographicEntity = null;
        logger.info("Processing bib owning institution bibid - ",incomingBibliographicEntity.getOwningInstitutionBibId());
        copyBibliographicEntity(fetchBibliographicEntity, incomingBibliographicEntity);
        List<HoldingsEntity> fetchedHoldingsEntityList = fetchBibliographicEntity.getHoldingsEntities();
        List<HoldingsEntity> incomingHoldingsEntityList = new ArrayList<>(incomingBibliographicEntity.getHoldingsEntities());
        List<ItemEntity> updatedItemEntityList = new ArrayList<>();
        boolean isValidHoldingToUpdate = false;
        boolean isValidItemToUpdate = false;
        String[] nonHoldingIdInstitutionArray = nonHoldingIdInstitution.split(",");
        String institutionCode = (String) setupDataService.getInstitutionIdCodeMap().get(incomingBibliographicEntity.getOwningInstitutionId());
        boolean isNonHoldingIdInstitution = Arrays.asList(nonHoldingIdInstitutionArray).contains(institutionCode);

        for(HoldingsEntity incomingHoldingsEntity:incomingHoldingsEntityList){
            for(HoldingsEntity fetchedHoldingsEntity:fetchedHoldingsEntityList){
                if (fetchedHoldingsEntity.getOwningInstitutionHoldingsId().equalsIgnoreCase(incomingHoldingsEntity.getOwningInstitutionHoldingsId())) {
                    copyHoldingsEntity(fetchedHoldingsEntity, incomingHoldingsEntity,false);
                    isValidHoldingToUpdate = true;
                } else if(isNonHoldingIdInstitution){//Added to handle non holding id institution
                    manageHoldingWithItem(incomingHoldingsEntity, fetchedHoldingsEntity);
                    isValidHoldingToUpdate = true;
                }
            }
        }

        List<ItemEntity> fetchItemEntityList = fetchBibliographicEntity.getItemEntities();
        List<ItemEntity> incomingItemEntityList = new ArrayList<>(incomingBibliographicEntity.getItemEntities());
        for(ItemEntity incomingItemEntity:incomingItemEntityList){
            boolean isItemUpdated = false;
            boolean isBarcodeMatched = false;
            for(ItemEntity fetchedItemEntity:fetchItemEntityList){
                    if (fetchedItemEntity.getOwningInstitutionItemId().equalsIgnoreCase(incomingItemEntity.getOwningInstitutionItemId())) {
                        if (fetchedItemEntity.getBarcode().equals(incomingItemEntity.getBarcode())) {
                            if(!isDeAccessionedItem(fetchedItemEntity)) {
                                copyItemEntity(fetchedItemEntity, incomingItemEntity, updatedItemEntityList);
                                isItemUpdated = true;
                                isValidItemToUpdate = true;
                                isBarcodeMatched = true;
                            } else {//add exception report for deaccession record
                                addExceptionReport(Arrays.asList(incomingItemEntity),submitCollectionReportInfoMap,ReCAPConstants.SUBMIT_COLLECTION_DEACCESSION_EXCEPTION_RECORD);
                            }
                        } else {//Owning institution id matched but barcode not matched
                            addExceptionReport(Arrays.asList(incomingItemEntity),submitCollectionReportInfoMap,ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD);
                        }
                    } else if(fetchedItemEntity.getBarcode().equals(incomingItemEntity.getBarcode())){
                        isBarcodeMatched = true;
                    }

                if(!isItemUpdated && !isBarcodeMatched){//Add to exception report when barcode is unavailable
                    addExceptionReport(Arrays.asList(incomingItemEntity),submitCollectionReportInfoMap,ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD);
                }
            }

        }

        fetchBibliographicEntity.setHoldingsEntities(fetchedHoldingsEntityList);
        fetchBibliographicEntity.setItemEntities(fetchItemEntityList);
        try {
            updateCatalogingStatusForBib(fetchBibliographicEntity);
            if (isValidHoldingToUpdate && isValidItemToUpdate) {
                savedOrUnsavedBibliographicEntity = repositoryService.getBibliographicDetailsRepository().saveAndFlush(fetchBibliographicEntity);
                saveItemChangeLogEntity(ReCAPConstants.SUBMIT_COLLECTION, ReCAPConstants.SUBMIT_COLLECTION_COMPLETE_RECORD_UPDATE,updatedItemEntityList);
            }
            submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap,fetchBibliographicEntity,incomingBibliographicEntity);
            return savedOrUnsavedBibliographicEntity;
        } catch (Exception e) {
            submitCollectionReportHelperService.setSubmitCollectionExceptionReportInfo(updatedItemEntityList,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST), ReCAPConstants.SUBMIT_COLLECTION_FAILED_RECORD);
            logger.error(ReCAPConstants.LOG_ERROR,e);
            return null;
        }
    }

    private boolean isDeAccessionedItem(ItemEntity fetchedItemEntity){
        if(fetchedItemEntity.isDeleted()){
            return true;
        }
        return false;
    }

    private BibliographicEntity updateExistingRecordForDummy(BibliographicEntity fetchBibliographicEntity, BibliographicEntity bibliographicEntity) {
        copyBibliographicEntity(fetchBibliographicEntity, bibliographicEntity);
        Map<String,HoldingsEntity> fetchedOwningInstHoldingIdHoldingsEntityMap = getOwningInstHoldingIdHoldingsEntityMap(fetchBibliographicEntity.getHoldingsEntities());
        Map<String,HoldingsEntity> incomingOwningInstHoldingIdHoldingsEntityMap = getOwningInstHoldingIdHoldingsEntityMap(bibliographicEntity.getHoldingsEntities());
        boolean isMatchingHoldingAvailable = false;
        for (Map.Entry<String,HoldingsEntity> incomingOwningInstHoldingIdHoldingsEntityMapEntry:incomingOwningInstHoldingIdHoldingsEntityMap.entrySet()){//To verify is there existing holding, if it is there then copy or add the new holding
            isMatchingHoldingAvailable = fetchedOwningInstHoldingIdHoldingsEntityMap.containsKey(incomingOwningInstHoldingIdHoldingsEntityMapEntry.getKey());
            if(isMatchingHoldingAvailable){
                HoldingsEntity fetchedHoldingEntity = fetchedOwningInstHoldingIdHoldingsEntityMap.get(incomingOwningInstHoldingIdHoldingsEntityMapEntry.getKey());
                copyHoldingsEntity(fetchedHoldingEntity, incomingOwningInstHoldingIdHoldingsEntityMapEntry.getValue(),true);
            } else {
                fetchBibliographicEntity.getHoldingsEntities().addAll(bibliographicEntity.getHoldingsEntities());
            }
        }

        fetchBibliographicEntity.getItemEntities().addAll(bibliographicEntity.getItemEntities());
        return fetchBibliographicEntity;
    }

    private Map<String,HoldingsEntity> getOwningInstHoldingIdHoldingsEntityMap(List<HoldingsEntity> holdingsEntityList){
        Map<String,HoldingsEntity> owningInstHoldingIdHoldingsEntityMap = new HashedMap();
        for(HoldingsEntity holdingsEntity:holdingsEntityList){
            owningInstHoldingIdHoldingsEntityMap.put(holdingsEntity.getOwningInstitutionHoldingsId(),holdingsEntity);
        }
        return owningInstHoldingIdHoldingsEntityMap;
    }

    private BibliographicEntity getBibEntityUsingBarcode(BibliographicEntity bibliographicEntity) {
        List<String> itemBarcodeList = new ArrayList<>();
        for (ItemEntity itemEntity : bibliographicEntity.getItemEntities()) {
            itemBarcodeList.add(itemEntity.getBarcode());
        }
        List<ItemEntity> itemEntityList = repositoryService.getItemDetailsRepository().findByBarcodeInAndOwningInstitutionId(itemBarcodeList,bibliographicEntity.getOwningInstitutionId());
        BibliographicEntity fetchedBibliographicEntity = null;
        if (itemEntityList != null && !itemEntityList.isEmpty() && (itemEntityList.get(0).getBibliographicEntities() != null && !itemEntityList.get(0).getBibliographicEntities().isEmpty())) {
            boolean isBoundWith = isBoundWithItem(itemEntityList.get(0));
            if (isBoundWith) {//To handle boundwith item
                for (BibliographicEntity resultBibliographicEntity : itemEntityList.get(0).getBibliographicEntities()) {
                    if (bibliographicEntity.getOwningInstitutionBibId().equals(resultBibliographicEntity.getOwningInstitutionBibId())) {
                        fetchedBibliographicEntity = resultBibliographicEntity;
                    }
                }
            }
            if((fetchedBibliographicEntity==null) && (itemEntityList.get(0).getBibliographicEntities() != null && !itemEntityList.get(0).getBibliographicEntities().isEmpty())){//To handle invalid incoming bound-with item and non bound-with item
                fetchedBibliographicEntity = itemEntityList.get(0).getBibliographicEntities().get(0);
            }
        }
        return fetchedBibliographicEntity;
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

    private void manageHoldingWithItem(HoldingsEntity incomingHoldingsEntity, HoldingsEntity fetchedHoldingsEntity) {
        List<ItemEntity> fetchedItemEntityList = fetchedHoldingsEntity.getItemEntities();
        List<ItemEntity> itemEntityList = incomingHoldingsEntity.getItemEntities();
        for (ItemEntity itemEntity : itemEntityList) {
            for (ItemEntity fetchedItemEntity : fetchedItemEntityList) {
                if (fetchedItemEntity.getOwningInstitutionItemId().equals(itemEntity.getOwningInstitutionItemId())) {
                    copyHoldingsEntity(fetchedHoldingsEntity, incomingHoldingsEntity,false);
                }
            }
        }
    }

    private HoldingsEntity copyHoldingsEntity(HoldingsEntity fetchHoldingsEntity, HoldingsEntity holdingsEntity, boolean isForDummyRecord){
        fetchHoldingsEntity.setContent(holdingsEntity.getContent());
        fetchHoldingsEntity.setLastUpdatedBy(holdingsEntity.getLastUpdatedBy());
        fetchHoldingsEntity.setLastUpdatedDate(holdingsEntity.getLastUpdatedDate());
        if(isForDummyRecord){
            fetchHoldingsEntity.getItemEntities().addAll(holdingsEntity.getItemEntities());
        }
        return fetchHoldingsEntity;
    }

    private boolean isBoundWithItem(ItemEntity itemEntity){
        if(itemEntity.getBibliographicEntities().size() > 1){
            return true;
        }
        return false;
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
        repositoryService.getItemChangeLogDetailsRepository().save(itemChangeLogEntityList);
    }

    private void updateCustomerCode(BibliographicEntity dummyBibliographicEntity, BibliographicEntity updatedBibliographicEntity) {
        updatedBibliographicEntity.getItemEntities().get(0).setCustomerCode(dummyBibliographicEntity.getItemEntities().get(0).getCustomerCode());
    }

    private BibliographicEntity updateCatalogingStatusForItem(BibliographicEntity bibliographicEntity) {
        for(ItemEntity itemEntity:bibliographicEntity.getItemEntities()){
            if(itemEntity.getUseRestrictions()==null || itemEntity.getCollectionGroupId()==null){
                itemEntity.setCatalogingStatus(ReCAPConstants.INCOMPLETE_STATUS);
            }else {
                itemEntity.setCatalogingStatus(ReCAPConstants.COMPLETE_STATUS);
            }
        }
        return bibliographicEntity;
    }

    private void removeDummyRecord(List<Map<String, String>> idMapToRemoveIndexList, BibliographicEntity fetchBibliographicEntity) {
        if (isNonCompleteBib(fetchBibliographicEntity)) {//This check is to not delete the existing bib which is complete for bound with (This happens when accession done for boundwith item which created as dummy and submit collection done for this boundwith item)
            Map<String,String> idMapToRemoveIndex = new HashedMap();
            idMapToRemoveIndex.put(ReCAPConstants.BIB_ID,String.valueOf(fetchBibliographicEntity.getBibliographicId()));
            idMapToRemoveIndex.put(ReCAPConstants.HOLDING_ID,String.valueOf(fetchBibliographicEntity.getHoldingsEntities().get(0).getHoldingsId()));
            idMapToRemoveIndex.put(ReCAPConstants.ITEM_ID,String.valueOf(fetchBibliographicEntity.getItemEntities().get(0).getItemId()));
            idMapToRemoveIndexList.add(idMapToRemoveIndex);
            logger.info("Added id to remove from solr - bib id - {}, holding id - {}, item id - {}",fetchBibliographicEntity.getBibliographicId(),fetchBibliographicEntity.getHoldingsEntities().get(0).getHoldingsId(),
                    fetchBibliographicEntity.getItemEntities().get(0).getItemId());
            logger.info("Delete dummy record - barcode - {}",fetchBibliographicEntity.getItemEntities().get(0).getBarcode());
            repositoryService.getBibliographicDetailsRepository().delete(fetchBibliographicEntity);
            repositoryService.getBibliographicDetailsRepository().flush();
        }
    }

    private boolean isNonCompleteBib(BibliographicEntity bibliographicEntity){
        boolean isNotComplete = true;
        if(bibliographicEntity.getCatalogingStatus().equals(ReCAPConstants.COMPLETE_STATUS)){
            isNotComplete = false;
        }
        return isNotComplete;
    }

    private BibliographicEntity copyBibliographicEntity(BibliographicEntity fetchBibliographicEntity, BibliographicEntity bibliographicEntity){
        fetchBibliographicEntity.setContent(bibliographicEntity.getContent());
        fetchBibliographicEntity.setLastUpdatedBy(bibliographicEntity.getLastUpdatedBy());
        fetchBibliographicEntity.setLastUpdatedDate(bibliographicEntity.getLastUpdatedDate());
        logger.info("updating existing bib - owning inst bibid - "+fetchBibliographicEntity.getOwningInstitutionBibId());
        return fetchBibliographicEntity;
    }

    private ItemEntity copyItemEntity(ItemEntity fetchItemEntity, ItemEntity itemEntity, List<ItemEntity> itemEntityList) {
        fetchItemEntity.setLastUpdatedBy(itemEntity.getLastUpdatedBy());
        fetchItemEntity.setLastUpdatedDate(itemEntity.getLastUpdatedDate());
        fetchItemEntity.setCallNumber(itemEntity.getCallNumber());
        fetchItemEntity.setCallNumberType(itemEntity.getCallNumberType());
        if((fetchItemEntity.getUseRestrictions() == null && itemEntity.getUseRestrictions() == null )
                || (fetchItemEntity.getCollectionGroupEntity().getCollectionGroupCode().equals(ReCAPConstants.NOT_AVAILABLE_CGD)
                && itemEntity.getCollectionGroupId()==null)){
            fetchItemEntity.setCatalogingStatus(ReCAPConstants.INCOMPLETE_STATUS);
        } else{
            if (fetchItemEntity.getCatalogingStatus().equals(ReCAPConstants.INCOMPLETE_STATUS)) {//To  update the item available status to available for existing incomplete record which is turning as complete record
                fetchItemEntity.setItemAvailabilityStatusId((Integer) setupDataService.getItemStatusCodeIdMap().get("Available"));
            }
            fetchItemEntity.setCatalogingStatus(ReCAPConstants.COMPLETE_STATUS);
        }

        if (isAvailableItem(fetchItemEntity.getItemAvailabilityStatusId())) {
            if (itemEntity.getCollectionGroupId() != null &&
                    (!itemEntity.isCgdProtection() || fetchItemEntity.getCollectionGroupId()==setupDataService.getCollectionGroupMap().get(ReCAPConstants.NOT_AVAILABLE_CGD))) {//Added condition to update CGD even if it is CGD protected when existing records cgd is NA
                fetchItemEntity.setCollectionGroupId(itemEntity.getCollectionGroupId());
            }
            fetchItemEntity.setUseRestrictions(itemEntity.getUseRestrictions());
        }
        fetchItemEntity.setCopyNumber(itemEntity.getCopyNumber());
        fetchItemEntity.setVolumePartYear(itemEntity.getVolumePartYear());

        fetchItemEntity.setCgdProtection(itemEntity.isCgdProtection());
        logger.info("updating existing barcode - "+fetchItemEntity.getBarcode());
        itemEntityList.add(fetchItemEntity);
        return fetchItemEntity;
    }

    /**
     * Is available item boolean.
     *
     * @param itemAvailabilityStatusId the item availability status id
     * @return the boolean
     */
    public boolean isAvailableItem(Integer itemAvailabilityStatusId){
        String itemStatusCode = (String) setupDataService.getItemStatusIdCodeMap().get(itemAvailabilityStatusId);
        if (itemStatusCode.equalsIgnoreCase(ReCAPConstants.ITEM_STATUS_AVAILABLE)) {
            return true;
        }
        return false;
    }

    private void setItemAvailabilityStatus(ItemEntity itemEntity){
        if(itemEntity.getItemAvailabilityStatusId()==null) {
            itemEntity.setItemAvailabilityStatusId((Integer) setupDataService.getItemStatusCodeIdMap().get("Available"));
        }
    }
}
