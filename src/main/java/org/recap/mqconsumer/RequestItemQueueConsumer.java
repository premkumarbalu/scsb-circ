package org.recap.mqconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.recap.ReCAPConstants;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.ItemRequestInformation;
import org.recap.request.ItemEDDRequestService;
import org.recap.request.ItemRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by sudhishk on 29/11/16.
 */
public class RequestItemQueueConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RequestItemQueueConsumer.class);

    private ItemRequestService itemRequestService;
    private ItemEDDRequestService itemEDDRequestService;

    public ItemRequestService getItemRequestService() {
        return itemRequestService;
    }

    public ItemEDDRequestService getItemEDDRequestService() {
        return itemEDDRequestService;
    }

    public RequestItemQueueConsumer(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    public RequestItemQueueConsumer(ItemRequestService itemRequestService, ItemEDDRequestService itemEDDRequestService) {
        this.itemRequestService = itemRequestService;
        this.itemEDDRequestService = itemEDDRequestService;
    }

    public RequestItemQueueConsumer(ItemEDDRequestService itemEDDRequestService) {
        this.itemEDDRequestService = itemEDDRequestService;
    }

    public ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }

    public Logger getLogger(){
        return logger;
    }


    public void requestItemOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = getObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        getLogger().info("Item Barcode Recevied for Processing Request -> " + itemRequestInformation.getItemBarcodes().get(0));
        getItemRequestService().requestItem(itemRequestInformation, exchange);
    }

    public void requestItemEDDOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = getObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        getLogger().info("Item Barcode Recevied for Processing EDD -> " + itemRequestInformation.getItemBarcodes().get(0));
        getItemEDDRequestService().eddRequestItem(itemRequestInformation, exchange);
    }

    public void requestItemBorrowDirectOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = getObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        getLogger().info("Item Barcode Recevied for Processing Borrow Direct -> " + itemRequestInformation.getItemBarcodes().get(0));
        getItemRequestService().requestItem(itemRequestInformation, exchange);
    }

    public void requestItemRecallOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = getObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        getLogger().info("Item Barcode Recevied for Processing Recall -> " + itemRequestInformation.getItemBarcodes().get(0));
        getItemRequestService().recallItem(itemRequestInformation, exchange);
    }

    public void pulRequestTopicOnMessage(@Body String body) {
        getLogger().info("PUL Request Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_REQUEST_TOPIC);
    }

    public void pulEDDTopicOnMessage(@Body String body) {
        getLogger().info("PUL EDD Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_EDD_TOPIC);
    }

    public void pulRecalTopicOnMessage(@Body String body) {
        getLogger().info("PUL Recall Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_RECALL_TOPIC);
    }

    public void pulBorrowDirectTopicOnMessage(@Body String body) {
        getLogger().info("PUL BorrowDirect Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_BORROW_DIRECT_TOPIC);
    }

    public void culRequestTopicOnMessage(@Body String body) {
        getLogger().info("CUL Request Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_REQUEST_TOPIC);
    }

    public void culEDDTopicOnMessage(@Body String body) {
        getLogger().info("CUL EDD Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_EDD_TOPIC);
    }

    public void culRecalTopicOnMessage(@Body String body) {
        getLogger().info("CUL Recall Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_RECALL_TOPIC);
    }

    public void culBorrowDirectTopicOnMessage(@Body String body) {
        getLogger().info("CUL Borrow Direct Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_BORROW_DIRECT_TOPIC);
    }

    public void nyplRequestTopicOnMessage(@Body String body) {
        getLogger().info("NYPL Request Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_REQUEST_TOPIC);
    }

    public void nyplEDDTopicOnMessage(@Body String body) {
        getLogger().info("NYPL EDD Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_EDD_TOPIC);
    }

    public void nyplRecalTopicOnMessage(@Body String body) {
        getLogger().info("NYPL Recall Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_RECALL_TOPIC);
    }

    public void nyplBorrowDirectTopicOnMessage(@Body String body) {
        getLogger().info("NYPL Borrow Direct Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_BORROW_DIRECT_TOPIC);
    }

    public void lasOutgoingQOnCompletion(@Body String body) {
        getLogger().info(body);
    }

    public void lasIngoingQOnCompletion(@Body String body) {
        getLogger().info(body);
    }

    public void lasResponseRetrivalOnMessage(@Body String body) {
        getLogger().info(body);
        getItemRequestService().processLASRetrieveResponse(body);
    }

    public void lasResponseEDDOnMessage(@Body String body) {
        getLogger().info(body);
    }

    public void lasResponsePWIOnMessage(@Body String body) {
        getLogger().info(body);
    }

    public void lasResponsePWDOnMessage(@Body String body) {
        getLogger().info(body);
    }

    private void setTopicMessageToDb(String body, String operationType) {
        ObjectMapper om = new ObjectMapper();
        ItemInformationResponse itemInformationResponse = null;
        try {
            itemInformationResponse = om.readValue(body, ItemInformationResponse.class);
            Integer intRecordId = 0;
            if (itemInformationResponse.getRequestId() != null && itemInformationResponse.getRequestId() > 0) {
                intRecordId = itemInformationResponse.getRequestId();
            }
            itemRequestService.saveItemChangeLogEntity(intRecordId, itemRequestService.getUser(itemInformationResponse.getUsername()), operationType, body);
            if (!itemInformationResponse.isSuccess()) {
                itemRequestService.updateRecapRequestItem(itemInformationResponse);
            }
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
    }
}
