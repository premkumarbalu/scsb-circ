package org.recap.mqconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.jboss.logging.Logger;
import org.recap.ReCAPConstants;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.ItemRequestInformation;
import org.recap.request.ItemEDDRequestService;
import org.recap.request.ItemRequestService;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by sudhishk on 29/11/16.
 */
public class RequestItemQueueConsumer {

    private Logger logger = Logger.getLogger(RequestItemQueueConsumer.class);

    private ItemRequestService itemRequestService;
    private ItemEDDRequestService itemEDDRequestService;

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

    public void requestItemOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = new ObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        logger.info("Item Barcode Recevied for Processing Request -> " + itemRequestInformation.getItemBarcodes().get(0));
        itemRequestService.requestItem(itemRequestInformation, exchange);
    }

    public void requestItemEDDOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = new ObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        logger.info("Item Barcode Recevied for Processing EDD -> " + itemRequestInformation.getItemBarcodes().get(0));
        itemEDDRequestService.eddRequestItem(itemRequestInformation, exchange);
    }

    public void requestItemBorrowDirectOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = new ObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        logger.info("Item Barcode Recevied for Processing Borrow Direct -> " + itemRequestInformation.getItemBarcodes().get(0));
        itemRequestService.requestItem(itemRequestInformation, exchange);
    }

    public void requestItemRecallOnMessage(@Body String body, Exchange exchange) throws IOException {
        ObjectMapper om = new ObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        logger.info("Item Barcode Recevied for Processing Recall -> " + itemRequestInformation.getItemBarcodes().get(0));
        itemRequestService.recallItem(itemRequestInformation, exchange);
    }

    public void pulRequestTopicOnMessage(@Body String body) {
        logger.info("PUL Request Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_REQUEST_TOPIC);
    }

    public void pulEDDTopicOnMessage(@Body String body) {
        logger.info("PUL EDD Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_EDD_TOPIC);
    }

    public void pulRecalTopicOnMessage(@Body String body) {
        logger.info("PUL Recall Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_RECALL_TOPIC);
    }

    public void pulBorrowDirectTopicOnMessage(@Body String body) {
        logger.info("PUL BorrowDirect Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_PUL_BORROW_DIRECT_TOPIC);
    }

    public void culRequestTopicOnMessage(@Body String body) {
        logger.info("CUL Request Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_REQUEST_TOPIC);
    }

    public void culEDDTopicOnMessage(@Body String body) {
        logger.info("CUL EDD Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_EDD_TOPIC);
    }

    public void culRecalTopicOnMessage(@Body String body) {
        logger.info("CUL Recall Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_RECALL_TOPIC);
    }

    public void culBorrowDirectTopicOnMessage(@Body String body) {
        logger.info("CUL Borrow Direct Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_CUL_BORROW_DIRECT_TOPIC);
    }

    public void nyplRequestTopicOnMessage(@Body String body) {
        logger.info("NYPL Request Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_REQUEST_TOPIC);
    }

    public void nyplEDDTopicOnMessage(@Body String body) {
        logger.info("NYPL EDD Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_EDD_TOPIC);
    }

    public void nyplRecalTopicOnMessage(@Body String body) {
        logger.info("NYPL Recall Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_RECALL_TOPIC);
    }

    public void nyplBorrowDirectTopicOnMessage(@Body String body) {
        logger.info("NYPL Borrow Direct Topic - Lisinting to messages");
        setTopicMessageToDb(body, ReCAPConstants.REQUEST_ITEM_NYPL_BORROW_DIRECT_TOPIC);
    }

    private void setTopicMessageToDb(String body, String operationType) {
        ObjectMapper om = new ObjectMapper();
        ItemInformationResponse itemInformationResponse = null;
        try {
            itemInformationResponse = om.readValue(body, ItemInformationResponse.class);
            itemRequestService.saveItemChangeLogEntity(itemInformationResponse.getRequestId(), ReCAPConstants.GUEST_USER, operationType, body);
        } catch (IOException e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
    }
}
