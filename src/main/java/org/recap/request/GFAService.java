package org.recap.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.camel.statusreconciliation.StatusReconciliationCSVRecord;
import org.recap.camel.statusreconciliation.StatusReconciliationErrorCSVRecord;
import org.recap.gfa.model.*;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.*;
import org.recap.processor.LasItemStatusCheckPollingProcessor;
import org.recap.repository.*;
import org.recap.util.ItemRequestServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sudhishk on 27/1/17.
 */
@Service
public class GFAService {

    private static final Logger logger = LoggerFactory.getLogger(GFAService.class);

    @Value("${gfa.item.status}")
    private String gfaItemStatus;

    @Value("${gfa.item.retrieval.order}")
    private String gfaItemRetrival;

    @Value("${gfa.item.edd.retrieval.order}")
    private String gfaItemEDDRetrival;

    @Value("${gfa.item.permanent.withdrawl.direct}")
    private String gfaItemPermanentWithdrawlDirect;

    @Value("${gfa.item.permanent.withdrawl.indirect}")
    private String gfaItemPermanentWithdrawlInDirect;

    @Value("${las.use.queue}")
    private boolean useQueueLasCall;

    @Value("${status.reconciliation.batch.size}")
    private Integer batchSize;

    @Value("${status.reconciliation.day.limit}")
    private Integer statusReconciliationDayLimit;

    @Value("${status.reconciliation.las.barcode.limit}")
    private Integer statusReconciliationLasBarcodeLimit;

    @Value("${gfa.server.response.timeout.milliseconds}")
    private Integer gfaServerResponseTimeOutMilliseconds;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestServiceUtil itemRequestServiceUtil;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    @Autowired
    private LasItemStatusCheckPollingProcessor lasItemStatusCheckPollingProcessor;

    @Autowired
    RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    /**
     * Gets gfa item status.
     *
     * @return the gfa item status
     */
    public String getGfaItemStatus() {
        return gfaItemStatus;
    }

    /**
     * Gets gfa item retrival.
     *
     * @return the gfa item retrival
     */
    public String getGfaItemRetrival() {
        return gfaItemRetrival;
    }

    /**
     * Gets gfa item edd retrival.
     *
     * @return the gfa item edd retrival
     */
    public String getGfaItemEDDRetrival() {
        return gfaItemEDDRetrival;
    }

    /**
     * Gets gfa item permanent withdrawl direct.
     *
     * @return the gfa item permanent withdrawl direct
     */
    public String getGfaItemPermanentWithdrawlDirect() {
        return gfaItemPermanentWithdrawlDirect;
    }

    /**
     * Gets gfa item permanent withdrawl in direct.
     *
     * @return the gfa item permanent withdrawl in direct
     */
    public String getGfaItemPermanentWithdrawlInDirect() {
        return gfaItemPermanentWithdrawlInDirect;
    }

    /**
     * Get gfa retrieve edd item request gfa retrieve edd item request.
     *
     * @return the gfa retrieve edd item request
     */
    public GFARetrieveEDDItemRequest getGFARetrieveEDDItemRequest() {
        return new GFARetrieveEDDItemRequest();
    }

