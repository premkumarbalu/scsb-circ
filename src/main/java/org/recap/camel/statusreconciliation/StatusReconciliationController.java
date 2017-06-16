package org.recap.camel.statusreconciliation;

import com.google.common.collect.Lists;
import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.model.ItemEntity;
import org.recap.model.ItemStatusEntity;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.ItemStatusDetailsRepository;
import org.recap.request.GFAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Created by hemalathas on 1/6/17.
 */
@RestController
@RequestMapping("/statusReconciliation")
public class StatusReconciliationController {

    private static final Logger logger = LoggerFactory.getLogger(StatusReconciliationController.class);

    @Autowired
    GFAService gfaService;

    @Value("${external.status.reconciliation.batch.size}")
    private Integer batchSize;

    @Value("${external.status.reconciliation.day.limit}")
    private Integer statusReconciliationDayLimit;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Value("${external.status.reconciliation.las.barcode.limit}")
    private Integer statusReconciliationLasBarcodeLimit;

    @Autowired
    private ProducerTemplate producer;

    public static Logger getLogger() {
        return logger;
    }

    public GFAService getGfaService() {
        return gfaService;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public Integer getStatusReconciliationDayLimit() {
        return statusReconciliationDayLimit;
    }

    public ItemStatusDetailsRepository getItemStatusDetailsRepository() {
        return itemStatusDetailsRepository;
    }

    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    public Integer getStatusReconciliationLasBarcodeLimit() {
        return statusReconciliationLasBarcodeLimit;
    }

    public ProducerTemplate getProducer() {
        return producer;
    }

    public Long getFromDate(int pageNum){
        return pageNum * Long.valueOf(getBatchSize());
    }

    public Date getCurrentDate(){
        return new Date();
    }

    @RequestMapping(value = "/itemStatusReconciliation", method = RequestMethod.GET)
    public ResponseEntity itemStatusReconciliation(){
        Map<String,Integer> itemCountAndStatusIdMap = getTotalPageCount();
        int totalPagesCount = itemCountAndStatusIdMap.get("totalPagesCount");
        getLogger().info("Total page count :"+totalPagesCount);
        int itemAvailabilityStatusId = itemCountAndStatusIdMap.get("itemAvailabilityStatusId");
        List<StatusReconciliationCSVRecord> statusReconciliationCSVRecordList = new ArrayList<>();
        List<StatusReconciliationCSVRecord> statusReconciliationCSVRecordList1 = new ArrayList<>();
        for (int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) { // 1000
            long from = getFromDate(pageNum);
            Date date = getCurrentDate();
            List<ItemEntity> itemEntityList = getItemDetailsRepository().getNotAvailableItems(itemAvailabilityStatusId, date, getStatusReconciliationDayLimit(), from, getBatchSize());
            List<List<ItemEntity>> itemEntityChunkList = Lists.partition(itemEntityList, getStatusReconciliationDayLimit());
            statusReconciliationCSVRecordList = getGfaService().itemStatusComparison(itemEntityChunkList);
            statusReconciliationCSVRecordList1.addAll(statusReconciliationCSVRecordList);
        }
        getProducer().sendBodyAndHeader(ReCAPConstants.STATUS_RECONCILIATION_REPORT, statusReconciliationCSVRecordList1, ReCAPConstants.REPORT_FILE_NAME, "status_reconciliation");
        return new ResponseEntity("Success", HttpStatus.OK);
    }

    public Map<String,Integer> getTotalPageCount(){
        Map<String,Integer> itemCountAndStatusIdMap = new HashMap<>();
        Integer itemAvailabilityStatusId = 0;
        ItemStatusEntity itemStatusEntity = getItemStatusDetailsRepository().findByStatusCode(ReCAPConstants.NOT_AVAILABLE);
        if(itemStatusEntity != null){
            itemAvailabilityStatusId = itemStatusEntity.getItemStatusId();
        }
        long itemCount = getItemDetailsRepository().getNotAvailableItemsCount(itemAvailabilityStatusId, new Date(), getStatusReconciliationDayLimit());
        getLogger().info("Total Records :" + itemCount);
        int totalPagesCount = (int) (itemCount / getBatchSize());
        itemCountAndStatusIdMap.put("itemAvailabilityStatusId",itemAvailabilityStatusId);
        itemCountAndStatusIdMap.put("totalPagesCount",totalPagesCount);
        return itemCountAndStatusIdMap;
    }


}
