package org.recap.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.DefaultFluentProducerTemplate;
import org.jboss.logging.Logger;
import org.recap.ReCAPConstants;
import org.recap.controller.RequestItemController;
import org.recap.controller.RequestItemValidatorController;
import org.recap.ils.model.AbstractResponseItem;
import org.recap.ils.model.ItemCreateBibResponse;
import org.recap.ils.model.ItemHoldResponse;
import org.recap.model.*;
import org.recap.mqconsumer.RequestItemQueueConsumer;
import org.recap.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by sudhishk on 1/12/16.
 */
@Component
public class ItemRequestService {

    private Logger logger = Logger.getLogger(RequestItemQueueConsumer.class);

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private RequestTypeDetailsRepository requestTypeDetailsRepository;

    @Autowired
    private RequestItemValidatorController requestItemValidatorController;

    @Autowired
    private RequestItemController requestItemController;

    @Autowired
    private PatronDetailsRepository patronDetailsRepository;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    public ItemResponseInformation requestItem(ItemRequestInformation itemRequestInfo, Exchange exchange){

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
            itemEntities = itemDetailsRepository.findByBarcodeIn(itemRequestInfo.getItemBarcodes());

            if(itemEntities !=null && itemEntities.size()>0) {
                logger.info("Item Exists in SCSB Database");
                itemEntity = itemEntities.get(0);
                itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getBibliographicId().toString());
                itemRequestInfo.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                // Validate Patron
                ResponseEntity res = requestItemValidatorController.validateItemRequestInformations(itemRequestInfo);
                if (res.getStatusCodeValue() == 200) {
                    logger.info("Request Validation Successful");
                    // Action based on Request Type
                    if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
                        requestTypeEntity= requestTypeDetailsRepository.findByrequestTypeCode(itemRequestInfo.getRequestType());
                        if(itemRequestInfo.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
                            itemRequestInfo.setDeliveryLocation("rcpcirc");
                        }
                        itemResponseInformation = checkOwningInstitution(itemRequestInfo,itemResponseInformation,itemEntities,requestTypeEntity);

                    } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_HOLD)) {

                    } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)) {

                    } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
                    } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT)) {
                    }


                    // Update GFA
                }else{
                    messagePublish = "Request Validation Error";
                    bsuccess = false;
                }
            }else{
                messagePublish = "Item Barcode does not exist";
                bsuccess = false;
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
            sendMessageToTopic(itemRequestInfo.getItemOwningInstitution(), itemRequestInfo.getRequestType(), exchange, itemResponseInformation);
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

    private ItemResponseInformation checkOwningInstitution(ItemRequestInformation itemRequestInfo,ItemResponseInformation itemResponseInformation,List<ItemEntity> itemEntities,RequestTypeEntity requestTypeEntity){
        String messagePublish="";
        boolean bsuccess=false;
        if (itemRequestInfo.isOwningInstitutionItem()) {
            ItemHoldResponse itemHoldResponse = (ItemHoldResponse)requestItemController.holdItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
            if (itemResponseInformation.isSuccess()) { // If Hold command is successfully
                itemResponseInformation =checkInstAfterPlacingRequest(itemRequestInfo,itemResponseInformation,itemEntities,requestTypeEntity);
            } else { // If Hold command Failure
                messagePublish = "Hold Failed from ILS server";
                bsuccess = false;
            }
        } else {// Not the Owning Institute
            ItemCreateBibResponse createItemResponse = (ItemCreateBibResponse)requestItemController.createBibliogrphicItem(itemRequestInfo,itemRequestInfo.getItemOwningInstitution());
            itemRequestInfo.setBibId(createItemResponse.getBibId());
            ItemHoldResponse itemHoldResponse = (ItemHoldResponse)requestItemController.holdItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());

            itemResponseInformation =checkInstAfterPlacingRequest(itemRequestInfo,itemResponseInformation,itemEntities,requestTypeEntity);
            bsuccess = true;
        }
        itemResponseInformation.setScreenMessage(messagePublish);
        itemResponseInformation.setSuccess(bsuccess);
        return itemResponseInformation;
    }

    private ItemResponseInformation checkInstAfterPlacingRequest(ItemRequestInformation itemRequestInfo,ItemResponseInformation itemResponseInformation,List<ItemEntity> itemEntities,RequestTypeEntity requestTypeEntity){
        String messagePublish="";
        boolean bsuccess=false;
        if (itemRequestInfo.isOwningInstitutionItem()) {
            // Update Recap DB
            updateRecap(itemRequestInfo, itemEntities, requestTypeEntity);
            bsuccess = true;
        } else { // Item does not belong to requesting Institute
            AbstractResponseItem checkoutItemResponse = requestItemController.checkoutItem(itemRequestInfo,itemRequestInfo.getRequestingInstitution());
            if (checkoutItemResponse.isSuccess()) {
                // Update Recap DB
                updateRecap(itemRequestInfo, itemEntities, requestTypeEntity);
                messagePublish = "Checkout failed from ILS server";
                bsuccess = true;
            } else {
                messagePublish = "Checkout failed from ILS server";
                bsuccess = false;
            }
        }
        itemResponseInformation.setScreenMessage(messagePublish);
        itemResponseInformation.setSuccess(bsuccess);
        return itemResponseInformation;
    }
}
