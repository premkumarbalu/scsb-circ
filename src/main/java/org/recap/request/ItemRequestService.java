package org.recap.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.DefaultFluentProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.controller.RequestItemController;
import org.recap.ils.model.response.ItemCreateBibResponse;
import org.recap.ils.model.response.ItemHoldResponse;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.ils.model.response.ItemRecallResponse;
import org.recap.model.*;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.RequestItemDetailsRepository;
import org.recap.repository.RequestItemStatusDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.Normalizer;
import java.util.Date;
import java.util.List;

/**
 * Created by sudhishk on 1/12/16.
 */
@Component
public class ItemRequestService {


    private static final Logger logger = LoggerFactory.getLogger(ItemRequestService.class);

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
    private String serverProtocol;

    @Value("${scsb.solr.client.url}")
    private String scsbSolrClientUrl;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private RequestItemController requestItemController;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Autowired
    private GFAService gfaService;

    @Autowired
    private ItemRequestDBService itemRequestDBService;

    private ItemRequestDBService getItemRequestDBService() {
        return itemRequestDBService;
    }

    private String getPrincetonCULPatron() {
        return princetonCULPatron;
    }

    private String getPrincetonNYPLPatron() {
        return princetonNYPLPatron;
    }

    private String getColumbiaPULPatron() {
        return columbiaPULPatron;
    }

    private String getColumbiaNYPLPatron() {
        return columbiaNYPLPatron;
    }

    private String getNyplPrincetonPatron() {
        return nyplPrincetonPatron;
    }

    private String getNyplColumbiaPatron() {
        return nyplColumbiaPatron;
    }

    private String getServerProtocol() {
        return serverProtocol;
    }

    private String getScsbSolrClientUrl() {
        return scsbSolrClientUrl;
    }

    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    private RequestItemController getRequestItemController() {
        return requestItemController;
    }

