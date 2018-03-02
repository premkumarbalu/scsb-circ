package org.recap.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.DefaultFluentProducerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.controller.RequestItemController;
import org.recap.ils.model.response.ItemCreateBibResponse;
import org.recap.ils.model.response.ItemHoldResponse;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.ils.model.response.ItemRecallResponse;
import org.recap.model.*;
import org.recap.repository.*;
import org.recap.service.RestHeaderService;
import org.recap.util.ItemRequestServiceUtil;
import org.recap.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class for Request Item Service
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

    @Autowired
    private CustomerCodeDetailsRepository customerCodeDetailsRepository;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private RestHeaderService restHeaderService;

    @Autowired
    private ItemRequestServiceUtil itemRequestServiceUtil;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private RequestParamaterValidatorService requestParamaterValidatorService;

    @Autowired
    private ItemValidatorService itemValidatorService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * @return
     */
    public RestHeaderService getRestHeaderService() {
        return restHeaderService;
    }

    /**
     * @return
     */
    public EmailService getEmailService() {
        return emailService;
    }

    /**
     * @return
     */
    public GFAService getGfaService() {
        return gfaService;
    }

    /**
     * Request item item information response.
     *
     * @param itemRequestInfo the item request info
     * @param exchange        the exchange
     * @return the item information response
     */
    public ItemInformationResponse requestItem(ItemRequestInformation itemRequestInfo, Exchange exchange) {

        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        ItemInformationResponse itemResponseInformation = new ItemInformationResponse();
        try {
            itemEntities = itemDetailsRepository.findByBarcodeIn(itemRequestInfo.getItemBarcodes());

            if (itemEntities != null && !itemEntities.isEmpty()) {
                itemEntity = itemEntities.get(0);
                CustomerCodeEntity customerCodeEntity = customerCodeDetailsRepository.findByCustomerCode(itemRequestInfo.getDeliveryLocation());
                if (StringUtils.isBlank(itemRequestInfo.getBibId())) {
                    itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                }
                itemRequestInfo.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                SearchResultRow searchResultRow = searchRecords(itemEntity); //Solr

                itemRequestInfo.setTitleIdentifier(getTitle(itemRequestInfo.getTitleIdentifier(), itemEntity, searchResultRow));
                itemRequestInfo.setAuthor(searchResultRow.getAuthor());
                itemRequestInfo.setCustomerCode(itemEntity.getCustomerCode());
                itemRequestInfo.setPickupLocation(customerCodeEntity.getPickupLocation());
                itemResponseInformation.setItemId(itemEntity.getItemId());

                boolean isItemStatusAvailable;
                synchronized (this) {
                    // Change Item Availablity
                    isItemStatusAvailable = updateItemAvailabilutyStatus(itemEntities, itemRequestInfo.getUsername());
                }

                Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, ReCAPConstants.REQUEST_STATUS_PROCESSING);
                itemRequestInfo.setRequestId(requestId);
                itemResponseInformation.setRequestId(requestId);

                if (requestId == 0) {
                    rollbackUpdateItemAvailabilutyStatus(itemEntity, itemRequestInfo.getUsername());
                    itemResponseInformation.setScreenMessage(ReCAPConstants.REQUEST_EXCEPTION + ReCAPConstants.INTERNAL_ERROR_DURING_REQUEST);
                    itemResponseInformation.setSuccess(false);
                } else if (!isItemStatusAvailable) {
                    itemResponseInformation.setScreenMessage(ReCAPConstants.REQUEST_SCSB_EXCEPTION + ReCAPConstants.RETRIEVAL_NOT_FOR_UNAVAILABLE_ITEM);
                    itemResponseInformation.setSuccess(false);
                } else {
                    // Process
                    itemResponseInformation = checkOwningInstitution(itemRequestInfo, itemResponseInformation, itemEntity);
                }
            } else {
                itemResponseInformation.setScreenMessage(ReCAPConstants.REQUEST_SCSB_EXCEPTION + ReCAPConstants.WRONG_ITEM_BARCODE);
                itemResponseInformation.setSuccess(false);
            }
            itemResponseInformation = setItemResponseInformation(itemRequestInfo, itemResponseInformation);

            if (isUseQueueLasCall() && (StringUtils.containsIgnoreCase(itemResponseInformation.getScreenMessage(), ReCAPConstants.REQUEST_ILS_EXCEPTION)
                    || StringUtils.containsIgnoreCase(itemResponseInformation.getScreenMessage(), ReCAPConstants.REQUEST_SCSB_EXCEPTION)
                    || StringUtils.containsIgnoreCase(itemResponseInformation.getScreenMessage(), ReCAPConstants.REQUEST_LAS_EXCEPTION))) {
                updateChangesToDb(itemResponseInformation, ReCAPConstants.REQUEST_RETRIEVAL + "-" + itemResponseInformation.getRequestingInstitution());
            }
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

    /**
     * Update request status based on success message from ILS
     *
     * @param itemResponseInformation
     * @param operationType
     */
    public void updateChangesToDb(ItemInformationResponse itemResponseInformation, String operationType) {
        if (itemResponseInformation.getRequestId() != null && itemResponseInformation.getRequestId() > 0) {
            saveItemChangeLogEntity(itemResponseInformation.getRequestId(), getUser(itemResponseInformation.getUsername()), operationType, itemResponseInformation.getRequestNotes());
            updateRecapRequestItem(itemResponseInformation);
        }
    }

    /**
     * Recall item item information response.
     *
     * @param itemRequestInfo the item request info
     * @param exchange        the exchange
     * @return the item information response
     */
    public ItemInformationResponse recallItem(ItemRequestInformation itemRequestInfo, Exchange exchange) {

        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        ItemInformationResponse itemResponseInformation = new ItemInformationResponse();
        try {
            itemEntities = itemDetailsRepository.findByBarcodeIn(itemRequestInfo.getItemBarcodes());

            if (itemEntities != null && !itemEntities.isEmpty()) {
                itemEntity = itemEntities.get(0);
                SearchResultRow searchResultRow = searchRecords(itemEntity); //Solr

                itemRequestInfo.setTitleIdentifier(getTitle(itemRequestInfo.getTitleIdentifier(), itemEntity, searchResultRow));
                itemRequestInfo.setAuthor(searchResultRow.getAuthor());
                itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                itemRequestInfo.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                itemRequestInfo.setPickupLocation(getPickupLocation(itemRequestInfo.getDeliveryLocation()));
                itemResponseInformation.setItemId(itemEntity.getItemId());
                Integer requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, ReCAPConstants.REQUEST_STATUS_PROCESSING);
                itemRequestInfo.setRequestId(requestId);
                itemResponseInformation.setRequestId(requestId);

                if (requestId == 0) {
                    itemResponseInformation.setScreenMessage(ReCAPConstants.REQUEST_EXCEPTION + ReCAPConstants.INTERNAL_ERROR_DURING_REQUEST);
                    itemResponseInformation.setSuccess(false);
                } else {
                    itemResponseInformation = checkOwningInstitutionRecall(itemRequestInfo, itemResponseInformation, itemEntity);
                }
            } else {
                itemResponseInformation.setScreenMessage(ReCAPConstants.REQUEST_SCSB_EXCEPTION + ReCAPConstants.WRONG_ITEM_BARCODE);
                itemResponseInformation.setSuccess(false);
            }
            logger.info(ReCAPConstants.FINISH_PROCESSING);
            itemResponseInformation = setItemResponseInformation(itemRequestInfo, itemResponseInformation);

            if (isUseQueueLasCall()) {
                updateChangesToDb(itemResponseInformation, ReCAPConstants.REQUEST_RECALL + "-" + itemResponseInformation.getRequestingInstitution());
            }
            // Update Topics
            sendMessageToTopic(itemRequestInfo.getItemOwningInstitution(), itemRequestInfo.getRequestType(), itemResponseInformation, exchange);
        } catch (RestClientException ex) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION_REST, ex);
        } catch (Exception ex) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, ex);
        }
        return itemResponseInformation;
    }

    /**
     * Re file item boolean.
     *
     * @param itemRefileRequest the item refile request
     * @return the boolean
     */
    public boolean reFileItem(ItemRefileRequest itemRefileRequest) {

        // Change Response for this Method
        boolean bSuccess = false;
        String itemBarcode;
        ItemEntity itemEntity;
        List<String> requestItemStatusList = Arrays.asList(ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED, ReCAPConstants.REQUEST_STATUS_EDD, ReCAPConstants.REQUEST_STATUS_CANCELED, ReCAPConstants.REQUEST_STATUS_INITIAL_LOAD);
        List<RequestItemEntity> requestEntities = requestItemDetailsRepository.findByRequestIdsAndStatusCodes(itemRefileRequest.getRequestIds(), requestItemStatusList);

        for (RequestItemEntity requestItemEntity : requestEntities) {
            itemEntity = requestItemEntity.getItemEntity();
            RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_REFILED);
            if (itemEntity.getItemAvailabilityStatusId() == 2) { // Only Item Not Availability, Status is Processed
                itemBarcode = itemEntity.getBarcode();
                ItemRequestInformation itemRequestInfo = new ItemRequestInformation();
                itemRequestInfo.setItemBarcodes(Collections.singletonList(itemBarcode));
                itemRequestInfo.setItemOwningInstitution(requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionCode());
                itemRequestInfo.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());

                RequestItemEntity requestItemEntityRecalled = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(itemBarcode, ReCAPConstants.REQUEST_STATUS_RECALLED);
                if (requestItemEntityRecalled == null) { // Recall Request Does not Exist
                    requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                    requestItemEntity.setLastUpdatedDate(new Date());
                    requestItemDetailsRepository.save(requestItemEntity);
                    rollbackUpdateItemAvailabilutyStatus(itemEntity, ReCAPConstants.GUEST_USER);
                    itemRequestServiceUtil.updateSolrIndex(itemEntity);
                    bSuccess = true;
                } else { // Recall Request Exist
                    if (requestItemEntityRecalled.getRequestingInstitutionId().intValue() == requestItemEntity.getRequestingInstitutionId().intValue()) { // Borrowed Inst same as Recall Requesting Inst
                        requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                        requestItemEntity.setLastUpdatedDate(new Date());
                        requestItemEntityRecalled.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                        requestItemEntityRecalled.setLastUpdatedDate(new Date());
                        requestItemDetailsRepository.save(requestItemEntity);
                        requestItemDetailsRepository.save(requestItemEntityRecalled);
                        rollbackUpdateItemAvailabilutyStatus(requestItemEntity.getItemEntity(), ReCAPConstants.GUEST_USER);
                        itemRequestServiceUtil.updateSolrIndex(requestItemEntity.getItemEntity());
                        bSuccess = true;
                    } else { // Borrowed Inst not same as Recall Requesting Inst, Change Retrieval Order Status to Refiled.
                        requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                        requestItemDetailsRepository.save(requestItemEntity);

                        // Checkout the item in Princeton and NYPL for the Recall order
                        if (!requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionCode().equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                            itemRequestInfo.setPatronBarcode(getPatronIdBorrwingInsttution(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution()));
                            requestItemController.checkoutItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                        }

                        itemRequestInfo.setRequestId(requestItemEntityRecalled.getRequestId());
                        itemRequestInfo.setRequestType(ReCAPConstants.REQUEST_TYPE_RETRIEVAL);
                        itemRequestInfo.setRequestNotes(requestItemEntityRecalled.getNotes());
                        itemRequestInfo.setUsername(requestItemEntityRecalled.getCreatedBy());
                        itemRequestInfo.setDeliveryLocation(requestItemEntityRecalled.getStopCode());
                        itemRequestInfo.setCustomerCode(itemEntity.getCustomerCode());
                        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
                        // Put back the Recall order to LAS. On success from LAS, recall order is updated to retrieval.
                        updateScsbAndGfa(itemRequestInfo, itemInformationResponse, itemEntity);
                        bSuccess = true;
                    }
                }
                logger.info("Refile Request Id = {} Refile Barcode = {}", requestItemEntity.getRequestId(), itemBarcode);
                if (itemRequestInfo.getRequestingInstitution().equalsIgnoreCase(ReCAPConstants.PRINCETON) || itemRequestInfo.getRequestingInstitution().equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                    itemRequestInfo.setPatronBarcode(requestItemEntity.getPatronId());
                    requestItemController.checkinItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                } else if (itemRequestInfo.getRequestingInstitution().equalsIgnoreCase(ReCAPConstants.NYPL)) {
                    requestItemController.getJsipConectorFactory().getJSIPConnector(itemRequestInfo.getRequestingInstitution()).refileItem(itemBarcode);
                }
                if (!itemRequestInfo.isOwningInstitutionItem() && (itemRequestInfo.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.NYPL) || itemRequestInfo.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.PRINCETON))) {
                    itemRequestInfo.setPatronBarcode(getPatronIdBorrwingInsttution(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getItemOwningInstitution()));
                    requestItemController.checkinItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
                }
            }
        }
        return bSuccess;
    }

    /**
     * Send message to topic.
     *
     * @param owningInstituteId the owning institute id
     * @param requestType       the request type
     * @param itemResponseInfo  the item response info
     * @param exchange          the exchange
     */
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
        itemResponseInformation.setRequestId(itemRequestInfo.getRequestId());
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
        itemResponseInformation.setExpirationDate(itemRequestInfo.getExpirationDate());
        itemResponseInformation.setUsername(itemRequestInfo.getUsername());
        return itemResponseInformation;
    }

    /**
     * Update recap request item integer.
     *
     * @param itemRequestInformation the item request information
     * @param itemEntity             the item entity
     * @param requestStatusCode      the request status code
     * @return the integer
     */
    public Integer updateRecapRequestItem(ItemRequestInformation itemRequestInformation, ItemEntity itemEntity, String requestStatusCode) {
        return itemRequestDBService.updateRecapRequestItem(itemRequestInformation, itemEntity, requestStatusCode, null);
    }

    /**
     * Update recap request item item information response.
     *
     * @param itemInformationResponse the item information response
     * @return the item information response
     */
    public ItemInformationResponse updateRecapRequestItem(ItemInformationResponse itemInformationResponse) {
        return itemRequestDBService.updateRecapRequestItem(itemInformationResponse);
    }

    /**
     * Update recap request status item information response.
     *
     * @param itemInformationResponse the item information response
     * @return the item information response
     */
    public ItemInformationResponse updateRecapRequestStatus(ItemInformationResponse itemInformationResponse) {
        return itemRequestDBService.updateRecapRequestStatus(itemInformationResponse);
    }

    public boolean updateItemAvailabilutyStatus(List<ItemEntity> itemEntities, String username) {
        ItemStatusEntity itemStatusEntity = itemStatusDetailsRepository.findByStatusCode(ReCAPConstants.NOT_AVAILABLE);
        for (ItemEntity itemEntity : itemEntities) {
            ItemEntity itemEntityByItemId = itemDetailsRepository.findByItemId(itemEntity.getItemId());
            logger.info("Item status : " + itemEntityByItemId.getItemStatusEntity().getStatusCode());
            if (itemStatusEntity.getItemStatusId() == itemEntityByItemId.getItemAvailabilityStatusId()) {
                return false;
            }
        }
        itemRequestDBService.updateItemAvailabilutyStatus(itemEntities, username);
        return true;
    }

    public void rollbackUpdateItemAvailabilutyStatus(ItemEntity itemEntity, String username) {
        itemRequestDBService.rollbackUpdateItemAvailabilutyStatus(itemEntity, username);
    }

    /**
     * Save item change log entity.
     *
     * @param recordId      the record id
     * @param userName      the user name
     * @param operationType the operation type
     * @param notes         the notes
     */
    public void saveItemChangeLogEntity(Integer recordId, String userName, String operationType, String notes) {
        itemRequestDBService.saveItemChangeLogEntity(recordId, userName, operationType, notes);
    }

    /**
     * Gets user.
     *
     * @param userId the user id
     * @return the user
     */
    public String getUser(String userId) {
        return itemRequestDBService.getUser(userId);
    }

    /**
     * Update gfa item information response.
     *
     * @param itemRequestInfo         the item request info
     * @param itemResponseInformation the item response information
     * @return the item information response
     */
    protected ItemInformationResponse updateGFA(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {

        try {
            if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL) || itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
                itemResponseInformation = gfaService.executeRetriveOrder(itemRequestInfo, itemResponseInformation);
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
                itemResponseInformation = holdItem(itemRequestInfo.getItemOwningInstitution(), itemRequestInfo, itemResponseInformation, itemEntity);
            } else {// Not the Owning Institute
                // Get Temporary bibId from SCSB DB
                ItemCreateBibResponse createBibResponse;
                if (!ReCAPConstants.NYPL.equalsIgnoreCase(itemRequestInfo.getRequestingInstitution())) {
                    createBibResponse = (ItemCreateBibResponse) requestItemController.createBibliogrphicItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                } else {
                    createBibResponse = new ItemCreateBibResponse();
                    createBibResponse.setSuccess(true);
                }
                if (createBibResponse.isSuccess()) {
                    itemRequestInfo.setBibId(createBibResponse.getBibId());
                    itemResponseInformation = holdItem(itemRequestInfo.getRequestingInstitution(), itemRequestInfo, itemResponseInformation, itemEntity);
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
                requestItemController.checkoutItem(itemRequestInfo, itemRequestInfo.getItemOwningInstitution());
            }
            itemRequestInfo.setPatronBarcode(requestingPatron);
            itemResponseInformation = updateScsbAndGfa(itemRequestInfo, itemResponseInformation, itemEntity);
        }
        if (itemResponseInformation.isSuccess()) {
            itemRequestServiceUtil.updateSolrIndex(itemEntity);
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse holdItem(String callingInst, ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        ItemHoldResponse itemHoldResponse = (ItemHoldResponse) requestItemController.holdItem(itemRequestInfo, callingInst);
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
        Integer requestId = 0;
        if (gfaService.isUseQueueLasCall()) {
            requestId = updateRecapRequestItem(itemRequestInfo, itemEntity, ReCAPConstants.REQUEST_STATUS_PENDING);
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
        RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(itemRequestInfo.getItemBarcodes().get(0), ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
        logger.info("Owning     Inst = " + requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionCode());
        logger.info("Borrowed   Inst = " + requestItemEntity.getInstitutionEntity().getInstitutionCode());
        logger.info("Requesting Inst = " + itemRequestInfo.getRequestingInstitution());
        ItemInformationResponse itemInformation = (ItemInformationResponse) requestItemController.itemInformation(itemRequestInfo, requestItemEntity.getInstitutionEntity().getInstitutionCode());
        if (itemInformation.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_CHARGED)
                || itemInformation.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_ON_HOLDSHELF)
                || itemInformation.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_IN_TRANSIT_NYPL)) {
            if (requestItemEntity.getInstitutionEntity().getInstitutionCode().equalsIgnoreCase(itemRequestInfo.getRequestingInstitution())) {
                ItemRecallResponse itemRecallResponse = (ItemRecallResponse) requestItemController.recallItem(itemRequestInfo, requestItemEntity.getInstitutionEntity().getInstitutionCode());
                if (itemRecallResponse.isSuccess()) {
                    // Update Recap DB
                    itemRequestInfo.setExpirationDate(itemRecallResponse.getExpirationDate());
                    sendEmail(requestItemEntity.getItemEntity().getCustomerCode(), itemRequestInfo.getItemBarcodes().get(0), itemRequestInfo.getPatronBarcode(), requestItemEntity.getInstitutionEntity().getInstitutionCode());
                    messagePublish = ReCAPConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM;
                    bsuccess = true;
                } else {
                    messagePublish = recallError(itemRecallResponse);
                    bsuccess = false;
                }
            } else {
                itemResponseInformation = createBibAndHold(itemRequestInfo, itemResponseInformation, itemEntity);
                if (itemResponseInformation.isSuccess()) { // IF Hold command is successfully
                    itemRequestInfo.setExpirationDate(itemRequestInfo.getExpirationDate());
                    String requestingPatron = itemRequestInfo.getPatronBarcode();
                    itemRequestInfo.setPatronBarcode(getPatronIdBorrwingInsttution(itemRequestInfo.getRequestingInstitution(), requestItemEntity.getInstitutionEntity().getInstitutionCode()));
                    itemRequestInfo.setPickupLocation(getPickupLocation(requestItemEntity.getStopCode()));
                    itemRequestInfo.setBibId(itemInformation.getBibID());
                    ItemRecallResponse itemRecallResponse = (ItemRecallResponse) requestItemController.recallItem(itemRequestInfo, requestItemEntity.getInstitutionEntity().getInstitutionCode());
                    itemRequestInfo.setPatronBarcode(requestingPatron);
                    if (itemRecallResponse.isSuccess()) {
                        sendEmail(requestItemEntity.getItemEntity().getCustomerCode(), itemRequestInfo.getItemBarcodes().get(0), itemRequestInfo.getPatronBarcode(), requestItemEntity.getInstitutionEntity().getInstitutionCode());
                        messagePublish = ReCAPConstants.SUCCESSFULLY_PROCESSED_REQUEST_ITEM;
                        bsuccess = true;
                    } else {
                        messagePublish = recallError(itemRecallResponse);
                        bsuccess = false;
                        requestItemController.cancelHoldItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
                        saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_HOLD_FAILURE, itemRequestInfo.getPatronBarcode() + " - " + itemResponseInformation.getScreenMessage());
                    }
                } else { // If Hold command Failure
                    messagePublish = itemResponseInformation.getScreenMessage();
                    bsuccess = false;
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
            } else if (requestingInstitution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                patronId = nyplColumbiaPatron;
            }
        }
        logger.info(patronId);
        return patronId;
    }

    private ItemInformationResponse createBibAndHold(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, ItemEntity itemEntity) {
        ItemCreateBibResponse createBibResponse;
        if (!ReCAPConstants.NYPL.equalsIgnoreCase(itemRequestInfo.getRequestingInstitution())) {
            createBibResponse = (ItemCreateBibResponse) requestItemController.createBibliogrphicItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
        } else {
            createBibResponse = new ItemCreateBibResponse();
            createBibResponse.setSuccess(true);
        }
        if (createBibResponse.isSuccess()) {
            itemRequestInfo.setBibId(createBibResponse.getBibId());
            ItemHoldResponse itemHoldResponse = (ItemHoldResponse) requestItemController.holdItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
            itemResponseInformation.setScreenMessage(itemHoldResponse.getScreenMessage());
            itemResponseInformation.setSuccess(itemHoldResponse.isSuccess());
        } else {
            itemResponseInformation.setScreenMessage(ReCAPConstants.REQUEST_ILS_EXCEPTION + ReCAPConstants.CREATING_A_BIB_RECORD_FAILED_IN_ILS);
            itemResponseInformation.setSuccess(createBibResponse.isSuccess());
            if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
                rollbackUpdateItemAvailabilutyStatus(itemEntity, itemRequestInfo.getUsername());
            }
        }
        return itemResponseInformation;
    }

    /**
     * Search records search result row.
     *
     * @param itemEntity the item entity
     * @return the search result row
     */
    protected SearchResultRow searchRecords(ItemEntity itemEntity) {
        List<SearchResultRow> statusResponse;
        SearchResultRow searchResultRow = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity<>(getRestHeaderService().getHttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(scsbSolrClientUrl + ReCAPConstants.SEARCH_RECORDS_SOLR)
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

    /**
     * Gets title.
     *
     * @param title           the title
     * @param itemEntity      the item entity
     * @param searchResultRow the search result row
     * @return the title
     */
    protected String getTitle(String title, ItemEntity itemEntity, SearchResultRow searchResultRow) {
        String titleIdentifier = "";
        String useRestrictions = ReCAPConstants.REQUEST_USE_RESTRICTIONS;
        String lTitle;
        String returnTitle = "";
        try {
            if (itemEntity != null && StringUtils.isNotBlank(itemEntity.getUseRestrictions())) {
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
                lTitle = lTitle.toUpperCase().substring(0, 126);
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
        if (!itemResponseInformation.getScreenMessage().equalsIgnoreCase(ReCAPConstants.GFA_ITEM_STATUS_CHECK_FAILED)) {
            rollbackUpdateItemAvailabilutyStatus(itemEntity, itemRequestInfo.getUsername());
            saveItemChangeLogEntity(itemEntity.getItemId(), getUser(itemRequestInfo.getUsername()), ReCAPConstants.REQUEST_ITEM_GFA_FAILURE, itemRequestInfo.getPatronBarcode() + " - " + itemResponseInformation.getScreenMessage());
        }
        requestItemController.cancelHoldItem(itemRequestInfo, itemRequestInfo.getRequestingInstitution());
    }

    private void rollbackAfterGFA(ItemInformationResponse itemResponseInformation) {
        ItemRequestInformation itemRequestInformation = itemRequestDBService.rollbackAfterGFA(itemResponseInformation);
        RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByRequestId(itemResponseInformation.getRequestId());
        if (null != requestItemEntity) {
            itemRequestServiceUtil.updateSolrIndex(requestItemEntity.getItemEntity());
        }
        if (itemResponseInformation.isBulk()) {
            requestItemController.checkinItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
        } else {
            requestItemController.cancelHoldItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
        }
    }

    /**
     * Gets notes.
     *
     * @param success       the success
     * @param screenMessage the screen message
     * @param userNotes     the user notes
     * @return the notes
     */
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

    /**
     * Process las retrieve response.
     *
     * @param body the body
     */
    public void processLASRetrieveResponse(String body) {
        ItemInformationResponse itemInformationResponse = gfaService.processLASRetrieveResponse(body);
        itemInformationResponse = updateRecapRequestStatus(itemInformationResponse);
        if (!itemInformationResponse.isSuccess()) {
            rollbackAfterGFA(itemInformationResponse);
        }
    }

    /**
     * @param body
     */
    public void processLASEddRetrieveResponse(String body) {
        ItemInformationResponse itemInformationResponse = gfaService.processLASEDDRetrieveResponse(body);
        if (itemInformationResponse.isSuccess()) {
            updateRecapRequestStatus(itemInformationResponse);
        } else {
            updateRecapRequestStatus(itemInformationResponse);
            rollbackAfterGFA(itemInformationResponse);
        }
    }

    /**
     * @param text
     * @return
     */
    public String removeDiacritical(String text) {
        return text == null ? null : Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    private void sendEmail(String customerCode, String itemBarcode, String patronBarcode, String toInstitution) {
        emailService.sendEmail(customerCode, itemBarcode, ReCAPConstants.REQUEST_RECALL_TO_BORRWER, patronBarcode, toInstitution, ReCAPConstants.REQUEST_RECALL_SUBJECT);
    }

    private String getPickupLocation(String deliveryLocation) {
        CustomerCodeEntity customerCodeEntity = customerCodeDetailsRepository.findByCustomerCode(deliveryLocation);
        return customerCodeEntity.getPickupLocation();
    }

    /**
     * Is use queue las call boolean.
     *
     * @return the boolean
     */
    public boolean isUseQueueLasCall() {
        return gfaService.isUseQueueLasCall();
    }

    public boolean executeLasitemCheck(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {
        RequestStatusEntity requestStatusEntity = null;
        RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByRequestId(itemRequestInfo.getRequestId());
        itemResponseInformation = gfaService.executeRetriveOrder(itemRequestInfo, itemResponseInformation);
        logger.info("itemResponseInformation-> " + itemResponseInformation.isSuccess());
        if (itemResponseInformation.isSuccess()) {
            requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_PENDING);
            requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
            requestItemEntity.setLastUpdatedDate(new Date());
            requestItemDetailsRepository.save(requestItemEntity);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Replaces the requests to LAS Queue.
     * @param replaceRequest
     * @return
     */
    public Map<String, String> replaceRequestsToLASQueue(ReplaceRequest replaceRequest) {
        Map<String, String> resultMap = new HashMap<>();
        String replaceRequestByType = replaceRequest.getReplaceRequestByType();
        try {
            if (StringUtils.isNotBlank(replaceRequestByType)) {
                resultMap = replaceRequestToLASQueueByType(replaceRequest, replaceRequestByType);
            } else {
                resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.REQUEST_REPLACE_BY_TYPE_NOT_SELECTED);
            }
        } catch (Exception exception) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, exception);
            resultMap.put(ReCAPConstants.FAILURE, exception.getMessage());
        }
        return resultMap;
    }

    /**
     * Replaces the requests to LAS Queue based on the replace request type.
     * @param replaceRequest
     * @param replaceRequestByType
     * @return
     * @throws ParseException
     */
    private Map<String, String> replaceRequestToLASQueueByType(ReplaceRequest replaceRequest, String replaceRequestByType) throws ParseException {
        Map<String, String> resultMap = new HashMap<>();
        if (ReCAPConstants.REQUEST_STATUS.equalsIgnoreCase(replaceRequestByType)) {
            String requestStatus = replaceRequest.getRequestStatus();
            if (StringUtils.isNotBlank(requestStatus)) {
                if (ReCAPConstants.REQUEST_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByRequestStatusCode(Arrays.asList(ReCAPConstants.REQUEST_STATUS_PENDING));
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ReCAPConstants.REQUEST_STATUS_EXCEPTION.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByRequestStatusCode(Arrays.asList(ReCAPConstants.REQUEST_STATUS_EXCEPTION));
                    resultMap = buildRequestInfoAndReplaceToSCSB(requestItemEntities);
                } else {
                    resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.REQUEST_STATUS_INVALID);
                }
            } else {
                resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.REQUEST_STATUS_INVALID);
            }
        } else if (ReCAPConstants.REQUEST_IDS.equalsIgnoreCase(replaceRequestByType)) {
            if (StringUtils.isNotBlank(replaceRequest.getRequestIds()) && StringUtils.isNotBlank(replaceRequest.getRequestStatus())) {
                String requestStatus = replaceRequest.getRequestStatus();
                List<Integer> requestIds = new ArrayList<>();
                Arrays.stream(replaceRequest.getRequestIds().split(",")).forEach(requestId -> requestIds.add(Integer.valueOf(requestId.trim())));
                resultMap.put(ReCAPConstants.TOTAL_REQUESTS_IDS, String.valueOf(requestIds.size()));
                if (ReCAPConstants.REQUEST_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByRequestIdsAndStatusCodes(requestIds, Arrays.asList(ReCAPConstants.REQUEST_STATUS_PENDING));
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ReCAPConstants.REQUEST_STATUS_EXCEPTION.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByRequestIdsAndStatusCodes(requestIds, Arrays.asList(ReCAPConstants.REQUEST_STATUS_EXCEPTION));
                    resultMap = buildRequestInfoAndReplaceToSCSB(requestItemEntities);
                } else {
                    resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.REQUEST_STATUS_INVALID);
                }
            } else {
                resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.REQUEST_IDS_INVALID);
            }
        } else if (ReCAPConstants.REQUEST_IDS_RANGE.equalsIgnoreCase(replaceRequestByType)) {
            if (StringUtils.isNotBlank(replaceRequest.getStartRequestId()) && StringUtils.isNotBlank(replaceRequest.getEndRequestId()) && StringUtils.isNotBlank(replaceRequest.getRequestStatus())) {
                String requestStatus = replaceRequest.getRequestStatus();
                Integer startRequestId = Integer.valueOf(replaceRequest.getStartRequestId());
                Integer endRequestId = Integer.valueOf(replaceRequest.getEndRequestId());
                if (ReCAPConstants.REQUEST_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.getRequestsBasedOnRequestIdRangeAndRequestStatusCode(startRequestId, endRequestId, ReCAPConstants.REQUEST_STATUS_PENDING);
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ReCAPConstants.REQUEST_STATUS_EXCEPTION.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.getRequestsBasedOnRequestIdRangeAndRequestStatusCode(startRequestId, endRequestId, ReCAPConstants.REQUEST_STATUS_EXCEPTION);
                    resultMap = buildRequestInfoAndReplaceToSCSB(requestItemEntities);
                } else {
                    resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.REQUEST_STATUS_INVALID);
                }
            } else {
                resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.REQUEST_START_END_IDS_INVALID);
            }
        } else if (ReCAPConstants.REQUEST_DATES_RANGE.equalsIgnoreCase(replaceRequestByType)) {
            if (StringUtils.isNotBlank(replaceRequest.getFromDate()) && StringUtils.isNotBlank(replaceRequest.getToDate()) && StringUtils.isNotBlank(replaceRequest.getRequestStatus())) {
                String requestStatus = replaceRequest.getRequestStatus();
                SimpleDateFormat dateFormatter = new SimpleDateFormat(ReCAPConstants.DEFAULT_DATE_FORMAT);
                Date fromDate = dateFormatter.parse(replaceRequest.getFromDate());
                Date toDate = dateFormatter.parse(replaceRequest.getToDate());
                if (ReCAPConstants.REQUEST_STATUS_PENDING.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.getRequestsBasedOnDateRangeAndRequestStatusCode(fromDate, toDate, ReCAPConstants.REQUEST_STATUS_PENDING);
                    resultMap = buildRequestInfoAndReplaceToLAS(requestItemEntities);
                } else if (ReCAPConstants.REQUEST_STATUS_EXCEPTION.equalsIgnoreCase(requestStatus)) {
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.getRequestsBasedOnDateRangeAndRequestStatusCode(fromDate, toDate, ReCAPConstants.REQUEST_STATUS_EXCEPTION);
                    resultMap = buildRequestInfoAndReplaceToSCSB(requestItemEntities);
                } else {
                    resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.REQUEST_STATUS_INVALID);
                }
            } else {
                resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.REQUEST_DATES_INVALID);
            }
        } else {
            resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.REQUEST_REPLACE_BY_TYPE_INVALID);
        }
        return resultMap;
    }

    /**
     * Builds request information and replaces them to LAS queue.
     * @param requestItemEntities
     * @return
     */
    private Map<String, String> buildRequestInfoAndReplaceToLAS(List<RequestItemEntity> requestItemEntities) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put(ReCAPConstants.TOTAL_REQUESTS_FOUND, String.valueOf(requestItemEntities.size()));
        if (CollectionUtils.isNotEmpty(requestItemEntities)) {
            String message;
            for (RequestItemEntity requestItemEntity : requestItemEntities) {
                String requestTypeCode = requestItemEntity.getRequestTypeEntity().getRequestTypeCode();
                if (ReCAPConstants.RETRIEVAL.equalsIgnoreCase(requestTypeCode)) {
                    message = gfaService.buildRetrieveRequestInfoAndReplaceToLAS(requestItemEntity);
                } else if (ReCAPConstants.EDD_REQUEST.equalsIgnoreCase(requestTypeCode)) {
                    message = gfaService.buildEddRequestInfoAndReplaceToLAS(requestItemEntity);
                } else {
                    message = ReCAPConstants.IGNORE_REQUEST_TYPE_NOT_VALID + requestTypeCode;
                }
                resultMap.put(String.valueOf(requestItemEntity.getRequestId()), message);
            }
        } else {
            resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.NO_REQUESTS_FOUND);
        }
        return resultMap;
    }

    /**
     * Builds request information and replaces them to SCSB queue.
     * @param requestItemEntities
     * @return
     */
    private Map<String, String> buildRequestInfoAndReplaceToSCSB(List<RequestItemEntity> requestItemEntities) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put(ReCAPConstants.TOTAL_REQUESTS_FOUND, String.valueOf(requestItemEntities.size()));
        if (CollectionUtils.isNotEmpty(requestItemEntities)) {
            String message;
            for (RequestItemEntity requestItemEntity : requestItemEntities) {
                String requestTypeCode = requestItemEntity.getRequestTypeEntity().getRequestTypeCode();
                if (ReCAPConstants.RETRIEVAL.equalsIgnoreCase(requestTypeCode)) {
                    message = buildRetrieveRequestInfoAndReplaceToSCSB(requestItemEntity);
                } else if (ReCAPConstants.EDD_REQUEST.equalsIgnoreCase(requestTypeCode)) {
                    message = buildEddRequestInfoAndReplaceToSCSB(requestItemEntity);
                } else {
                    message = ReCAPConstants.IGNORE_REQUEST_TYPE_NOT_VALID + requestTypeCode;
                }
                resultMap.put(String.valueOf(requestItemEntity.getRequestId()), message);
            }
        } else {
            resultMap.put(ReCAPConstants.INVALID_REQUEST, ReCAPConstants.NO_REQUESTS_FOUND);
        }
        return resultMap;
    }

    /**
     * Validates the request information and returns message.
     * @param itemRequestInformation
     * @return
     */
    private String validateItemRequest(ItemRequestInformation itemRequestInformation) {
        ResponseEntity responseEntity = requestParamaterValidatorService.validateItemRequestParameters(itemRequestInformation);
        if (responseEntity == null) {
            responseEntity = itemValidatorService.itemValidation(itemRequestInformation);
        }
        return responseEntity.getBody().toString();
    }

    /**
     * Builds retrieval request information and replaces them to SCSB queue.
     * @param requestItemEntity
     * @return
     */
    private String buildRetrieveRequestInfoAndReplaceToSCSB(RequestItemEntity requestItemEntity) {
        try {
            ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
            itemRequestInformation.setUsername(requestItemEntity.getCreatedBy());
            itemRequestInformation.setItemBarcodes(Arrays.asList(requestItemEntity.getItemEntity().getBarcode()));
            itemRequestInformation.setPatronBarcode(requestItemEntity.getPatronId());
            itemRequestInformation.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());
            itemRequestInformation.setEmailAddress(securityUtil.getDecryptedValue(requestItemEntity.getEmailId()));
            itemRequestInformation.setItemOwningInstitution(requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionCode());
            itemRequestInformation.setRequestType(requestItemEntity.getRequestTypeEntity().getRequestTypeCode());
            itemRequestInformation.setDeliveryLocation(requestItemEntity.getStopCode());

            String notes = requestItemEntity.getNotes();
            new BufferedReader(new StringReader(notes)).lines().forEach(line -> itemRequestServiceUtil.setEddInfoToScsbRequest(line, itemRequestInformation));

            String validationMessage = validateItemRequest(itemRequestInformation);
            if (!ReCAPConstants.VALID_REQUEST.equals(validationMessage)) {
                return ReCAPConstants.FAILURE + " : " + validationMessage;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(itemRequestInformation);
            producerTemplate.sendBodyAndHeader(ReCAPConstants.REQUEST_ITEM_QUEUE, json, ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER, itemRequestInformation.getRequestType());
        } catch (Exception exception) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, exception);
            return ReCAPConstants.FAILURE + ":" + exception.getMessage();
        }
        return ReCAPConstants.SUCCESS + " : " + ReCAPConstants.REQUEST_MESSAGE_RECEVIED;
    }

    /**
     * Builds EDD request information and replaces them to SCSB queue.
     * @param requestItemEntity
     * @return
     */
    private String buildEddRequestInfoAndReplaceToSCSB(RequestItemEntity requestItemEntity) {
        try {
            ItemEntity itemEntity = requestItemEntity.getItemEntity();
            ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
            itemRequestInformation.setUsername(requestItemEntity.getCreatedBy());
            itemRequestInformation.setItemBarcodes(Arrays.asList(itemEntity.getBarcode()));
            itemRequestInformation.setPatronBarcode(requestItemEntity.getPatronId());
            itemRequestInformation.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());
            itemRequestInformation.setEmailAddress(securityUtil.getDecryptedValue(requestItemEntity.getEmailId()));
            itemRequestInformation.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
            itemRequestInformation.setRequestType(requestItemEntity.getRequestTypeEntity().getRequestTypeCode());
            itemRequestInformation.setDeliveryLocation(requestItemEntity.getStopCode());

            String notes = requestItemEntity.getNotes();
            new BufferedReader(new StringReader(notes)).lines().forEach(line -> itemRequestServiceUtil.setEddInfoToScsbRequest(line, itemRequestInformation));

            SearchResultRow searchResultRow = searchRecords(itemEntity);
            itemRequestInformation.setTitleIdentifier(searchResultRow.getTitle());

            String validationMessage = validateItemRequest(itemRequestInformation);
            if (!ReCAPConstants.VALID_REQUEST.equals(validationMessage)) {
                return ReCAPConstants.FAILURE + ":" + validationMessage;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(itemRequestInformation);
            producerTemplate.sendBodyAndHeader(ReCAPConstants.REQUEST_ITEM_QUEUE, json, ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER, itemRequestInformation.getRequestType());
        } catch (Exception exception) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, exception);
            return ReCAPConstants.FAILURE + ":" + exception.getMessage();
        }
        return ReCAPConstants.SUCCESS + " : " + ReCAPConstants.REQUEST_MESSAGE_RECEVIED;
    }
}
