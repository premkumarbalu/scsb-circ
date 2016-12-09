package org.recap.mqconsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.*;
import org.apache.camel.builder.DefaultFluentProducerTemplate;
import org.jboss.logging.Logger;
import org.recap.ReCAPConstants;
import org.recap.controller.ItemController;
import org.recap.controller.RequestItemController;
import org.recap.controller.RequestItemValidatorController;
import org.recap.model.*;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.PatronDetailsRepository;
import org.recap.repository.RequestItemDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by sudhishk on 29/11/16.
 */
@Component
public class RequestItemQueueConsumer {

    private Logger logger = Logger.getLogger(RequestItemQueueConsumer.class);

    private RequestItemValidatorController requestItemValidatorController;

    private RequestItemController requestItemController;

    private ItemController itemController;

    private ItemDetailsRepository itemDetailsRepository;

    private RequestItemDetailsRepository requestItemDetailsRepository;

    private PatronDetailsRepository patronDetailsRepository;

    public RequestItemQueueConsumer(RequestItemValidatorController requestItemValidatorController,RequestItemController requestItemController,ItemController itemController,ItemDetailsRepository itemDetailsRepository,RequestItemDetailsRepository requestItemDetailsRepository,PatronDetailsRepository patronDetailsRepository){
        this.requestItemValidatorController=requestItemValidatorController;
        this.requestItemController=requestItemController;
        this.itemController=itemController;
        this.itemDetailsRepository=itemDetailsRepository;
        this.requestItemDetailsRepository = requestItemDetailsRepository;
        this.patronDetailsRepository =patronDetailsRepository;
    }

    public void requestItemOnMessage(@Body String body,Exchange exchange) throws IOException, InterruptedException {
        ObjectMapper om = new ObjectMapper();
        ItemRequestInformation itemRequestInformation = om.readValue(body, ItemRequestInformation.class);
        logger.info("itemResponseInformation -> " +itemRequestInformation.getPatronBarcode());
        requestItem(itemRequestInformation,exchange);
    }

    public void requestItemHoldOnMessage(@Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
    }

    public void requestItemEDDOnMessage(@Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
    }

    public void requestItemBorrowDirectOnMessage(@Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
    }

    public void requestItemRecallOnMessage(@Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
    }

    public void pulRequestTopicOnMessage(@Body String body) {
        logger.info("------------------------- PUL RequestTopic lisinting to messages");
        logger.info("Body -> " +body.toString());
    }

    public void pulEDDTopicOnMessage(@Body String body) {
        logger.info("Start Message Processing");
        logger.info("Body -> " +body.toString());
    }

