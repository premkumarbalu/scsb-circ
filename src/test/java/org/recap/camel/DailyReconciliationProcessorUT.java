package org.recap.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.camel.dailyreconciliation.DailyReconciliationProcessor;
import org.recap.model.*;
import org.recap.repository.RequestItemDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by akulak on 8/5/17.
 */
public class DailyReconciliationProcessorUT extends BaseTestCase {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    DailyReconciliationProcessor dailyReconciliationProcessor;

    @Autowired
    RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    CamelContext camelContext;

    @Autowired
    ProducerTemplate producerTemplate;

    @Test
    public void testCreateCell() throws Exception{
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet testSheet = xssfWorkbook.createSheet("test");
        XSSFRow row = testSheet.createRow(0);
        CellStyle cellStyle = xssfWorkbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);

        dailyReconciliationProcessor.createCell(xssfWorkbook,row,cellStyle,"test",0);
        XSSFSheet test = xssfWorkbook.getSheet("test");
        XSSFRow testRow = test.getRow(0);
        assertNotNull(testRow);
        XSSFCell cell = testRow.getCell(0);
        assertEquals("test",cell.getStringCellValue());
    }

    @Test
    public void testBuildRequestsRows() throws Exception{
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet testSheet = xssfWorkbook.createSheet("test");
        BibliographicEntity bibliographicEntity = saveBibHoldingItemEntity();
        ItemEntity itemEntity = bibliographicEntity.getItemEntities().get(0);
        RequestItemEntity requestItemEntity = saveRequestItemEntity(itemEntity.getItemId(),itemEntity);
        XSSFCellStyle xssfCellStyleForDate = dailyReconciliationProcessor.getXssfCellStyleForDate(xssfWorkbook);
        dailyReconciliationProcessor.buildRequestsRows(xssfWorkbook,testSheet,xssfCellStyleForDate,0, String.valueOf(requestItemEntity.getRequestId()));
        XSSFSheet test = xssfWorkbook.getSheet("test");
        XSSFRow testRow = test.getRow(0);
        assertNotNull(testRow);
        Cell cell0 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 0);
        Cell cell1 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 1);
        Cell cell2 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 2);
        Cell cell3 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 3);
        Cell cell4 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 4);
        Cell cell7 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 7);
        Cell cell8 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 8);
        Cell cell9 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 9);
        Cell cell10 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 10);

        assertEquals(String.valueOf(requestItemEntity.getRequestId()),cell0.getStringCellValue());
        assertEquals(String.valueOf(itemEntity.getBarcode()),cell1.getStringCellValue());
        assertEquals(String.valueOf(itemEntity.getCustomerCode()),cell2.getStringCellValue());
        assertEquals(String.valueOf(requestItemEntity.getStopCode()),cell3.getStringCellValue());
        assertEquals(String.valueOf(requestItemEntity.getPatronId()),cell4.getStringCellValue());
        assertEquals(String.valueOf(requestItemEntity.getInstitutionEntity().getInstitutionCode()),cell7.getStringCellValue());
        assertEquals(String.valueOf(itemEntity.getInstitutionEntity().getInstitutionCode()),cell8.getStringCellValue());
        assertEquals(requestItemEntity.getRequestTypeEntity().getRequestTypeCode(),cell9.getStringCellValue());
        assertEquals(itemEntity.getItemStatusEntity().getStatusCode(),cell10.getStringCellValue());

    }


    @Test
    public void testBuildDeacessionRows() throws Exception{
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet testSheet = xssfWorkbook.createSheet("test");
        BibliographicEntity bibliographicEntity = saveBibHoldingItemEntity();
        ItemEntity itemEntity = bibliographicEntity.getItemEntities().get(0);
        XSSFCellStyle xssfCellStyleForDate = dailyReconciliationProcessor.getXssfCellStyleForDate(xssfWorkbook);
        dailyReconciliationProcessor.buildDeacessionRows(xssfWorkbook,testSheet,xssfCellStyleForDate,0,itemEntity.getBarcode());
        XSSFSheet test = xssfWorkbook.getSheet("test");
        XSSFRow testRow = test.getRow(0);
        assertNotNull(testRow);
        Cell cell1 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 1);
        Cell cell2 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 2);
        Cell cell8 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 8);
        Cell cell10 = dailyReconciliationProcessor.getRowValuesForCompare(testRow, 10);
        assertEquals(itemEntity.getBarcode(),cell1.getStringCellValue());
        assertEquals(itemEntity.getCustomerCode(),cell2.getStringCellValue());
        assertEquals(itemEntity.getInstitutionEntity().getInstitutionCode(),cell8.getStringCellValue());
        assertEquals(itemEntity.getItemStatusEntity().getStatusCode(),cell10.getStringCellValue());
    }


    @Test
    public void testCompareLasAndScsbSheets() throws Exception{
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet testSheet = xssfWorkbook.createSheet("test");
        testSheet.createRow(1).createCell(0).setCellValue("1");
        XSSFSheet testSheet1 = xssfWorkbook.createSheet("test1");
        testSheet1.createRow(1).createCell(0).setCellValue("1");
        xssfWorkbook.setSheetOrder("test",0);
        xssfWorkbook.setSheetOrder("test1",1); CellStyle cellStyle = xssfWorkbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        dailyReconciliationProcessor.compareLasAndScsbSheets(xssfWorkbook,cellStyle);
        XSSFSheet compareSheet = xssfWorkbook.getSheetAt(2);
        assertEquals("1",compareSheet.getRow(2).getCell(0).getStringCellValue());
        assertEquals("1",compareSheet.getRow(2).getCell(3).getStringCellValue());
        assertEquals("Matched",compareSheet.getRow(2).getCell(6).getStringCellValue());
    }


    private BibliographicEntity saveBibHoldingItemEntity() throws Exception {
        Random random = new Random();
        String owningInstitutionBibId = String.valueOf(random.nextInt());
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("Mock Bib Content".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("ut");
        bibliographicEntity.setLastUpdatedBy("ut");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);
        bibliographicEntity.setOwningInstitutionId(1);

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("ut");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("ut");
        holdingsEntity.setOwningInstitutionId(1);
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setItemId(new Random().nextInt());
        itemEntity.setBarcode("b3");
        itemEntity.setCustomerCode("c1");
        itemEntity.setCallNumber("cn1");
        itemEntity.setCallNumberType("ct1");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setCopyNumber(1);
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("ut");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("ut");
        itemEntity.setUseRestrictions("no");
        itemEntity.setVolumePartYear("v3");
        itemEntity.setOwningInstitutionItemId(String.valueOf(new Random().nextInt()));
        InstitutionEntity institutionEntity = getInstitutionEntity();
        ItemStatusEntity itemStatusEntity = getItemStatusEntity();
        itemEntity.setItemStatusEntity(itemStatusEntity);
        itemEntity.setInstitutionEntity(institutionEntity);
        itemEntity.setDeleted(false);

        itemEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);

        return savedBibliographicEntity;
    }

    public RequestItemEntity saveRequestItemEntity(Integer itemId,ItemEntity itemEntity){
        RequestItemEntity requestItemEntity = new RequestItemEntity();
        requestItemEntity.setItemId(itemId);
        requestItemEntity.setRequestId(new Random().nextInt());
        requestItemEntity.setRequestTypeId(1);
        requestItemEntity.setCreatedBy("test");
        requestItemEntity.setStopCode("PA");
        requestItemEntity.setPatronId("45678912");
        requestItemEntity.setCreatedDate(new Date());
        requestItemEntity.setLastUpdatedDate(new Date());
        requestItemEntity.setEmailId("test@mail");
        requestItemEntity.setRequestStatusId(1);
        requestItemEntity.setRequestingInstitutionId(1);
        RequestTypeEntity requestTypeEntity = getRequestTypeEntity();
        requestItemEntity.setRequestTypeEntity(requestTypeEntity);
        requestItemEntity.setItemEntity(itemEntity);
        RequestItemEntity requestItemEntity1 = requestItemDetailsRepository.saveAndFlush(requestItemEntity);
        entityManager.refresh(requestItemEntity1);
        return requestItemEntity1;
    }

    private RequestTypeEntity getRequestTypeEntity() {
        RequestTypeEntity requestTypeEntity = new RequestTypeEntity();
        requestTypeEntity.setRequestTypeId(1);
        requestTypeEntity.setRequestTypeCode("EDD");
        requestTypeEntity.setRequestTypeDesc("EDD");
        return requestTypeEntity;
    }

    private ItemStatusEntity getItemStatusEntity() {
        ItemStatusEntity itemStatusEntity = new ItemStatusEntity();
        itemStatusEntity.setItemStatusId(1);
        itemStatusEntity.setStatusCode("Available");
        itemStatusEntity.setStatusDescription("Available");
        return itemStatusEntity;
    }

    private InstitutionEntity getInstitutionEntity() {
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionId(1);
        institutionEntity.setInstitutionCode("PUL");
        institutionEntity.setInstitutionName("PUL");
        return institutionEntity;
    }

}
