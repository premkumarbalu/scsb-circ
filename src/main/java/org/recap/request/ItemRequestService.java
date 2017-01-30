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
import org.recap.ils.model.response.*;
import org.recap.model.*;
import org.recap.mqconsumer.RequestItemQueueConsumer;
import org.recap.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.DateFormat;
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

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

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
    EmailService emailService;

    @Autowired
    RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;
    @Autowired
    GFAService gfaService;

    public ItemInformationResponse requestItem(ItemRequestInformation itemRequestInfo, Exchange exchange) {

        String messagePublish = "";
        boolean bsuccess = false;
        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        RequestTypeEntity requestTypeEntity = new RequestTypeEntity();
        ItemInformationResponse itemResponseInformation = new ItemInformationResponse();
        ResponseEntity res = null;

        try {
            itemEntities = itemDetailsRepository.findByBarcodeIn(itemRequestInfo.getItemBarcodes());

            if (itemEntities != null && itemEntities.size() > 0) {
                logger.info("Item Exists in SCSB Database");
                itemEntity = itemEntities.get(0);
                if (itemRequestInfo.getBibId().trim().length() <= 0) {
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
                    updateItemAvailabilutyStatus(itemEntities);
                    // Action based on Request Type
                    if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
                        requestTypeEntity = requestTypeDetailsRepository.findByrequestTypeCode(itemRequestInfo.getRequestType());

                        itemResponseInformation = checkOwningInstitution(itemRequestInfo, itemResponseInformation, itemEntity, requestTypeEntity);
                        messagePublish = itemResponseInformation.getScreenMessage();

                    } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
                        updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
                        itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
                    } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT)) {
                        updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
                        itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
                    }
                } else {
                    logger.warn("Validate Request Errors : " + res.getBody().toString());
                    messagePublish = res.getBody().toString();
                    bsuccess = false;
                }
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

            // Update Topics
            sendMessageToTopic(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getRequestType(), itemResponseInformation, exchange);
        } catch (RestClientException ex) {
            logger.error("RestClient : " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            logger.error("Exception : " + ex.getMessage());
            ex.printStackTrace();
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

            if (itemEntities != null && itemEntities.size() > 0) {
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
                    bsuccess = true;
                } else {

                    messagePublish = res.getBody().toString();
                    bsuccess = false;
                }
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

            // Update Topics
            sendMessageToTopic(itemRequestInfo.getItemOwningInstitution(), itemRequestInfo.getRequestType(), itemResponseInformation, exchange);
        } catch (RestClientException ex) {
            logger.error("RestClient : " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            logger.error("Exception : " + ex.getMessage());
            ex.printStackTrace();
        }
        return itemResponseInformation;
    }

    public boolean reFileItem(ItemRefileRequest itemRefileRequest) {

        // Change Response for this Method
        boolean bSuccess = false;
        List<ItemEntity> itemEntities = null;
        String ItemBarcode = itemRefileRequest.getItemBarcodes().get(0);
        RequestItemEntity requestItemEntity = null;

        RequestItemEntity requestItemEntityRecalled = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(ItemBarcode, ReCAPConstants.REQUEST_STATUS_RECALLED);
        if (requestItemEntityRecalled == null) { // Recal Request Does not Exist
            requestItemEntity = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(ItemBarcode, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
            if (requestItemEntity != null) {  // Check Retrival Order
                bSuccess = updateItemStatus_SolrIndexing(itemEntities, itemRefileRequest);
            }
        } else { // Recall Request Exist
            requestItemEntity = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(ItemBarcode, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
            if (requestItemEntity.getRequestingInstitutionId().intValue() == requestItemEntity.getItemEntity().getOwningInstitutionId().intValue()) { // Borrowing Inst same as Owning
                RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_REFILED);
                requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                requestItemEntityRecalled.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                requestItemDetailsRepository.save(requestItemEntity);
                requestItemDetailsRepository.save(requestItemEntityRecalled);
                rollbackUpdateItemAvailabilutyStatus(requestItemEntity.getItemEntity());
                updateSolrIndex(requestItemEntity.getItemEntity());
                bSuccess = true;
            } else { // Borrowing Inst not same as Owning
                // New Request for Retival
                RequestItemEntity requestItemEntityNew = new RequestItemEntity();
                requestItemEntityNew.setItemId(requestItemEntity.getItemId());
                requestItemEntityNew.setRequestingInstitutionId(requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionId());
                requestItemEntityNew.setRequestTypeId(requestItemEntity.getRequestTypeId());
                requestItemEntityNew.setRequestExpirationDate(requestItemEntity.getRequestExpirationDate());
                requestItemEntityNew.setCreatedBy(ReCAPConstants.GUEST_USER);
                requestItemEntityNew.setCreatedDate(new Date());
                requestItemEntityNew.setLastUpdatedDate(new Date());
                requestItemEntityNew.setPatronId(requestItemEntity.getPatronId());
                requestItemEntityNew.setStopCode(requestItemEntity.getStopCode());
                requestItemEntityNew.setRequestStatusId(requestItemEntity.getRequestStatusId());

                // Change Retrieval Status
                requestItemEntity.getRequestStatusEntity().setRequestStatusCode(ReCAPConstants.REQUEST_STATUS_REFILED);
                requestItemDetailsRepository.save(requestItemEntity);
                // Change Existing Recall to RECALL_RETRIEVAL_ORDER_PLACED
                requestItemEntityRecalled.getRequestStatusEntity().setRequestStatusCode(ReCAPConstants.REQUEST_STATUS_RECALL_RETRIEVAL_ORDER_PLACED);
                requestItemDetailsRepository.save(requestItemEntityRecalled);
                // Create New Retival Order

                requestItemDetailsRepository.save(requestItemEntityRecalled);

                requestItemDetailsRepository.save(requestItemEntityNew);

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

    private void sendMessageToTopic(String owningInstituteId, String requestType, ItemInformationResponse itemResponseInfo, Exchange exchange) {
        String selectTopic = ReCAPConstants.PUL_REQUEST_TOPIC;
        if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
            selectTopic = ReCAPConstants.PUL_REQUEST_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
            selectTopic = ReCAPConstants.PUL_EDD_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)) {
            selectTopic = ReCAPConstants.PUL_RECALL_TOPIC;
        } else if (owningInstituteId.equalsIgnoreCase(ReCAPConstants.PRINCETON) && requestType.equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT)) {
            selectTopic = ReCAPConstants.PUL_BORROW_DIRECT_TOPIC;
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
            logger.error(e.getMessage());
        }
        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(exchange.getContext());
        fluentProducerTemplate
                .to(selectTopic)
                .withBody(json);
        fluentProducerTemplate.send();
    }

    private Integer updateRecapRequestItem(ItemRequestInformation itemRequestInformation, ItemEntity itemEntity, RequestTypeEntity requestTypeEntity, String requestStatusCode) {

        RequestItemEntity requestItemEntity = new RequestItemEntity();
        PatronEntity patronEntity = null;
        InstitutionEntity institutionEntity = null;
        PatronEntity savedPatronEntity = null;
        RequestItemEntity savedItemRequest = null;
        try {

            // Patron Information
            patronEntity = patronDetailsRepository.findByInstitutionIdentifier(itemRequestInformation.getPatronBarcode());
            RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(requestStatusCode);
            if (patronEntity == null) {
                patronEntity = new PatronEntity();
                patronEntity.setInstitutionIdentifier(itemRequestInformation.getPatronBarcode());
                patronEntity.setInstitutionId(itemEntity.getInstitutionEntity().getInstitutionId());
                patronEntity.setEmailId(itemRequestInformation.getEmailAddress());
                savedPatronEntity = patronDetailsRepository.save(patronEntity);
            } else {
                savedPatronEntity = patronEntity;
            }

            requestItemEntity.setItemId(itemEntity.getItemId());
            requestItemEntity.setRequestingInstitutionId(itemEntity.getInstitutionEntity().getInstitutionId());
            requestItemEntity.setRequestTypeId(requestTypeEntity.getRequestTypeId());
            if (ReCAPConstants.NYPL.equalsIgnoreCase(itemRequestInformation.getRequestingInstitution())) {
                DateFormat dateFormatter = new SimpleDateFormat(ReCAPConstants.NYPL_HOLD_DATE_FORMAT);
                requestItemEntity.setRequestExpirationDate(dateFormatter.parse(itemRequestInformation.getExpirationDate()));
            } else {
                if (itemRequestInformation.getExpirationDate() != null && itemRequestInformation.getExpirationDate().trim().length() > 0) {
                    requestItemEntity.setRequestExpirationDate(simpleDateFormat.parse(itemRequestInformation.getExpirationDate()));
                }
            }
            requestItemEntity.setCreatedBy(ReCAPConstants.GUEST_USER);
            requestItemEntity.setCreatedDate(new Date());
            requestItemEntity.setLastUpdatedDate(new Date());
            requestItemEntity.setPatronId(savedPatronEntity.getPatronId());
            requestItemEntity.setStopCode(itemRequestInformation.getDeliveryLocation());
            requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());

            savedItemRequest = requestItemDetailsRepository.save(requestItemEntity);
            saveItemChangeLogEntity(savedItemRequest.getRequestId(), "Guest", "Request Item Insert", savedItemRequest.getItemId() + " - " + savedItemRequest.getPatronId());

            logger.info("SCSB DB Update Successful");
        } catch (ParseException e) {
            logger.error("ParseException : ", e);
        } catch (Exception e) {
            logger.error("Exception", e);
            e.printStackTrace();
        }
        return savedItemRequest.getRequestId();
    }

    private void updateItemAvailabilutyStatus(List<ItemEntity> itemEntities) {
        for (int i = 0; i < itemEntities.size(); i++) {
            ItemEntity itemEntity = itemEntities.get(i);
            itemEntity.setItemAvailabilityStatusId(2);
            saveItemChangeLogEntity(itemEntity.getItemId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_AVAILABILITY_STATUS_UPDATE, ReCAPConstants.REQUEST_ITEM_AVAILABILITY_STATUS_DATA_UPDATE);
        }
        // Not Available
        itemDetailsRepository.save(itemEntities);

    }

    private void rollbackUpdateItemAvailabilutyStatus(ItemEntity itemEntity) {
        itemEntity.setItemAvailabilityStatusId(1); // Not Available
        itemDetailsRepository.save(itemEntity);
        saveItemChangeLogEntity(itemEntity.getItemId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_AVAILABILITY_STATUS_UPDATE, ReCAPConstants.REQUEST_ITEM_AVAILABILITY_STATUS_DATA_ROLLBACK);
    }

    private boolean updateItemStatus_SolrIndexing(List<ItemEntity> itemEntities, ItemRefileRequest itemRefileRequest) {
        boolean bSuccess = false;
        itemEntities = itemDetailsRepository.findByBarcodeIn(itemRefileRequest.getItemBarcodes());
        if (itemEntities != null && itemEntities.size() > 0) {
            for (int i = 0; i < itemEntities.size(); i++) {
                ItemEntity itemEntity = itemEntities.get(i);
                if (itemEntity.getItemAvailabilityStatusId().intValue() == 2) {
                    rollbackUpdateItemAvailabilutyStatus(itemEntity);
                    updateSolrIndex(itemEntity);
                    bSuccess = true;
                } else {
                    bSuccess = false;
                }
            }
        }
        return bSuccess;
    }

    private ItemInformationResponse updateGFA(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {

        try {
            gfaService.executeRetriveOrder(itemRequestInfo, itemResponseInformation);
        } catch (Exception e) {
            logger.error(e.getMessage());
            itemResponseInformation.setSuccess(false);
            itemResponseInformation.setScreenMessage(e.getMessage());
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
                } else { // If Hold command Failure
                    messagePublish = itemHoldResponse.getScreenMessage();
                    bsuccess = false;
                    rollbackUpdateItemAvailabilutyStatus(itemEntity);
                    saveItemChangeLogEntity(itemEntity.getItemId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, itemHoldResponse.getPatronIdentifier() + " - " + itemHoldResponse.getScreenMessage());
                }

            } else {// Not the Owning Institute
                ItemCreateBibResponse createBibResponse = new ItemCreateBibResponse();
                if (!ReCAPConstants.NYPL.equalsIgnoreCase(itemRequestInfo.getRequestingInstitution())) {
                    createBibResponse = (ItemCreateBibResponse) requestItemController.createBibliogrphicItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                }
                if (createBibResponse.isSuccess() || ReCAPConstants.NYPL.equalsIgnoreCase(itemRequestInfo.getRequestingInstitution())) {
                    deliveryCode = itemRequestInfo.getDeliveryLocation();
                    setpickupLoacation(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                    ItemHoldResponse itemHoldResponse = (ItemHoldResponse) requestItemController.holdItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                    if (itemHoldResponse.isSuccess()) {
                        itemResponseInformation.setExpirationDate(itemHoldResponse.getExpirationDate());
                        itemRequestInfo.setExpirationDate(itemHoldResponse.getExpirationDate());
                        itemRequestInfo.setDeliveryLocation(deliveryCode);
                        itemResponseInformation = checkInstAfterPlacingRequest(itemRequestInfo, itemResponseInformation, itemEntity, requestTypeEntity);
                        bsuccess = true;
                        messagePublish = itemResponseInformation.getScreenMessage();
                    } else {
                        messagePublish = itemHoldResponse.getScreenMessage();
                        bsuccess = false;
                        rollbackUpdateItemAvailabilutyStatus(itemEntity);
                        saveItemChangeLogEntity(itemEntity.getItemId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, itemHoldResponse.getPatronIdentifier() + " - " + itemHoldResponse.getScreenMessage());
                    }

                } else {
                    messagePublish = createBibResponse.getScreenMessage();
                    bsuccess = false;
                    rollbackUpdateItemAvailabilutyStatus(itemEntity);
                    saveItemChangeLogEntity(itemEntity.getItemId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, createBibResponse.getBibId() + " - " + createBibResponse.getScreenMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Exception : ", e);
            messagePublish = "Failed to process request.";
            bsuccess = false;
            saveItemChangeLogEntity(itemEntity.getItemId(), ReCAPConstants.GUEST_USER, "RequestItem - Exception", itemRequestInfo.getItemBarcodes() + " - " + e.getMessage());
        }
        itemResponseInformation.setScreenMessage(messagePublish);
        itemResponseInformation.setSuccess(bsuccess);
        return itemResponseInformation;
    }

    private ItemInformationResponse checkInstAfterPlacingRequest(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity, RequestTypeEntity requestTypeEntity) {
        String messagePublish = "";
        boolean bsuccess = false;
        if (itemRequestInfo.isOwningInstitutionItem()) {
            // GFA
            itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
            // Update Recap DB
            Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
            itemResponseInformation.setRequestId(requestId);
            itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
            messagePublish = "Successfully Processed Request Item";
            bsuccess = true;
        } else { // Item does not belong to requesting Institute
            String requestingPatron = itemRequestInfo.getPatronBarcode();
            itemRequestInfo.setPatronBarcode(getPatronIdBorrwingInsttution(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution()));
            if (!itemRequestInfo.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                AbstractResponseItem checkoutItemResponse = requestItemController.checkoutItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
            }
            // GFA
            itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
            // Update Recap DB
            Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
            itemResponseInformation.setRequestId(requestId);
            messagePublish = "Successfully Processed Request Item";
            bsuccess = true;
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
        String messagePublish = "";
        String deliveryCode = "";
        boolean bsuccess = false;
        RequestTypeEntity requestTypeEntity = null;

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
                    Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity, ReCAPConstants.REQUEST_STATUS_RECALLED);
                    itemResponseInformation.setRequestId(requestId);
                    messagePublish = "Successfully Processed Request Item";
                    bsuccess = true;

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
                        // Update Recap DB
                        Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, requestTypeEntity, ReCAPConstants.REQUEST_STATUS_RECALLED);
                        itemResponseInformation.setRequestId(requestId);
                        messagePublish = "Successfully Processed Request Item";
                        bsuccess = true;
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
                    saveItemChangeLogEntity(itemEntity.getItemId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, itemHoldResponse.getPatronIdentifier() + " - " + itemHoldResponse.getScreenMessage());
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

    public void saveItemChangeLogEntity(Integer recordId, String userName, String operationType, String notes) {
        ItemChangeLogEntity itemChangeLogEntity = new ItemChangeLogEntity();
        itemChangeLogEntity.setUpdatedBy(userName);
        itemChangeLogEntity.setUpdatedDate(new Date());
        itemChangeLogEntity.setOperationType(operationType);
        itemChangeLogEntity.setRecordId(recordId);
        itemChangeLogEntity.setNotes(notes);
        itemChangeLogDetailsRepository.save(itemChangeLogEntity);
    }

    private String getPatronIdBorrwingInsttution(String requestingInstitution, String OwningInstitution) {
        String patronId = "";
        if (OwningInstitution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                patronId = princetonCULPatron;
            } else if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
                patronId = princetonNYPLPatron;
            }
        } else if (OwningInstitution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
                patronId = columbiaPULPatron;
            } else if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
                patronId = columbiaNYPLPatron;
            }
        } else if (OwningInstitution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
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
        } catch (Exception e) {
            logger.error("Exception : ", e);
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
            logger.error("Exception : ", e);
        }
        return statusResponse;
    }

    private String getTitle(String title, ItemEntity itemEntity) {
        String titleIdentifier = "";
        String useRestrictions = "No Restrictions";
        try {
            if (itemEntity.getUseRestrictions() != null) {
                useRestrictions = itemEntity.getUseRestrictions();
            }
            if (!(title != null && title.trim().length() > 0)) {
                List<SearchResultRow> searchRecordsResponse = searchRecords(itemEntity);
                title = searchRecordsResponse.get(0).getTitle();
            }

            if (title.trim().length() > 126) {
                title.toUpperCase().substring(126);
            }
            titleIdentifier = "[" + useRestrictions + "] " + title.toUpperCase() + ReCAPConstants.REQUEST_ITEM_TITLE_SUFFIX;
            logger.info(titleIdentifier);
        } catch (Exception e) {
            logger.error("Exception : ", e);
        }
        return titleIdentifier;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(ReCAPConstants.API_KEY, ReCAPConstants.RECAP);
        return headers;
    }


}
