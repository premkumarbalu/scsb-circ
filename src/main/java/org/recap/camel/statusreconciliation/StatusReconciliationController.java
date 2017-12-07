package org.recap.camel.statusreconciliation;

import com.google.common.collect.Lists;
import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.model.ItemEntity;
import org.recap.model.ItemStatusEntity;
import org.recap.model.RequestStatusEntity;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.ItemStatusDetailsRepository;
import org.recap.repository.RequestItemStatusDetailsRepository;
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
import java.util.stream.Collectors;

/**
 * Created by hemalathas on 1/6/17.
 */
@RestController
@RequestMapping("/statusReconciliation")
public class StatusReconciliationController {

    private static final Logger logger = LoggerFactory.getLogger(StatusReconciliationController.class);

    @Autowired
    private GFAService gfaService;

    @Value("${status.reconciliation.batch.size}")
    private Integer batchSize;

    @Value("${status.reconciliation.day.limit}")
    private Integer statusReconciliationDayLimit;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Value("${status.reconciliation.las.barcode.limit}")
    private Integer statusReconciliationLasBarcodeLimit;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    private RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    /**
     * Gets logger.
     *
     * @return the logger
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Gets gfa service.
     *
     * @return the gfa service
     */
    public GFAService getGfaService() {
        return gfaService;
    }

    /**
     * Gets batch size.
     *
     * @return the batch size
     */
    public Integer getBatchSize() {
        return batchSize;
    }

    /**
     * Gets status reconciliation day limit.
     *
     * @return the status reconciliation day limit
     */
    public Integer getStatusReconciliationDayLimit() {
        return statusReconciliationDayLimit;
    }

    /**
     * Gets item status details repository.
     *
     * @return the item status details repository
     */
    public ItemStatusDetailsRepository getItemStatusDetailsRepository() {
        return itemStatusDetailsRepository;
    }

    /**
     * Gets item details repository.
     *
     * @return the item details repository
     */
    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }


    /**
     * Get request item status details repository request item status details repository.
     *
     * @return the request item status details repository
     */
    public RequestItemStatusDetailsRepository getRequestItemStatusDetailsRepository(){
        return requestItemStatusDetailsRepository ;
    }

    /**
     * Gets status reconciliation las barcode limit.
     *
     * @return the status reconciliation las barcode limit
     */
    public Integer getStatusReconciliationLasBarcodeLimit() {
        return statusReconciliationLasBarcodeLimit;
    }

    /**
     * Gets producer.
     *
     * @return the producer
     */
    public ProducerTemplate getProducer() {
        return producer;
    }

    /**
     * Get from date long.
     *
     * @param pageNum the page num
     * @return the long
     */
    public Long getFromDate(int pageNum){
        return pageNum * Long.valueOf(getBatchSize());
    }

    /**
     * Prepare the item entites for the status reconciliation and
     * placing the status reconciliation csv records in the scsb active-mq.
     *
     * @return the response entity
     */
    @RequestMapping(value = "/itemStatusReconciliation", method = RequestMethod.GET)
    public ResponseEntity itemStatusReconciliation(){
        ItemStatusEntity itemStatusEntity = getItemStatusDetailsRepository().findByStatusCode(ReCAPConstants.ITEM_STATUS_NOT_AVAILABLE);
        List<String> requestStatusCodes = Arrays.asList(ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED, ReCAPConstants.REQUEST_STATUS_EDD, ReCAPConstants.REQUEST_STATUS_CANCELED, ReCAPConstants.REQUEST_STATUS_INITIAL_LOAD);
        List<RequestStatusEntity> requestStatusEntityList = getRequestItemStatusDetailsRepository().findByRequestStatusCodeIn(requestStatusCodes);
        List<Integer> requestStatusIds = requestStatusEntityList.stream().map(RequestStatusEntity::getRequestStatusId).collect(Collectors.toList());
        logger.info("status reconciliation request ids : {} ",requestStatusIds);
        Map<String,Integer> itemCountAndStatusIdMap = getTotalPageCount(requestStatusIds,itemStatusEntity.getItemStatusId());
        if (itemCountAndStatusIdMap.size() > 0){
            int totalPagesCount = itemCountAndStatusIdMap.get("totalPagesCount");
            getLogger().info("status reconciliation total page count :{}",totalPagesCount);
            List<StatusReconciliationCSVRecord> statusReconciliationCSVRecordList = new ArrayList<>();
            List<StatusReconciliationCSVRecord> statusReconciliationCSVRecordList1 = new ArrayList<>();
            List<StatusReconciliationErrorCSVRecord> statusReconciliationErrorCSVRecords = new ArrayList<>();
            for (int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) {
                long from = getFromDate(pageNum);
                List<ItemEntity> itemEntityList = getItemDetailsRepository().getNotAvailableItems(getStatusReconciliationDayLimit(),requestStatusIds,from, getBatchSize(),itemStatusEntity.getItemStatusId());
                logger.info("items fetched from data base ----->{}",itemEntityList.size());
                List<List<ItemEntity>> itemEntityChunkList = Lists.partition(itemEntityList, getStatusReconciliationLasBarcodeLimit());
                statusReconciliationCSVRecordList = getGfaService().itemStatusComparison(itemEntityChunkList,statusReconciliationErrorCSVRecords);
                statusReconciliationCSVRecordList1.addAll(statusReconciliationCSVRecordList);
                getLogger().info("status reconciliation page num:{} and records {} processed",pageNum,from+getBatchSize());
            }
            getProducer().sendBodyAndHeader(ReCAPConstants.STATUS_RECONCILIATION_REPORT, statusReconciliationCSVRecordList1, ReCAPConstants.FOR,ReCAPConstants.STATUS_RECONCILIATION);
            getProducer().sendBodyAndHeader(ReCAPConstants.STATUS_RECONCILIATION_REPORT,statusReconciliationErrorCSVRecords,ReCAPConstants.FOR,ReCAPConstants.STATUS_RECONCILIATION_FAILURE);
        }
        return new ResponseEntity("Success", HttpStatus.OK);
    }

    /**
     * Get total page count for the status reconciliation.
     *
     * @return the map
     * @param requestStatusIds
     * @param itemStatusId
     */
    public Map<String,Integer> getTotalPageCount(List<Integer> requestStatusIds, Integer itemStatusId){
        Map<String,Integer> itemCountAndStatusIdMap = new HashMap<>();
        long itemCount = getItemDetailsRepository().getNotAvailableItemsCount(getStatusReconciliationDayLimit(),requestStatusIds,itemStatusId);
        getLogger().info("status reconciliation total item records count :{}" ,itemCount);
        if (itemCount > 0){
            int totalPagesCount = (int) (itemCount / getBatchSize());
            itemCountAndStatusIdMap.put("totalPagesCount",totalPagesCount);
        }
        return itemCountAndStatusIdMap;
    }
}
