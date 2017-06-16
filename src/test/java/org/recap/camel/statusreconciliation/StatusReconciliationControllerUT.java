package org.recap.camel.statusreconciliation;

import com.google.common.collect.Lists;
import org.apache.camel.ProducerTemplate;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCase;
import org.recap.gfa.model.Dsitem;
import org.recap.gfa.model.GFAItemStatusCheckResponse;
import org.recap.gfa.model.Ttitem;
import org.recap.model.ItemEntity;
import org.recap.repository.ItemChangeLogDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.ItemStatusDetailsRepository;
import org.recap.repository.RequestItemDetailsRepository;
import org.recap.request.GFAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 2/6/17.
 */
public class StatusReconciliationControllerUT extends BaseTestCase{

    @Mock
    private StatusReconciliationController statusReconciliationController;

    private static final Logger logger = LoggerFactory.getLogger(StatusReconciliationController.class);

    @Mock
    GFAService gfaService;

    @Value("${external.status.reconciliation.batch.size}")
    private Integer batchSize;

    @Value("${external.status.reconciliation.day.limit}")
    private Integer statusReconciliationDayLimit;

    @Mock
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Mock
    private ItemDetailsRepository itemDetailsRepository;

    @Value("${external.status.reconciliation.las.barcode.limit}")
    private Integer statusReconciliationLasBarcodeLimit;

    @Mock
    private ProducerTemplate producer;

    @Mock
    private RequestItemDetailsRepository mockedRequestItemDetailsRepository;

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
        Mockito.when(gfaService.getGFAItemStatusCheckResponse(Mockito.any())).thenReturn(gfaItemStatusCheckResponse);
        Mockito.when(gfaService.getRequestItemDetailsRepository()).thenReturn(mockedRequestItemDetailsRepository);
        Mockito.when(gfaService.getItemDetailsRepository()).thenReturn(itemDetailsRepository);
        Mockito.when(gfaService.getItemStatusDetailsRepository()).thenReturn(itemStatusDetailsRepository);
        Mockito.when(gfaService.getItemChangeLogDetailsRepository()).thenReturn(itemChangeLogDetailsRepository);
        Mockito.when(statusReconciliationController.getGfaService()).thenReturn(gfaService);
        Mockito.when(statusReconciliationController.getFromDate(0)).thenReturn(from);
        Mockito.when(statusReconciliationController.getCurrentDate()).thenReturn(date);
        Mockito.when(statusReconciliationController.getTotalPageCount()).thenReturn(itemCountAndStatusIdMap);
        Mockito.when(statusReconciliationController.getItemDetailsRepository()).thenReturn(itemDetailsRepository);
        Mockito.when(statusReconciliationController.getBatchSize()).thenReturn(batchSize);
        Mockito.when(statusReconciliationController.getItemStatusDetailsRepository()).thenReturn(itemStatusDetailsRepository);
        Mockito.when(statusReconciliationController.getStatusReconciliationDayLimit()).thenReturn(statusReconciliationDayLimit);
        Mockito.when(statusReconciliationController.getStatusReconciliationLasBarcodeLimit()).thenReturn(statusReconciliationLasBarcodeLimit);
        Mockito.when(statusReconciliationController.getProducer()).thenReturn(producer);
        Mockito.when(statusReconciliationController.getGfaService().itemStatusComparison(Mockito.any())).thenCallRealMethod();
        Mockito.when(statusReconciliationController.getItemDetailsRepository().getNotAvailableItems(itemAvailabilityStatusId, date, statusReconciliationDayLimit, from,batchSize)).thenReturn(itemEntityList);
        Mockito.when(statusReconciliationController.getTotalPageCount()).thenCallRealMethod();
        Mockito.when(statusReconciliationController.itemStatusReconciliation()).thenCallRealMethod();
        ResponseEntity responseEntity = statusReconciliationController.itemStatusReconciliation();
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getBody().toString(),"Success");
    }

}