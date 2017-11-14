package org.recap.mqconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.recap.ReCAPConstants;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.ItemRequestInformation;
import org.recap.model.RequestInformation;
import org.recap.request.BulkItemRequestProcessService;
import org.recap.request.BulkItemRequestService;
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
    private BulkItemRequestService bulkItemRequestService;
    private BulkItemRequestProcessService bulkItemRequestProcessService;

    /**
     * Gets item request service.
     *
     * @return the item request service
     */
    public ItemRequestService getItemRequestService() {
        return itemRequestService;
    }

    /**
     * Gets item edd request service.
     *
     * @return the item edd request service
     */
    public ItemEDDRequestService getItemEDDRequestService() {
        return itemEDDRequestService;
    }

    /**
     * Gets bulk item request service.
     *
     * @return the bulk item request service
     */
    public BulkItemRequestService getBulkItemRequestService() {
        return bulkItemRequestService;
    }

    /**
     * Gets bulk item request processservice.
     *
     * @return the bulk item request process service
     */
    public BulkItemRequestProcessService getBulkItemRequestProcessService() {
        return bulkItemRequestProcessService;
    }

    /**
     * Gets bulk item request service.
     *
     * @return the bulk item request service
     */
    public RequestItemQueueConsumer(BulkItemRequestService bulkItemRequestService) {
        this.bulkItemRequestService = bulkItemRequestService;
    }

    /**
     * Gets bulk item request process service.
     *
     * @return the bulk item request process service
     */
    public RequestItemQueueConsumer(BulkItemRequestProcessService bulkItemRequestProcessService) {
        this.bulkItemRequestProcessService = bulkItemRequestProcessService;
    }

    /**
     * Instantiates a new Request item queue consumer.
     *
     * @param itemRequestService the item request service
     */
    public RequestItemQueueConsumer(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    /**
     * Instantiates a new Request item queue consumer.
     *
     * @param itemRequestService    the item request service
     * @param itemEDDRequestService the item edd request service
     */
    public RequestItemQueueConsumer(ItemRequestService itemRequestService, ItemEDDRequestService itemEDDRequestService) {
        this.itemRequestService = itemRequestService;
        this.itemEDDRequestService = itemEDDRequestService;
    }

    /**
     * Instantiates a new Request item queue consumer.
     *
     * @param itemEDDRequestService the item edd request service
     */
    public RequestItemQueueConsumer(ItemEDDRequestService itemEDDRequestService) {
        this.itemEDDRequestService = itemEDDRequestService;
    }

    /**
     * Get object mapper object mapper.
     *
     * @return the object mapper
     */
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    /**
     * Get logger logger.
     *
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }


    /**
     * Request item on message.
     *
     * @param body     the body
     * @param exchange the exchange
     * @throws IOException the io exception
     */
    public void requestItemOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = getObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        getLogger().info("Item Barcode Recevied for Processing Request -> " + itemRequestInformation.getItemBarcodes().get(0));
        getItemRequestService().requestItem(itemRequestInformation, exchange);
    }

    /**
     * Request item edd on message.
     *
     * @param body     the body
     * @param exchange the exchange
     * @throws IOException the io exception
     */
    public void requestItemEDDOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = getObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        getLogger().info("Item Barcode Recevied for Processing EDD -> " + itemRequestInformation.getItemBarcodes().get(0));
        getItemEDDRequestService().eddRequestItem(itemRequestInformation, exchange);
    }

    /**
     * Bulk Request item on message.
     *
     * @param body     the body
     * @param exchange the exchange
     * @throws IOException the io exception
     */
    public void bulkRequestItemOnMessage(@Body String body, Exchange exchange) throws IOException {
        Integer bulkRequestId = Integer.valueOf(body);
        getLogger().info("Bulk item request received for bulk request id -> {}", bulkRequestId);
        getBulkItemRequestService().bulkRequestItems(bulkRequestId);
    }

    /**
     * Bulk Request item process on message.
     *
     * @param body     the body
     * @param exchange the exchange
     * @throws IOException the io exception
     */
    public void bulkRequestProcessItemOnMessage(@Body String body, Exchange exchange) throws IOException {
        Integer bulkRequestId = (Integer) exchange.getIn().getHeaders().get(ReCAPConstants.BULK_REQUEST_ID);
        getLogger().info("Bulk item request barcode received for bulk request id -> {} is -> {}", bulkRequestId, body);
        getBulkItemRequestProcessService().processBulkRequestItem(body, bulkRequestId);
    }

    public void requestItemLasStatusCheckOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = new ObjectMapper();

        RequestInformation requestInformation = null;
        try {
            logger.info(body);
            requestInformation = om.readValue(body, RequestInformation.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (requestInformation != null) {
            getItemRequestService().executeLasitemCheck(requestInformation.getItemRequestInfo(), requestInformation.getItemResponseInformation());
        }
        getLogger().info("End On Message");
    }

    /**
     * Request item borrow direct on message.
     *
     * @param body     the body
     * @param exchange the exchange
     * @throws IOException the io exception
     */
    public void requestItemBorrowDirectOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = getObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        getLogger().info("Item Barcode Recevied for Processing Borrow Direct -> " + itemRequestInformation.getItemBarcodes().get(0));
        getItemRequestService().requestItem(itemRequestInformation, exchange);
    }

    /**
     * Request item recall on message.
     *
     * @param body     the body
     * @param exchange the exchange
     * @throws IOException the io exception
     */
    public void requestItemRecallOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = getObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        getLogger().info("Item Barcode Recevied for Processing Recall -> " + itemRequestInformation.getItemBarcodes().get(0));
        getItemRequestService().recallItem(itemRequestInformation, exchange);
    }

    /**
     * Pul request topic on message.
     *
     * @param body the body
     */
    public void pulRequestTopicOnMessage(@Body String body) {
        getLogger().info("PUL Request Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_REQUEST_TOPIC);
    }

    /**
     * Pul edd topic on message.
     *
     * @param body the body
     */
    public void pulEDDTopicOnMessage(@Body String body) {
        getLogger().info("PUL EDD Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_EDD_TOPIC);
    }

    /**
     * Pul recal topic on message.
     *
     * @param body the body
     */
    public void pulRecalTopicOnMessage(@Body String body) {
        getLogger().info("PUL Recall Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_RECALL_TOPIC);
    }

    /**
     * Pul borrow direct topic on message.
     *
     * @param body the body
     */
    public void pulBorrowDirectTopicOnMessage(@Body String body) {
        getLogger().info("PUL BorrowDirect Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_BORROW_DIRECT_TOPIC);
    }

    /**
     * Cul request topic on message.
     *
     * @param body the body
     */
    public void culRequestTopicOnMessage(@Body String body) {
        getLogger().info("CUL Request Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_REQUEST_TOPIC);
    }

    /**
     * Cul edd topic on message.
     *
     * @param body the body
     */
    public void culEDDTopicOnMessage(@Body String body) {
        getLogger().info("CUL EDD Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_EDD_TOPIC);
    }

    /**
     * Cul recal topic on message.
     *
     * @param body the body
     */
    public void culRecalTopicOnMessage(@Body String body) {
        getLogger().info("CUL Recall Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_RECALL_TOPIC);
    }

    /**
     * Cul borrow direct topic on message.
     *
     * @param body the body
     */
    public void culBorrowDirectTopicOnMessage(@Body String body) {
        getLogger().info("CUL Borrow Direct Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_BORROW_DIRECT_TOPIC);
    }

    /**
     * Nypl request topic on message.
     *
     * @param body the body
     */
    public void nyplRequestTopicOnMessage(@Body String body) {
        getLogger().info("NYPL Request Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_REQUEST_TOPIC);
    }

    /**
     * Nypl edd topic on message.
     *
     * @param body the body
     */
    public void nyplEDDTopicOnMessage(@Body String body) {
        getLogger().info("NYPL EDD Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_EDD_TOPIC);
    }

    /**
     * Nypl recal topic on message.
     *
     * @param body the body
     */
    public void nyplRecalTopicOnMessage(@Body String body) {
        getLogger().info("NYPL Recall Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_RECALL_TOPIC);
    }

    /**
     * Nypl borrow direct topic on message.
     *
     * @param body the body
     */
    public void nyplBorrowDirectTopicOnMessage(@Body String body) {
        getLogger().info("NYPL Borrow Direct Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_BORROW_DIRECT_TOPIC);
    }

    /**
     * Las outgoing q on completion.
     *
     * @param body the body
     */
    public void lasOutgoingQOnCompletion(@Body String body) {
        getLogger().info(body);
    }

    /**
     * Las ingoing q on completion.
     *
     * @param body the body
     */
    public void lasIngoingQOnCompletion(@Body String body) {
        getLogger().info(body);
    }

    /**
     * Las response retrival on message.
     *
     * @param body the body
     */
    public void lasResponseRetrivalOnMessage(@Body String body) {
        getLogger().info(body);
        getItemRequestService().processLASRetrieveResponse(body);
    }

    /**
     * Las response edd on message.
     *
     * @param body the body
     */
    public void lasResponseEDDOnMessage(@Body String body) {
        getLogger().info(body);
        getItemRequestService().processLASEddRetrieveResponse(body);
    }

    /**
     * Las response pwi on message.
     *
     * @param body the body
     */
    public void lasResponsePWIOnMessage(@Body String body) {
        getLogger().info(body);
    }

    /**
     * Las response pwd on message.
     *
     * @param body the body
     */
    public void lasResponsePWDOnMessage(@Body String body) {
        getLogger().info(body);
    }

    private void setTopicMessageToDb(String body, String operationType) {
        if (!getItemRequestService().isUseQueueLasCall()) {
            ObjectMapper om = new ObjectMapper();
            ItemInformationResponse itemInformationResponse = null;
            try {
                itemInformationResponse = om.readValue(body, ItemInformationResponse.class);
                getItemRequestService().updateChangesToDb(itemInformationResponse, operationType);
            } catch (Exception e) {
                logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
            }
        }
    }
}