    private RequestItemDetailsRepository getRequestItemDetailsRepository() {
        return requestItemDetailsRepository;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    private RequestItemStatusDetailsRepository getRequestItemStatusDetailsRepository() {
        return requestItemStatusDetailsRepository;
    }

    public GFAService getGfaService() {
        return gfaService;
    }

    public ItemInformationResponse requestItem(ItemRequestInformation itemRequestInfo, Exchange exchange) {

        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        ItemInformationResponse itemResponseInformation = new ItemInformationResponse();
        try {
            itemEntities = getItemDetailsRepository().findByBarcodeIn(itemRequestInfo.getItemBarcodes());

            if (itemEntities != null && !itemEntities.isEmpty()) {
                itemEntity = itemEntities.get(0);
                if (StringUtils.isBlank(itemRequestInfo.getBibId())) {
                    itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                }
                itemRequestInfo.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                SearchResultRow searchResultRow = searchRecords(itemEntity);

                itemRequestInfo.setTitleIdentifier(getTitle(itemRequestInfo.getTitleIdentifier(), itemEntity, searchResultRow));
                itemRequestInfo.setAuthor(searchResultRow.getAuthor());
                itemRequestInfo.setCustomerCode(itemEntity.getCustomerCode());
                itemResponseInformation.setItemId(itemEntity.getItemId());
                // Change Item Availablity
                updateItemAvailabilutyStatus(itemEntities, itemRequestInfo.getUsername());
                itemResponseInformation = checkOwningInstitution(itemRequestInfo, itemResponseInformation, itemEntity);
            } else {
                itemResponseInformation.setScreenMessage(ReCAPConstants.WRONG_ITEM_BARCODE);
                itemResponseInformation.setSuccess(false);
            }
            itemResponseInformation = setItemResponseInformation(itemRequestInfo, itemResponseInformation);
            // Update Topics
            sendMessageToTopic(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getRequestType(), itemResponseInformation, exchange);
            logger.info(ReCAPConstants.FINISH_PROCESSING);
        } catch (RestClientException ex) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION_REST, ex);
        } catch (Exception ex) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, ex);
        }
        return itemResponseInformation;
    }

    public ItemInformationResponse recallItem(ItemRequestInformation itemRequestInfo, Exchange exchange) {

        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        ItemInformationResponse itemResponseInformation = new ItemInformationResponse();
        try {
            itemEntities = itemDetailsRepository.findByBarcodeIn(itemRequestInfo.getItemBarcodes());

            if (itemEntities != null && !itemEntities.isEmpty()) {
                itemEntity = itemEntities.get(0);
                itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                itemRequestInfo.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                itemResponseInformation.setItemId(itemEntity.getItemId());
                itemResponseInformation = checkOwningInstitutionRecall(itemRequestInfo, itemResponseInformation, itemEntity);
            } else {
                itemResponseInformation.setScreenMessage(ReCAPConstants.WRONG_ITEM_BARCODE);
                itemResponseInformation.setSuccess(false);
            }
            logger.info(ReCAPConstants.FINISH_PROCESSING);
            itemResponseInformation = setItemResponseInformation(itemRequestInfo, itemResponseInformation);
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
        String itemBarcode;
        ItemEntity itemEntity;
        List<RequestItemEntity> requestEntities = getRequestItemDetailsRepository().findByRequestIdIn(itemRefileRequest.getRequestIds());

        for (RequestItemEntity requestItemEntity : requestEntities) {
            itemEntity = requestItemEntity.getItemEntity();
            if (itemEntity.getItemAvailabilityStatusId().intValue() == 2) { // Only Item Not Availability Status is Processed
                itemBarcode = itemEntity.getBarcode();
                RequestStatusEntity requestStatusEntity = getRequestItemStatusDetailsRepository().findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_REFILED);
                if (requestItemEntity.getRequestTypeEntity().getRequestTypeCode().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
                    requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                    requestItemEntity.setLastUpdatedDate(new Date());
                    getRequestItemDetailsRepository().save(requestItemEntity);
                    bSuccess = true;
                } else {
                    RequestItemEntity requestItemEntityRecalled = getRequestItemDetailsRepository().findByItemBarcodeAndRequestStaCode(itemBarcode, ReCAPConstants.REQUEST_STATUS_RECALLED);
                    if (requestItemEntityRecalled == null) { // Recall Request Does not Exist
                        requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                        requestItemEntity.setLastUpdatedDate(new Date());
                        getRequestItemDetailsRepository().save(requestItemEntity);
                        rollbackUpdateItemAvailabilutyStatus(itemEntity, ReCAPConstants.GUEST_USER);
                        updateSolrIndex(itemEntity);
                        bSuccess = true;
                    } else { // Recall Request Exist
                        if (requestItemEntity.getRequestingInstitutionId().intValue() == requestItemEntity.getItemEntity().getOwningInstitutionId().intValue()) { // Borrowing Inst same as Owning
                            requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                            requestItemEntity.setLastUpdatedDate(new Date());
                            requestItemEntityRecalled.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                            requestItemEntityRecalled.setLastUpdatedDate(new Date());
                            getRequestItemDetailsRepository().save(requestItemEntity);
                            getRequestItemDetailsRepository().save(requestItemEntityRecalled);
                            rollbackUpdateItemAvailabilutyStatus(requestItemEntity.getItemEntity(), ReCAPConstants.GUEST_USER);
                            updateSolrIndex(requestItemEntity.getItemEntity());
                            bSuccess = true;
                        } else {
                            // Borrowing Inst not same as Owning, Change Retrieval Status to Refiled
                            requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                            getRequestItemDetailsRepository().save(requestItemEntity);
                            RequestStatusEntity requestStatusRO = getRequestItemStatusDetailsRepository().findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
                            // Change Existing Recall to Retrieval Order
                            requestItemEntityRecalled.setRequestStatusId(requestStatusRO.getRequestStatusId());
                            requestItemEntityRecalled.setLastUpdatedDate(new Date());
                            getRequestItemDetailsRepository().save(requestItemEntityRecalled);
                            bSuccess = true;
                        }
                    }
                }
                if (requestItemEntity != null) {
                    ItemRequestInformation itemRequestInfo = new ItemRequestInformation();
                    itemRequestInfo.setItemBarcodes(itemRefileRequest.getItemBarcodes());

                    itemRequestInfo.setItemOwningInstitution(requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionCode());
                    itemRequestInfo.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());

                    if (itemRequestInfo.getRequestingInstitution().equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
                        itemRequestInfo.setPatronBarcode(requestItemEntity.getPatronId());
                        getRequestItemController().checkinItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                    }
                    if (!itemRequestInfo.isOwningInstitutionItem() && (itemRequestInfo.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.NYPL) || itemRequestInfo.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.PRINCETON))) {
                        itemRequestInfo.setPatronBarcode(getPatronIdBorrwingInsttution(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution()));
                        getRequestItemController().checkinItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                    }
                }
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

    private ItemInformationResponse setItemResponseInformation(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInfo) {
        ItemInformationResponse itemResponseInformation = itemResponseInfo;
        itemResponseInformation.setDueDate(itemRequestInfo.getExpirationDate());
        itemResponseInformation.setBibID(itemRequestInfo.getBibId());
        itemResponseInformation.setItemOwningInstitution(itemRequestInfo.getItemOwningInstitution());
        itemResponseInformation.setRequestingInstitution(itemRequestInfo.getRequestingInstitution());
        itemResponseInformation.setPatronBarcode(itemRequestInfo.getPatronBarcode());
        itemResponseInformation.setRequestType(itemRequestInfo.getRequestType());
        itemResponseInformation.setEmailAddress(itemRequestInfo.getEmailAddress());
        itemResponseInformation.setDeliveryLocation(itemRequestInfo.getDeliveryLocation());
        itemResponseInformation.setRequestNotes(getNotes(itemResponseInformation.isSuccess(), itemResponseInformation.getScreenMessage(), itemRequestInfo.getRequestNotes()));
        itemResponseInformation.setItemBarcode(itemRequestInfo.getItemBarcodes().get(0));
        itemResponseInformation.setTitleIdentifier(itemRequestInfo.getTitleIdentifier());
        itemResponseInformation.setUsername(itemRequestInfo.getUsername());
        return itemResponseInformation;
    }

    public Integer updateRecapRequestItem(ItemRequestInformation itemRequestInformation, ItemEntity itemEntity, String requestStatusCode) {
        return getItemRequestDBService().updateRecapRequestItem(itemRequestInformation, itemEntity, requestStatusCode);
    }

    public ItemInformationResponse updateRecapRequestItem(ItemInformationResponse itemInformationResponse) {
        return getItemRequestDBService().updateRecapRequestItem(itemInformationResponse);
    }

    public ItemInformationResponse updateRecapRequestStatus(ItemInformationResponse itemInformationResponse) {
        return getItemRequestDBService().updateRecapRequestStatus(itemInformationResponse);
    }

    private void updateItemAvailabilutyStatus(List<ItemEntity> itemEntities, String username) {
        getItemRequestDBService().updateItemAvailabilutyStatus(itemEntities, username);
    }

    private void rollbackUpdateItemAvailabilutyStatus(ItemEntity itemEntity, String username) {
        getItemRequestDBService().rollbackUpdateItemAvailabilutyStatus(itemEntity, username);
    }

    public void saveItemChangeLogEntity(Integer recordId, String userName, String operationType, String notes) {
        getItemRequestDBService().saveItemChangeLogEntity(recordId, userName, operationType, notes);
    }

    public String getUser(String userId) {
        return getItemRequestDBService().getUser(userId);
    }

    protected ItemInformationResponse updateGFA(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {

        try {
            if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL) || itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
                itemResponseInformation = getGfaService().executeRetriveOrder(itemRequestInfo, itemResponseInformation);
            } else {
                itemResponseInformation.setSuccess(true);
                itemResponseInformation.setScreenMessage(ReCAPConstants.RETRIVAL_ORDER_NOT_REQUIRED_FOR_RECALL);
            }
        } catch (Exception e) {
            itemResponseInformation.setSuccess(false);
            itemResponseInformation.setScreenMessage(ReCAPConstants.REQUEST_SCSB_EXCEPTION + e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse checkOwningInstitution(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        try {
            if (itemRequestInfo.isOwningInstitutionItem()) {
                itemResponseInformation = hodlItem(itemRequestInfo.getItemOwningInstitution(), itemRequestInfo, itemResponseInformation, itemEntity);
            } else {// Not the Owning Institute
                // Get Temporary bibI from SCSB DB
                ItemCreateBibResponse createBibResponse;
                if (!ReCAPConstants.NYPL.equalsIgnoreCase(itemRequestInfo.getRequestingInstitution())) {
                    createBibResponse = (ItemCreateBibResponse) getRequestItemController().createBibliogrphicItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                } else {
                    createBibResponse = new ItemCreateBibResponse();
                }
                if (createBibResponse.isSuccess() || ReCAPConstants.NYPL.equalsIgnoreCase(itemRequestInfo.getRequestingInstitution())) {
                    itemRequestInfo.setBibId(createBibResponse.getBibId());
                    itemResponseInformation = hodlItem(itemRequestInfo.getRequestingInstitution(), itemRequestInfo, itemResponseInformation, itemEntity);
                } else {
                    itemResponseInformation.setScreenMessage(ReCAPConstants.REQUEST_ILS_EXCEPTION + ReCAPConstants.CREATING_A_BIB_RECORD_FAILED_IN_ILS);
                    itemResponseInformation.setSuccess(createBibResponse.isSuccess());
                    rollbackUpdateItemAvailabilutyStatus(itemEntity, itemRequestInfo.getUsername());
                    saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, createBibResponse.getBibId() + " - " + createBibResponse.getScreenMessage());
                }
            }
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
            itemResponseInformation.setScreenMessage(ReCAPConstants.REQUEST_SCSB_EXCEPTION + e.getMessage());
            itemResponseInformation.setSuccess(false);
            saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_ITEM_CHANGE_LOG_EXCEPTION, itemRequestInfo.getItemBarcodes() + " - " + e.getMessage());
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse checkInstAfterPlacingRequest(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        if (itemRequestInfo.isOwningInstitutionItem()) {
            itemResponseInformation = updateScsbAndGfa(itemRequestInfo, itemResponseInformation, itemEntity);
        } else { // Item does not belong to requesting Institute
            String requestingPatron = itemRequestInfo.getPatronBarcode();
            itemRequestInfo.setPatronBarcode(getPatronIdBorrwingInsttution(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution()));
            if (!itemRequestInfo.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                getRequestItemController().checkoutItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
            }
            itemRequestInfo.setPatronBarcode(requestingPatron);
            itemResponseInformation = updateScsbAndGfa(itemRequestInfo, itemResponseInformation, itemEntity);
        }
        if (itemResponseInformation.isSuccess()) {
            updateSolrIndex(itemEntity);
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse hodlItem(String callingInst, ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        ItemHoldResponse itemHoldResponse = (ItemHoldResponse) getRequestItemController().holdItem(itemRequestInfo, callingInst);
        if (itemHoldResponse.isSuccess()) { // IF Hold command is successfully
            itemResponseInformation.setExpirationDate(itemHoldResponse.getExpirationDate());
            itemRequestInfo.setExpirationDate(itemHoldResponse.getExpirationDate());
            itemResponseInformation = checkInstAfterPlacingRequest(itemRequestInfo, itemResponseInformation, itemEntity);
        } else { // If Hold command Failure
            itemResponseInformation.setScreenMessage(ReCAPConstants.REQUEST_ILS_EXCEPTION + itemHoldResponse.getScreenMessage());
            itemResponseInformation.setSuccess(itemHoldResponse.isSuccess());
            rollbackUpdateItemAvailabilutyStatus(itemEntity, itemRequestInfo.getUsername());
            saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, itemHoldResponse.getPatronIdentifier() + " - " + itemHoldResponse.getScreenMessage());
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse updateScsbAndGfa(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        Integer requestId;
        if (getGfaService().isUseQueueLasCall()) {
            requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, ReCAPConstants.REQUEST_STATUS_PENDING);
        } else {
            requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
        }
        itemResponseInformation.setRequestId(requestId);
        itemResponseInformation = updateGFA(itemRequestInfo, itemResponseInformation);
        if (itemResponseInformation.isSuccess()) {
            itemResponseInformation.setScreenMessage(ReCAPConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM);
        } else {
            rollbackAfterGFA(itemEntity, itemRequestInfo, itemResponseInformation);
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse checkOwningInstitutionRecall(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        String messagePublish;
        boolean bsuccess;
        RequestItemEntity requestItemEntity = getRequestItemDetailsRepository().findByItemBarcodeAndRequestStaCode(itemRequestInfo.getItemBarcodes().get(0), ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
        ItemInformationResponse itemInformation = (ItemInformationResponse) getRequestItemController().itemInformation(itemRequestInfo, requestItemEntity.getInstitutionEntity().getInstitutionCode());
        if (itemInformation.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_CHARGED)) {
            if (requestItemEntity.getInstitutionEntity().getInstitutionCode().equalsIgnoreCase(itemRequestInfo.getRequestingInstitution())) {
                ItemRecallResponse itemRecallResponse = (ItemRecallResponse) getRequestItemController().recallItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                if (itemRecallResponse.isSuccess()) {
                    // Update Recap DB
                    itemResponseInformation.setExpirationDate(itemRecallResponse.getExpirationDate());
                    itemRequestInfo.setExpirationDate(itemRecallResponse.getExpirationDate());
                    Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, ReCAPConstants.REQUEST_STATUS_RECALLED);
                    itemResponseInformation.setRequestId(requestId);
                    messagePublish = ReCAPConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM;
                    bsuccess = true;
                } else {
                    messagePublish = recallError(itemRecallResponse);
                    bsuccess = false;
                }
            } else {
                ItemHoldResponse itemHoldResponse = (ItemHoldResponse) getRequestItemController().holdItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                if (itemHoldResponse.isSuccess()) { // IF Hold command is successfully
                    itemResponseInformation.setExpirationDate(itemHoldResponse.getExpirationDate());
                    itemRequestInfo.setExpirationDate(itemHoldResponse.getExpirationDate());
                    ItemRecallResponse itemRecallResponse = (ItemRecallResponse) getRequestItemController().recallItem(itemRequestInfo, requestItemEntity.getInstitutionEntity().getInstitutionCode());
                    if (itemRecallResponse.isSuccess()) {
                        Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, ReCAPConstants.REQUEST_STATUS_RECALLED);
                        itemResponseInformation.setRequestId(requestId);
                        messagePublish = ReCAPConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM;
                        bsuccess = true;
                    } else {
                        messagePublish = recallError(itemRecallResponse);
                        bsuccess = false;
                    }
                } else { // If Hold command Failure
                    messagePublish = itemHoldResponse.getScreenMessage();
                    bsuccess = false;
                    saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, itemHoldResponse.getPatronIdentifier() + " - " + itemHoldResponse.getScreenMessage());
                }
            }
        } else {
            messagePublish = ReCAPConstants.REQUEST_SCSB_EXCEPTION + ReCAPConstants.RECALL_CANNOT_BE_PROCESSED_THE_ITEM_IS_NOT_CHECKED_OUT_IN_ILS;
            bsuccess = false;
        }
        itemResponseInformation.setScreenMessage(messagePublish);
        itemResponseInformation.setSuccess(bsuccess);
        return itemResponseInformation;
    }

    private String recallError(ItemRecallResponse itemRecallResponse) {
        if (itemRecallResponse.getScreenMessage() != null && itemRecallResponse.getScreenMessage().trim().length() > 0) {
            return ReCAPConstants.REQUEST_SCSB_EXCEPTION + itemRecallResponse.getScreenMessage();
        } else {
            return ReCAPConstants.REQUEST_SCSB_EXCEPTION + ReCAPConstants.RECALL_FAILED_NO_MESSAGE_RETURNED;
        }
    }

    private String getPatronIdBorrwingInsttution(String requestingInstitution, String owningInstitution) {
        String patronId = "";
        if (owningInstitution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                patronId = getPrincetonCULPatron();
            } else if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
                patronId = getPrincetonNYPLPatron();
            }
        } else if (owningInstitution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
                patronId = getColumbiaPULPatron();
            } else if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
                patronId = getColumbiaNYPLPatron();
            }
        } else if (owningInstitution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
            if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
                patronId = getNyplPrincetonPatron();
            } else if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                patronId = getNyplColumbiaPatron();
            }
        }
        logger.info(patronId);
        return patronId;
    }

    public void updateSolrIndex(ItemEntity itemEntity) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity<>(getHttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getServerProtocol() + getScsbSolrClientUrl() + ReCAPConstants.UPDATE_ITEM_STATUS_SOLR).queryParam(ReCAPConstants.UPDATE_ITEM_STATUS_SOLR_PARAM_ITEM_ID, itemEntity.getBarcode());
            ResponseEntity<String> responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, String.class);
            logger.info(responseEntity.getBody());
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
    }

    protected SearchResultRow searchRecords(ItemEntity itemEntity) {
        List<SearchResultRow> statusResponse = null;
        SearchResultRow searchResultRow = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity<>(getHttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getServerProtocol() + getScsbSolrClientUrl() + ReCAPConstants.SEARCH_RECORDS_SOLR)
                    .queryParam(ReCAPConstants.SEARCH_RECORDS_SOLR_PARAM_FIELD_NAME, ReCAPConstants.SEARCH_RECORDS_SOLR_PARAM_FIELD_NAME_VALUE)
                    .queryParam(ReCAPConstants.SEARCH_RECORDS_SOLR_PARAM_FIELD_VALUE, itemEntity.getBarcode());
            ResponseEntity<List<SearchResultRow>> responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<SearchResultRow>>() {
            });
            statusResponse = responseEntity.getBody();
            if (statusResponse != null && !statusResponse.isEmpty()) {
                searchResultRow = statusResponse.get(0);
            }
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return searchResultRow;
    }

    protected String getTitle(String title, ItemEntity itemEntity, SearchResultRow searchResultRow) {
        String titleIdentifier = "";
        String useRestrictions = ReCAPConstants.REQUEST_USE_RESTRICTIONS;
        String lTitle = "";
        String returnTitle = "";
        try {
            if (itemEntity != null && itemEntity.getUseRestrictions() != null) {
                useRestrictions = itemEntity.getUseRestrictions();
            }
            if (!(title != null && title.trim().length() > 0)) {
                if (searchResultRow != null) {
                    lTitle = searchResultRow.getTitle();
                } else {
                    lTitle = "";
                }
            } else {
                lTitle = title;
            }

            if (lTitle != null && lTitle.trim().length() > 126) {
                lTitle = lTitle.toUpperCase().substring(126);
            } else if (lTitle != null && lTitle.trim().length() <= 0) {
                lTitle = "";
            }
            if (lTitle != null) {
                titleIdentifier = String.format("[%s] %s%s", useRestrictions, lTitle.toUpperCase(), ReCAPConstants.REQUEST_ITEM_TITLE_SUFFIX);
            }
            returnTitle = removeDiacritical(titleIdentifier);
            logger.info(returnTitle);
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return returnTitle;
    }

    private void rollbackAfterGFA(ItemEntity itemEntity, ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {
        rollbackUpdateItemAvailabilutyStatus(itemEntity, itemRequestInfo.getUsername());
        saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_GFA_FAILURE, itemRequestInfo.getPatronBarcode() + " - " + itemResponseInformation.getScreenMessage());
        getRequestItemController().cancelHoldItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
    }

    private void rollbackAfterGFA(ItemInformationResponse itemResponseInformation) {
        ItemRequestInformation itemRequestInformation = getItemRequestDBService().rollbackAfterGFA(itemResponseInformation);
        getRequestItemController().cancelHoldItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
    }

    protected String getNotes(boolean success, String screenMessage, String userNotes) {
        String notes = "";
        if (!StringUtils.isBlank(userNotes)) {
            notes = String.format("User: %s", userNotes);
        }
        if (!success && !StringUtils.isBlank(screenMessage)) {
            if (!StringUtils.isBlank(notes)) {
                notes += "\n";
            }
            notes += screenMessage;
        }
        return notes;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(ReCAPConstants.API_KEY, ReCAPConstants.RECAP);
        return headers;
    }

    public void processLASRetrieveResponse(String body) {
        ItemInformationResponse itemInformationResponse = getGfaService().processLASRetrieveResponse(body);
        if (itemInformationResponse.isSuccess()) {
            updateRecapRequestStatus(itemInformationResponse);
        } else {
            updateRecapRequestStatus(itemInformationResponse);
            rollbackAfterGFA(itemInformationResponse);
        }
    }

    private static String removeDiacritical(String text) {
        return text == null ? null : Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
