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
import org.recap.ils.model.*;
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

    @Autowired
    ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    public ItemInformationResponse requestItem(ItemRequestInformation itemRequestInfo, Exchange exchange){

        String patronResponse = "";
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper om = new ObjectMapper();
        String messagePublish="";
        boolean bsuccess=false;
        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        RequestTypeEntity requestTypeEntity = new RequestTypeEntity();
        ItemInformationResponse itemResponseInformation =new ItemInformationResponse();
        ResponseEntity res =null;
        try {
            itemEntities = itemDetailsRepository.findByBarcodeIn(itemRequestInfo.getItemBarcodes());

            if(itemEntities !=null && itemEntities.size()>0) {
                logger.info("Item Exists in SCSB Database");
                itemEntity = itemEntities.get(0);
                itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                itemRequestInfo.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                // Validate Patron
                res = requestItemValidatorController.validateItemRequestInformations(itemRequestInfo);
                if (res.getStatusCodeValue() == 200) {
                    logger.info("Request Validation Successful");
                    // Change Item Availablity
                    updateItemAvailabilutyStatus(itemEntity);
                    // Action based on Request Type
                    if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
                        requestTypeEntity= requestTypeDetailsRepository.findByrequestTypeCode(itemRequestInfo.getRequestType());
                        if(itemRequestInfo.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
                            itemRequestInfo.setDeliveryLocation("rcpcirc");
                        }
                        itemResponseInformation = checkOwningInstitution(itemRequestInfo,itemResponseInformation,itemEntity,requestTypeEntity);
                    } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)) {
                        // Check if Request Item  for any existint request
                        ItemRecallResponse itemRecallResponse = (ItemRecallResponse) requestItemController.recallItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                        itemResponseInformation = checkOwningInstitution(itemRequestInfo,itemResponseInformation,itemEntity,requestTypeEntity);
                        updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity);
                        updateGFA(itemRequestInfo,itemResponseInformation,exchange);
                    } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
                        updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity);
                        updateGFA(itemRequestInfo,itemResponseInformation,exchange);
                    } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT)) {
                        updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity);
                        updateGFA(itemRequestInfo,itemResponseInformation,exchange);
                    }
                }else{
                    logger.warn("Validate Request Errors : "+res.getBody().toString());
                    messagePublish = res.getBody().toString();
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
            itemResponseInformation.setBibID(itemRequestInfo.getBibId());

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

    public ItemInformationResponse recallItem(ItemRequestInformation itemRequestInfo, Exchange exchange){
        String patronResponse = "";
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper om = new ObjectMapper();
        String messagePublish="";
        boolean bsuccess=false;
        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        RequestTypeEntity requestTypeEntity = new RequestTypeEntity();
        ItemInformationResponse itemResponseInformation =new ItemInformationResponse();
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
                    // Check if Request Item  for any existint request
                    ItemRecallResponse itemRecallResponse = (ItemRecallResponse) requestItemController.recallItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                    itemResponseInformation = checkOwningInstitution(itemRequestInfo,itemResponseInformation,itemEntity,requestTypeEntity);
                    updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity);
                    updateGFA(itemRequestInfo,itemResponseInformation,exchange);
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
            itemResponseInformation.setTitleIdentifier(itemRequestInfo.getTitleIdentifier());
            itemResponseInformation.setBibID(itemRequestInfo.getBibId());

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

    private void sendMessageToTopic(String owningInstituteId,String requestType,Exchange exchange,ItemInformationResponse itemResponseInfo ){
        String selectTopic= ReCAPConstants.PUL_REQUEST_TOPIC;
        if(owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)){
            selectTopic = ReCAPConstants.PUL_REQUEST_TOPIC;
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

    private Integer updateRecapRequestItem(ItemRequestInformation itemRequestInformation, ItemEntity itemEntity, RequestTypeEntity requestTypeEntity){

        RequestItemEntity requestItemEntity=new RequestItemEntity();
        PatronEntity patronEntity =null;
        InstitutionEntity institutionEntity = null;
        PatronEntity savedPatronEntity =null;
        RequestItemEntity savedItemRequest =null;
        try {
            SimpleDateFormat simpleDateFormat=  new SimpleDateFormat("YYYYMMDD    HHMMSS");
            SimpleDateFormat SDF=  new SimpleDateFormat("HHMMSS");

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
            requestItemEntity.setPatronId(savedPatronEntity.getPatronId());
            requestItemEntity.setStopCode("PA");

            savedItemRequest = requestItemDetailsRepository.save(requestItemEntity);
            saveItemChangeLogEntity(savedItemRequest.getRequestId(),"Guest","Request Item Insert",savedItemRequest.getItemId()+" - "+ savedItemRequest.getPatronId());

            logger.info("SCSB DB Update Successful");
        } catch (ParseException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
        return savedItemRequest.getRequestId();
    }

    private void updateItemAvailabilutyStatus(ItemEntity itemEntity){
        itemEntity.setItemAvailabilityStatusId(2); // Not Available
        itemDetailsRepository.save(itemEntity);
        saveItemChangeLogEntity(itemEntity.getItemId(),"Guest","Request ItemAvailabilityStatus Change","1 - 2");
    }

    private void updateGFA(ItemRequestInformation itemRequestInfo,ItemInformationResponse itemResponseInformation,Exchange exchange ){
        ObjectMapper objectMapper= new ObjectMapper();
        String json ="";
        try {
            json = objectMapper.writeValueAsString(itemResponseInformation);


        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(exchange.getContext());
//        fluentProducerTemplate
//                .to(ReCAPConstants.GFA_CIRCULATION_TOPIC)
//                .withBody(json);
//        fluentProducerTemplate.send();
    }

    private ItemInformationResponse checkOwningInstitution(ItemRequestInformation itemRequestInfo,ItemInformationResponse itemResponseInformation,ItemEntity itemEntity,RequestTypeEntity requestTypeEntity){
        String messagePublish="";
        boolean bsuccess=false;
        try {
            if (itemRequestInfo.isOwningInstitutionItem()) {
                ItemHoldResponse itemHoldResponse = (ItemHoldResponse)requestItemController.holdItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                if (itemHoldResponse.isSuccess()) { // If Hold command is successfully
                    itemResponseInformation =checkInstAfterPlacingRequest(itemRequestInfo,itemResponseInformation,itemEntity,requestTypeEntity);
                } else { // If Hold command Failure
                    messagePublish = itemHoldResponse.getScreenMessage();
                    bsuccess = false;
                    saveItemChangeLogEntity(itemEntity.getItemId(),"Guest","RequestItem - Hold Request Failed",itemHoldResponse.getPatronIdentifier()+" - "+itemHoldResponse.getScreenMessage());
                }
            } else {// Not the Owning Institute
                ItemCreateBibResponse createItemResponse = (ItemCreateBibResponse)requestItemController.createBibliogrphicItem(itemRequestInfo,itemRequestInfo.getItemOwningInstitution());
                if(createItemResponse.isSuccess()){
                    itemRequestInfo.setBibId(createItemResponse.getBibId());
                    ItemHoldResponse itemHoldResponse = (ItemHoldResponse)requestItemController.holdItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                    if(itemHoldResponse.isSuccess()) {
                        itemResponseInformation = checkInstAfterPlacingRequest(itemRequestInfo, itemResponseInformation, itemEntity, requestTypeEntity);
                        bsuccess = true;
                    }else{
                        messagePublish = itemHoldResponse.getScreenMessage();
                        bsuccess = false;
                        saveItemChangeLogEntity(itemEntity.getItemId(),"Guest","RequestItem - Hold Request Failed",itemHoldResponse.getPatronIdentifier()+" - "+itemHoldResponse.getScreenMessage());
                    }
                }else{
                    messagePublish = createItemResponse.getScreenMessage();
                    bsuccess = false;
                    saveItemChangeLogEntity(itemEntity.getItemId(),"Guest","RequestItem - Hold Request Failed",createItemResponse.getBibId()+" - "+createItemResponse.getScreenMessage());
                }
            }
        } catch (Exception e) {
            messagePublish = "Failed to process request.";
            bsuccess = false;
        }
        itemResponseInformation.setScreenMessage(messagePublish);
        itemResponseInformation.setSuccess(bsuccess);
        return itemResponseInformation;
    }

    private ItemInformationResponse checkInstAfterPlacingRequest(ItemRequestInformation itemRequestInfo,ItemInformationResponse itemResponseInformation,ItemEntity itemEntity,RequestTypeEntity requestTypeEntity){
        String messagePublish="";
        boolean bsuccess=false;
        if (itemRequestInfo.isOwningInstitutionItem()) {
            // Update Recap DB
            Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity);
            itemResponseInformation.setRequestId(requestId);
            bsuccess = true;
        } else { // Item does not belong to requesting Institute
            AbstractResponseItem checkoutItemResponse = requestItemController.checkoutItem(itemRequestInfo,itemRequestInfo.getRequestingInstitution());
            if (checkoutItemResponse.isSuccess()) {
                // Update Recap DB
                Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity);
                itemResponseInformation.setRequestId(requestId);
                messagePublish = checkoutItemResponse.getScreenMessage();
                bsuccess = true;
            } else {
                messagePublish = "Checkout failed from ILS server";
                bsuccess = false;
                saveItemChangeLogEntity(itemEntity.getItemId(),"Guest","RequestItem - Hold Request Failed",checkoutItemResponse.getItemOwningInstitution()+" - "+checkoutItemResponse.getScreenMessage());
            }
        }
        itemResponseInformation.setScreenMessage(messagePublish);
        itemResponseInformation.setSuccess(bsuccess);
        return itemResponseInformation;
    }

    public void saveItemChangeLogEntity(Integer recordId, String userName, String operationType, String notes) {
        ItemChangeLogEntity itemChangeLogEntity = new ItemChangeLogEntity();
        itemChangeLogEntity.setUpdatedBy(userName);
        itemChangeLogEntity.setUpdatedDate(new Date());
        itemChangeLogEntity.setOperationType(operationType);
        itemChangeLogEntity.setRecordId(recordId);
        itemChangeLogEntity.setNotes(notes);
        itemChangeLogDetailsRepository.save(itemChangeLogEntity);
    }

    private String getPatronIdBorrwingInsttution(String requestingInstitution, String OwningInstitution){
        String patronId="";

        return patronId;
    }
}