    public void pulHoldTopicOnMessage(@Body String body) {
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

    private ItemResponseInformation requestItem(ItemRequestInformation itemRequestInfo,Exchange exchange){

        String patronResponse = "";
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper om = new ObjectMapper();
        String messagePublish="";
        boolean bsuccess=false;
        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        RequestTypeEntity requestTypeEntity = new RequestTypeEntity();
        ItemResponseInformation itemResponseInformation =null;
        try {
            // Fetch Item Information from Recap DB
//            List<ItemEntity> itemEntities= itemController.findByBarcodeIn(itemRequestInfo.getItemBarcodes().get(0));
            itemEntities = itemDetailsRepository.findByBarcodeIn(itemRequestInfo.getItemBarcodes());
            if(itemEntities !=null && itemEntities.size()>0) {
                itemEntity = itemEntities.get(0);
                itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getBibliographicId().toString());
                itemRequestInfo.setItemOwningInstitution("PUL");
                // Validate Patron
                ResponseEntity res = requestItemValidatorController.validateItemRequestInformations(itemRequestInfo);

                // Action based on Request Type
                if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
                    requestTypeEntity.setRequestTypeCode("RETRIEVAL");
                    requestTypeEntity.setRequestTypeDesc("RETRIEVAL");
                    requestTypeEntity.setRequestTypeId(2);
                    itemRequestInfo.setDeliveryLocation("htcsc");
                } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_HOLD)) {
                } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)) {
                } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
                } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT)) {
                }

                if (itemRequestInfo.isOwningInstitutionItem()) {
                    itemResponseInformation = requestItemController.holdItem(itemRequestInfo,itemRequestInfo.getRequestingInstitution());
                    if (itemResponseInformation.isSuccess()) { // If Hold command is successfully
                        if (itemRequestInfo.isOwningInstitutionItem()) {
                            // Update Recap DB
                            updateRecap(itemRequestInfo,itemEntities,requestTypeEntity);
                            bsuccess=true;
                        } else { // Item does not belong to requesting Institute
                            ItemResponseInformation checkoutItemResponse = requestItemController.checkoutItem(itemRequestInfo);
                            if (checkoutItemResponse.isSuccess()) {
                                // Update Recap DB
                                updateRecap(itemRequestInfo,itemEntities,requestTypeEntity);
                                messagePublish="Checkout failed from ILS server";
                                bsuccess=true;
                            } else {
                                messagePublish="Checkout failed from ILS server";
                                bsuccess=false;
                            }
                        }
                    } else { // If Hold command Failure
                        messagePublish="Hold Failed from ILS server";
                        bsuccess=false;
                    }
                } else {// Not the Owning Institute
                    ItemResponseInformation createItemResponse = requestItemController.createBibliogrphicItem(itemRequestInfo);
                    itemRequestInfo.setBibId(createItemResponse.getBibiid());
                    itemResponseInformation = requestItemController.holdItem(itemRequestInfo,itemRequestInfo.getRequestingInstitution());
                    updateRecap(itemRequestInfo,itemEntities,requestTypeEntity);
                    bsuccess=true;
                }
                // Update GFA
            }
            logger.info("Finish Processing");
            itemResponseInformation.setScreenMessage(messagePublish);
            itemResponseInformation.setSuccess(bsuccess);
            itemResponseInformation.setDueDate(itemRequestInfo.getExpirationDate());
            itemResponseInformation.setRequestingInstitution(itemRequestInfo.getRequestingInstitution());
            itemResponseInformation.setTitleIdentifier(itemRequestInfo.getTitleIdentifier());
            itemResponseInformation.setPatronBarcode(itemRequestInfo.getPatronBarcode());
            itemResponseInformation.setBibiid(itemRequestInfo.getBibId());

            // Update Topics
            sendMessageToTopic(itemRequestInfo.getItemOwningInstitution(),itemRequestInfo.getRequestType(),exchange,itemResponseInformation);
        }catch(RestClientException ex){
            logger.error("RestClient : "+ ex.getMessage());
            ex.printStackTrace();
        }catch(Exception ex){
            logger.error("Exception : "+ex.getMessage());
            ex.printStackTrace();
        }
        return itemResponseInformation;
    }

    private void sendMessageToTopic(String owningInstituteId,String requestType,Exchange exchange,ItemResponseInformation itemResponseInfo ){
        String selectTopic= ReCAPConstants.PUL_REQUEST_TOPIC;
        if(owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)){
            selectTopic = ReCAPConstants.PUL_REQUEST_TOPIC;
        }else if(owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_HOLD)){
            selectTopic = ReCAPConstants.PUL_HOLD_TOPIC;
        }else if(owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)){
            selectTopic = ReCAPConstants.PUL_EDD_TOPIC;
        }else if(owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)){
            selectTopic = ReCAPConstants.PUL_RECALL_TOPIC;
        }else if(owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT)){
            selectTopic = ReCAPConstants.PUL_BORROW_DIRECT_TOPIC;
        }
        ObjectMapper objectMapper= new ObjectMapper();
        String json ="";
        try {
            json = objectMapper.writeValueAsString(itemResponseInfo);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(exchange.getContext());
        fluentProducerTemplate
                .to(selectTopic)
                .withBody(json);
        fluentProducerTemplate.send();
    }

    private void updateRecap(ItemRequestInformation itemRequestInformation,List<ItemEntity> itemEntities, RequestTypeEntity requestTypeEntity){

        RequestItemEntity requestItemEntity=new RequestItemEntity();
        PatronEntity patronEntity =null;
        InstitutionEntity institutionEntity = null;
        PatronEntity savedPatronEntity =null;

        try {
            SimpleDateFormat simpleDateFormat=  new SimpleDateFormat("YYYYMMDD    HHMMSS");
            SimpleDateFormat SDF=  new SimpleDateFormat("HHMMSS");

            // Item from SCSB
            ItemEntity itemEntity = itemEntities.get(0);

            // Patron Information
            patronEntity =  patronDetailsRepository.findByInstitutionIdentifier(itemRequestInformation.getPatronBarcode());
            if(patronEntity == null) {
                patronEntity = new PatronEntity();
                patronEntity.setInstitutionIdentifier(itemRequestInformation.getPatronBarcode());
                patronEntity.setInstitutionId(itemEntity.getInstitutionEntity().getInstitutionId());
                patronEntity.setEmailId(itemRequestInformation.getEmailAddress());
                savedPatronEntity = patronDetailsRepository.save(patronEntity);
            }else{
                savedPatronEntity =patronEntity;
            }

            requestItemEntity.setItemId(itemEntity.getItemId());
            requestItemEntity.setRequestingInstitutionId(itemEntity.getInstitutionEntity().getInstitutionId());
            requestItemEntity.setRequestTypeId(requestTypeEntity.getRequestTypeId());
            requestItemEntity.setRequestExpirationDate(simpleDateFormat.parse(itemRequestInformation.getExpirationDate()));
            requestItemEntity.setCreatedDate(new Date());
            requestItemEntity.setLastUpdatedDate(new Date());
            requestItemEntity.setRequestPosition(new Random().nextInt());
            requestItemEntity.setPatronId(savedPatronEntity.getPatronId());
            requestItemEntity.setStopCode("PA");

            RequestItemEntity savedItemRequest = requestItemDetailsRepository.save(requestItemEntity);
            logger.info("SCSB DB Update Successful");
        } catch (ParseException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }

    }

    private void updateGFA(){

    }
}
