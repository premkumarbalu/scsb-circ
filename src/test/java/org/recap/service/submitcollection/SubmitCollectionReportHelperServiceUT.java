package org.recap.service.submitcollection;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.converter.MarcToBibEntityConverter;
import org.recap.model.BibliographicEntity;
import org.recap.model.HoldingsEntity;
import org.recap.model.InstitutionEntity;
import org.recap.model.ItemEntity;
import org.recap.model.report.SubmitCollectionReportInfo;
import org.recap.repository.InstitutionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by premkb on 18/6/17.
 */
public class SubmitCollectionReportHelperServiceUT extends BaseTestCase {

    @Autowired
    private SubmitCollectionReportHelperService submitCollectionReportHelperService;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private MarcToBibEntityConverter marcToBibEntityConverter;
    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Test
    public void buildSubmitCollectionReportInfoForValidRecord() throws Exception {
        Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap = getSubmitCollectionReportMap();
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsSingleItem(1,"32101095533293","PA","24252","PUL","9919400","9734816", "7453441",true);
        BibliographicEntity incomingBibliographicEntity = getConvertedBibliographicEntity("MarcRecord.xml","PUL");
        submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap,bibliographicEntity,incomingBibliographicEntity);
        List<SubmitCollectionReportInfo> submitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST);
        assertEquals(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD,submitCollectionReportInfoList.get(0).getMessage());
    }

    @Test
    public void buildSubmitCollectionReportInfoForValidRecord1BibMultipleItems()  throws Exception {
        Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap = getSubmitCollectionReportMap();
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsSingleItem(1,"32101095533293","PA","24252","PUL","9919400","9734816", "7453441",true);
        BibliographicEntity incomingBibliographicEntity = getConvertedBibliographicEntity("MarcRecord.xml","PUL");
        submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap,bibliographicEntity,incomingBibliographicEntity);
        List<SubmitCollectionReportInfo> submitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_LIST);
        assertEquals(ReCAPConstants.SUBMIT_COLLECTION_SUCCESS_RECORD,submitCollectionReportInfoList.get(0).getMessage());
    }

    @Test
    public void buildSubmitCollectionReportInfoForInvalidOwningInstHoldingId() throws Exception {
        Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap = getSubmitCollectionReportMap();
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsSingleItem(1,"32101095533293","PA","24252","PUL","9919400","222420", "7453441",true);
        BibliographicEntity incomingBibliographicEntity = getConvertedBibliographicEntity("MarcRecord.xml","PUL");
        submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap,bibliographicEntity,incomingBibliographicEntity);
        List<SubmitCollectionReportInfo> submitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST);
        assertEquals("Failed record - Owning institution holding id 9734816 for the incoming barcode 32101095533293, owning institution item id 7453441 is unavailable in the existing bib - owning institution bib id - 9919400",submitCollectionReportInfoList.get(0).getMessage());
    }

    @Test
    public void buildSubmitCollectionReportInfoForInvalidRecordOwningInstItemId() throws Exception {
        Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap = getSubmitCollectionReportMap();
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsSingleItem(1,"32101095533293","PA","24252","PUL","9919400","9734816", "7453442",true);
        BibliographicEntity incomingBibliographicEntity = getConvertedBibliographicEntity("MarcRecord.xml","PUL");
        submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap,bibliographicEntity,incomingBibliographicEntity);
        List<SubmitCollectionReportInfo> submitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST);
        assertEquals("Failed record - Incoming item 32101095533293, owning institution item id 7453441 is not matched with the existing item 32101095533293, owning institution item id 7453442, owning institution holding id 9734816, owning institution bib id 9919400",submitCollectionReportInfoList.get(0).getMessage());
    }

    @Test
    public void buildSubmitCollectionReportInfoForRejectionRecord() throws Exception {
        Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap = getSubmitCollectionReportMap();
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsSingleItem(1,"32101095533293","PA","24252","PUL","9919400","9734816", "7453441",false);
        BibliographicEntity incomingBibliographicEntity = getConvertedBibliographicEntity("MarcRecord.xml","PUL");
        submitCollectionReportHelperService.buildSubmitCollectionReportInfo(submitCollectionReportInfoMap,bibliographicEntity,incomingBibliographicEntity);
        List<SubmitCollectionReportInfo> submitCollectionReportInfoList = submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_LIST);
        assertEquals(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_RECORD,submitCollectionReportInfoList.get(0).getMessage());
    }

    public BibliographicEntity saveBibSingleHoldingsSingleItem(Integer itemCount,String itemBarcode, String customerCode, String callnumber, String institution,String owningInstBibId,String owningInstHoldingId, String owningInstItemId,boolean availableItem) throws Exception {
        File bibContentFile = getBibContentFile(institution);
        File holdingsContentFile = getHoldingsContentFile(institution);
        String sourceBibContent = FileUtils.readFileToString(bibContentFile, "UTF-8");
        String sourceHoldingsContent = FileUtils.readFileToString(holdingsContentFile, "UTF-8");

        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent(sourceBibContent.getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setCreatedBy("tst");
        bibliographicEntity.setLastUpdatedBy("tst");
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId(owningInstBibId);
        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent(sourceHoldingsContent.getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setCreatedBy("tst");
        holdingsEntity.setLastUpdatedBy("tst");
        holdingsEntity.setOwningInstitutionId(1);
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(owningInstHoldingId));

        List<ItemEntity> itemEntityList = getItemEntityList(itemCount,itemBarcode,customerCode,callnumber,owningInstItemId,availableItem);
        for(ItemEntity itemEntity1:itemEntityList){
            itemEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity));
            itemEntity1.setBibliographicEntities(Arrays.asList(bibliographicEntity));
        }
        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(itemEntityList);
        holdingsEntity.setItemEntities(itemEntityList);

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        return savedBibliographicEntity;

    }

    public List<ItemEntity> getItemEntityList(Integer itemCount,String itemBarcode,String customerCode,String callnumber,String owningInstItemId,boolean availableItem){
        List<ItemEntity> itemEntityList = new ArrayList<>();
        for(int count=0;count<itemCount;count++){
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setLastUpdatedDate(new Date());
            if (count==0) {
                itemEntity.setOwningInstitutionItemId(owningInstItemId);
                itemEntity.setBarcode(itemBarcode);
            } else {
                itemEntity.setOwningInstitutionItemId(owningInstItemId+count);
                itemEntity.setBarcode(itemBarcode+count);
            }
            itemEntity.setOwningInstitutionId(1);
            itemEntity.setCallNumber(callnumber);
            itemEntity.setCollectionGroupId(1);
            itemEntity.setCallNumberType("1");
            itemEntity.setCustomerCode(customerCode);
            itemEntity.setCatalogingStatus("Complete");
            if (availableItem) {
                itemEntity.setItemAvailabilityStatusId(1);
            } else {
                itemEntity.setItemAvailabilityStatusId(2);
            }
            itemEntity.setCreatedDate(new Date());
            itemEntity.setCreatedBy("tst");
            itemEntity.setLastUpdatedBy("tst");
            itemEntityList.add(itemEntity);
        }
        return itemEntityList;
    }

    private File getBibContentFile(String institution) throws URISyntaxException {
        URL resource = null;
        resource = getClass().getResource("PUL-BibContent.xml");
        return new File(resource.toURI());
    }

    private File getHoldingsContentFile(String institution) throws URISyntaxException {
        URL resource = null;
        resource = getClass().getResource("PUL-HoldingsContent.xml");
        return new File(resource.toURI());
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

    private BibliographicEntity getConvertedBibliographicEntity(String xmlFileName,String institutionCode) throws URISyntaxException, IOException {
        File bibContentFile = getXmlContent(xmlFileName);
        String marcXmlString = FileUtils.readFileToString(bibContentFile, "UTF-8");
        List<Record> records = readMarcXml(marcXmlString);
        InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(institutionCode);
        Map map = marcToBibEntityConverter.convert(records.get(0),institutionEntity);
        assertNotNull(map);
        BibliographicEntity convertedBibliographicEntity = (BibliographicEntity) map.get("bibliographicEntity");
        StringBuilder errorMessage = new StringBuilder();
        return convertedBibliographicEntity;
    }

    private File getXmlContent(String fileName) throws URISyntaxException {
        URL resource = null;
        resource = getClass().getResource(fileName);
        return new File(resource.toURI());
    }

    private List<Record> readMarcXml(String marcXmlString) {
        List<Record> recordList = new ArrayList<>();
        InputStream in = new ByteArrayInputStream(marcXmlString.getBytes());
        MarcReader reader = new MarcXmlReader(in);
        while (reader.hasNext()) {
            Record record = reader.next();
            recordList.add(record);
        }
        return recordList;
    }
}
