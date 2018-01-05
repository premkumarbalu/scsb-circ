package org.recap.service.submitcollection;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections4.ListUtils;
import org.marc4j.marc.Record;
import org.recap.ReCAPConstants;
import org.recap.model.BibliographicEntity;
import org.recap.model.HoldingsEntity;
import org.recap.model.InstitutionEntity;
import org.recap.model.ItemEntity;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.model.report.SubmitCollectionReportInfo;
import org.recap.model.submitcollection.BarcodeBibliographicEntityObject;
import org.recap.model.submitcollection.BoundWithBibliographicEntityObject;
import org.recap.model.submitcollection.NonBoundWithBibliographicEntityObject;
import org.recap.service.common.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.xml.bind.JAXBException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by premkb on 10/10/17.
 */
@Service
public class SubmitCollectionBatchService extends SubmitCollectionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionBatchService.class);

    @Autowired
    private SubmitCollectionReportHelperService submitCollectionReportHelperService;

    @Autowired
    private RepositoryService repositoryService;

    @Value("${submit.collection.input.limit}")
    private Integer inputLimit;

    @Value("${submit.collection.partition.size}")
    private Integer partitionSize;

    @Override
    public String processMarc(String inputRecords, Set<Integer> processedBibIds, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String, String>> idMapToRemoveIndexList, List<Map<String, String>> bibIdMapToRemoveIndexList, boolean checkLimit
            , boolean isCGDProtection, InstitutionEntity institutionEntity,Set<String> updatedDummyRecordOwnInstBibIdSet) {
        logger.info("inside SubmitCollectionBatchService");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String format;
        format = ReCAPConstants.FORMAT_MARC;
        List<Record> recordList = null;
        try {
            recordList = getMarcUtil().convertMarcXmlToRecord(inputRecords);
            if(checkLimit && recordList.size() > inputLimit){
                return ReCAPConstants.SUBMIT_COLLECTION_LIMIT_EXCEED_MESSAGE + inputLimit;
            }
        } catch (Exception e) {
            logger.info(String.valueOf(e.getCause()));
            logger.error(ReCAPConstants.LOG_ERROR,e);
            return ReCAPConstants.INVALID_MARC_XML_FORMAT_MESSAGE;
        }

        List<BibliographicEntity> validBibliographicEntityList = new ArrayList<>();
        for(Record record:recordList){
            BibliographicEntity bibliographicEntity = prepareBibliographicEntity(record, format, submitCollectionReportInfoMap,idMapToRemoveIndexList,isCGDProtection,institutionEntity);
            validBibliographicEntityList.add(bibliographicEntity);
        }
        logger.info("Total incoming marc records for processing--->{}",recordList.size());
        processConvertedBibliographicEntityFromIncomingRecords(processedBibIds, submitCollectionReportInfoMap, idMapToRemoveIndexList, bibIdMapToRemoveIndexList, institutionEntity, updatedDummyRecordOwnInstBibIdSet, validBibliographicEntityList);
        stopWatch.stop();
        logger.info("Total time take for processMarc--->{}",stopWatch.getTotalTimeSeconds());
        return null;
    }

    private void processConvertedBibliographicEntityFromIncomingRecords(Set<Integer> processedBibIds, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String, String>> idMapToRemoveIndexList, List<Map<String, String>> bibIdMapToRemoveIndexList, InstitutionEntity institutionEntity, Set<String> updatedDummyRecordOwnInstBibIdSet, List<BibliographicEntity> validBibliographicEntityList) {
        //TODO need to remove the list - remove the intermediate process
        List<BibliographicEntity> boundwithBibliographicEntityList = new ArrayList<>();
        List<BibliographicEntity> nonBoundWithBibliographicEntityList = new ArrayList<>();
        List<BibliographicEntity> splittedBibliographicEntityList = splitBibWithOneItem(validBibliographicEntityList);
        prepareBoundWithAndNonBoundWithList(splittedBibliographicEntityList,nonBoundWithBibliographicEntityList,boundwithBibliographicEntityList);

        Map<String,List<BibliographicEntity>> groupByOwnInstBibIdBibliographicEntityListMap = groupByOwnInstBibIdBibliographicEntityListMap(nonBoundWithBibliographicEntityList);//Added to avoid data discrepancy during multithreading
        Map<String,List<BibliographicEntity>> groupByBarcodeBibliographicEntityListMap = groupByBarcodeBibliographicEntityListMap(boundwithBibliographicEntityList);//Added to avoid data discrepancy during multithreading
        List<NonBoundWithBibliographicEntityObject> nonBoundWithBibliographicEntityObjectList = prepareNonBoundWithBibliographicEntity(groupByOwnInstBibIdBibliographicEntityListMap);
        List<BoundWithBibliographicEntityObject> boundWithBibliographicEntityObjectList = prepareBoundWithBibliographicEntityObjectList(groupByBarcodeBibliographicEntityListMap);
        logger.info("boundwithBibliographicEntityList size--->{}",boundwithBibliographicEntityList.size());
        logger.info("boundWithBibliographicEntityObjectList size--->{}",boundWithBibliographicEntityObjectList.size());
        logger.info("nonBoundWithBibliographicEntityList size--->{}",nonBoundWithBibliographicEntityList.size());
        if (!nonBoundWithBibliographicEntityObjectList.isEmpty()) {
            processRecordsInBatchesForNonBoundWith(nonBoundWithBibliographicEntityObjectList,institutionEntity.getInstitutionId(),submitCollectionReportInfoMap,processedBibIds,idMapToRemoveIndexList);
        }
        if (!boundwithBibliographicEntityList.isEmpty()) {
            processRecordsInBatchesForBoundWith(boundWithBibliographicEntityObjectList,institutionEntity.getInstitutionId(),submitCollectionReportInfoMap,processedBibIds,idMapToRemoveIndexList,bibIdMapToRemoveIndexList,updatedDummyRecordOwnInstBibIdSet);//updatedDummyRecordOwnInstBibIdSet is required only for boundwith
        }
    }

    private List<BibliographicEntity> splitBibWithOneItem(List<BibliographicEntity> bibliographicEntityList){
        List<BibliographicEntity> splitedBibliographicEntityList = new ArrayList<>();
        for(BibliographicEntity bibliographicEntity:bibliographicEntityList){
            if(bibliographicEntity.getItemEntities().size()>1){
                for(HoldingsEntity holdingsEntity:bibliographicEntity.getHoldingsEntities()){
                    for (ItemEntity itemEntity:holdingsEntity.getItemEntities()){
                        BibliographicEntity splitedBibliographicEntity = new BibliographicEntity();
                        splitedBibliographicEntity.setOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionBibId());
                        splitedBibliographicEntity.setCatalogingStatus(bibliographicEntity.getCatalogingStatus());
                        splitedBibliographicEntity.setContent(bibliographicEntity.getContent());
                        splitedBibliographicEntity.setOwningInstitutionId(bibliographicEntity.getOwningInstitutionId());
                        splitedBibliographicEntity.setCreatedBy(bibliographicEntity.getCreatedBy());
                        splitedBibliographicEntity.setCreatedDate(bibliographicEntity.getCreatedDate());
                        splitedBibliographicEntity.setLastUpdatedBy(bibliographicEntity.getLastUpdatedBy());
                        splitedBibliographicEntity.setLastUpdatedDate(bibliographicEntity.getLastUpdatedDate());
                        HoldingsEntity splitedHoldingsEntity = new HoldingsEntity();
                        splitedHoldingsEntity.setOwningInstitutionId(holdingsEntity.getOwningInstitutionId());
                        splitedHoldingsEntity.setContent(holdingsEntity.getContent());
                        splitedHoldingsEntity.setOwningInstitutionHoldingsId(holdingsEntity.getOwningInstitutionHoldingsId());
                        splitedHoldingsEntity.setCreatedBy(holdingsEntity.getCreatedBy());
                        splitedHoldingsEntity.setCreatedDate(holdingsEntity.getCreatedDate());
                        splitedHoldingsEntity.setLastUpdatedBy(holdingsEntity.getLastUpdatedBy());
                        splitedHoldingsEntity.setLastUpdatedDate(holdingsEntity.getLastUpdatedDate());
                        splitedHoldingsEntity.setItemEntities(Arrays.asList(itemEntity));
                        splitedBibliographicEntity.setHoldingsEntities(Arrays.asList(splitedHoldingsEntity));
                        splitedBibliographicEntity.setItemEntities(Arrays.asList(itemEntity));
                        splitedBibliographicEntityList.add(splitedBibliographicEntity);
                    }
                }
            } else {
                splitedBibliographicEntityList.add(bibliographicEntity);
            }
        }
        return splitedBibliographicEntityList;
    }

    @Override
    public String processSCSB(String inputRecords, Set<Integer> processedBibIds, Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap,
                               List<Map<String, String>> idMapToRemoveIndexList, List<Map<String, String>> bibIdMapToRemoveIndexList, boolean checkLimit,boolean isCGDProtected,InstitutionEntity institutionEntity,Set<String> updatedDummyRecordOwnInstBibIdSet) {
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

        List<BibliographicEntity> validBibliographicEntityList = new ArrayList<>();
        for (BibRecord bibRecord : bibRecords.getBibRecords()) {
            BibliographicEntity bibliographicEntity = prepareBibliographicEntity(bibRecord, format, submitCollectionReportInfoMap,idMapToRemoveIndexList,isCGDProtected,institutionEntity);
            validBibliographicEntityList.add(bibliographicEntity);
        }
        logger.info("Total incoming scsb records for processing--->{}",bibRecords.getBibRecords().size());
        processConvertedBibliographicEntityFromIncomingRecords(processedBibIds, submitCollectionReportInfoMap, idMapToRemoveIndexList, bibIdMapToRemoveIndexList, institutionEntity, updatedDummyRecordOwnInstBibIdSet, validBibliographicEntityList);
        stopWatch.stop();
        logger.info("Total time take for process SCSB--->{}",stopWatch.getTotalTimeSeconds());
        return null;
    }

    private void prepareBoundWithAndNonBoundWithList(List<BibliographicEntity> validBibliographicEntityList,List<BibliographicEntity> nonBoundWithBibliographicEntityList
        ,List<BibliographicEntity> boundwithBibliographicEntityList){
        List<BarcodeBibliographicEntityObject> barcodeBibliographicEntityObjectList = getBarcodeOwningInstitutionBibIdObjectList(validBibliographicEntityList);
        Map<String,List<BarcodeBibliographicEntityObject>> groupByBarcodeBibliographicEntityObjectMap  = groupByBarcodeAndGetBarcodeBibliographicEntityObjectMap(barcodeBibliographicEntityObjectList);

        for(Map.Entry<String,List<BarcodeBibliographicEntityObject>> groupByBarcodeBibliographicEntityObjectMapEntry:groupByBarcodeBibliographicEntityObjectMap.entrySet()){
            if(groupByBarcodeBibliographicEntityObjectMapEntry.getValue().size()>1){
                for(BarcodeBibliographicEntityObject barcodeBibliographicEntityObject:groupByBarcodeBibliographicEntityObjectMapEntry.getValue()){
                    boundwithBibliographicEntityList.add(barcodeBibliographicEntityObject.getBibliographicEntity());
                    logger.info("boundwith barcode--->{}",barcodeBibliographicEntityObject.getBarcode());
                }
            } else {
                BibliographicEntity bibliographicEntity = groupByBarcodeBibliographicEntityObjectMapEntry.getValue().get(0).getBibliographicEntity();
                if(!nonBoundWithBibliographicEntityList.contains(bibliographicEntity)){
                    nonBoundWithBibliographicEntityList.add(bibliographicEntity);
                }
            }
        }
    }

    private List<NonBoundWithBibliographicEntityObject> prepareNonBoundWithBibliographicEntity(Map<String,List<BibliographicEntity>> groupByOwnInstBibIdBibliographicEntityListMap){
        List<NonBoundWithBibliographicEntityObject> nonBoundWithBibliographicEntityObjectList = new ArrayList<>();
        for(Map.Entry<String,List<BibliographicEntity>> groupByOwnInstBibIdBibliographicEntityListMapEntry: groupByOwnInstBibIdBibliographicEntityListMap.entrySet()) {
            NonBoundWithBibliographicEntityObject nonBoundWithBibliographicEntityObject = new NonBoundWithBibliographicEntityObject();
            nonBoundWithBibliographicEntityObject.setOwningInstitutionBibId(groupByOwnInstBibIdBibliographicEntityListMapEntry.getKey());
            nonBoundWithBibliographicEntityObject.setBibliographicEntityList(groupByOwnInstBibIdBibliographicEntityListMapEntry.getValue());
            nonBoundWithBibliographicEntityObjectList.add(nonBoundWithBibliographicEntityObject);
        }
        return nonBoundWithBibliographicEntityObjectList;
    }

    private List<BoundWithBibliographicEntityObject> prepareBoundWithBibliographicEntityObjectList(Map<String,List<BibliographicEntity>> groupByBarcodeBibliographicEntityListMap){
        List<BoundWithBibliographicEntityObject> boundWithBibliographicEntityObjectList = new ArrayList<>();
        for(Map.Entry<String,List<BibliographicEntity>> groupByBarcodeBibliographicEntityListMapEntry: groupByBarcodeBibliographicEntityListMap.entrySet()) {
            BoundWithBibliographicEntityObject boundWithBibliographicEntityObject = new BoundWithBibliographicEntityObject();
            boundWithBibliographicEntityObject.setBarcode(groupByBarcodeBibliographicEntityListMapEntry.getKey());
            boundWithBibliographicEntityObject.setBibliographicEntityList(groupByBarcodeBibliographicEntityListMapEntry.getValue());
            boundWithBibliographicEntityObjectList.add(boundWithBibliographicEntityObject);
        }
        return boundWithBibliographicEntityObjectList;
    }

    private BibliographicEntity prepareBibliographicEntity(Object record, String format, Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String,String>> idMapToRemoveIndexList
            , boolean isCGDProtected, InstitutionEntity institutionEntity){
        BibliographicEntity incomingBibliographicEntity = null;
        try {
            Map responseMap = getConverter(format).convert(record,institutionEntity);
            StringBuilder errorMessage = (StringBuilder)responseMap.get("errorMessage");
            incomingBibliographicEntity = responseMap.get("bibliographicEntity") != null ? (BibliographicEntity) responseMap.get("bibliographicEntity"):null;
            if (errorMessage != null && errorMessage.length()==0) {//Valid bibliographic entity is returned for further processing
                setCGDProtectionForItems(incomingBibliographicEntity,isCGDProtected);//TODO need to test cgd protected and customer code for dummy
                if (incomingBibliographicEntity != null) {
                    return incomingBibliographicEntity;
                }
            } else {//Invalid bibliographic entity is added to the failure report
                logger.error("Error while parsing xml for a barcode in submit collection");
                submitCollectionReportHelperService.setSubmitCollectionFailureReportForUnexpectedException(incomingBibliographicEntity,
                        submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),"Failed record - Item not updated - "+errorMessage.toString(),institutionEntity);
            }
        } catch (Exception e) {
            logger.error("Exception while preparing bibliographic entity");
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }
        return incomingBibliographicEntity;
    }

    private void processRecordsInBatchesForNonBoundWith(List<NonBoundWithBibliographicEntityObject> nonBoundWithBibliographicEntityObjectList, Integer owningInstitutionId, Map<String,
            List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, Set<Integer> processedBibIds, List<Map<String, String>> idMapToRemoveIndexList){
        Set<String> processedBarcodeSetForDummyRecords = new HashSet<>();
        List<List<NonBoundWithBibliographicEntityObject>> nonBoundWithBibliographicEntityPartitionList = ListUtils.partition(nonBoundWithBibliographicEntityObjectList,partitionSize);
        logger.info("Total non bound-with batch count--->{}",nonBoundWithBibliographicEntityPartitionList.size());
        List<BibliographicEntity> updatedBibliographicEntityToSaveList = new ArrayList<>();
        int batchCounter = 1;
        for(List<NonBoundWithBibliographicEntityObject> nonBoundWithBibliographicEntityObjectListToProces :nonBoundWithBibliographicEntityPartitionList){
            logger.info("nonBoundWithBibliographicEntityObjectListToProces.size---->{}",nonBoundWithBibliographicEntityObjectListToProces.size());
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            logger.info("Processing non bound-with batch no. ---->{}",batchCounter);
            List<BibliographicEntity> updatedBibliographicEntityList = null;
            updatedBibliographicEntityList = getSubmitCollectionDAOService().updateBibliographicEntityInBatchForNonBoundWith(nonBoundWithBibliographicEntityObjectListToProces
                    ,owningInstitutionId,submitCollectionReportInfoMap,processedBibIds,idMapToRemoveIndexList,processedBarcodeSetForDummyRecords);
            if (updatedBibliographicEntityList!=null && !updatedBibliographicEntityList.isEmpty()) {
                updatedBibliographicEntityToSaveList.addAll(updatedBibliographicEntityList);
            }
            stopWatch.stop();
            logger.info("Time taken to process and save {} non bound-with records batch--->{}",partitionSize,stopWatch.getTotalTimeSeconds());
            batchCounter++;
        }
    }

    private void processRecordsInBatchesForBoundWith(List<BoundWithBibliographicEntityObject> boundWithBibliographicEntityObjectList, Integer owningInstitutionId, Map<String,
            List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, Set<Integer> processedBibIds, List<Map<String, String>> idMapToRemoveIndexList,List<Map<String, String>> bibIdMapToRemoveIndexList, Set<String> updatedDummyRecordOwnInstBibIdSet){

        Set<String> processedBarcodeSetForDummyRecords = new HashSet<>();
        List<List<BoundWithBibliographicEntityObject>> boundWithBibliographicEntityObjectPartitionList = ListUtils.partition(boundWithBibliographicEntityObjectList,partitionSize);
        logger.info("Total bound-with batch count--->{}",boundWithBibliographicEntityObjectPartitionList.size());
        List<BibliographicEntity> updatedBibliographicEntityToSaveList = new ArrayList<>();
        int batchCounter = 1;
        for(List<BoundWithBibliographicEntityObject> boundWithBibliographicEntityObjectToProcess :boundWithBibliographicEntityObjectPartitionList){
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            logger.info("boundWithBibliographicEntityObjectToProcess.size---->{}",boundWithBibliographicEntityObjectToProcess.size());
            logger.info("Processing bound-with batch no. ---->{}",batchCounter);
            List<BibliographicEntity> updatedBibliographicEntityList = null;
            updatedBibliographicEntityList = getSubmitCollectionDAOService().updateBibliographicEntityInBatchForBoundWith(boundWithBibliographicEntityObjectToProcess,owningInstitutionId,submitCollectionReportInfoMap,processedBibIds,idMapToRemoveIndexList,bibIdMapToRemoveIndexList,processedBarcodeSetForDummyRecords);
            if (updatedBibliographicEntityList!=null && !updatedBibliographicEntityList.isEmpty()) {
                updatedBibliographicEntityToSaveList.addAll(updatedBibliographicEntityList);
            }
            setUpdatedDummyRecordOwningInstBibId(updatedBibliographicEntityList,updatedDummyRecordOwnInstBibIdSet);
            stopWatch.stop();
            logger.info("Time taken to process and save {} bound-with records batch--->{}",partitionSize,stopWatch.getTotalTimeSeconds());
            logger.info("Total updatedDummyRecordOwnInstBibIdSet size--->{}",updatedDummyRecordOwnInstBibIdSet.size());
            batchCounter++;
        }
    }

    private void setUpdatedDummyRecordOwningInstBibId(List<BibliographicEntity> bibliographicEntityList, Set<String> updatedDummyRecordOwnInstBibIdSet){
        for(BibliographicEntity bibliographicEntity:bibliographicEntityList){
            if (bibliographicEntity.getBibliographicId()==null) {
                updatedDummyRecordOwnInstBibIdSet.add(bibliographicEntity.getOwningInstitutionBibId());
            }
        }
    }

    private List<BarcodeBibliographicEntityObject> getBarcodeOwningInstitutionBibIdObjectList(List<BibliographicEntity> bibliographicEntityList){
        List<BarcodeBibliographicEntityObject> barcodeOwningInstitutionBibIdObjectList = new ArrayList<>();
        for(BibliographicEntity bibliographicEntity:bibliographicEntityList){
            for(ItemEntity itemEntity:bibliographicEntity.getItemEntities()){
                BarcodeBibliographicEntityObject barcodeOwningInstitutionBibIdObject = new BarcodeBibliographicEntityObject();
                barcodeOwningInstitutionBibIdObject.setBarcode(itemEntity.getBarcode());
                barcodeOwningInstitutionBibIdObject.setOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionBibId());
                barcodeOwningInstitutionBibIdObject.setBibliographicEntity(bibliographicEntity);
                barcodeOwningInstitutionBibIdObjectList.add(barcodeOwningInstitutionBibIdObject);
            }
        }
        return barcodeOwningInstitutionBibIdObjectList;
    }

    private Map<String,List<BarcodeBibliographicEntityObject>> groupByBarcodeAndGetBarcodeBibliographicEntityObjectMap(List<BarcodeBibliographicEntityObject> barcodeOwningInstitutionBibIdObjectList){
        Map<String,List<BarcodeBibliographicEntityObject>> groupByBarcodeOwningInstitutionBibIdObjectMap = barcodeOwningInstitutionBibIdObjectList.stream()
                .collect(Collectors.groupingBy(BarcodeBibliographicEntityObject::getBarcode));
        return groupByBarcodeOwningInstitutionBibIdObjectMap;
    }

    private Map<String,List<BibliographicEntity>> groupByOwnInstBibIdBibliographicEntityListMap(List<BibliographicEntity> bibliographicEntityList){
        Map<String,List<BibliographicEntity>> groupByOwnInstBibIdBibliographicEntityListMap = bibliographicEntityList.stream()
                .collect(Collectors.groupingBy(BibliographicEntity::getOwningInstitutionBibId));
        return  groupByOwnInstBibIdBibliographicEntityListMap;
    }
    private Map<String,List<BibliographicEntity>> groupByBarcodeBibliographicEntityListMap(List<BibliographicEntity> bibliographicEntityList){
        Map<String,List<BibliographicEntity>> groupByBarcodeBibliographicEntityListMap = new HashedMap();
        for(BibliographicEntity bibliographicEntity:bibliographicEntityList){
            List<BibliographicEntity> addedBibliographicEntityList = groupByBarcodeBibliographicEntityListMap.get(bibliographicEntity.getItemEntities().get(0).getBarcode());
            if(addedBibliographicEntityList!=null){
                List<BibliographicEntity> updatedBibliographicEntityList = new ArrayList<>(addedBibliographicEntityList);
                updatedBibliographicEntityList.add(bibliographicEntity);
                groupByBarcodeBibliographicEntityListMap.put(bibliographicEntity.getItemEntities().get(0).getBarcode(),updatedBibliographicEntityList);
            } else {
                groupByBarcodeBibliographicEntityListMap.put(bibliographicEntity.getItemEntities().get(0).getBarcode(),Arrays.asList(bibliographicEntity));
            }
        }
        return groupByBarcodeBibliographicEntityListMap;
    }
}
