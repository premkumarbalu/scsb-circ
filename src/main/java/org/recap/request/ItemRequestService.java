package org.recap.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.DefaultFluentProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.recap.ReCAPConstants;
import org.recap.controller.RequestItemController;
import org.recap.controller.RequestItemValidatorController;
import org.recap.ils.model.response.ItemCreateBibResponse;
import org.recap.ils.model.response.ItemHoldResponse;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.ils.model.response.ItemRecallResponse;
import org.recap.model.*;
import org.recap.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;
import java.util.List;

/**
 * Created by sudhishk on 1/12/16.
 */
@Component
public class ItemRequestService {

    private Logger logger = Logger.getLogger(ItemRequestService.class);

    @Value("${ils.princeton.cul.patron}")
    private String princetonCULPatron;

    @Value("${ils.princeton.nypl.patron}")
    private String princetonNYPLPatron;

    @Value("${ils.columbia.pul.patron}")
    private String columbiaPULPatron;

    @Value("${ils.columbia.nypl.patron}")
    private String columbiaNYPLPatron;

    @Value("${ils.nypl.princeton.patron}")
    private String nyplPrincetonPatron;

    @Value("${ils.nypl.columbia.patron}")
    private String nyplColumbiaPatron;

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.solr.client.url}")
    String scsbSolrClientUrl;


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

    @Autowired
    private EmailService emailService;

    @Autowired
    private RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Autowired
    private GFAService gfaService;

    @Autowired
    private RequestInstitutionBibDetailsRepository requestInstitutionBibDetailsRepository;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private ItemRequestDBService itemRequestDBService;

    public ItemInformationResponse requestItem(ItemRequestInformation itemRequestInfo, Exchange exchange) {

        String messagePublish = "";
        boolean bsuccess = false;
        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        RequestTypeEntity requestTypeEntity;
        ItemInformationResponse itemResponseInformation = new ItemInformationResponse();
        ResponseEntity res;

        try {
            itemEntities = itemDetailsRepository.findByBarcodeIn(itemRequestInfo.getItemBarcodes());

            if (itemEntities != null && !itemEntities.isEmpty()) {
                logger.info("Item Exists in SCSB Database");
                itemEntity = itemEntities.get(0);
                if (StringUtils.isBlank(itemRequestInfo.getBibId())) {
                    itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                }
                itemRequestInfo.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                itemRequestInfo.setTitleIdentifier(getTitle(itemRequestInfo.getTitleIdentifier(), itemEntity));
                itemRequestInfo.setCustomerCode(itemEntity.getCustomerCode());
                // Validate Patron
                res = requestItemValidatorController.validateItemRequestInformations(itemRequestInfo);
                if (res.getStatusCode() == HttpStatus.OK) {
                    logger.info("Request Validation Successful");
                    // Change Item Availablity
                    updateItemAvailabilutyStatus(itemEntities, itemRequestInfo.getUsername());
                    requestTypeEntity = requestTypeDetailsRepository.findByrequestTypeCode(itemRequestInfo.getRequestType());
                    // Action based on Request Type
                    if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
                        itemResponseInformation = checkOwningInstitution(itemRequestInfo, itemResponseInformation, itemEntity, requestTypeEntity);
                        bsuccess = itemResponseInformation.isSuccess();
                        messagePublish = itemResponseInformation.getScreenMessage();
                    } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT)) {
                        itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
                        if (itemResponseInformation.isSuccess()) {
                            updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
                            bsuccess = itemResponseInformation.isSuccess();
                            messagePublish = "Borrow Direct request is successfull";
                        } else {
                            rollbackAfterGFA(itemEntity, itemRequestInfo, itemResponseInformation);
                            bsuccess = false;
                            messagePublish = itemResponseInformation.getScreenMessage();
                        }
                    }
                } else {
                    logger.warn("Validate Request Errors : " + res.getBody().toString());
                    messagePublish = res.getBody().toString();
                    bsuccess = false;
                }
                itemResponseInformation.setItemId(itemEntity.getItemId());
            } else {
                messagePublish = ReCAPConstants.WRONG_ITEM_BARCODE;
                bsuccess = false;
            }
            logger.info("Finish Processing");
            itemResponseInformation.setScreenMessage(messagePublish);
            itemResponseInformation.setSuccess(bsuccess);
            itemResponseInformation.setItemOwningInstitution(itemRequestInfo.getItemOwningInstitution());
            itemResponseInformation.setDueDate(itemRequestInfo.getExpirationDate());
            itemResponseInformation.setRequestingInstitution(itemRequestInfo.getRequestingInstitution());
            itemResponseInformation.setTitleIdentifier(itemRequestInfo.getTitleIdentifier());
            itemResponseInformation.setPatronBarcode(itemRequestInfo.getPatronBarcode());
            itemResponseInformation.setBibID(itemRequestInfo.getBibId());
            itemResponseInformation.setItemBarcode(itemRequestInfo.getItemBarcodes().get(0));
            itemResponseInformation.setRequestType(itemRequestInfo.getRequestType());
            itemResponseInformation.setEmailAddress(itemRequestInfo.getEmailAddress());
            itemResponseInformation.setDeliveryLocation(itemRequestInfo.getDeliveryLocation());
            itemResponseInformation.setRequestNotes(itemRequestInfo.getRequestNotes());

            // Update Topics
            sendMessageToTopic(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getRequestType(), itemResponseInformation, exchange);
        } catch (RestClientException ex) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION_REST, ex);
        } catch (Exception ex) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, ex);
        }
        return itemResponseInformation;
    }

    public ItemInformationResponse recallItem(ItemRequestInformation itemRequestInfo, Exchange exchange) {
        String messagePublish = "";
        boolean bsuccess = false;
        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        ItemInformationResponse itemResponseInformation = new ItemInformationResponse();
        try {
            itemEntities = itemDetailsRepository.findByBarcodeIn(itemRequestInfo.getItemBarcodes());

            if (itemEntities != null && !itemEntities.isEmpty()) {
                logger.info("Item Exists in SCSB Database");
                itemEntity = itemEntities.get(0);
                itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                itemRequestInfo.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                // Validate Patron
                ResponseEntity res = requestItemValidatorController.validateItemRequestInformations(itemRequestInfo);
                if (res.getStatusCode() == HttpStatus.OK) {
                    logger.info("Request Validation Successful");
                    // Check if Request Item  for any existint request
                    itemResponseInformation = checkOwningInstitutionRecall(itemRequestInfo, itemResponseInformation, itemEntity);
                    messagePublish = itemResponseInformation.getScreenMessage();
                    bsuccess = itemResponseInformation.isSuccess();
                } else {
                    messagePublish = res.getBody().toString();
                    bsuccess = false;
                }
                itemResponseInformation.setItemId(itemEntity.getItemId());
            } else {
                messagePublish = ReCAPConstants.WRONG_ITEM_BARCODE;
                bsuccess = false;
            }
            logger.info("Finish Processing");
            itemResponseInformation.setScreenMessage(messagePublish);
            itemResponseInformation.setSuccess(bsuccess);
            itemResponseInformation.setItemOwningInstitution(itemRequestInfo.getItemOwningInstitution());
            itemResponseInformation.setDueDate(itemRequestInfo.getExpirationDate());
            itemResponseInformation.setRequestingInstitution(itemRequestInfo.getRequestingInstitution());
            itemResponseInformation.setTitleIdentifier(itemRequestInfo.getTitleIdentifier());
            itemResponseInformation.setPatronBarcode(itemRequestInfo.getPatronBarcode());
            itemResponseInformation.setBibID(itemRequestInfo.getBibId());
            itemResponseInformation.setItemBarcode(itemRequestInfo.getItemBarcodes().get(0));
            itemResponseInformation.setRequestType(itemRequestInfo.getRequestType());
            itemResponseInformation.setEmailAddress(itemRequestInfo.getEmailAddress());
            itemResponseInformation.setDeliveryLocation(itemRequestInfo.getDeliveryLocation());
            itemResponseInformation.setRequestNotes(itemRequestInfo.getRequestNotes());
            // Update Topics
            sendMessageToTopic(itemRequestInfo.getItemOwningInstitution(), itemRequestInfo.getRequestType(), itemResponseInformation, exchange);
        } catch (RestClientException ex) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION_REST, ex);
        } catch (Exception ex) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, ex);
        }
        return itemResponseInformation;
    }

    public boolean reFileItem(ItemRefileRequest itemRefileRequest) {

        // Change Response for this Method
        boolean bSuccess = false;
        String itemBarcode = itemRefileRequest.getItemBarcodes().get(0);
        RequestItemEntity requestItemEntity;

        RequestItemEntity requestItemEntityRecalled = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(itemBarcode, ReCAPConstants.REQUEST_STATUS_RECALLED);
        if (requestItemEntityRecalled == null) { // Recall Request Does not Exist
            requestItemEntity = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(itemBarcode, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
            if (requestItemEntity != null) {  // Check Retrival Order
                bSuccess = updateItemStatusSolrIndexing(itemRefileRequest);
            }
        } else { // Recall Request Exist
            requestItemEntity = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(itemBarcode, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
            RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_REFILED);
            if (requestItemEntity.getRequestingInstitutionId().intValue() == requestItemEntity.getItemEntity().getOwningInstitutionId().intValue()) { // Borrowing Inst same as Owning
                requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                requestItemEntityRecalled.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                requestItemDetailsRepository.save(requestItemEntity);
                requestItemDetailsRepository.save(requestItemEntityRecalled);
                rollbackUpdateItemAvailabilutyStatus(requestItemEntity.getItemEntity(), ReCAPConstants.GUEST_USER);
                updateSolrIndex(requestItemEntity.getItemEntity());
                bSuccess = true;
            } else {
                // Borrowing Inst not same as Owning

                // Change Retrieval Status to Refiled
                requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                requestItemDetailsRepository.save(requestItemEntity);
                RequestStatusEntity requestStatusRO = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
                // Change Existing Recall to Retrieval Order
                requestItemEntityRecalled.setRequestStatusId(requestStatusRO.getRequestStatusId());
                requestItemDetailsRepository.save(requestItemEntityRecalled);

                bSuccess = true;
            }
        }
        if (requestItemEntity != null) {
            ItemRequestInformation itemRequestInfo = new ItemRequestInformation();
            itemRequestInfo.setItemBarcodes(itemRefileRequest.getItemBarcodes());

            itemRequestInfo.setItemOwningInstitution(requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionCode());
            itemRequestInfo.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());

            if (itemRequestInfo.getRequestingInstitution().equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
                itemRequestInfo.setPatronBarcode(requestItemEntity.getPatronEntity().getInstitutionIdentifier());
                requestItemController.checkinItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
            }
            if (!itemRequestInfo.isOwningInstitutionItem() && (itemRequestInfo.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.NYPL) || itemRequestInfo.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.PRINCETON))) {
                itemRequestInfo.setPatronBarcode(getPatronIdBorrwingInsttution(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution()));
                requestItemController.checkinItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
            }
        }
        return bSuccess;
    }

    public void sendMessageToTopic(String owningInstituteId, String requestType, ItemInformationResponse itemResponseInfo, Exchange exchange) {
        String selectTopic = ReCAPConstants.PUL_REQUEST_TOPIC;
        if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
            selectTopic = ReCAPConstants.PUL_REQUEST_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
            selectTopic = ReCAPConstants.PUL_EDD_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)) {
            selectTopic = ReCAPConstants.PUL_RECALL_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT)) {
            selectTopic = ReCAPConstants.PUL_BORROW_DIRECT_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.COLUMBIA) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
            selectTopic = ReCAPConstants.CUL_REQUEST_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.COLUMBIA) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
            selectTopic = ReCAPConstants.CUL_EDD_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.COLUMBIA) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)) {
            selectTopic = ReCAPConstants.CUL_RECALL_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.COLUMBIA) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT)) {
            selectTopic = ReCAPConstants.CUL_BORROW_DIRECT_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.NYPL) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
            selectTopic = ReCAPConstants.NYPL_REQUEST_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.NYPL) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
            selectTopic = ReCAPConstants.NYPL_EDD_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.NYPL) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)) {
            selectTopic = ReCAPConstants.NYPL_RECALL_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.NYPL) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT)) {
            selectTopic = ReCAPConstants.NYPL_BORROW_DIRECT_TOPIC;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(itemResponseInfo);
        } catch (JsonProcessingException e) {
            logger.error(ReCAPConstants.REQUEST_PARSE_EXCEPTION, e);
        }
        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(exchange.getContext());
        fluentProducerTemplate
                .to(selectTopic)
                .withBody(json);
        fluentProducerTemplate.send();
    }

    public Integer updateRecapRequestItem(ItemRequestInformation itemRequestInformation, ItemEntity itemEntity, RequestTypeEntity requestTypeEntity, String requestStatusCode) {
        return itemRequestDBService.updateRecapRequestItem(itemRequestInformation, itemEntity, requestTypeEntity, requestStatusCode);
    }

    public ItemInformationResponse updateRecapRequestItem(ItemInformationResponse itemInformationResponse) {
        return itemRequestDBService.updateRecapRequestItem(itemInformationResponse);
    }

    private void updateItemAvailabilutyStatus(List<ItemEntity> itemEntities, String username) {
        itemRequestDBService.updateItemAvailabilutyStatus(itemEntities, username);
    }

    private void rollbackUpdateItemAvailabilutyStatus(ItemEntity itemEntity, String username) {
        itemRequestDBService.rollbackUpdateItemAvailabilutyStatus(itemEntity, username);
    }

    private boolean updateItemStatusSolrIndexing(ItemRefileRequest itemRefileRequest) {
        boolean bSuccess = false;
        List<ItemEntity> itemEntities = itemDetailsRepository.findByBarcodeIn(itemRefileRequest.getItemBarcodes());
        if (itemEntities != null && !itemEntities.isEmpty()) {
            for (int i = 0; i < itemEntities.size(); i++) {
                ItemEntity itemEntity = itemEntities.get(i);
                if (itemEntity.getItemAvailabilityStatusId().intValue() == 2) {
                    rollbackUpdateItemAvailabilutyStatus(itemEntity, ReCAPConstants.GUEST_USER);
                    updateSolrIndex(itemEntity);
                    bSuccess = true;
                } else {
                    bSuccess = false;
                }
            }
        }
        return bSuccess;
    }

    public ItemInformationResponse updateGFA(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {

        try {
            if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL) || itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
                itemResponseInformation = gfaService.executeRetriveOrder(itemRequestInfo, itemResponseInformation);
            } else {
                itemResponseInformation.setSuccess(true);
                itemResponseInformation.setScreenMessage("Retrival Order Not Required for Recall");
            }
        } catch (Exception e) {
            itemResponseInformation.setSuccess(false);
            itemResponseInformation.setScreenMessage(e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse checkOwningInstitution(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity, RequestTypeEntity requestTypeEntity) {
        String messagePublish = "";
        boolean bsuccess = false;
        String deliveryCode = "";
        try {
            if (itemRequestInfo.isOwningInstitutionItem()) {
                deliveryCode = itemRequestInfo.getDeliveryLocation();
                setpickupLoacation(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                ItemHoldResponse itemHoldResponse = (ItemHoldResponse) requestItemController.holdItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                if (itemHoldResponse.isSuccess()) { // IF Hold command is successfully
                    itemResponseInformation.setExpirationDate(itemHoldResponse.getExpirationDate());
                    itemRequestInfo.setExpirationDate(itemHoldResponse.getExpirationDate());
                    itemRequestInfo.setDeliveryLocation(deliveryCode);
                    itemResponseInformation = checkInstAfterPlacingRequest(itemRequestInfo, itemResponseInformation, itemEntity, requestTypeEntity);
                    messagePublish = itemResponseInformation.getScreenMessage();
                    bsuccess = true;
                } else { // If Hold command Failure
                    messagePublish = itemHoldResponse.getScreenMessage();
                    bsuccess = false;
                    rollbackUpdateItemAvailabilutyStatus(itemEntity,itemRequestInfo.getUsername());
                    saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, itemHoldResponse.getPatronIdentifier() + " - " + itemHoldResponse.getScreenMessage());
                }
            } else {// Not the Owning Institute
                // Get Temporary bibI from SCSB DB
                getTempBibId(itemRequestInfo, itemEntity);
                ItemCreateBibResponse createBibResponse;
                if (!ReCAPConstants.NYPL.equalsIgnoreCase(itemRequestInfo.getRequestingInstitution()) && itemRequestInfo.getBibId().trim().length() <= 0) {
                    createBibResponse = (ItemCreateBibResponse) requestItemController.createBibliogrphicItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                    if (createBibResponse.isSuccess() || ReCAPConstants.NYPL.equalsIgnoreCase(itemRequestInfo.getRequestingInstitution())) {
                        itemRequestInfo.setBibId(createBibResponse.getBibId());
                        createTempBibId(itemRequestInfo, itemEntity);
                        itemResponseInformation = holdafterCreateBibCheck(itemRequestInfo, itemResponseInformation, itemEntity, requestTypeEntity);
                        messagePublish = createBibResponse.getScreenMessage();
                        bsuccess = true;
                    } else {
                        messagePublish = createBibResponse.getScreenMessage();
                        bsuccess = false;
                        rollbackUpdateItemAvailabilutyStatus(itemEntity,itemRequestInfo.getUsername());
                        saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, createBibResponse.getBibId() + " - " + createBibResponse.getScreenMessage());
                    }
                } else {
                    itemResponseInformation = holdafterCreateBibCheck(itemRequestInfo, itemResponseInformation, itemEntity, requestTypeEntity);
                    messagePublish = itemResponseInformation.getScreenMessage();
                    bsuccess = false;
                }
            }
        } catch (Exception e) {
            logger.error("Exception : ", e);
            messagePublish = "Failed to process request.";
            bsuccess = false;
            saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), "RequestItem - Exception", itemRequestInfo.getItemBarcodes() + " - " + e.getMessage());
        } finally {
            itemResponseInformation.setScreenMessage(messagePublish);
            itemResponseInformation.setSuccess(bsuccess);
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse checkInstAfterPlacingRequest(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity, RequestTypeEntity requestTypeEntity) {
        String messagePublish;
        boolean bsuccess;
        if (itemRequestInfo.isOwningInstitutionItem()) {
            // GFA
            itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
            if (itemResponseInformation.isSuccess()) {
                // Update Recap DB
                Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
                itemResponseInformation.setRequestId(requestId);
                messagePublish = ReCAPConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM;
                bsuccess = true;
            } else {
                rollbackAfterGFA(itemEntity, itemRequestInfo, itemResponseInformation);
                messagePublish = itemResponseInformation.getScreenMessage();
                bsuccess = false;
            }

        } else { // Item does not belong to requesting Institute
            String requestingPatron = itemRequestInfo.getPatronBarcode();
            itemRequestInfo.setPatronBarcode(getPatronIdBorrwingInsttution(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution()));
            if (!itemRequestInfo.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                requestItemController.checkoutItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
            }
            // GFA
            itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
            if (itemResponseInformation.isSuccess()) {
                // Update Recap DB
                Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
                itemResponseInformation.setRequestId(requestId);
                messagePublish = ReCAPConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM;
                bsuccess = true;
            } else {
                rollbackAfterGFA(itemEntity, itemRequestInfo, itemResponseInformation);
                messagePublish = itemResponseInformation.getScreenMessage();
                bsuccess = false;
            }
            itemRequestInfo.setPatronBarcode(requestingPatron);
        }
        if (bsuccess) {
            updateSolrIndex(itemEntity);
        }
        itemResponseInformation.setScreenMessage(messagePublish);
        itemResponseInformation.setSuccess(bsuccess);
        return itemResponseInformation;
    }

    private ItemInformationResponse checkOwningInstitutionRecall(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        String messagePublish;
        String deliveryCode;
        boolean bsuccess;
        RequestTypeEntity requestTypeEntity;

        RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(itemRequestInfo.getItemBarcodes().get(0), ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
        ItemInformationResponse itemInformation = (ItemInformationResponse) requestItemController.itemInformation(itemRequestInfo, requestItemEntity.getInstitutionEntity().getInstitutionCode());
        if (itemInformation.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_CHARGED)) {
            if (requestItemEntity.getInstitutionEntity().getInstitutionCode().equalsIgnoreCase(itemRequestInfo.getRequestingInstitution())) {
                deliveryCode = itemRequestInfo.getDeliveryLocation();
                setpickupLoacation(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                ItemRecallResponse itemRecallResponse = (ItemRecallResponse) requestItemController.recallItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                itemRequestInfo.setDeliveryLocation(deliveryCode);
                if (itemRecallResponse.isSuccess()) {
                    // Update Recap DB
                    requestTypeEntity = requestTypeDetailsRepository.findByrequestTypeCode(itemRequestInfo.getRequestType());
                    itemResponseInformation.setExpirationDate(itemRecallResponse.getExpirationDate());
                    itemRequestInfo.setExpirationDate(itemRecallResponse.getExpirationDate());

                    itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
                    if (itemResponseInformation.isSuccess()) {
                        Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity, ReCAPConstants.REQUEST_STATUS_RECALLED);
                        itemResponseInformation.setRequestId(requestId);
                        messagePublish = ReCAPConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM;
                        bsuccess = true;
                    } else {
                        rollbackAfterGFA(itemEntity, itemRequestInfo, itemResponseInformation);
                        messagePublish = itemResponseInformation.getScreenMessage();
                        bsuccess = false;
                    }
                } else {
                    if (itemRecallResponse.getScreenMessage() != null && itemRecallResponse.getScreenMessage().trim().length() > 0) {
                        messagePublish = itemRecallResponse.getScreenMessage();
                    } else {
                        messagePublish = "Recall failed from ILS";
                    }
                    bsuccess = false;
                }
            } else {
                deliveryCode = itemRequestInfo.getDeliveryLocation();
                setpickupLoacation(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                ItemHoldResponse itemHoldResponse = (ItemHoldResponse) requestItemController.holdItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                if (itemHoldResponse.isSuccess()) { // IF Hold command is successfully
                    itemResponseInformation.setExpirationDate(itemHoldResponse.getExpirationDate());
                    itemRequestInfo.setExpirationDate(itemHoldResponse.getExpirationDate());
                    itemRequestInfo.setDeliveryLocation(deliveryCode);

                    deliveryCode = itemRequestInfo.getDeliveryLocation();
                    setpickupLoacation(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                    ItemRecallResponse itemRecallResponse = (ItemRecallResponse) requestItemController.recallItem(itemRequestInfo, requestItemEntity.getInstitutionEntity().getInstitutionCode());
                    itemRequestInfo.setDeliveryLocation(deliveryCode);
                    if (itemRecallResponse.isSuccess()) {

                        requestTypeEntity = requestTypeDetailsRepository.findByrequestTypeCode(itemRequestInfo.getRequestType());
                        // GFA Update
                        itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
                        if (itemResponseInformation.isSuccess()) {
                            // Update Recap DB
                            Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity, ReCAPConstants.REQUEST_STATUS_RECALLED);
                            itemResponseInformation.setRequestId(requestId);
                            messagePublish = ReCAPConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM;
                            bsuccess = true;
                        } else {
                            rollbackAfterGFA(itemEntity, itemRequestInfo, itemResponseInformation);
                            messagePublish = itemResponseInformation.getScreenMessage();
                            bsuccess = false;
                        }
                    } else {
                        if (itemRecallResponse.getScreenMessage() != null && itemRecallResponse.getScreenMessage().trim().length() > 0) {
                            messagePublish = itemRecallResponse.getScreenMessage();
                        } else {
                            messagePublish = "Recall failed from ILS";
                        }
                        bsuccess = false;
                    }
                } else { // If Hold command Failure
                    messagePublish = itemHoldResponse.getScreenMessage();
                    bsuccess = false;
                    saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, itemHoldResponse.getPatronIdentifier() + " - " + itemHoldResponse.getScreenMessage());
                }
            }
        } else {
            messagePublish = "Recall Cannot be processed, the item is not checked out in ILS";
            bsuccess = false;
        }
        itemResponseInformation.setScreenMessage(messagePublish);
        itemResponseInformation.setSuccess(bsuccess);
        return itemResponseInformation;
    }

    private ItemInformationResponse holdafterCreateBibCheck(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity, RequestTypeEntity requestTypeEntity) {
        boolean bsuccess = false;
        String deliveryCode;
        deliveryCode = itemRequestInfo.getDeliveryLocation();
        setpickupLoacation(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
        ItemHoldResponse itemHoldResponse = (ItemHoldResponse) requestItemController.holdItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
        if (itemHoldResponse.isSuccess()) {
            itemResponseInformation.setExpirationDate(itemHoldResponse.getExpirationDate());
            itemRequestInfo.setExpirationDate(itemHoldResponse.getExpirationDate());
            itemRequestInfo.setDeliveryLocation(deliveryCode);
            itemResponseInformation = checkInstAfterPlacingRequest(itemRequestInfo, itemResponseInformation, itemEntity, requestTypeEntity);
            bsuccess = true;
        } else {
            rollbackUpdateItemAvailabilutyStatus(itemEntity,itemRequestInfo.getUsername());
            saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, itemHoldResponse.getPatronIdentifier() + " - " + itemHoldResponse.getScreenMessage());
        }
        itemResponseInformation.setScreenMessage(itemResponseInformation.getScreenMessage());
        itemResponseInformation.setSuccess(bsuccess);
        return itemResponseInformation;
    }

    public void saveItemChangeLogEntity(Integer recordId, String userName, String operationType, String notes) {
        itemRequestDBService.saveItemChangeLogEntity(recordId, userName, operationType, notes);
    }

    private String getPatronIdBorrwingInsttution(String requestingInstitution, String owningInstitution) {
        String patronId = "";
        if (owningInstitution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                patronId = princetonCULPatron;
            } else if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
                patronId = princetonNYPLPatron;
            }
        } else if (owningInstitution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
                patronId = columbiaPULPatron;
            } else if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
                patronId = columbiaNYPLPatron;
            }
        } else if (owningInstitution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
            if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
                patronId = nyplPrincetonPatron;
            } else if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
                patronId = nyplColumbiaPatron;
            }
        }
        logger.info(patronId);
        return patronId;
    }

    private void setpickupLoacation(ItemRequestInformation itemRequestInfo, String institution) {
        if (institution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            itemRequestInfo.setDeliveryLocation(ReCAPConstants.DEFAULT_PICK_UP_LOCATION_PUL);
        } else if (institution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            itemRequestInfo.setDeliveryLocation(ReCAPConstants.DEFAULT_PICK_UP_LOCATION_CUL);
        } else if (institution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
            itemRequestInfo.setDeliveryLocation(ReCAPConstants.DEFAULT_PICK_UP_LOCATION_NYPL);
        }
    }

    private void updateSolrIndex(ItemEntity itemEntity) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity<>(getHttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverProtocol + scsbSolrClientUrl + ReCAPConstants.UPDATE_ITEM_STATUS_SOLR).queryParam(ReCAPConstants.UPDATE_ITEM_STATUS_SOLR_PARAM_ITEM_ID, itemEntity.getBarcode());
            ResponseEntity<String> responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, String.class);
            logger.info(responseEntity.getBody());
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
    }

    private List<SearchResultRow> searchRecords(ItemEntity itemEntity) {
        List<SearchResultRow> statusResponse = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity<>(getHttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverProtocol + scsbSolrClientUrl + ReCAPConstants.SEARCH_RECORDS_SOLR)
                    .queryParam(ReCAPConstants.SEARCH_RECORDS_SOLR_PARAM_FIELD_NAME, ReCAPConstants.SEARCH_RECORDS_SOLR_PARAM_FIELD_NAME_VALUE)
                    .queryParam(ReCAPConstants.SEARCH_RECORDS_SOLR_PARAM_FIELD_VALUE, itemEntity.getBarcode());
            ResponseEntity<List<SearchResultRow>> responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<SearchResultRow>>() {
            });
            statusResponse = responseEntity.getBody();
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return statusResponse;
    }

    public String getTitle(String title, ItemEntity itemEntity) {
        String titleIdentifier = "";
        String useRestrictions = ReCAPConstants.REQUEST_USE_RESTRICTIONS;
        String lTitle = "";
        try {
            if (itemEntity != null && itemEntity.getUseRestrictions() != null) {
                useRestrictions = itemEntity.getUseRestrictions();
            }
            if (!(title != null && title.trim().length() > 0)) {
                List<SearchResultRow> searchRecordsResponse = searchRecords(itemEntity);
                if (searchRecordsResponse != null && !searchRecordsResponse.isEmpty()) {
                    lTitle = searchRecordsResponse.get(0).getTitle();
                } else {
                    lTitle = "";
                }
            }

            if (lTitle != null && lTitle.trim().length() > 126) {
                lTitle = lTitle.toUpperCase().substring(126);
            } else if (lTitle != null && lTitle.trim().length() <= 0) {
                lTitle = "";
            }
            if (lTitle != null) {
                titleIdentifier = String.format("[%s] %s%s", useRestrictions, lTitle.toUpperCase(), ReCAPConstants.REQUEST_ITEM_TITLE_SUFFIX);
            }
            logger.info(titleIdentifier);
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return titleIdentifier;
    }

    private void getTempBibId(ItemRequestInformation itemRequestInfo, ItemEntity itemEntity) {
        itemRequestDBService.getTempBibId(itemRequestInfo, itemEntity);
    }

    private void createTempBibId(ItemRequestInformation itemRequestInfo, ItemEntity itemEntity) {
        itemRequestDBService.createTempBibId(itemRequestInfo, itemEntity);
    }

    private void rollbackAfterGFA(ItemEntity itemEntity, ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {
        rollbackUpdateItemAvailabilutyStatus(itemEntity,itemRequestInfo.getUsername());
        saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_GFA_FAILURE, itemRequestInfo.getPatronBarcode() + " - " + itemResponseInformation.getScreenMessage());
        String deliveryCode;
        deliveryCode = itemRequestInfo.getDeliveryLocation();
        setpickupLoacation(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
        requestItemController.cancelHoldItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
        itemRequestInfo.setDeliveryLocation(deliveryCode);
    }

    public String getUser(String userId) {
        return itemRequestDBService.getUser(userId);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(ReCAPConstants.API_KEY, ReCAPConstants.RECAP);
        return headers;
    }
}
