package org.recap.camel.statusreconciliation;

import org.apache.camel.ProducerTemplate;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.gfa.model.*;
import org.recap.model.ItemEntity;
import org.recap.model.ItemStatusEntity;
import org.recap.repository.*;
import org.recap.request.GFAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hemalathas on 2/6/17.
 */
public class StatusReconciliationControllerUT extends BaseTestCase{

    @Mock
    private StatusReconciliationController statusReconciliationController;

    private static final Logger logger = LoggerFactory.getLogger(StatusReconciliationController.class);

    @Mock
    GFAService gfaService;


    private Integer batchSize = 100;

    @Value("${status.reconciliation.day.limit}")
    private Integer statusReconciliationDayLimit;

    @Mock
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Mock
    private ItemDetailsRepository itemDetailsRepository;


    private Integer statusReconciliationLasBarcodeLimit = 100;

    @Mock
    private ProducerTemplate producer;

    @Mock
    private RequestItemDetailsRepository mockedRequestItemDetailsRepository;

    @Mock
    private RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Mock
    private ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    @Test
    public void testStatusReconciliation(){
        Ttitem ttitem = new Ttitem();
        ttitem.setItemBarcode("3321545824554545");
        ttitem.setItemStatus("IN");
        Dsitem dsitem = new Dsitem();
        dsitem.setTtitem(Arrays.asList(ttitem));
        GFAItemStatusCheckResponse gfaItemStatusCheckResponse = new GFAItemStatusCheckResponse();
        gfaItemStatusCheckResponse.setDsitem(dsitem);
        Map<String,Integer> itemCountAndStatusIdMap = new HashMap<>();
        itemCountAndStatusIdMap.put("itemAvailabilityStatusId",0);
        itemCountAndStatusIdMap.put("totalPagesCount",0);
        int totalPagesCount = itemCountAndStatusIdMap.get("totalPagesCount");
        int itemAvailabilityStatusId = itemCountAndStatusIdMap.get("itemAvailabilityStatusId");
        List<List<ItemEntity>> itemEntityChunkList = new ArrayList<>();
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setBarcode("3321545824554545");
        itemEntity.setItemId(1);
        itemEntity.setItemAvailabilityStatusId(2);
        List<ItemEntity> itemEntityList = Arrays.asList(itemEntity);
        itemEntityChunkList = Arrays.asList(itemEntityList);
        long from = 10 * Long.valueOf(batchSize);
        Date date = new Date();
        ItemStatusEntity itemStatusEntity = new ItemStatusEntity();
        itemStatusEntity.setItemStatusId(2);
        Mockito.when(gfaService.getGFAItemStatusCheckResponse(Mockito.any())).thenReturn(gfaItemStatusCheckResponse);
        Mockito.when(gfaService.getRequestItemDetailsRepository()).thenReturn(mockedRequestItemDetailsRepository);
        Mockito.when(gfaService.getItemDetailsRepository()).thenReturn(itemDetailsRepository);
        Mockito.when(gfaService.getItemStatusDetailsRepository()).thenReturn(itemStatusDetailsRepository);
        Mockito.when(gfaService.getItemChangeLogDetailsRepository()).thenReturn(itemChangeLogDetailsRepository);
        Mockito.when(statusReconciliationController.getGfaService()).thenReturn(gfaService);
        Mockito.when(statusReconciliationController.getFromDate(0)).thenReturn(from);
        Mockito.when(statusReconciliationController.getTotalPageCount(Arrays.asList(1,9), itemStatusEntity.getItemStatusId())).thenReturn(itemCountAndStatusIdMap);
        Mockito.when(statusReconciliationController.getItemDetailsRepository()).thenReturn(itemDetailsRepository);
        Mockito.when(statusReconciliationController.getBatchSize()).thenReturn(batchSize);
        Mockito.when(statusReconciliationController.getItemStatusDetailsRepository()).thenReturn(itemStatusDetailsRepository);
        Mockito.when(statusReconciliationController.getRequestItemStatusDetailsRepository()).thenReturn(requestItemStatusDetailsRepository);
        Mockito.when(statusReconciliationController.getStatusReconciliationDayLimit()).thenReturn(statusReconciliationDayLimit);
        Mockito.when(statusReconciliationController.getStatusReconciliationLasBarcodeLimit()).thenReturn(statusReconciliationLasBarcodeLimit);
        Mockito.when(statusReconciliationController.getProducer()).thenReturn(producer);
        Mockito.when(statusReconciliationController.getGfaService().itemStatusComparison(Mockito.any(),Mockito.any())).thenCallRealMethod();
        Mockito.when(statusReconciliationController.getItemDetailsRepository().getNotAvailableItems(statusReconciliationDayLimit,Arrays.asList(1,9),from,batchSize,itemStatusEntity.getItemStatusId())).thenReturn(itemEntityList);
        Mockito.when(statusReconciliationController.getTotalPageCount(Arrays.asList(1,9), itemStatusEntity.getItemStatusId())).thenCallRealMethod();
        Mockito.when(statusReconciliationController.getItemStatusDetailsRepository().findByStatusCode(ReCAPConstants.ITEM_STATUS_NOT_AVAILABLE)).thenReturn(itemStatusEntity);
        Mockito.when(statusReconciliationController.itemStatusReconciliation()).thenCallRealMethod();
        ResponseEntity responseEntity = statusReconciliationController.itemStatusReconciliation();
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getBody().toString(),"Success");
    }

    @Test
    public void testTtitem(){
        Ttitem ttitem = new Ttitem();
        ttitem.setItemBarcode("332445645758458");
        ttitem.setCustomerCode("AD");
        ttitem.setRequestId(1);
        ttitem.setRequestor("Test");
        ttitem.setRequestorFirstName("test");
        ttitem.setRequestorLastName("test");
        ttitem.setRequestorMiddleName("test");
        ttitem.setRequestorEmail("hemalatha.s@htcindia.com");
        ttitem.setRequestorOther("test");
        ttitem.setBiblioTitle("test");
        ttitem.setBiblioLocation("Discovery");
        ttitem.setBiblioAuthor("John");
        ttitem.setBiblioVolume("V1");
        ttitem.setBiblioCode("A1");
        ttitem.setArticleTitle("Title");
        ttitem.setArticleDate(new Date().toString());
        ttitem.setArticleAuthor("john");
        ttitem.setArticleIssue("Test");
        ttitem.setArticleVolume("V1");
        ttitem.setStartPage("1");
        ttitem.setEndPage("10");
        ttitem.setPages("9");
        ttitem.setOther("test");
        ttitem.setPriority("test");
        ttitem.setNotes("notes");
        ttitem.setRequestDate(new Date().toString());
        ttitem.setRequestTime("06:05:00");
        ttitem.setErrorCode("test");
        ttitem.setErrorNote("test");
        ttitem.setItemStatus("Available");
        ttitem.setDestination("Discovery");
        ttitem.setDeliveryMethod("test");

        RetrieveItem retrieveItem = new RetrieveItem();
        retrieveItem.setTtitem(Arrays.asList(ttitem));

        GFARetrieveItemResponse gfaRetrieveItemResponse = new GFARetrieveItemResponse();
        gfaRetrieveItemResponse.setScrenMessage("Success");
        gfaRetrieveItemResponse.setSuccess(true);
        gfaRetrieveItemResponse.setRetrieveItem(retrieveItem);

        assertNotNull(ttitem.getItemBarcode());
        assertNotNull(ttitem.getCustomerCode());
        assertNotNull(ttitem.getRequestor());
        assertNotNull(ttitem.getRequestorFirstName());
        assertNotNull(ttitem.getRequestorLastName());
        assertNotNull(ttitem.getRequestorMiddleName());
        assertNotNull(ttitem.getRequestorEmail());
        assertNotNull(ttitem.getRequestorOther());
        assertNotNull(ttitem.getBiblioTitle());
        assertNotNull(ttitem.getBiblioLocation());
        assertNotNull(ttitem.getBiblioAuthor());
        assertNotNull(ttitem.getBiblioVolume());
        assertNotNull(ttitem.getBiblioCode());
        assertNotNull(ttitem.getArticleTitle());
        assertNotNull(ttitem.getArticleAuthor());
        assertNotNull(ttitem.getArticleVolume());
        assertNotNull(ttitem.getArticleIssue());
        assertNotNull(ttitem.getArticleDate());
        assertNotNull(ttitem.getStartPage());
        assertNotNull(ttitem.getEndPage());
        assertNotNull(ttitem.getPages());
        assertNotNull(ttitem.getOther());
        assertNotNull(ttitem.getPriority());
        assertNotNull(ttitem.getNotes());
        assertNotNull(ttitem.getRequestDate());
        assertNotNull(ttitem.getRequestTime());
        assertNotNull(ttitem.getErrorCode());
        assertNotNull(ttitem.getErrorNote());
        assertNotNull(ttitem.getRequestId());
        assertNotNull(ttitem.getItemStatus());
        assertNotNull(ttitem.getDeliveryMethod());
        assertNotNull(ttitem.getDestination());
        assertNotNull(retrieveItem.getTtitem());
        assertNotNull(gfaRetrieveItemResponse.getRetrieveItem());
        assertNotNull(gfaRetrieveItemResponse.getScrenMessage());
        assertNotNull(gfaRetrieveItemResponse.isSuccess());
    }

}