package org.recap.request;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.recap.ReCAPConstants;
import org.recap.model.BulkRequestItem;
import org.recap.model.BulkRequestItemEntity;
import org.recap.model.ItemEntity;
import org.recap.repository.BulkRequestItemDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
import org.recap.util.ItemRequestServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;

/**
 * Created by rajeshbabuk on 10/10/17.
 */
@Component
public class BulkItemRequestService {

    private final Logger logger = LoggerFactory.getLogger(BulkItemRequestService.class);

    @Value("${bulk.request.item.count.limit}")
    private Integer bulkRequestItemCountLimit;

    @Autowired
    private BulkRequestItemDetailsRepository bulkRequestItemDetailsRepository;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private ItemRequestServiceUtil itemRequestServiceUtil;

    @Autowired
    private ProducerTemplate producerTemplate;

    /**
     * Bulk request items.
     *
     * @param bulkRequestId the bulk request id
     */
    public void bulkRequestItems(Integer bulkRequestId) {
        LinkedList<String> bulkRequestItemBarcodeList = new LinkedList<>();
        BulkRequestItemEntity bulkRequestItemEntity = bulkRequestItemDetailsRepository.findOne(bulkRequestId);
        String requestData = new String(bulkRequestItemEntity.getBulkRequestFileData());
        new BufferedReader(new StringReader(requestData)).lines().forEach(barcodeRow -> bulkRequestItemBarcodeList.add(barcodeRow.split(",")[0]));
        bulkRequestItemBarcodeList.remove(0);
        Integer bulkRequestItemBarcodeCount = bulkRequestItemBarcodeList.size();
        logger.info("Total number of barcodes received for bulk request with id {} is - {}", bulkRequestId, bulkRequestItemBarcodeCount);
        Set<String> nonDuplicateBarcodeList = removeDuplicates(bulkRequestItemBarcodeList);
        logger.info("Duplicate barcodes count : {}", bulkRequestItemBarcodeCount - nonDuplicateBarcodeList.size());
        bulkRequestItemBarcodeList.clear();
        bulkRequestItemBarcodeList.addAll(nonDuplicateBarcodeList);
        List<String> bulkRequestItemBarcodeLimitedList = new ArrayList<>();
        List<String> bulkRequestItemBarcodeExcessList = new ArrayList<>();
        if (bulkRequestItemBarcodeCount > bulkRequestItemCountLimit) {
            bulkRequestItemBarcodeLimitedList.addAll(bulkRequestItemBarcodeList.subList(0, bulkRequestItemCountLimit));
            bulkRequestItemBarcodeExcessList.addAll(bulkRequestItemBarcodeList.subList(bulkRequestItemCountLimit, nonDuplicateBarcodeList.size()));
        } else {
            bulkRequestItemBarcodeLimitedList.addAll(bulkRequestItemBarcodeList.subList(0, bulkRequestItemBarcodeList.size()));
            bulkRequestItemBarcodeList.clear();
        }
        List<BulkRequestItem> exceptionBulkRequestItems = new ArrayList<>();
        for (String itemBarcode : bulkRequestItemBarcodeLimitedList) {
            List<ItemEntity> itemEntities = itemDetailsRepository.findByBarcode(itemBarcode);
            if (CollectionUtils.isNotEmpty(itemEntities)) {
                ItemEntity itemEntity = itemEntities.get(0);
                if (itemEntity.getItemStatusEntity().getStatusCode().equalsIgnoreCase(ReCAPConstants.NOT_AVAILABLE)) {
                    exceptionBulkRequestItems.add(buildBulkRequestItem(itemBarcode, itemEntity.getCustomerCode(), ReCAPConstants.RETRIEVAL_NOT_FOR_UNAVAILABLE_ITEM));
                } else if (itemEntity.getOwningInstitutionId() != bulkRequestItemEntity.getRequestingInstitutionId()) {
                    exceptionBulkRequestItems.add(buildBulkRequestItem(itemBarcode, itemEntity.getCustomerCode(), "Item doesn't belong to the requesting institution."));
                } else {
                    producerTemplate.sendBodyAndHeader(ReCAPConstants.BULK_REQUEST_ITEM_PROCESSING_QUEUE, itemBarcode, ReCAPConstants.BULK_REQUEST_ID, bulkRequestId);
                }
            } else {
                exceptionBulkRequestItems.add(buildBulkRequestItem(itemBarcode, null, ReCAPConstants.WRONG_ITEM_BARCODE));
            }
        }
        for (String itemBarcode : bulkRequestItemBarcodeExcessList) {
            exceptionBulkRequestItems.add(buildBulkRequestItem(itemBarcode, null, "Item barcode ignored. Limit of bulk request exceeded."));
        }
        nonDuplicateBarcodeList.clear();
        bulkRequestItemBarcodeLimitedList.clear();
        bulkRequestItemBarcodeExcessList.clear();
        updateStatusToBarcodes(exceptionBulkRequestItems, bulkRequestId);
        producerTemplate.sendBodyAndHeader(ReCAPConstants.BULK_REQUEST_ITEM_PROCESSING_QUEUE, ReCAPConstants.COMPLETE, ReCAPConstants.BULK_REQUEST_ID, bulkRequestId);
    }

    /**
     * Updates process status to each barcode in csv format.
     * @param exceptionBulkRequestItems
     * @param bulkRequestId
     */
    private void updateStatusToBarcodes(List<BulkRequestItem> exceptionBulkRequestItems, Integer bulkRequestId) {
        BulkRequestItemEntity bulkRequestItemEntity = bulkRequestItemDetailsRepository.findOne(bulkRequestId);
        if (!ReCAPConstants.PROCESSED.equals(bulkRequestItemEntity.getBulkRequestStatus())) {
            StringBuilder csvFormatDataBuilder = new StringBuilder();
            csvFormatDataBuilder.append("BARCODE,CUSTOMER CODE,REQUEST ID,REQUEST STATUS,STATUS");
            itemRequestServiceUtil.buildCsvFormatData(exceptionBulkRequestItems, csvFormatDataBuilder);
            bulkRequestItemEntity.setBulkRequestFileData(csvFormatDataBuilder.toString().getBytes());
            bulkRequestItemDetailsRepository.save(bulkRequestItemEntity);
        } else {
            itemRequestServiceUtil.updateStatusToBarcodes(exceptionBulkRequestItems, bulkRequestItemEntity);
        }
    }

    /**
     * Removes duplicates from list.
     * @param bulkRequestItemBarcodeList
     * @return
     */
    private Set<String> removeDuplicates(List<String> bulkRequestItemBarcodeList) {
        return new HashSet<>(bulkRequestItemBarcodeList);
    }

    /**
     * Builds bulk request item object.
     * @param barcode
     * @param customerCode
     * @param status
     * @return
     */
    private BulkRequestItem buildBulkRequestItem(String barcode, String customerCode, String status) {
        BulkRequestItem bulkRequestItem = new BulkRequestItem();
        bulkRequestItem.setItemBarcode(barcode);
        bulkRequestItem.setCustomerCode(customerCode);
        bulkRequestItem.setStatus(status);
        return bulkRequestItem;
    }
}
