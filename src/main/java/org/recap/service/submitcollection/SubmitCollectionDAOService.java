package org.recap.service.submitcollection;

import org.apache.commons.collections.map.HashedMap;
import org.recap.ReCAPConstants;
import org.recap.model.BibliographicEntity;
import org.recap.model.HoldingsEntity;
import org.recap.model.ItemChangeLogEntity;
import org.recap.model.ItemEntity;
import org.recap.model.report.SubmitCollectionReportInfo;
import org.recap.model.submitcollection.BoundWithBibliographicEntityObject;
import org.recap.model.submitcollection.NonBoundWithBibliographicEntityObject;
import org.recap.service.common.RepositoryService;
import org.recap.service.common.SetupDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<BibliographicEntity> updateBibliographicEntityInBatchForNonBoundWith(List<NonBoundWithBibliographicEntityObject> nonBoundWithBibliographicEntityObjectList, Integer owningInstitutionId,
                                                                                     Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap
            , Set<Integer> processedBibIds, List<Map<String, String>> idMapToRemoveIndexList, Set<String> processedBarcodeSetForDummyRecords) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<String> incomingItemBarcodeList = new ArrayList<>();
        incomingItemBarcodeList.addAll(getBarcodeSetFromNonBoundWithBibliographicEntity(nonBoundWithBibliographicEntityObjectList));
        Map<String,ItemEntity> incomingBarcodeItemEntityMapFromBibliographicEntityList = getBarcodeItemEntityMapFromNonBoundWithBibliographicEntityList(nonBoundWithBibliographicEntityObjectList);
        List<ItemEntity> fetchedItemEntityList = getItemEntityListUsingBarcodeList(incomingItemBarcodeList, owningInstitutionId);
        List<String> fetchedItemBarcodeList = new ArrayList<>();
        fetchedItemBarcodeList.addAll(getBarcodeSetFromItemEntityList(fetchedItemEntityList));
        Map<String, ItemEntity> fetchedBarcodeItemEntityMap = getBarcodeItemEntityMap(fetchedItemEntityList);
        Map<String,Map<String,BibliographicEntity>> fetchedBarcodeBibliographicEntityMap = getBarcodeBibliographicEntityMap(fetchedItemEntityList);
        List<BibliographicEntity> updatedBibliographicEntityList = new ArrayList<>();
        List<ItemChangeLogEntity> itemChangeLogEntityList = new ArrayList<>();
        for(NonBoundWithBibliographicEntityObject nonBoundWithBibliographicEntityObject : nonBoundWithBibliographicEntityObjectList){
            for (BibliographicEntity incomingBibliographicEntity : nonBoundWithBibliographicEntityObject.getBibliographicEntityList()) {
                for (ItemEntity incomingItemEntity : incomingBibliographicEntity.getItemEntities()) {
                    ItemEntity fetchedItemEntity = fetchedBarcodeItemEntityMap.get(incomingItemEntity.getBarcode());
                    if (fetchedItemEntity != null) {
                        List<BibliographicEntity> fetchedBibliographicEntityList = fetchedItemEntity.getBibliographicEntities();
                        for (BibliographicEntity fetchedBibliographicEntity : fetchedBibliographicEntityList) {
                            Map<String,BibliographicEntity> fetchedOwnInstBibIdBibliographicEntityMap = fetchedBarcodeBibliographicEntityMap.get(incomingItemEntity.getBarcode());
                            if (fetchedOwnInstBibIdBibliographicEntityMap != null && fetchedOwnInstBibIdBibliographicEntityMap.containsKey(incomingBibliographicEntity.getOwningInstitutionBibId())) {//TODO need to check the if condition, remove the condition if not required
                                if (fetchedBibliographicEntity.getOwningInstitutionBibId().equals(incomingBibliographicEntity.getOwningInstitutionBibId())) {//update existing record
                                    BibliographicEntity updatedBibliographicEntity = updateExistingRecordToEntityObject(fetchedBibliographicEntity, incomingBibliographicEntity, submitCollectionReportInfoMap, processedBibIds,itemChangeLogEntityList);
                                    if (updatedBibliographicEntity != null) {
                                        updatedBibliographicEntityList.add(updatedBibliographicEntity);
                                    }
                                }
                            } else if(fetchedBibliographicEntity.getOwningInstitutionBibId().substring(0, 1).equals("d")) {//update existing dummy record if any (Removes existing dummy record and creates new record for the same barcode based on the input xml)
                                BibliographicEntity updatedBibliographicEntity = null;
                                updatedBibliographicEntity = updateDummyRecordForNonBoundWith(incomingBibliographicEntity, submitCollectionReportInfoMap, idMapToRemoveIndexList, processedBarcodeSetForDummyRecords, updatedBibliographicEntity, fetchedBibliographicEntity,itemChangeLogEntityList);
                                if (updatedBibliographicEntity != null) {
                                    updatedBibliographicEntityList.add(updatedBibliographicEntity);
                                    processedBibIds.add(updatedBibliographicEntity.getBibliographicId());
                                }
                            }
                            else if (!fetchedBibliographicEntity.getOwningInstitutionBibId().equals(incomingBibliographicEntity.getOwningInstitutionBibId()) && !fetchedBibliographicEntity.getOwningInstitutionBibId().substring(0, 1).equals("d")) {//Owning inst bib id mismatch for non dummy record
                                submitCollectionReportHelperService.setSubmitCollectionReportInfoForOwningInstitutionBibIdMismatch(fetchedBibliographicEntity, incomingBibliographicEntity, submitCollectionReportInfoMap);
                            }
                        }
                    }
                }
            }
        }
        prepareExceptionReport(incomingItemBarcodeList,fetchedItemBarcodeList,incomingBarcodeItemEntityMapFromBibliographicEntityList,submitCollectionReportInfoMap);
        stopWatch.stop();
        saveUpdatedBibliographicEntityListAndItemChangeLogList(updatedBibliographicEntityList,itemChangeLogEntityList);
        logger.info("Total bibs to update in the current batch--->{}", updatedBibliographicEntityList.size());
        logger.info("Time taken to update in batches----->{}", stopWatch.getTotalTimeSeconds());
        return updatedBibliographicEntityList;
    }

    public List<BibliographicEntity> updateBibliographicEntityInBatchForBoundWith(List<BoundWithBibliographicEntityObject> boundWithBibliographicEntityObjectList, Integer owningInstitutionId,
                                                                                  Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap
            , Set<Integer> processedBibIds, List<Map<String, String>> idMapToRemoveIndexList, Set<String> processedBarcodeSetForDummyRecords) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<String> incomingItemBarcodeList = new ArrayList<>();
        incomingItemBarcodeList.addAll(getBarcodeSetFromBoundWithBibliographicEntity(boundWithBibliographicEntityObjectList));
        Map<String,ItemEntity> incomingBarcodeItemEntityMapFromBibliographicEntityList = getBarcodeItemEntityMapFromBoundWithBibliographicEntityList(boundWithBibliographicEntityObjectList);
        List<ItemEntity> fetchedItemEntityList = getItemEntityListUsingBarcodeList(incomingItemBarcodeList, owningInstitutionId);
        List<String> fetchedItemBarcodeList = new ArrayList<>();
        fetchedItemBarcodeList.addAll(getBarcodeSetFromItemEntityList(fetchedItemEntityList));
        Map<String, ItemEntity> fetchedBarcodeItemEntityMap = getBarcodeItemEntityMap(fetchedItemEntityList);
        List<BibliographicEntity> updatedBibliographicEntityList = new ArrayList<>();
        List<ItemChangeLogEntity> itemChangeLogEntityList = new ArrayList<>();
        for(BoundWithBibliographicEntityObject boundWithBibliographicEntityObject : boundWithBibliographicEntityObjectList){
            for (BibliographicEntity incomingBibliographicEntity : boundWithBibliographicEntityObject.getBibliographicEntityList()) {
                for (ItemEntity incomingItemEntity : incomingBibliographicEntity.getItemEntities()) {
                    ItemEntity fetchedItemEntity = fetchedBarcodeItemEntityMap.get(incomingItemEntity.getBarcode());
                    if (fetchedItemEntity != null) {
                        List<BibliographicEntity> fetchedBibliographicEntityList = fetchedItemEntity.getBibliographicEntities();
                        List<String> notMatchedIncomingOwnInstBibId = new ArrayList<>();
                        List<String> notMatchedFetchedOwnInstBibId = new ArrayList<>();
                        verifyAndSetMisMatchBoundWithOwnInstBibIdIfAny(boundWithBibliographicEntityObject.getBibliographicEntityList(),fetchedBibliographicEntityList
                        ,notMatchedIncomingOwnInstBibId,notMatchedFetchedOwnInstBibId);
                        if(notMatchedIncomingOwnInstBibId.isEmpty() && notMatchedFetchedOwnInstBibId.isEmpty()){
                            Map<String,BibliographicEntity> fetchedOwnInstBibIdBibliographicEntityMap = getOwnInstBibIdBibliographicEntityMap(fetchedBibliographicEntityList);
                            BibliographicEntity fetchedBibliographicEntity = fetchedOwnInstBibIdBibliographicEntityMap.get(incomingBibliographicEntity.getOwningInstitutionBibId());
                            BibliographicEntity updatedBibliographicEntity = updateExistingRecordToEntityObject(fetchedBibliographicEntity, incomingBibliographicEntity, submitCollectionReportInfoMap, processedBibIds,itemChangeLogEntityList);
                            if (updatedBibliographicEntity != null) {
                                updatedBibliographicEntityList.add(updatedBibliographicEntity);
                            }
                        } else if(fetchedBibliographicEntityList.get(0).getOwningInstitutionBibId().substring(0, 1).equals("d")) {//update existing dummy record if any (Removes existing dummy record and creates new record for the same barcode based on the input xml)
                            BibliographicEntity updatedBibliographicEntity = null;
                            updatedBibliographicEntity = updateDummyRecordForBoundWith(incomingBibliographicEntity, submitCollectionReportInfoMap, idMapToRemoveIndexList, processedBarcodeSetForDummyRecords, updatedBibliographicEntity, fetchedBibliographicEntityList.get(0),itemChangeLogEntityList);
                            if (updatedBibliographicEntity != null) {
                                updatedBibliographicEntityList.add(updatedBibliographicEntity);
                            }
                        } else { //Owning inst bib id mismatch for non dummy record
                            boolean isBarcodeAlreadyAdded = submitCollectionReportHelperService.isBarcodeAlreadyAdded(incomingItemEntity.getBarcode(),submitCollectionReportInfoMap);
                            if (!isBarcodeAlreadyAdded) {
                                submitCollectionReportHelperService.setSubmitCollectionReportInfoForOwningInstitutionBibIdMismatchForBoundWith(notMatchedIncomingOwnInstBibId, notMatchedFetchedOwnInstBibId,incomingItemEntity,fetchedItemEntity, submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST));
                            }
                        }
                    }
                }
            }
        }
        prepareExceptionReport(incomingItemBarcodeList,fetchedItemBarcodeList,incomingBarcodeItemEntityMapFromBibliographicEntityList,submitCollectionReportInfoMap);
        stopWatch.stop();
        saveUpdatedBibliographicEntityListAndItemChangeLogList(updatedBibliographicEntityList,itemChangeLogEntityList);
        logger.info("Total bibs to update in the current batch--->{}", updatedBibliographicEntityList.size());
        logger.info("Time taken to update in batches----->{}", stopWatch.getTotalTimeSeconds());
        return updatedBibliographicEntityList;
    }

    private void saveUpdatedBibliographicEntityListAndItemChangeLogList(List<BibliographicEntity> updatedBibliographicEntityList,List<ItemChangeLogEntity> itemChangeLogEntityList){
        if (!updatedBibliographicEntityList.isEmpty()) {
            try {
                saveUpdatedBibliographicEntityList(updatedBibliographicEntityList);
                if (!itemChangeLogEntityList.isEmpty()){
                    saveItemChangeLogEntityList(itemChangeLogEntityList);
                }
            } catch (Exception e) {
                logger.error("Exception while saving non bound with batch ");
                logger.info(ReCAPConstants.LOG_ERROR,e);
                e.printStackTrace();
            }
        }
    }

    private void saveUpdatedBibliographicEntityList(List<BibliographicEntity> updatedBibliographicEntityList){
        logger.info("updatedBibliographicEntityList size--->{}",updatedBibliographicEntityList.size());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        repositoryService.getBibliographicDetailsRepository().save(updatedBibliographicEntityList);
        repositoryService.getBibliographicDetailsRepository().flush();
        stopWatch.stop();
        logger.info("Time taken to save {} bib size---->{} sec",updatedBibliographicEntityList.size(),stopWatch.getTotalTimeSeconds());
    }

    private void saveItemChangeLogEntityList(List<ItemChangeLogEntity> itemChangeLogEntityList){
        StopWatch itemChangeLogStopWatch = new StopWatch();
        itemChangeLogStopWatch.start();
        repositoryService.getItemChangeLogDetailsRepository().save(itemChangeLogEntityList);
        repositoryService.getItemChangeLogDetailsRepository().flush();
        itemChangeLogStopWatch.stop();
        logger.info("Time taken to save item change log--->{}",itemChangeLogStopWatch.getTotalTimeSeconds());
        repositoryService.getItemChangeLogDetailsRepository().save(itemChangeLogEntityList);
    }

    private void verifyAndSetMisMatchBoundWithOwnInstBibIdIfAny(List<BibliographicEntity> incomingBibliographicEntityList,List<BibliographicEntity> fetchedBibliographicEntityList
            ,List<String> notMatchedIncomingOwnInstBibId,List<String> notMatchedFetchedOwnInstBibId){
        List<String> incomingOwnInstBibIdList = getOwnInstBibIdList(incomingBibliographicEntityList);
        List<String> fetchedOwnInstBibIdList = getOwnInstBibIdList(fetchedBibliographicEntityList);

        for(String incomingOwnInstBibId:incomingOwnInstBibIdList){
            if(!fetchedOwnInstBibIdList.contains(incomingOwnInstBibId)){
                notMatchedIncomingOwnInstBibId.add(incomingOwnInstBibId);
            }
        }

        for (String fetchedOwnInstBibId:fetchedOwnInstBibIdList){
            if(!incomingOwnInstBibIdList.contains(fetchedOwnInstBibId)){
                notMatchedFetchedOwnInstBibId.add(fetchedOwnInstBibId);
            }
        }
    }

    private List<String> getOwnInstBibIdList(List<BibliographicEntity> bibliographicEntityList){
        List<String> ownInstBibIdList = new ArrayList<>();
        for(BibliographicEntity bibliographicEntity:bibliographicEntityList){
            ownInstBibIdList.add(bibliographicEntity.getOwningInstitutionBibId());
        }
        return ownInstBibIdList;
    }

    public void prepareExceptionReport(List<String> incomingItemBarcodeList,List<String> fetchedItemBarcodeList,Map<String,ItemEntity> incomingBarcodeItemEntityMapFromBibliographicEntityList
            ,Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap){
        for(String incomingBarcode:incomingItemBarcodeList) {
            if (!fetchedItemBarcodeList.contains(incomingBarcode)) {
                boolean isBarcodeAlreadyAdded = submitCollectionReportHelperService.isBarcodeAlreadyAdded(incomingBarcode,submitCollectionReportInfoMap);
                if(!isBarcodeAlreadyAdded){
                    ItemEntity unavailableItemEntity = incomingBarcodeItemEntityMapFromBibliographicEntityList.get(incomingBarcode);
                    addExceptionReport(Arrays.asList(unavailableItemEntity), submitCollectionReportInfoMap,ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD);
                }
            }
        }
    }

    public Collection<? extends String> getBarcodeSetFromItemEntityList(List<ItemEntity> itemEntityList) {
        Set<String> barcodeList = itemEntityList.stream().map(ItemEntity::getBarcode).collect(Collectors.toSet());
        return barcodeList;
    }

    private Map<String,BibliographicEntity> getOwnInstBibIdBibliographicEntityMap(List<BibliographicEntity> bibliographicEntityList){
        return bibliographicEntityList.stream().collect(Collectors.toMap(BibliographicEntity::getOwningInstitutionBibId,bibliographicEntity -> bibliographicEntity));

    }

    public Map<String,Map<String,BibliographicEntity>> getBarcodeBibliographicEntityMap(List<ItemEntity> itemEntityList){
        Map<String,Map<String,BibliographicEntity>> barcodeBibliographicEntityMap = new HashedMap();
        for(ItemEntity itemEntity:itemEntityList){
            Map<String,BibliographicEntity> ownBibIdBibliographicEntityMap = new HashedMap();
            for(BibliographicEntity bibliographicEntity:itemEntity.getBibliographicEntities()){
                ownBibIdBibliographicEntityMap.put(bibliographicEntity.getOwningInstitutionBibId(),bibliographicEntity);
            }
            barcodeBibliographicEntityMap.put(itemEntity.getBarcode(),ownBibIdBibliographicEntityMap);
        }
        return barcodeBibliographicEntityMap;
    }

    public Map<String,ItemEntity> getBarcodeItemEntityMapFromNonBoundWithBibliographicEntityList(List<NonBoundWithBibliographicEntityObject> nonBoundWithBibliographicEntityObjectList){
        Map<String,ItemEntity> barcodeItemEntityMapFromBibliographicEntityList = new HashedMap();
        for(NonBoundWithBibliographicEntityObject nonBoundWithBibliographicEntityObject : nonBoundWithBibliographicEntityObjectList){
            for (BibliographicEntity bibliographicEntity: nonBoundWithBibliographicEntityObject.getBibliographicEntityList()){
                Map<String,ItemEntity> barcodeItemEntityMap = getBarcodeItemEntityMap(bibliographicEntity.getItemEntities());
                barcodeItemEntityMapFromBibliographicEntityList.putAll(barcodeItemEntityMap);
            }
        }
        return barcodeItemEntityMapFromBibliographicEntityList;
    }

    private Map<String,ItemEntity> getBarcodeItemEntityMapFromBoundWithBibliographicEntityList(List<BoundWithBibliographicEntityObject> boundWithBibliographicEntityObjectList){
        Map<String,ItemEntity> barcodeItemEntityMapFromBibliographicEntityList = new HashedMap();
        for(BoundWithBibliographicEntityObject boundWithBibliographicEntityObject : boundWithBibliographicEntityObjectList){
            for (BibliographicEntity bibliographicEntity: boundWithBibliographicEntityObject.getBibliographicEntityList()){
                Map<String,ItemEntity> barcodeItemEntityMap = getBarcodeItemEntityMap(bibliographicEntity.getItemEntities());
                barcodeItemEntityMapFromBibliographicEntityList.putAll(barcodeItemEntityMap);
            }
        }
        return barcodeItemEntityMapFromBibliographicEntityList;
    }

    public Map<String,ItemEntity> getBarcodeItemEntityMap(List<ItemEntity> itemEntityList){
        return itemEntityList.stream().collect(Collectors.toMap(ItemEntity::getBarcode,itemEntity -> itemEntity));
    }

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
            } else if(!fetchBibliographicEntity.getOwningInstitutionBibId().equals(bibliographicEntity.getOwningInstitutionBibId()) && !fetchBibliographicEntity.getOwningInstitutionBibId().substring(0,1).equals("d")){
                submitCollectionReportHelperService.setSubmitCollectionReportInfoForOwningInstitutionBibIdMismatch(fetchBibliographicEntity,bibliographicEntity,submitCollectionReportInfoMap);
            } else {//update existing dummy record if any (Removes existing dummy record and creates new record for the same barcode based on the input xml)
                savedBibliographicEntity = updateDummyRecord(bibliographicEntity, submitCollectionReportInfoMap, idMapToRemoveIndexList, processedBarcodeSetForDummyRecords, savedBibliographicEntity, fetchBibliographicEntity);
            }
        } else {//if no record found to update, generate exception info
            savedBibliographicEntity = bibliographicEntity;
            addExceptionReport(bibliographicEntity.getItemEntities(), submitCollectionReportInfoMap,ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD);
        }
        return savedBibliographicEntity;
    }

    public void addExceptionReport(List<ItemEntity> itemEntityList, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap,String message) {
        boolean isBarcodeAlreadyAdded = submitCollectionReportHelperService.isBarcodeAlreadyAdded(itemEntityList.get(0).getBarcode(),submitCollectionReportInfoMap);
        if (!isBarcodeAlreadyAdded) {//This is to avoid repeated error message for non-existing boundwith records
            submitCollectionReportHelperService.setSubmitCollectionExceptionReportInfo(itemEntityList,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_LIST), message);
        }
    }

    public BibliographicEntity updateDummyRecord(BibliographicEntity bibliographicEntity, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String, String>> idMapToRemoveIndexList, Set<String> processedBarcodeSet, BibliographicEntity savedBibliographicEntity, BibliographicEntity fetchBibliographicEntity) {
        List<ItemEntity> fetchedItemBasedOnOwningInstitutionItemId = submitCollectionReportHelperService.getItemBasedOnOwningInstitutionItemIdAndOwningInstitutionId(bibliographicEntity.getItemEntities());
        boolean boundWith = isBoundWithItem(bibliographicEntity,processedBarcodeSet);
        if (fetchedItemBasedOnOwningInstitutionItemId.isEmpty() || boundWith) {//To check there should not be existing item record with same own item id and for bound with own item id can be different
            boolean isCheckCGDNotNull = checkIsCGDNotNull(bibliographicEntity);
            if (isCheckCGDNotNull) {
                updateCustomerCode(fetchBibliographicEntity, bibliographicEntity);//Added to get customer code for existing dummy record, this value is used when the input xml dosent have the customer code in it, this happens mostly for CUL
                removeDummyRecord(idMapToRemoveIndexList, fetchBibliographicEntity);
                BibliographicEntity fetchedBibliographicEntity = repositoryService.getBibliographicDetailsRepository().findByOwningInstitutionIdAndOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionId(), bibliographicEntity.getOwningInstitutionBibId());
                BibliographicEntity bibliographicEntityToSave = bibliographicEntity;
                setItemAvailabilityStatus(bibliographicEntity.getItemEntities());
                updateCatalogingStatusForItem(bibliographicEntityToSave);
                updateCatalogingStatusForBib(bibliographicEntityToSave);
                if (fetchedBibliographicEntity != null) {//1Bib n holding n item
                    bibliographicEntityToSave = updateExistingRecordForDummy(fetchedBibliographicEntity, bibliographicEntity);
                }
                savedBibliographicEntity = repositoryService.getBibliographicDetailsRepository().saveAndFlush(bibliographicEntityToSave);
                entityManager.refresh(savedBibliographicEntity);
                saveItemChangeLogEntity(ReCAPConstants.SUBMIT_COLLECTION, ReCAPConstants.SUBMIT_COLLECTION_DUMMY_RECORD_UPDATE, savedBibliographicEntity.getItemEntities());
                setProcessedBarcode(bibliographicEntity, processedBarcodeSet);
                submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap, savedBibliographicEntity, bibliographicEntity);
            } else {
                    submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap,fetchBibliographicEntity,bibliographicEntity);
            }
        } else if (!fetchedItemBasedOnOwningInstitutionItemId.isEmpty()) {
                submitCollectionReportHelperService.setSubmitCollectionReportInfoForInvalidDummyRecordBasedOnOwnInstItemId(bibliographicEntity,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),fetchedItemBasedOnOwningInstitutionItemId);
        }
        return savedBibliographicEntity;
    }

    public BibliographicEntity updateDummyRecordForNonBoundWith(BibliographicEntity bibliographicEntity, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap
            , List<Map<String, String>> idMapToRemoveIndexList, Set<String> processedBarcodeSet, BibliographicEntity savedBibliographicEntity
            , BibliographicEntity fetchBibliographicEntity,List<ItemChangeLogEntity> itemChangeLogEntityList) {
        List<ItemEntity> fetchedItemBasedOnOwningInstitutionItemId = submitCollectionReportHelperService.getItemBasedOnOwningInstitutionItemIdAndOwningInstitutionId(bibliographicEntity.getItemEntities());
        boolean boundWith = isBoundWithItem(bibliographicEntity,processedBarcodeSet);
        if (fetchedItemBasedOnOwningInstitutionItemId.isEmpty() || boundWith) {//To check there should not be existing item record with same own item id and for bound with own item id can be different
            boolean isCheckCGDNotNull = checkIsCGDNotNull(bibliographicEntity);
            if (isCheckCGDNotNull) {
                updateCustomerCode(fetchBibliographicEntity, bibliographicEntity);//Added to get customer code for existing dummy record, this value is used when the input xml dosent have the customer code in it, this happens mostly for CUL
                removeDummyRecord(idMapToRemoveIndexList, fetchBibliographicEntity);
                BibliographicEntity fetchedBibliographicEntity = repositoryService.getBibliographicDetailsRepository().findByOwningInstitutionIdAndOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionId(), bibliographicEntity.getOwningInstitutionBibId());
                BibliographicEntity bibliographicEntityToSave = bibliographicEntity;
                setItemAvailabilityStatus(bibliographicEntity.getItemEntities());
                updateCatalogingStatusForItem(bibliographicEntityToSave);
                updateCatalogingStatusForBib(bibliographicEntityToSave);
                if (fetchedBibliographicEntity != null) {//1Bib n holding n item
                    bibliographicEntityToSave = updateExistingRecordForDummy(fetchedBibliographicEntity, bibliographicEntity);
                }
                savedBibliographicEntity = repositoryService.getBibliographicDetailsRepository().saveAndFlush(bibliographicEntityToSave);
                entityManager.refresh(savedBibliographicEntity);
                List<ItemChangeLogEntity> preparedItemChangeLogEntityList = prepareItemChangeLogEntity(ReCAPConstants.SUBMIT_COLLECTION, ReCAPConstants.SUBMIT_COLLECTION_DUMMY_RECORD_UPDATE, savedBibliographicEntity.getItemEntities());
                itemChangeLogEntityList.addAll(preparedItemChangeLogEntityList);
                setProcessedBarcode(bibliographicEntity, processedBarcodeSet);
                submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap, savedBibliographicEntity, bibliographicEntity);
            } else {
                submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap,fetchBibliographicEntity,bibliographicEntity);
            }
        } else if (!fetchedItemBasedOnOwningInstitutionItemId.isEmpty()) {
            submitCollectionReportHelperService.setSubmitCollectionReportInfoForInvalidDummyRecordBasedOnOwnInstItemId(bibliographicEntity,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),fetchedItemBasedOnOwningInstitutionItemId);
        }
        return savedBibliographicEntity;
    }

    public BibliographicEntity updateDummyRecordForBoundWith(BibliographicEntity bibliographicEntity, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap
            , List<Map<String, String>> idMapToRemoveIndexList, Set<String> processedBarcodeSet, BibliographicEntity savedBibliographicEntity
            , BibliographicEntity fetchBibliographicEntity,List<ItemChangeLogEntity> itemChangeLogEntityList) {
        List<ItemEntity> fetchedItemBasedOnOwningInstitutionItemId = submitCollectionReportHelperService.getItemBasedOnOwningInstitutionItemIdAndOwningInstitutionId(bibliographicEntity.getItemEntities());
        boolean boundWith = isBoundWithItem(bibliographicEntity,processedBarcodeSet);
        BibliographicEntity bibliographicEntityToSave = null;
        if (fetchedItemBasedOnOwningInstitutionItemId.isEmpty() || boundWith) {//To check there should not be existing item record with same own item id and for bound with own item id can be different
            boolean isCheckCGDNotNull = checkIsCGDNotNull(bibliographicEntity);
            if (isCheckCGDNotNull) {
                updateCustomerCode(fetchBibliographicEntity, bibliographicEntity);//Added to get customer code for existing dummy record, this value is used when the input xml dosent have the customer code in it, this happens mostly for CUL
                removeDummyRecord(idMapToRemoveIndexList, fetchBibliographicEntity);
                BibliographicEntity fetchedBibliographicEntity = repositoryService.getBibliographicDetailsRepository().findByOwningInstitutionIdAndOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionId(), bibliographicEntity.getOwningInstitutionBibId());
                bibliographicEntityToSave = bibliographicEntity;
                setItemAvailabilityStatus(bibliographicEntity.getItemEntities());
                updateCatalogingStatusForItem(bibliographicEntityToSave);
                updateCatalogingStatusForBib(bibliographicEntityToSave);
                if (fetchedBibliographicEntity != null) {//1Bib n holding n item
                    bibliographicEntityToSave = updateExistingRecordForDummy(fetchedBibliographicEntity, bibliographicEntity);
                }
                savedBibliographicEntity = bibliographicEntityToSave;
                List<ItemChangeLogEntity> preparedItemChangeLogEntityList = prepareItemChangeLogEntity(ReCAPConstants.SUBMIT_COLLECTION, ReCAPConstants.SUBMIT_COLLECTION_DUMMY_RECORD_UPDATE, savedBibliographicEntity.getItemEntities());
                itemChangeLogEntityList.addAll(preparedItemChangeLogEntityList);
                setProcessedBarcode(bibliographicEntity, processedBarcodeSet);
                submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap, savedBibliographicEntity, bibliographicEntity);
            } else {
                submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap,fetchBibliographicEntity,bibliographicEntity);
            }
        } else if (!fetchedItemBasedOnOwningInstitutionItemId.isEmpty()) {
            submitCollectionReportHelperService.setSubmitCollectionReportInfoForInvalidDummyRecordBasedOnOwnInstItemId(bibliographicEntity,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),fetchedItemBasedOnOwningInstitutionItemId);
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

        List<ItemEntity> fetchedItemEntityList = fetchBibliographicEntity.getItemEntities();
        List<ItemEntity> incomingItemEntityList = new ArrayList<>(incomingBibliographicEntity.getItemEntities());

        Map<String,ItemEntity> fetchedBarcodeItemEntityMap = getBarcodeItemEntityMap(fetchedItemEntityList);
        Map<String,ItemEntity> incomingBarcodeItemEntityMap = getBarcodeItemEntityMap(incomingItemEntityList);
        for(Map.Entry<String,ItemEntity> incomingBarcodeItemEntityMapEntry:incomingBarcodeItemEntityMap.entrySet()){
            ItemEntity incomingItemEntity = incomingBarcodeItemEntityMapEntry.getValue();
            ItemEntity fetchedItemEntity = fetchedBarcodeItemEntityMap.get(incomingBarcodeItemEntityMapEntry.getKey());
            logger.info("Processing barcode--->{}",incomingItemEntity.getBarcode());
            if(fetchedItemEntity != null){
                if (fetchedItemEntity.getOwningInstitutionItemId().equalsIgnoreCase(incomingItemEntity.getOwningInstitutionItemId())) {
                    if (fetchedItemEntity.getBarcode().equals(incomingItemEntity.getBarcode())) {
                        if(!isDeAccessionedItem(fetchedItemEntity)) {
                            copyItemEntity(fetchedItemEntity, incomingItemEntity, updatedItemEntityList);
                            isValidItemToUpdate = true;
                        } else {//add exception report for deaccession record
                            addExceptionReport(Arrays.asList(incomingItemEntity),submitCollectionReportInfoMap,ReCAPConstants.SUBMIT_COLLECTION_DEACCESSION_EXCEPTION_RECORD);
                        }
                    }
                }
            } else {//Add to exception report when barcode is unavailable
                addExceptionReport(Arrays.asList(incomingItemEntity),submitCollectionReportInfoMap,ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD);
            }
        }

        fetchBibliographicEntity.setHoldingsEntities(fetchedHoldingsEntityList);
        fetchBibliographicEntity.setItemEntities(fetchedItemEntityList);
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

    public BibliographicEntity updateExistingRecordToEntityObject(BibliographicEntity fetchBibliographicEntity, BibliographicEntity incomingBibliographicEntity,
            Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, Set<Integer> processedBibIds,List<ItemChangeLogEntity> itemChangeLogEntityList) {
        BibliographicEntity bibliographicEntityToSave = null;
        logger.info("Processing bib owning institution bibid - ",incomingBibliographicEntity.getOwningInstitutionBibId());
        copyBibliographicEntity(fetchBibliographicEntity, incomingBibliographicEntity);
        List<HoldingsEntity> fetchedHoldingsEntityList = fetchBibliographicEntity.getHoldingsEntities();
        List<HoldingsEntity> incomingHoldingsEntityList = new ArrayList<>(incomingBibliographicEntity.getHoldingsEntities());
        List<ItemEntity> updatedItemEntityList = new ArrayList<>();
        boolean isAnyValidHoldingToUpdate = false;
        boolean isAnyValidItemToUpdate = false;
        String[] nonHoldingIdInstitutionArray = nonHoldingIdInstitution.split(",");
        String institutionCode = (String) setupDataService.getInstitutionIdCodeMap().get(incomingBibliographicEntity.getOwningInstitutionId());
        boolean isNonHoldingIdInstitution = Arrays.asList(nonHoldingIdInstitutionArray).contains(institutionCode);

        Set<String> barcodeHavingMismatchHoldingsId = new HashSet<>();
        if(isNonHoldingIdInstitution){//Added to handle non holding id institution
            for(HoldingsEntity incomingHoldingsEntity:incomingHoldingsEntityList) {
                for (HoldingsEntity fetchedHoldingsEntity : fetchedHoldingsEntityList) {
                    manageHoldingWithItem(incomingHoldingsEntity, fetchedHoldingsEntity);
                    isAnyValidHoldingToUpdate = true;
                }
            }
        } else {
            Map<String,HoldingsEntity> incomingOwningInstHoldingsIdHoldingsEntityMap = getOwningInstHoldingsIdHoldingsEntityMap(incomingHoldingsEntityList);
            Map<String,HoldingsEntity> fetchedOwningInstHoldingsIdHoldingsEntityMap = getOwningInstHoldingsIdHoldingsEntityMap(fetchedHoldingsEntityList);
            for(Map.Entry<String,HoldingsEntity> incomingOwningInstHoldingsIdHoldingsEntityMapEntry:incomingOwningInstHoldingsIdHoldingsEntityMap.entrySet()){
                HoldingsEntity incomingHoldingsEntity = incomingOwningInstHoldingsIdHoldingsEntityMapEntry.getValue();
                HoldingsEntity fetchedHoldingsEntity = fetchedOwningInstHoldingsIdHoldingsEntityMap.get(incomingOwningInstHoldingsIdHoldingsEntityMapEntry.getKey());
                if(fetchedHoldingsEntity != null){
                    copyHoldingsEntity(fetchedHoldingsEntity, incomingHoldingsEntity,false);
                    isAnyValidHoldingToUpdate = true;
                } else {
                    for(ItemEntity itemEntity:incomingHoldingsEntity.getItemEntities()){
                        barcodeHavingMismatchHoldingsId.add(itemEntity.getBarcode());
                    }
                }
            }
        }

        List<ItemEntity> fetchedItemEntityList = fetchBibliographicEntity.getItemEntities();
        List<ItemEntity> incomingItemEntityList = new ArrayList<>(incomingBibliographicEntity.getItemEntities());

        StopWatch itemStopWatch = new StopWatch();
        itemStopWatch.start();
        Map<String,ItemEntity> fetchedBarcodeItemEntityMap = getBarcodeItemEntityMap(fetchedItemEntityList);
        Map<String,ItemEntity> incomingBarcodeItemEntityMap = getBarcodeItemEntityMap(incomingItemEntityList);
        for(Map.Entry<String,ItemEntity> incomingBarcodeItemEntityMapEntry:incomingBarcodeItemEntityMap.entrySet()){
            ItemEntity incomingItemEntity = incomingBarcodeItemEntityMapEntry.getValue();
            ItemEntity fetchedItemEntity = fetchedBarcodeItemEntityMap.get(incomingBarcodeItemEntityMapEntry.getKey());
            logger.info("Processing barcode--->{}",incomingItemEntity.getBarcode());
            if(fetchedItemEntity != null){
                if (fetchedItemEntity.getOwningInstitutionItemId().equalsIgnoreCase(incomingItemEntity.getOwningInstitutionItemId())) {
                    if (fetchedItemEntity.getBarcode().equals(incomingItemEntity.getBarcode()) && !barcodeHavingMismatchHoldingsId.contains(incomingItemEntity.getBarcode())) {
                        if(!isDeAccessionedItem(fetchedItemEntity)) {
                                copyItemEntity(fetchedItemEntity, incomingItemEntity, updatedItemEntityList);
                                isAnyValidItemToUpdate = true;
                        } else {//add exception report for deaccession record
                            addExceptionReport(Arrays.asList(incomingItemEntity),submitCollectionReportInfoMap,ReCAPConstants.SUBMIT_COLLECTION_DEACCESSION_EXCEPTION_RECORD);
                        }
                    }
                }
            } else {//Add to exception report when barcode is unavailable
                addExceptionReport(Arrays.asList(incomingItemEntity),submitCollectionReportInfoMap,ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_RECORD);
            }
        }
        fetchBibliographicEntity.setHoldingsEntities(fetchedHoldingsEntityList);
        fetchBibliographicEntity.setItemEntities(fetchedItemEntityList);
        try {
            updateCatalogingStatusForBib(fetchBibliographicEntity);
            if (isAnyValidHoldingToUpdate && isAnyValidItemToUpdate) {
                bibliographicEntityToSave = fetchBibliographicEntity;
                processedBibIds.add(bibliographicEntityToSave.getBibliographicId());
                List<ItemChangeLogEntity> preparedItemChangeLogEntityList = prepareItemChangeLogEntity(ReCAPConstants.SUBMIT_COLLECTION, ReCAPConstants.SUBMIT_COLLECTION_COMPLETE_RECORD_UPDATE,updatedItemEntityList);
                itemChangeLogEntityList.addAll(preparedItemChangeLogEntityList);
            }
            submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap,fetchBibliographicEntity,incomingBibliographicEntity);
            return bibliographicEntityToSave;
        } catch (Exception e) {
            submitCollectionReportHelperService.setSubmitCollectionExceptionReportInfo(updatedItemEntityList,submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST), ReCAPConstants.SUBMIT_COLLECTION_FAILED_RECORD);
            logger.error(ReCAPConstants.LOG_ERROR,e);
            return null;
        }
    }

    private Map<String,HoldingsEntity> getOwningInstHoldingsIdHoldingsEntityMap(List<HoldingsEntity> holdingsEntityList){
        return holdingsEntityList.stream().collect((Collectors.toMap(HoldingsEntity::getOwningInstitutionHoldingsId,holdingsEntity -> holdingsEntity)));
    }

    private boolean isDeAccessionedItem(ItemEntity fetchedItemEntity){
        return fetchedItemEntity.isDeleted();
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

    public List<ItemEntity> getItemEntityListUsingBarcodeList(List<String> itemBarcodeList,Integer owningInstitutionId){
        return repositoryService.getItemDetailsRepository().findByBarcodeInAndOwningInstitutionId(itemBarcodeList,owningInstitutionId);
    }

    public Set<String> getBarcodeSetFromNonBoundWithBibliographicEntity(List<NonBoundWithBibliographicEntityObject> nonBoundWithBibliographicEntityObjectList){
        Set<String> itemBarcodeList = new HashSet<>();
        for(NonBoundWithBibliographicEntityObject nonBoundWithBibliographicEntityObject :nonBoundWithBibliographicEntityObjectList){
            for (BibliographicEntity bibliographicEntity: nonBoundWithBibliographicEntityObject.getBibliographicEntityList()){
                for (ItemEntity itemEntity : bibliographicEntity.getItemEntities()) {
                    itemBarcodeList.add(itemEntity.getBarcode());
                }
            }
        }
        return itemBarcodeList;
    }

    private Set<String> getBarcodeSetFromBoundWithBibliographicEntity(List<BoundWithBibliographicEntityObject> boundWithBibliographicEntityObjectList){
        Set<String> itemBarcodeList = new HashSet<>();
        for(BoundWithBibliographicEntityObject boundWithBibliographicEntityObject :boundWithBibliographicEntityObjectList){
            for (BibliographicEntity bibliographicEntity: boundWithBibliographicEntityObject.getBibliographicEntityList()){
                for (ItemEntity itemEntity : bibliographicEntity.getItemEntities()) {
                    itemBarcodeList.add(itemEntity.getBarcode());
                }
            }
        }
        return itemBarcodeList;
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
        return itemEntity.getBibliographicEntities().size() > 1;
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

    private List<ItemChangeLogEntity> prepareItemChangeLogEntity(String operationType, String message, List<ItemEntity> itemEntityList) {
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
        return itemChangeLogEntityList;
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
        return itemStatusCode.equalsIgnoreCase(ReCAPConstants.ITEM_STATUS_AVAILABLE);
    }

    private void setItemAvailabilityStatus(List<ItemEntity> itemEntityList){
        for (ItemEntity itemEntity:itemEntityList) {
            if(itemEntity.getItemAvailabilityStatusId()==null) {
                itemEntity.setItemAvailabilityStatusId((Integer) setupDataService.getItemStatusCodeIdMap().get("Available"));
            }
        }
    }
}