    /**
     * Gets rest template.
     *
     * @return the rest template
     */
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    /**
     * Gets logger.
     *
     * @return the logger
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Gets producer.
     *
     * @return the producer
     */
    public ProducerTemplate getProducer() {
        return producer;
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
     * Is use queue las call boolean.
     *
     * @return the boolean
     */
    public boolean isUseQueueLasCall() {
        return useQueueLasCall;
    }

    /**
     * Gets request item details repository.
     *
     * @return the request item details repository
     */
    public RequestItemDetailsRepository getRequestItemDetailsRepository() {
        return requestItemDetailsRepository;
    }

    /**
     * Gets item details repository.
     *
     * @return the item details repository
     */
    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    /**
     * Gets item status details repository.
     *
     * @return the item status details repository
     */
    public ItemStatusDetailsRepository getItemStatusDetailsRepository() {
        return itemStatusDetailsRepository;
    }

    /**
     * Gets item change log details repository.
     *
     * @return the item change log details repository
     */
    public ItemChangeLogDetailsRepository getItemChangeLogDetailsRepository() {
        return itemChangeLogDetailsRepository;
    }

    /**
     * Gets gfa server response time out milliseconds.
     *
     * @return the gfa server response time out milliseconds
     */
    public Integer getGfaServerResponseTimeOutMilliseconds() {
        return gfaServerResponseTimeOutMilliseconds;
    }

    /**
     * Item status check gfa item status check response.
     *
     * @param gfaItemStatusCheckRequest the gfa item status check request
     * @return the gfa item status check response
     */
    public GFAItemStatusCheckResponse itemStatusCheck(GFAItemStatusCheckRequest gfaItemStatusCheckRequest) {

        ObjectMapper objectMapper = new ObjectMapper();
        String filterParamValue = "";
        GFAItemStatusCheckResponse gfaItemStatusCheckResponse = null;
        try {
            filterParamValue = objectMapper.writeValueAsString(gfaItemStatusCheckRequest);
            logger.info(filterParamValue);

            RestTemplate restTemplate = getRestTemplate();
            HttpEntity requestEntity = new HttpEntity<>(new HttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(gfaItemStatus).queryParam(ReCAPConstants.GFA_SERVICE_PARAM, filterParamValue);
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(getGfaServerResponseTimeOutMilliseconds());
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(getGfaServerResponseTimeOutMilliseconds());
            ResponseEntity<GFAItemStatusCheckResponse> responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, GFAItemStatusCheckResponse.class);
            if (responseEntity != null && responseEntity.getBody() != null) {
                gfaItemStatusCheckResponse = responseEntity.getBody();
            }
            if (responseEntity != null && responseEntity.getStatusCode() != null) {
                logger.info("" + responseEntity.getStatusCode());
            }
        } catch (JsonProcessingException e) {
            logger.error(ReCAPConstants.REQUEST_PARSE_EXCEPTION, e);
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return gfaItemStatusCheckResponse;
    }

    /**
     * To compare the item status for the status reconciliation process with LAS .
     *
     * @param itemEntityChunkList                    the item entity chunk list
     * @param statusReconciliationErrorCSVRecordList the status reconciliation error csv record list
     * @return the list
     */
    public List<StatusReconciliationCSVRecord> itemStatusComparison(List<List<ItemEntity>> itemEntityChunkList, List<StatusReconciliationErrorCSVRecord> statusReconciliationErrorCSVRecordList) {
        List<StatusReconciliationCSVRecord> statusReconciliationCSVRecordList = new ArrayList<>();
        List<ItemChangeLogEntity> itemChangeLogEntityList = new ArrayList<>();
        for (List<ItemEntity> itemEntities : itemEntityChunkList) {
            List<String> lasNotAvailableStatusList = ReCAPConstants.getGFAStatusNotAvailableList();
            GFAItemStatusCheckResponse gfaItemStatusCheckResponse = getGFAItemStatusCheckResponse(itemEntities);
            if (gfaItemStatusCheckResponse != null && gfaItemStatusCheckResponse.getDsitem() != null && gfaItemStatusCheckResponse.getDsitem().getTtitem() != null) {
                List<Ttitem> ttitemList = gfaItemStatusCheckResponse.getDsitem().getTtitem();
                String lasStatus = null;
                StatusReconciliationErrorCSVRecord statusReconciliationErrorCSVRecord = new StatusReconciliationErrorCSVRecord();
                for (ItemEntity itemEntity : itemEntities) {
                    boolean isBarcodeAvailableForErrorReport = false;
                    for (Ttitem ttitem : ttitemList) {
                        if (itemEntity.getBarcode().equalsIgnoreCase(ttitem.getItemBarcode())) {
                            isBarcodeAvailableForErrorReport = true;
                            lasStatus = ttitem.getItemStatus();
                            boolean isNotAvailable = false;
                            for (String status : lasNotAvailableStatusList) {
                                if (StringUtils.startsWithIgnoreCase(lasStatus, status)) {
                                    isNotAvailable = true;
                                }
                            }
                            if (!isNotAvailable) {
                                processMismatchStatus(statusReconciliationCSVRecordList, itemChangeLogEntityList, lasStatus, itemEntity);
                            }
                            break;
                        } else {
                            continue;
                        }

                    }
                    if (!isBarcodeAvailableForErrorReport) {
                        statusReconciliationErrorCSVRecord.setBarcode(itemEntity.getBarcode());
                        statusReconciliationErrorCSVRecord.setInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                        statusReconciliationErrorCSVRecord.setReasonForFailure(ReCAPConstants.BARCODE_NOT_FOUND_IN_LAS);
                        statusReconciliationErrorCSVRecordList.add(statusReconciliationErrorCSVRecord);
                    }
                }
            }
            getItemChangeLogDetailsRepository().save(itemChangeLogEntityList);
            getItemChangeLogDetailsRepository().flush();
        }
        return statusReconciliationCSVRecordList;
    }

    private void processMismatchStatus(List<StatusReconciliationCSVRecord> statusReconciliationCSVRecordList, List<ItemChangeLogEntity> itemChangeLogEntityList, String lasStatus, ItemEntity itemEntity) {
        StatusReconciliationCSVRecord statusReconciliationCSVRecord = new StatusReconciliationCSVRecord();
        List<RequestItemEntity> requestItemEntityList = getRequestItemDetailsRepository().findByitemId(itemEntity.getItemId(), Arrays.asList(ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED, ReCAPConstants.REQUEST_STATUS_INITIAL_LOAD));
        List<String> barcodeList = new ArrayList<>();
        List<Integer> requestIdList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
        ItemStatusEntity itemStatusEntity = getItemStatusDetailsRepository().findByItemStatusId(itemEntity.getItemAvailabilityStatusId());
        if (!requestItemEntityList.isEmpty()) {
            for (RequestItemEntity requestItemEntity : requestItemEntityList) {
                statusReconciliationCSVRecord = getStatusReconciliationCSVRecord(itemEntity.getBarcode(), "yes", requestItemEntity.getRequestId().toString(), lasStatus, simpleDateFormat.format(new Date()), itemStatusEntity);
                barcodeList.add(itemEntity.getBarcode());
                requestIdList.add(requestItemEntity.getRequestId());
                logger.info("found mismatch in item status and refilled for the item id :{}", requestItemEntity.getItemId());
            }
        } else {
            statusReconciliationCSVRecord = getStatusReconciliationCSVRecord(itemEntity.getBarcode(), "No", null, lasStatus, simpleDateFormat.format(new Date()), itemStatusEntity);
            getItemDetailsRepository().updateAvailabilityStatus(1, ReCAPConstants.GUEST_USER, itemEntity.getBarcode());
            ItemChangeLogEntity itemChangeLogEntity = saveItemChangeLogEntity(itemEntity.getItemId(), ReCAPConstants.GUEST_USER, ReCAPConstants.STATUS_RECONCILIATION_CHANGE_LOG_OPERATION_TYPE, itemEntity.getBarcode());
            itemChangeLogEntityList.add(itemChangeLogEntity);
            itemRequestServiceUtil.updateSolrIndex(itemEntity);
            logger.info("found mismatch in item status and updated availability status for the item barcode:{}", itemEntity.getBarcode());
        }
        if (!barcodeList.isEmpty() && !requestIdList.isEmpty()) {
            ItemRefileRequest itemRefileRequest = new ItemRefileRequest();
            itemRefileRequest.setItemBarcodes(barcodeList);
            itemRefileRequest.setRequestIds(requestIdList);
            itemRequestService.reFileItem(itemRefileRequest);
        }
        statusReconciliationCSVRecordList.add(statusReconciliationCSVRecord);
    }

    /**
     * For the given item entities this method prepares item barcodes to check status with LAS.
     *
     * @param itemEntities the item entities
     * @return the gfa item status check response
     */
    public GFAItemStatusCheckResponse getGFAItemStatusCheckResponse(List<ItemEntity> itemEntities) {
        GFAItemStatusCheckResponse gfaItemStatusCheckResponse = new GFAItemStatusCheckResponse();
        List<GFAItemStatus> gfaItemStatusList = new ArrayList<>();
        if (itemEntities != null) {
            for (ItemEntity itemEntity : itemEntities) {
                GFAItemStatus gfaItemStatus = new GFAItemStatus();
                gfaItemStatus.setItemBarCode(itemEntity.getBarcode());
                gfaItemStatusList.add(gfaItemStatus);
            }
            GFAItemStatusCheckRequest gfaItemStatusCheckRequest = new GFAItemStatusCheckRequest();
            gfaItemStatusCheckRequest.setItemStatus(gfaItemStatusList);
            gfaItemStatusCheckResponse = itemStatusCheck(gfaItemStatusCheckRequest);
        }
        return gfaItemStatusCheckResponse;
    }

    /**
     * For the given input this method prepares the status reconciliation csv record.
     *
     * @param barcode          the barcode
     * @param availability     the availability
     * @param requestId        the request id
     * @param statusInLas      the status in las
     * @param dateTime         the date time
     * @param itemStatusEntity the item status entity
     * @return the status reconciliation csv record
     */
    public StatusReconciliationCSVRecord getStatusReconciliationCSVRecord(String barcode, String availability, String requestId, String statusInLas, String dateTime, ItemStatusEntity itemStatusEntity) {
        StatusReconciliationCSVRecord statusReconciliationCSVRecord = new StatusReconciliationCSVRecord();
        statusReconciliationCSVRecord.setBarcode(barcode);
        statusReconciliationCSVRecord.setRequestAvailability(availability);
        statusReconciliationCSVRecord.setRequestId(requestId);
        statusReconciliationCSVRecord.setStatusInLas(statusInLas);
        if (itemStatusEntity != null) {
            statusReconciliationCSVRecord.setStatusInScsb(itemStatusEntity.getStatusDescription());
        }
        statusReconciliationCSVRecord.setDateTime(dateTime);
        return statusReconciliationCSVRecord;

    }

    private ItemChangeLogEntity saveItemChangeLogEntity(Integer requestId, String deaccessionUser, String operationType, String barcode) {
        ItemChangeLogEntity itemChangeLogEntity = new ItemChangeLogEntity();
        String notes = "ItemBarcode:" + barcode + " , " + "ItemAvailabilityStatusChange" + ReCAPConstants.REQUEST_ITEM_AVAILABILITY_STATUS_DATA_ROLLBACK;
        itemChangeLogEntity.setUpdatedBy(deaccessionUser);
        itemChangeLogEntity.setUpdatedDate(new Date());
        itemChangeLogEntity.setOperationType(operationType);
        itemChangeLogEntity.setRecordId(requestId);
        itemChangeLogEntity.setNotes(notes);
        return itemChangeLogEntity;

    }

    /**
     * Item retrival gfa retrieve item response.
     *
     * @param gfaRetrieveItemRequest the gfa retrieve item request
     * @return the gfa retrieve item response
     */
    public GFARetrieveItemResponse itemRetrival(GFARetrieveItemRequest gfaRetrieveItemRequest) {
        GFARetrieveItemResponse gfaRetrieveItemResponse = null;
        ResponseEntity<GFARetrieveItemResponse> responseEntity = null;
        try {
            HttpEntity requestEntity = new HttpEntity(gfaRetrieveItemRequest, getHttpHeaders());
            responseEntity = getRestTemplate().exchange(getGfaItemRetrival(), HttpMethod.POST, requestEntity, GFARetrieveItemResponse.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                gfaRetrieveItemResponse = responseEntity.getBody();
                gfaRetrieveItemResponse = getLASRetrieveResponse(gfaRetrieveItemResponse);
            } else {
                gfaRetrieveItemResponse = new GFARetrieveItemResponse();
                gfaRetrieveItemResponse.setSuccess(false);
            }
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
            gfaRetrieveItemResponse = new GFARetrieveItemResponse();
            gfaRetrieveItemResponse.setSuccess(false);
            gfaRetrieveItemResponse.setScrenMessage(ReCAPConstants.REQUEST_LAS_EXCEPTION + ReCAPConstants.LAS_SERVER_NOT_REACHABLE);
        } catch (Exception e) {
            gfaRetrieveItemResponse = new GFARetrieveItemResponse();
            gfaRetrieveItemResponse.setSuccess(false);
            gfaRetrieveItemResponse.setScrenMessage(ReCAPConstants.SCSB_REQUEST_EXCEPTION + e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return gfaRetrieveItemResponse;
    }

    private GFARetrieveItemResponse getLASRetrieveResponse(GFARetrieveItemResponse gfaRetrieveItemResponseParam) {
        GFARetrieveItemResponse gfaRetrieveItemResponse = gfaRetrieveItemResponseParam;
        if (gfaRetrieveItemResponse != null && gfaRetrieveItemResponse.getRetrieveItem() != null && gfaRetrieveItemResponse.getRetrieveItem().getTtitem() != null && !gfaRetrieveItemResponse.getRetrieveItem().getTtitem().isEmpty()) {
            List<Ttitem> titemList = gfaRetrieveItemResponse.getRetrieveItem().getTtitem();
            for (Ttitem ttitem : titemList) {
                if (StringUtils.isNotBlank(ttitem.getErrorCode())) {
                    gfaRetrieveItemResponse.setSuccess(false);
                    gfaRetrieveItemResponse.setScrenMessage(ttitem.getErrorNote());
                } else {
                    if (gfaRetrieveItemResponse == null) {
                        gfaRetrieveItemResponse = new GFARetrieveItemResponse();
                    }
                    gfaRetrieveItemResponse.setSuccess(true);
                }
            }
        } else {
            if (gfaRetrieveItemResponse == null) {
                gfaRetrieveItemResponse = new GFARetrieveItemResponse();
            }
            gfaRetrieveItemResponse.setSuccess(true);
        }
        return gfaRetrieveItemResponse;
    }

    /**
     * Item edd retrival gfa retrieve item response.
     *
     * @param gfaRetrieveEDDItemRequest the gfa retrieve edd item request
     * @return the gfa retrieve item response
     */
    public GFARetrieveItemResponse itemEDDRetrival(GFARetrieveEDDItemRequest gfaRetrieveEDDItemRequest) {
        GFARetrieveItemResponse gfaRetrieveItemResponse = null;
        GFAItemStatusCheckRequest gfaItemStatusCheckRequest = new GFAItemStatusCheckRequest();
        GFAItemStatusCheckResponse gfaItemStatusCheckResponse;
        try {
            GFAItemStatus gfaItemStatus001 = new GFAItemStatus();
            gfaItemStatus001.setItemBarCode(gfaRetrieveEDDItemRequest.getRetrieveEDD().getTtitem().get(0).getItemBarcode());
            List<GFAItemStatus> gfaItemStatuses = new ArrayList<>();
            gfaItemStatuses.add(gfaItemStatus001);
            gfaItemStatusCheckRequest.setItemStatus(gfaItemStatuses);
            gfaItemStatusCheckResponse = itemStatusCheck(gfaItemStatusCheckRequest);
            if (gfaItemStatusCheckResponse != null
                    && gfaItemStatusCheckResponse.getDsitem() != null
                    && gfaItemStatusCheckResponse.getDsitem().getTtitem() != null && !gfaItemStatusCheckResponse.getDsitem().getTtitem().isEmpty()) {

                RestTemplate restTemplate = new RestTemplate();
                HttpEntity requestEntity = new HttpEntity(gfaRetrieveEDDItemRequest, getHttpHeaders());
                logger.info("" + convertJsontoString(requestEntity.getBody()));
                ResponseEntity<GFARetrieveItemResponse> responseEntity = restTemplate.exchange(getGfaItemEDDRetrival(), HttpMethod.POST, requestEntity, GFARetrieveItemResponse.class);
                logger.info(responseEntity.getStatusCode() + " - " + convertJsontoString(responseEntity.getBody()));
                if (responseEntity.getStatusCode() == HttpStatus.OK) {
                    gfaRetrieveItemResponse = responseEntity.getBody();
                    gfaRetrieveItemResponse = getLASRetrieveResponse(gfaRetrieveItemResponse);
                } else {
                    gfaRetrieveItemResponse = new GFARetrieveItemResponse();
                    gfaRetrieveItemResponse.setSuccess(false);
                    gfaRetrieveItemResponse.setScrenMessage(ReCAPConstants.REQUEST_LAS_EXCEPTION + "HTTP Error response from LAS");
                }
            } else {
                gfaRetrieveItemResponse.setSuccess(false);
                gfaRetrieveItemResponse.setScrenMessage(ReCAPConstants.GFA_ITEM_STATUS_CHECK_FAILED);
            }
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            gfaRetrieveItemResponse = new GFARetrieveItemResponse();
            gfaRetrieveItemResponse.setSuccess(false);
            gfaRetrieveItemResponse.setScrenMessage(ReCAPConstants.REQUEST_LAS_EXCEPTION + ReCAPConstants.LAS_SERVER_NOT_REACHABLE);
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        } catch (Exception e) {
            gfaRetrieveItemResponse = new GFARetrieveItemResponse();
            gfaRetrieveItemResponse.setSuccess(false);
            gfaRetrieveItemResponse.setScrenMessage(ReCAPConstants.SCSB_REQUEST_EXCEPTION + e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return gfaRetrieveItemResponse;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Execute retrive order item information response.
     *
     * @param itemRequestInfo         the item request info
     * @param itemResponseInformation the item response information
     * @return the item information response
     */
    public ItemInformationResponse executeRetriveOrder(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {
        GFAItemStatusCheckRequest gfaItemStatusCheckRequest = new GFAItemStatusCheckRequest();

        GFAItemStatusCheckResponse gfaItemStatusCheckResponse;
        String itemStatus;
        String gfaOnlyStatus;

        try {
            GFAItemStatus gfaItemStatus001 = new GFAItemStatus();
            gfaItemStatus001.setItemBarCode(itemRequestInfo.getItemBarcodes().get(0));
            List<GFAItemStatus> gfaItemStatuses = new ArrayList<>();
            gfaItemStatuses.add(gfaItemStatus001);
            gfaItemStatusCheckRequest.setItemStatus(gfaItemStatuses);
            gfaItemStatusCheckResponse = itemStatusCheck(gfaItemStatusCheckRequest);
            if (gfaItemStatusCheckResponse != null
                    && gfaItemStatusCheckResponse.getDsitem() != null
                    && gfaItemStatusCheckResponse.getDsitem().getTtitem() != null && !gfaItemStatusCheckResponse.getDsitem().getTtitem().isEmpty()) {

                itemStatus = gfaItemStatusCheckResponse.getDsitem().getTtitem().get(0).getItemStatus();
                if (itemStatus.contains(":")) {
                    gfaOnlyStatus = itemStatus.substring(0, itemStatus.indexOf(':') + 1).toUpperCase();
                } else {
                    gfaOnlyStatus = itemStatus.toUpperCase();
                }

                logger.info(gfaOnlyStatus);
                // Call Retrival Order
                if (ReCAPConstants.getGFAStatusAvailableList().contains(gfaOnlyStatus)) {
                    if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
                        itemResponseInformation = callItemEDDRetrivate(itemRequestInfo, itemResponseInformation);
                    } else if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
                        itemResponseInformation = callItemRetrivate(itemRequestInfo, itemResponseInformation);
                    }
                } else {
                    itemResponseInformation.setSuccess(false);
                    itemResponseInformation.setScreenMessage(ReCAPConstants.GFA_RETRIVAL_ITEM_NOT_AVAILABLE);
                }
            } else {
                itemResponseInformation.setSuccess(false);
                itemResponseInformation.setScreenMessage(ReCAPConstants.GFA_ITEM_STATUS_CHECK_FAILED);
            }
        } catch (Exception e) {
            itemResponseInformation.setSuccess(false);
            itemResponseInformation.setScreenMessage(ReCAPConstants.SCSB_REQUEST_EXCEPTION + e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return itemResponseInformation;
    }

    private ItemInformationResponse callItemRetrivate(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {
        GFARetrieveItemRequest gfaRetrieveItemRequest = new GFARetrieveItemRequest();
        TtitemRequest ttitem001 = new TtitemRequest();
        try {
            ttitem001.setCustomerCode(itemRequestInfo.getCustomerCode());
            ttitem001.setItemBarcode(itemRequestInfo.getItemBarcodes().get(0));
            ttitem001.setDestination(itemRequestInfo.getDeliveryLocation());
            ttitem001.setRequestId(itemResponseInformation.getRequestId().toString());
            ttitem001.setRequestor(itemRequestInfo.getPatronBarcode());

            List<TtitemRequest> ttitems = new ArrayList<>();
            ttitems.add(ttitem001);
            RetrieveItemRequest retrieveItem = new RetrieveItemRequest();
            retrieveItem.setTtitem(ttitems);
            gfaRetrieveItemRequest.setRetrieveItem(retrieveItem);
            logger.info("RequestId"+itemResponseInformation.getRequestId());
            if (isUseQueueLasCall()) { // Queue
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(gfaRetrieveItemRequest);
                getProducer().sendBodyAndHeader(ReCAPConstants.SCSB_OUTGOING_QUEUE, json, ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER, itemRequestInfo.getRequestType());
                itemResponseInformation.setSuccess(true);
                itemResponseInformation.setScreenMessage(ReCAPConstants.GFA_RETRIVAL_ORDER_SUCCESSFUL);
            } else {
                GFARetrieveItemResponse gfaRetrieveItemResponse = itemRetrival(gfaRetrieveItemRequest);
                if (gfaRetrieveItemResponse.isSuccess()) {
                    itemResponseInformation.setSuccess(true);
                    itemResponseInformation.setScreenMessage(ReCAPConstants.GFA_RETRIVAL_ORDER_SUCCESSFUL);
                } else {
                    itemResponseInformation.setSuccess(false);
                    itemResponseInformation.setScreenMessage(gfaRetrieveItemResponse.getScrenMessage());
                }
            }
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
            itemResponseInformation.setSuccess(false);
            itemResponseInformation.setScreenMessage(ReCAPConstants.SCSB_REQUEST_EXCEPTION + e.getMessage());
        }
        return itemResponseInformation;
    }

    /**
     * Call item edd retrivate item information response.
     *
     * @param itemRequestInfo         the item request info
     * @param itemResponseInformation the item response information
     * @return the item information response
     */
    public ItemInformationResponse callItemEDDRetrivate(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {
        GFARetrieveEDDItemRequest gfaRetrieveEDDItemRequest = getGFARetrieveEDDItemRequest();
        GFARetrieveItemResponse gfaRetrieveItemResponse;
        TtitemEDDResponse ttitem001 = new TtitemEDDResponse();
        try {
            ttitem001.setCustomerCode(itemRequestInfo.getCustomerCode());
            ttitem001.setItemBarcode(itemRequestInfo.getItemBarcodes().get(0));
            ttitem001.setRequestId(itemRequestInfo.getRequestId());
            ttitem001.setRequestor(itemRequestInfo.getPatronBarcode());
            ttitem001.setRequestorEmail(itemRequestInfo.getEmailAddress());

            ttitem001.setStartPage(itemRequestInfo.getStartPage());
            ttitem001.setEndPage(itemRequestInfo.getEndPage());

            ttitem001.setArticleTitle(itemRequestInfo.getChapterTitle());
            ttitem001.setArticleAuthor(itemRequestInfo.getAuthor());
            ttitem001.setArticleVolume(itemRequestInfo.getVolume() + ", " + itemRequestInfo.getIssue());
            ttitem001.setArticleIssue(itemRequestInfo.getIssue());

            ttitem001.setNotes(itemRequestInfo.getRequestNotes());

            ttitem001.setBiblioTitle(itemRequestInfo.getTitleIdentifier());
            ttitem001.setBiblioAuthor(itemRequestInfo.getItemAuthor());
            ttitem001.setBiblioVolume(itemRequestInfo.getItemVolume());
            ttitem001.setBiblioLocation(itemRequestInfo.getCallNumber());

            List<TtitemEDDResponse> ttitems = new ArrayList<>();
            ttitems.add(ttitem001);
            RetrieveItemEDDRequest retrieveItemEDDRequest = new RetrieveItemEDDRequest();
            retrieveItemEDDRequest.setTtitem(ttitems);
            gfaRetrieveEDDItemRequest.setRetrieveEDD(retrieveItemEDDRequest);
            if (isUseQueueLasCall()) { // Queue
                ObjectMapper objectMapper = getObjectMapper();
                String json = objectMapper.writeValueAsString(gfaRetrieveEDDItemRequest);
                getProducer().sendBodyAndHeader(ReCAPConstants.SCSB_OUTGOING_QUEUE, json, ReCAPConstants.REQUEST_TYPE_QUEUE_HEADER, itemRequestInfo.getRequestType());
                itemResponseInformation.setSuccess(true);
                itemResponseInformation.setScreenMessage(ReCAPConstants.GFA_RETRIVAL_ORDER_SUCCESSFUL);
            } else {
                gfaRetrieveItemResponse = itemEDDRetrival(gfaRetrieveEDDItemRequest);
                if (gfaRetrieveItemResponse.isSuccess()) {
                    itemResponseInformation.setSuccess(true);
                    itemResponseInformation.setScreenMessage(ReCAPConstants.GFA_RETRIVAL_ORDER_SUCCESSFUL);
                } else {
                    itemResponseInformation.setSuccess(false);
                    itemResponseInformation.setScreenMessage(gfaRetrieveItemResponse.getScrenMessage());
                }
            }
        } catch (Exception e) {
            itemResponseInformation.setSuccess(false);
            itemResponseInformation.setScreenMessage(ReCAPConstants.SCSB_REQUEST_EXCEPTION + e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return itemResponseInformation;
    }

    /**
     * Gfa permanent withdrawl direct gfa pwd response.
     *
     * @param gfaPwdRequest the gfa pwd request
     * @return the gfa pwd response
     */
    public GFAPwdResponse gfaPermanentWithdrawlDirect(GFAPwdRequest gfaPwdRequest) {
        GFAPwdResponse gfaPwdResponse = null;
        try {
            HttpEntity<GFAPwdRequest> requestEntity = new HttpEntity(gfaPwdRequest, getHttpHeaders());
            logger.info("GFA PWD Request : {}", convertJsontoString(requestEntity.getBody()));
            RestTemplate restTemplate = getRestTemplate();
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(getGfaServerResponseTimeOutMilliseconds());
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(getGfaServerResponseTimeOutMilliseconds());
            ResponseEntity<GFAPwdResponse> responseEntity = restTemplate.exchange(getGfaItemPermanentWithdrawlDirect(), HttpMethod.POST, requestEntity, GFAPwdResponse.class);
            gfaPwdResponse = responseEntity.getBody();
            logger.info("GFA PWD Response Status Code : {}", responseEntity.getStatusCode().toString());
            logger.info("GFA PWD Response : {}", convertJsontoString(responseEntity.getBody()));
            logger.info("GFA PWD item status processed");
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return gfaPwdResponse;
    }

    /**
     * Gfa permanent withdrawl in direct gfa pwi response.
     *
     * @param gfaPwiRequest the gfa pwi request
     * @return the gfa pwi response
     */
    public GFAPwiResponse gfaPermanentWithdrawlInDirect(GFAPwiRequest gfaPwiRequest) {
        GFAPwiResponse gfaPwiResponse = null;
        try {
            HttpEntity<GFAPwiRequest> requestEntity = new HttpEntity(gfaPwiRequest, getHttpHeaders());
            logger.info("GFA PWI Request : {}", convertJsontoString(requestEntity.getBody()));
            RestTemplate restTemplate = getRestTemplate();
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(getGfaServerResponseTimeOutMilliseconds());
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(getGfaServerResponseTimeOutMilliseconds());
            ResponseEntity<GFAPwiResponse> responseEntity = restTemplate.exchange(getGfaItemPermanentWithdrawlInDirect(), HttpMethod.POST, requestEntity, GFAPwiResponse.class);
            gfaPwiResponse = responseEntity.getBody();
            logger.info("GFA PWI Response Status Code : {}", responseEntity.getStatusCode().toString());
            logger.info("GFA PWI Response : {}", convertJsontoString(responseEntity.getBody()));
            logger.info("GFA PWI item status processed");
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return gfaPwiResponse;
    }

    /**
     * Process las retrieve response item information response.
     *
     * @param body the body
     * @return the item information response
     */
    public ItemInformationResponse processLASRetrieveResponse(String body) {
        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
        ObjectMapper om = new ObjectMapper();
        try {
            GFARetrieveItemResponse gfaRetrieveItemResponse = om.readValue(body, GFARetrieveItemResponse.class);
            gfaRetrieveItemResponse = getLASRetrieveResponse(gfaRetrieveItemResponse);
            if (gfaRetrieveItemResponse.isSuccess()) {
                itemInformationResponse.setRequestId(gfaRetrieveItemResponse.getRetrieveItem().getTtitem().get(0).getRequestId());
                itemInformationResponse.setSuccess(true);
                itemInformationResponse.setScreenMessage(ReCAPConstants.GFA_RETRIVAL_ORDER_SUCCESSFUL);
            } else {
                itemInformationResponse.setRequestId(gfaRetrieveItemResponse.getRetrieveItem().getTtitem().get(0).getRequestId());
                itemInformationResponse.setSuccess(false);
                itemInformationResponse.setScreenMessage(gfaRetrieveItemResponse.getScrenMessage());
            }
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return itemInformationResponse;
    }

    /**
     * Process las EDD response item information response.
     *
     * @param body the body
     * @return item information response
     */
    public ItemInformationResponse processLASEDDRetrieveResponse(String body) {
        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
        ObjectMapper om = new ObjectMapper();
        try {
            GFAEddItemResponse gfaEddItemResponse = om.readValue(body, GFAEddItemResponse.class);
            gfaEddItemResponse = getLASEddRetrieveResponse(gfaEddItemResponse);
            if (gfaEddItemResponse.isSuccess()) {
                itemInformationResponse.setRequestId(gfaEddItemResponse.getRetrieveEDD().getTtitem().get(0).getRequestId());
                itemInformationResponse.setSuccess(true);
                itemInformationResponse.setScreenMessage(ReCAPConstants.GFA_RETRIVAL_ORDER_SUCCESSFUL);
            } else {
                itemInformationResponse.setRequestId(gfaEddItemResponse.getRetrieveEDD().getTtitem().get(0).getRequestId());
                itemInformationResponse.setSuccess(false);
                itemInformationResponse.setScreenMessage(gfaEddItemResponse.getScrenMessage());
            }
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return itemInformationResponse;
    }

    private GFAEddItemResponse getLASEddRetrieveResponse(GFAEddItemResponse gfaEddItemResponse) {
        GFAEddItemResponse gfaRetrieveItemResponse = gfaEddItemResponse;
        if (gfaRetrieveItemResponse != null && gfaRetrieveItemResponse.getRetrieveEDD() != null && gfaRetrieveItemResponse.getRetrieveEDD().getTtitem() != null && !gfaRetrieveItemResponse.getRetrieveEDD().getTtitem().isEmpty()) {
            List<TtitemEDDResponse> titemList = gfaRetrieveItemResponse.getRetrieveEDD().getTtitem();
            for (TtitemEDDResponse ttitemEDDRequest : titemList) {
                if (!ttitemEDDRequest.getErrorCode().isEmpty()) {
                    gfaRetrieveItemResponse.setSuccess(false);
                    gfaRetrieveItemResponse.setScrenMessage(ttitemEDDRequest.getErrorNote());
                } else {
                    gfaRetrieveItemResponse.setSuccess(true);
                }
            }
        } else {
            gfaRetrieveItemResponse.setSuccess(true);
        }
        return gfaRetrieveItemResponse;
    }

    private String convertJsontoString(Object objJson) {
        String strJson = "";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            strJson = objectMapper.writeValueAsString(objJson);
        } catch (JsonProcessingException e) {
            logger.error("", e);
        }
        return strJson;
    }

    public LasItemStatusCheckPollingProcessor getLasItemStatusCheckPollingProcessor() {
        return lasItemStatusCheckPollingProcessor;
    }

    public void setLasItemStatusCheckPollingProcessor(LasItemStatusCheckPollingProcessor lasItemStatusCheckPollingProcessor) {
        this.lasItemStatusCheckPollingProcessor = lasItemStatusCheckPollingProcessor;
    }

    private void lasPolling(ItemRequestInformation itemRequestInfo){
        // Update Request_item_t table with new status - each Item
        RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_LAS_ITEM_STATUS_PENDING);
        RequestItemEntity requestItemEntity = requestItemDetailsRepository.findRequestItemByRequestId(itemRequestInfo.getRequestId());
        requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
        requestItemEntity.setLastUpdatedDate(new Date());
        requestItemDetailsRepository.save(requestItemEntity);

        // Solr Index - each Item
        itemRequestServiceUtil.updateSolrIndex(requestItemEntity.getItemEntity());
        if(ReCAPConstants.LAS_ITEM_STATUS_REST_SERVICE_STATUS == 0) {
            // Start Polling program - Once
            getLasItemStatusCheckPollingProcessor().pollLasItemStatusJobResponse();
        }
    }
}
