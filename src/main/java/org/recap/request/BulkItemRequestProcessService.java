package org.recap.request;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.controller.RequestItemController;
import org.recap.ils.model.response.ItemCheckoutResponse;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.*;
import org.recap.repository.BulkRequestItemDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
import org.recap.util.ItemRequestServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rajeshbabuk on 10/10/17.
 */
@Component
public class BulkItemRequestProcessService {

    private final Logger logger = LoggerFactory.getLogger(BulkItemRequestProcessService.class);

    @Autowired
    private BulkRequestItemDetailsRepository bulkRequestItemDetailsRepository;

    @Autowired
    private ItemRequestDBService itemRequestDBService;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private RequestItemController requestItemController;

    @Autowired
    private ItemRequestServiceUtil itemRequestServiceUtil;

    @Autowired
    private GFAService gfaService;

    @Autowired
    private EmailService emailService;

    public void processBulkRequestItem(String itemBarcode, Integer bulkRequestId) {
        BulkRequestItemEntity bulkRequestItemEntity = bulkRequestItemDetailsRepository.findOne(bulkRequestId);
        if (ReCAPConstants.COMPLETE.equals(itemBarcode)) {
            logger.info("Bulk request processing completed for bulk request id : {}", bulkRequestId);
            bulkRequestItemEntity = bulkRequestItemDetailsRepository.findOne(bulkRequestId);
            bulkRequestItemEntity.setBulkRequestStatus(ReCAPConstants.PROCESSED);
            BulkRequestItemEntity savedBulkRequestItemEntity = bulkRequestItemDetailsRepository.save(bulkRequestItemEntity);
            List<RequestItemEntity> requestItemEntities = bulkRequestItemEntity.getRequestItemEntities();
            if (CollectionUtils.isNotEmpty(requestItemEntities)) {
                List<BulkRequestItem> bulkRequestItems = new ArrayList<>();
                for (RequestItemEntity requestItemEntity : requestItemEntities) {
                    BulkRequestItem bulkRequestItem = new BulkRequestItem();
                    bulkRequestItem.setItemBarcode(requestItemEntity.getItemEntity().getBarcode());
                    bulkRequestItem.setCustomerCode(requestItemEntity.getItemEntity().getCustomerCode());
                    if (requestItemEntity.getRequestStatusEntity().getRequestStatusCode().equals(ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED)) {
                        bulkRequestItem.setStatus(ReCAPConstants.SUCCESS);
                    } else {
                        bulkRequestItem.setStatus(StringUtils.substringAfter(requestItemEntity.getNotes(), "Exception : "));
                    }
                    bulkRequestItems.add(bulkRequestItem);
                }
                itemRequestServiceUtil.updateStatusToBarcodes(bulkRequestItems, savedBulkRequestItemEntity);
            }
            itemRequestServiceUtil.generateReportAndSendEmail(bulkRequestId);
        } else {
            processBulkRequestForBarcode(itemBarcode, bulkRequestItemEntity);
        }
    }

    private void processBulkRequestForBarcode(String itemBarcode, BulkRequestItemEntity bulkRequestItemEntity) {
        try {
            List<ItemEntity> itemEntities = itemDetailsRepository.findByBarcode(itemBarcode);
            ItemEntity itemEntity = itemEntities.get(0);
            ItemRequestInformation itemRequestInformation = buildItemRequestInformation(bulkRequestItemEntity);
            itemRequestDBService.updateItemAvailabilutyStatus(itemEntities, bulkRequestItemEntity.getCreatedBy());
            Integer requestId = itemRequestDBService.updateRecapRequestItem(itemRequestInformation, itemEntity, ReCAPConstants.REQUEST_STATUS_PROCESSING, bulkRequestItemEntity);
            itemRequestInformation.setRequestId(requestId);
            itemRequestInformation.setItemBarcodes(Arrays.asList(itemEntity.getBarcode()));
            ItemCheckoutResponse itemCheckoutResponse = (ItemCheckoutResponse) requestItemController.checkoutItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
            itemCheckoutResponse.setSuccess(true);
            if (itemCheckoutResponse.isSuccess()) {
                if (gfaService.isUseQueueLasCall()) {
                    itemRequestDBService.updateRecapRequestItem(itemRequestInformation, itemEntity, ReCAPConstants.REQUEST_STATUS_PENDING, bulkRequestItemEntity);
                }
                ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
                itemInformationResponse.setRequestId(requestId);
                itemInformationResponse = gfaService.executeRetriveOrder(itemRequestInformation, itemInformationResponse);
                if (itemInformationResponse.isSuccess()) {
                    itemInformationResponse.setScreenMessage(ReCAPConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM);
                    itemRequestInformation.setRequestNotes(itemRequestInformation.getRequestNotes() + "\n" + "Bulk Request Id : " + bulkRequestItemEntity.getBulkRequestId());
                    itemRequestDBService.updateRecapRequestItem(itemRequestInformation, itemEntity, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED, bulkRequestItemEntity);
                } else {
                    requestItemController.checkinItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
                    itemRequestDBService.rollbackUpdateItemAvailabilutyStatus(itemEntity, bulkRequestItemEntity.getCreatedBy());
                    itemRequestInformation.setRequestNotes(ReCAPConstants.USER + ":" + itemRequestInformation.getRequestNotes() + "\n" + "Bulk Request Id : " + bulkRequestItemEntity.getBulkRequestId() + "\n" + itemInformationResponse.getScreenMessage());
                    itemRequestDBService.updateRecapRequestItem(itemRequestInformation, itemEntity, ReCAPConstants.REQUEST_STATUS_EXCEPTION, bulkRequestItemEntity);
                }
            } else {
                itemRequestDBService.rollbackUpdateItemAvailabilutyStatus(itemEntity, bulkRequestItemEntity.getCreatedBy());
                itemRequestInformation.setRequestNotes(ReCAPConstants.USER + ":" + itemRequestInformation.getRequestNotes() + "\n" + "Bulk Request Id : " + bulkRequestItemEntity.getBulkRequestId() + "\n" + ReCAPConstants.REQUEST_ILS_EXCEPTION + itemCheckoutResponse.getScreenMessage());
                itemRequestDBService.updateRecapRequestItem(itemRequestInformation, itemEntity, ReCAPConstants.REQUEST_STATUS_EXCEPTION, bulkRequestItemEntity);
            }
            itemRequestServiceUtil.updateSolrIndex(itemEntity);
            logger.info("Request processing completed for barcode : {}", itemBarcode);
        } catch (Exception ex) {
            logger.error(ReCAPConstants.LOG_ERROR, itemBarcode);
        }
    }

    private ItemRequestInformation buildItemRequestInformation(BulkRequestItemEntity bulkRequestItemEntity) {
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setRequestType(ReCAPConstants.REQUEST_TYPE_RETRIEVAL);
        itemRequestInformation.setRequestingInstitution(bulkRequestItemEntity.getInstitutionEntity().getInstitutionCode());
        itemRequestInformation.setPatronBarcode(bulkRequestItemEntity.getPatronId());
        itemRequestInformation.setDeliveryLocation(bulkRequestItemEntity.getStopCode());
        itemRequestInformation.setEmailAddress(bulkRequestItemEntity.getEmailId());
        itemRequestInformation.setRequestNotes(bulkRequestItemEntity.getNotes());
        itemRequestInformation.setUsername(bulkRequestItemEntity.getCreatedBy() + "(Bulk)");
        return itemRequestInformation;
    }
}
