package org.recap.mqconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.jboss.logging.Logger;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.ItemRequestInformation;
import org.recap.model.ItemResponseInformation;
import org.recap.request.ItemRequestService;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by sudhishk on 29/11/16.
 */
@Component
public class RequestItemQueueConsumer {

    private Logger logger = Logger.getLogger(RequestItemQueueConsumer.class);

    private ItemRequestService itemRequestService;

    public RequestItemQueueConsumer(ItemRequestService itemRequestService){
        this.itemRequestService=itemRequestService;
    }

    public void requestItemOnMessage(@Body String body,Exchange exchange) throws IOException, InterruptedException {
        ObjectMapper om = new ObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        logger.info("Item Barcode Recevied for Processing -> " +itemRequestInformation.getPatronBarcode());
        itemRequestService.requestItem(itemRequestInformation,exchange);
    }

    public void requestItemEDDOnMessage(@Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
    }

    public void requestItemBorrowDirectOnMessage(@Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
    }

    public void requestItemRecallOnMessage(@Body String body,Exchange exchange) throws IOException, InterruptedException {
        ObjectMapper om = new ObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        logger.info("Item Barcode Recevied for Processing -> " +itemRequestInformation.getPatronBarcode());
        itemRequestService.recallItem(itemRequestInformation,exchange);
    }

    public void pulRequestTopicOnMessage(@Body String body,Exchange exchange) throws IOException {
        logger.info("------------------------- PUL RequestTopic lisinting to messages");
        ObjectMapper om = new ObjectMapper();
        ItemInformationResponse itemInformationResponse = om.readValue(body, ItemInformationResponse.class);
        itemRequestService.saveItemChangeLogEntity(itemInformationResponse.getRequestId(),"Guest","RequestItem-pulRequestTopic",body.toString());
        logger.info("Body -> " +body.toString());
    }

    public void pulEDDTopicOnMessage(@Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
    }

    public void pulRecalTopicOnMessage(@Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
    }

    public void pulBorrowDirectTopicOnMessage(@Header("RequestType") String requestType, @Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
        logger.info("Hold -> " +requestType);
    }

    public void nyplRequestTopicOnMessage(@Body String body,Exchange exchange) throws IOException {
        logger.info("------------------------- NYPL RequestTopic lisinting to messages");
        ObjectMapper om = new ObjectMapper();
        ItemInformationResponse itemInformationResponse = om.readValue(body, ItemInformationResponse.class);
        itemRequestService.saveItemChangeLogEntity(itemInformationResponse.getRequestId(),"Guest","RequestItem-nyplRequestTopic",body.toString());
        logger.info("Body -> " +body.toString());
    }

    public void nyplEDDTopicOnMessage(@Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
    }

    public void nyplRecalTopicOnMessage(@Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
    }

    public void nyplBorrowDirectTopicOnMessage(@Header("RequestType") String requestType, @Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
        logger.info("Hold -> " +requestType);
    }
}
