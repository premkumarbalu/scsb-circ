package org.recap.service.deaccession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.recap.ReCAPConstants;
import org.recap.controller.RequestItemController;
import org.recap.gfa.model.*;
import org.recap.ils.model.response.ItemHoldResponse;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.*;
import org.recap.model.deaccession.DeAccessionDBResponseEntity;
import org.recap.model.deaccession.DeAccessionItem;
import org.recap.model.deaccession.DeAccessionRequest;
import org.recap.model.deaccession.DeAccessionSolrRequest;
import org.recap.repository.*;
import org.recap.request.GFAService;
import org.recap.request.ItemRequestService;
import org.recap.service.RestHeaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by chenchulakshmig on 28/9/16.
 */
@Component
public class DeAccessionService {

    private static final Logger logger = Logger.getLogger(DeAccessionService.class);

    /**
     * The Bibliographic details repository.
     */
    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    /**
     * The Holdings details repository.
     */
    @Autowired
    HoldingsDetailsRepository holdingsDetailsRepository;

    /**
     * The Item details repository.
     */
    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    /**
     * The Report detail repository.
     */
    @Autowired
    ReportDetailRepository reportDetailRepository;

    /**
     * The Request item details repository.
     */
    @Autowired
    RequestItemDetailsRepository requestItemDetailsRepository;

    /**
     * The Request item status details repository.
     */
    @Autowired
    RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    /**
     * The Item change log details repository.
     */
    @Autowired
    ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    /**
     * The Request item controller.
     */
    @Autowired
    RequestItemController requestItemController;

    /**
     * The Gfa service.
     */
    @Autowired
    GFAService gfaService;
    /**
     * The Item Request Service.
     */
    @Autowired
    ItemRequestService itemRequestService;

    @Autowired
    RestHeaderService restHeaderService;

    public RestHeaderService getRestHeaderService(){
        return restHeaderService;
    }

    /**
     * The Scsb solr client url.
     */
    @Value("${scsb.solr.client.url}")
    String scsbSolrClientUrl;

    @Value("${gfa.item.permanent.withdrawl.direct}")
    private String gfaItemPermanentWithdrawlDirect;

    @Value("${gfa.item.permanent.withdrawl.indirect}")
    private String gfaItemPermanentWithdrawlInDirect;

    @Value("${recap.assist.email.to}")
    private String recapAssistanceEmailTo;


    /**
     * De accession map.
     *
     * @param deAccessionRequest the de accession request
     * @return the map
     */
    public Map<String, String> deAccession(DeAccessionRequest deAccessionRequest) {
        Map<String, String> resultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(deAccessionRequest.getDeAccessionItems())) {
            Map<String, String> barcodeAndStopCodeMap = new HashMap<>();
            List<DeAccessionDBResponseEntity> deAccessionDBResponseEntities = new ArrayList<>();
            String username = StringUtils.isNotBlank(deAccessionRequest.getUsername()) ? deAccessionRequest.getUsername() : ReCAPConstants.DISCOVERY;

            checkGfaItemStatus(deAccessionRequest.getDeAccessionItems(), deAccessionDBResponseEntities, barcodeAndStopCodeMap);
            checkAndCancelHolds(barcodeAndStopCodeMap, deAccessionDBResponseEntities, username);
            deAccessionItemsInDB(barcodeAndStopCodeMap, deAccessionDBResponseEntities, username);
            callGfaDeaccessionService(deAccessionDBResponseEntities, username);
            rollbackLASRejectedItems(deAccessionDBResponseEntities, username);
            deAccessionItemsInSolr(deAccessionDBResponseEntities, resultMap);
            processAndSaveReportEntities(deAccessionDBResponseEntities);
        } else {
            resultMap.put("", ReCAPConstants.DEACCESSION_NO_BARCODE_ERROR);
            return resultMap;
        }
        return resultMap;
    }

    private void rollbackLASRejectedItems(List<DeAccessionDBResponseEntity> deAccessionDBResponseEntities, String username) {
        if (CollectionUtils.isNotEmpty(deAccessionDBResponseEntities)) {
            Map<Integer, String> itemIdAndMessageMap = new HashMap<>();
            List<Integer> bibIds = new ArrayList<>();
            List<Integer> holdingsIds = new ArrayList<>();
            List<Integer> itemIds = new ArrayList<>();
            for (DeAccessionDBResponseEntity deAccessionDBResponseEntity : deAccessionDBResponseEntities) {
                if (deAccessionDBResponseEntity.getStatus().equalsIgnoreCase(ReCAPConstants.FAILURE) && deAccessionDBResponseEntity.getReasonForFailure().contains(ReCAPConstants.LAS_REJECTED)) {
                    bibIds.addAll(deAccessionDBResponseEntity.getBibliographicIds());
                    holdingsIds.addAll(deAccessionDBResponseEntity.getHoldingIds());
                    itemIds.add(deAccessionDBResponseEntity.getItemId());
                    itemIdAndMessageMap.put(deAccessionDBResponseEntity.getItemId(), deAccessionDBResponseEntity.getReasonForFailure());
                }
            }
            Date currentDate = new Date();
            if (CollectionUtils.isNotEmpty(bibIds)) {
                bibliographicDetailsRepository.markBibsAsNotDeleted(bibIds, username, currentDate);
            }
            if (CollectionUtils.isNotEmpty(holdingsIds)) {
                holdingsDetailsRepository.markHoldingsAsNotDeleted(holdingsIds, username, currentDate);
            }
            if (CollectionUtils.isNotEmpty(itemIds)) {
                itemDetailsRepository.markItemsAsNotDeleted(itemIds, username, currentDate);
                saveItemChangeLogEntities(itemIds, username, ReCAPConstants.DEACCESSION_ROLLBACK, currentDate, ReCAPConstants.DEACCESSION_ROLLBACK_NOTES, itemIdAndMessageMap);
            }
        }
    }

    private void checkGfaItemStatus(List<DeAccessionItem> deAccessionItems, List<DeAccessionDBResponseEntity> deAccessionDBResponseEntities, Map<String, String> barcodeAndStopCodeMap) {
        try {
            for (DeAccessionItem deAccessionItem : deAccessionItems) {
                String itemBarcode = deAccessionItem.getItemBarcode();
                if (StringUtils.isNotBlank(itemBarcode)) {
                    List<ItemEntity> itemEntities = itemDetailsRepository.findByBarcode(itemBarcode.trim());
                    if (CollectionUtils.isNotEmpty(itemEntities)) {
                        ItemEntity itemEntity = itemEntities.get(0);
                        if (itemEntity.isDeleted()) {
                            deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deAccessionItem.getDeliveryLocation(), ReCAPConstants.REQUESTED_ITEM_DEACCESSIONED, itemEntity));
                        } else if (!itemEntity.isComplete()) {
                            deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deAccessionItem.getDeliveryLocation(), ReCAPConstants.ITEM_BARCDE_DOESNOT_EXIST, itemEntity));
                        } else {
                            String scsbItemStatus = itemEntity.getItemStatusEntity().getStatusCode();
                            String gfaItemStatus = callGfaItemStatus(itemBarcode);
                            if (StringUtils.isNotBlank(gfaItemStatus)) {
                                gfaItemStatus = gfaItemStatus.toUpperCase();
                                gfaItemStatus = gfaItemStatus.contains(":") ? gfaItemStatus.substring(0, gfaItemStatus.indexOf(':') + 1) : gfaItemStatus;
                                if ((StringUtils.isNotBlank(gfaItemStatus) && !ReCAPConstants.GFA_STATUS_NOT_ON_FILE.equalsIgnoreCase(gfaItemStatus))
                                        && ((ReCAPConstants.AVAILABLE.equals(scsbItemStatus) && ReCAPConstants.getGFAStatusAvailableList().contains(gfaItemStatus))
                                        || (ReCAPConstants.NOT_AVAILABLE.equals(scsbItemStatus) && ReCAPConstants.getGFAStatusNotAvailableList().contains(gfaItemStatus)))) {
                                    barcodeAndStopCodeMap.put(itemBarcode.trim(), deAccessionItem.getDeliveryLocation());
                                } else {
                                    deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deAccessionItem.getDeliveryLocation(), MessageFormat.format(ReCAPConstants.GFA_ITEM_STATUS_MISMATCH, recapAssistanceEmailTo, recapAssistanceEmailTo), itemEntity));
                                }
                            } else {
                                deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deAccessionItem.getDeliveryLocation(), MessageFormat.format(ReCAPConstants.GFA_SERVER_DOWN, recapAssistanceEmailTo, recapAssistanceEmailTo), itemEntity));
                            }
                        }
                    } else {
                        deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deAccessionItem.getDeliveryLocation(), ReCAPConstants.ITEM_BARCDE_DOESNOT_EXIST, null));
                    }
                } else {
                    deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deAccessionItem.getDeliveryLocation(), ReCAPConstants.DEACCESSION_NO_BARCODE_PROVIDED_ERROR, null));
                }
            }
        } catch (Exception e) {
            logger.error("Exception : ", e);
        }
    }

    private String callGfaItemStatus(String itemBarcode) {
        String gfaItemStatusValue = null;
        GFAItemStatusCheckRequest gfaItemStatusCheckRequest = new GFAItemStatusCheckRequest();
        GFAItemStatus gfaItemStatus = new GFAItemStatus();
        gfaItemStatus.setItemBarCode(itemBarcode);
        gfaItemStatusCheckRequest.setItemStatus(Arrays.asList(gfaItemStatus));
        GFAItemStatusCheckResponse gfaItemStatusCheckResponse = gfaService.itemStatusCheck(gfaItemStatusCheckRequest);
        if (null != gfaItemStatusCheckResponse) {
            Dsitem dsitem = gfaItemStatusCheckResponse.getDsitem();
            if (null != dsitem) {
                List<Ttitem> ttitems = dsitem.getTtitem();
                if (CollectionUtils.isNotEmpty(ttitems)) {
                    gfaItemStatusValue = ttitems.get(0).getItemStatus();
                }
            }
        }
        return gfaItemStatusValue;
    }

    private void callGfaDeaccessionService(List<DeAccessionDBResponseEntity> deAccessionDBResponseEntities, String username) {
        if (CollectionUtils.isNotEmpty(deAccessionDBResponseEntities)) {
            for (DeAccessionDBResponseEntity deAccessionDBResponseEntity : deAccessionDBResponseEntities) {
                if (ReCAPConstants.SUCCESS.equalsIgnoreCase(deAccessionDBResponseEntity.getStatus()) && ReCAPConstants.AVAILABLE.equalsIgnoreCase(deAccessionDBResponseEntity.getItemStatus())) {
                    GFAPwdRequest gfaPwdRequest = new GFAPwdRequest();
                    GFAPwdDsItemRequest gfaPwdDsItemRequest = new GFAPwdDsItemRequest();
                    GFAPwdTtItemRequest gfaPwdTtItemRequest = new GFAPwdTtItemRequest();
                    gfaPwdTtItemRequest.setCustomerCode(deAccessionDBResponseEntity.getCustomerCode());
                    gfaPwdTtItemRequest.setItemBarcode(deAccessionDBResponseEntity.getBarcode());
                    gfaPwdTtItemRequest.setDestination(deAccessionDBResponseEntity.getDeliveryLocation());
                    gfaPwdTtItemRequest.setRequestor(username);
                    gfaPwdDsItemRequest.setTtitem(Arrays.asList(gfaPwdTtItemRequest));
                    gfaPwdRequest.setDsitem(gfaPwdDsItemRequest);
                    GFAPwdResponse gfaPwdResponse = gfaService.gfaPermanentWithdrawlDirect(gfaPwdRequest);
                    if (null != gfaPwdResponse) {
                        GFAPwdDsItemResponse gfaPwdDsItemResponse = gfaPwdResponse.getDsitem();
                        if (null != gfaPwdDsItemResponse) {
                            List<GFAPwdTtItemResponse> gfaPwdTtItemResponses = gfaPwdDsItemResponse.getTtitem();
                            if (CollectionUtils.isNotEmpty(gfaPwdTtItemResponses)) {
                                GFAPwdTtItemResponse gfaPwdTtItemResponse = gfaPwdTtItemResponses.get(0);
                                String errorCode = (String) gfaPwdTtItemResponse.getErrorCode();
                                String errorNote = (String) gfaPwdTtItemResponse.getErrorNote();
                                if (StringUtils.isNotBlank(errorCode) && StringUtils.isNotBlank(errorNote)) {
                                    deAccessionDBResponseEntity.setStatus(ReCAPConstants.FAILURE);
                                    deAccessionDBResponseEntity.setReasonForFailure(MessageFormat.format(ReCAPConstants.LAS_DEACCESSION_REJECT_ERROR, ReCAPConstants.REQUEST_TYPE_PW_DIRECT, errorCode, errorNote));
                                }
                            }
                        }
                    }
                } else if (ReCAPConstants.SUCCESS.equalsIgnoreCase(deAccessionDBResponseEntity.getStatus()) && ReCAPConstants.NOT_AVAILABLE.equalsIgnoreCase(deAccessionDBResponseEntity.getItemStatus())) {
                    GFAPwiRequest gfaPwiRequest = new GFAPwiRequest();
                    GFAPwiDsItemRequest gfaPwiDsItemRequest = new GFAPwiDsItemRequest();
                    GFAPwiTtItemRequest gfaPwiTtItemRequest = new GFAPwiTtItemRequest();
                    gfaPwiTtItemRequest.setCustomerCode(deAccessionDBResponseEntity.getCustomerCode());
                    gfaPwiTtItemRequest.setItemBarcode(deAccessionDBResponseEntity.getBarcode());
                    gfaPwiDsItemRequest.setTtitem(Arrays.asList(gfaPwiTtItemRequest));
                    gfaPwiRequest.setDsitem(gfaPwiDsItemRequest);
                    GFAPwiResponse gfaPwiResponse = gfaService.gfaPermanentWithdrawlInDirect(gfaPwiRequest);
                    if (null != gfaPwiResponse) {
                        GFAPwiDsItemResponse gfaPwiDsItemResponse = gfaPwiResponse.getDsitem();
                        if (null != gfaPwiDsItemResponse) {
                            List<GFAPwiTtItemResponse> gfaPwiTtItemResponses = gfaPwiDsItemResponse.getTtitem();
                            if (CollectionUtils.isNotEmpty(gfaPwiTtItemResponses)) {
                                GFAPwiTtItemResponse gfaPwiTtItemResponse = gfaPwiTtItemResponses.get(0);
                                String errorCode = gfaPwiTtItemResponse.getErrorCode();
                                String errorNote = gfaPwiTtItemResponse.getErrorNote();
                                if (StringUtils.isNotBlank(errorCode) && StringUtils.isNotBlank(errorNote)) {
                                    deAccessionDBResponseEntity.setStatus(ReCAPConstants.FAILURE);
                                    deAccessionDBResponseEntity.setReasonForFailure(MessageFormat.format(ReCAPConstants.LAS_DEACCESSION_REJECT_ERROR, ReCAPConstants.REQUEST_TYPE_PW_INDIRECT, errorCode, errorNote));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check and cancel holds.
     *
     * @param barcodeAndStopCodeMap         the barcode and stop code map
     * @param deAccessionDBResponseEntities the de accession db response entities
     * @param username                      the username
     */
    public void checkAndCancelHolds(Map<String, String> barcodeAndStopCodeMap, List<DeAccessionDBResponseEntity> deAccessionDBResponseEntities, String username) {
        Set<String> itemBarcodeList = barcodeAndStopCodeMap.keySet();
        if (CollectionUtils.isNotEmpty(itemBarcodeList)) {
            String deliveryLocation = null;
            for (String itemBarcode : itemBarcodeList) {
                try {
                    deliveryLocation = barcodeAndStopCodeMap.get(itemBarcode);
                    List<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByItemBarcode(itemBarcode);
                    if (CollectionUtils.isNotEmpty(requestItemEntities)) {
                        RequestItemEntity activeRetrievalRequest = null;
                        RequestItemEntity activeRecallRequest = null;
                        for (RequestItemEntity requestItemEntity : requestItemEntities) { // Get active retrieval and recall requests.
                            if (ReCAPConstants.RETRIEVAL.equals(requestItemEntity.getRequestTypeEntity().getRequestTypeCode()) && ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED.equals(requestItemEntity.getRequestStatusEntity().getRequestStatusCode())) {
                                activeRetrievalRequest = requestItemEntity;
                            }
                            if (ReCAPConstants.REQUEST_TYPE_RECALL.equals(requestItemEntity.getRequestTypeEntity().getRequestTypeCode()) && ReCAPConstants.REQUEST_STATUS_RECALLED.equals(requestItemEntity.getRequestStatusEntity().getRequestStatusCode())) {
                                activeRecallRequest = requestItemEntity;
                            }
                        }
                        if (activeRetrievalRequest != null && activeRecallRequest != null) {
                            String retrievalRequestingInstitution = activeRetrievalRequest.getInstitutionEntity().getInstitutionCode();
                            String recallRequestingInstitution = activeRecallRequest.getInstitutionEntity().getInstitutionCode();
                            if (retrievalRequestingInstitution.equals(recallRequestingInstitution)) { // If retrieval order institution and recall order institution are same, cancel recall request.
                                ItemHoldResponse cancelRecallResponse = cancelRequest(activeRecallRequest, username);
                                if (cancelRecallResponse.isSuccess()) {
                                    barcodeAndStopCodeMap.put(itemBarcode, deliveryLocation);
                                } else {
                                    deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deliveryLocation, ReCAPConstants.REASON_CANCEL_REQUEST_FAILED + " - " + cancelRecallResponse.getScreenMessage(), null));
                                }
                            } else { // If retrieval order institution and recall order institution are different, cancel retrieval request and recall request.
                                ItemInformationResponse itemInformationResponse = getItemInformation(activeRetrievalRequest);
                                if (getHoldQueueLength(itemInformationResponse) > 0) {
                                    ItemHoldResponse cancelRetrievalResponse = cancelRequest(activeRetrievalRequest, username);
                                    if (cancelRetrievalResponse.isSuccess()) {
                                        ItemHoldResponse cancelRecallResponse = cancelRequest(activeRecallRequest, username);
                                        if (cancelRecallResponse.isSuccess()) {
                                            barcodeAndStopCodeMap.put(itemBarcode, deliveryLocation);
                                        } else {
                                            deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deliveryLocation, ReCAPConstants.REASON_CANCEL_REQUEST_FAILED + " - " + cancelRecallResponse.getScreenMessage(), null));
                                        }
                                    } else {
                                        deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deliveryLocation, ReCAPConstants.REASON_CANCEL_REQUEST_FAILED + " - " + cancelRetrievalResponse.getScreenMessage(), null));
                                    }
                                } else {
                                    barcodeAndStopCodeMap.put(itemBarcode, deliveryLocation);
                                }
                            }
                        } else if (activeRetrievalRequest != null && activeRecallRequest == null) {
                            ItemInformationResponse itemInformationResponse = getItemInformation(activeRetrievalRequest);
                            if (getHoldQueueLength(itemInformationResponse) > 0) {
                                ItemHoldResponse cancelRetrievalResponse = cancelRequest(activeRetrievalRequest, username);
                                if (cancelRetrievalResponse.isSuccess()) {
                                    barcodeAndStopCodeMap.put(itemBarcode, deliveryLocation);
                                } else {
                                    deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deliveryLocation, ReCAPConstants.REASON_CANCEL_REQUEST_FAILED + " - " + cancelRetrievalResponse.getScreenMessage(), null));
                                }
                            } else {
                                barcodeAndStopCodeMap.put(itemBarcode, deliveryLocation);
                            }
                        } else if (activeRetrievalRequest == null && activeRecallRequest != null) {
                            barcodeAndStopCodeMap.put(itemBarcode, deliveryLocation);
                        } else if (activeRetrievalRequest == null && activeRecallRequest == null) {
                            barcodeAndStopCodeMap.put(itemBarcode, deliveryLocation);
                        }
                    } else {
                        barcodeAndStopCodeMap.put(itemBarcode, deliveryLocation);
                    }
                } catch (Exception e) {
                    deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deliveryLocation, ReCAPConstants.FAILURE + " - " + e, null));
                    logger.error("Exception : ", e);
                }
            }
        }
    }

    private ItemInformationResponse getItemInformation(RequestItemEntity activeRetrievalRequest) {
        ItemEntity itemEntity = activeRetrievalRequest.getItemEntity();
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(Arrays.asList(itemEntity.getBarcode()));
        itemRequestInformation.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
        itemRequestInformation.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
        itemRequestInformation.setRequestingInstitution(activeRetrievalRequest.getInstitutionEntity().getInstitutionCode());
        itemRequestInformation.setPatronBarcode(activeRetrievalRequest.getPatronId());
        itemRequestInformation.setDeliveryLocation(activeRetrievalRequest.getStopCode());
        return (ItemInformationResponse) requestItemController.itemInformation(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
    }

    /**
     * Cancel request item hold response.
     *
     * @param requestItemEntity the request item entity
     * @param username          the username
     * @return the item hold response
     */
    public ItemHoldResponse cancelRequest(RequestItemEntity requestItemEntity, String username) {
        ItemEntity itemEntity = requestItemEntity.getItemEntity();
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(Arrays.asList(itemEntity.getBarcode()));
        itemRequestInformation.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
        itemRequestInformation.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
        itemRequestInformation.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());
        itemRequestInformation.setPatronBarcode(requestItemEntity.getPatronId());
        itemRequestInformation.setDeliveryLocation(requestItemEntity.getStopCode());
        itemRequestInformation.setUsername(username);
        ItemHoldResponse itemCancelHoldResponse = (ItemHoldResponse) requestItemController.cancelHoldItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
        if (itemCancelHoldResponse.isSuccess()) {
            RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_CANCELED);
            requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
            if (requestItemEntity.getRequestTypeEntity().getRequestTypeCode().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
                requestItemEntity.getItemEntity().setItemAvailabilityStatusId(1);
            }
            RequestItemEntity savedRequestItemEntity = requestItemDetailsRepository.save(requestItemEntity);
            saveItemChangeLogEntity(savedRequestItemEntity.getRequestId(), username, ReCAPConstants.REQUEST_ITEM_CANCEL_DEACCESSION_ITEM, ReCAPConstants.REQUEST_ITEM_CANCELED_FOR_DEACCESSION + savedRequestItemEntity.getItemId());
            itemRequestService.updateSolrIndex(savedRequestItemEntity.getItemEntity());
            itemCancelHoldResponse.setSuccess(true);
            itemCancelHoldResponse.setScreenMessage(ReCAPConstants.REQUEST_CANCELLATION_SUCCCESS);
        }
        return itemCancelHoldResponse;
    }

    private void saveItemChangeLogEntity(Integer requestId, String deaccessionUser, String operationType, String notes) {
        ItemChangeLogEntity itemChangeLogEntity = new ItemChangeLogEntity();
        itemChangeLogEntity.setUpdatedBy(deaccessionUser);
        itemChangeLogEntity.setUpdatedDate(new Date());
        itemChangeLogEntity.setOperationType(operationType);
        itemChangeLogEntity.setRecordId(requestId);
        itemChangeLogEntity.setNotes(notes);
        itemChangeLogDetailsRepository.save(itemChangeLogEntity);
    }

    private void saveItemChangeLogEntities(List<Integer> itemIds, String deaccessionUser, String operationType, Date updatedDate, String notes, Map<Integer, String> itemIdAndMessageMap) {
        List<ItemChangeLogEntity> itemChangeLogEntities = new ArrayList<>();
        for (Integer itemId : itemIds) {
            ItemChangeLogEntity itemChangeLogEntity = new ItemChangeLogEntity();
            itemChangeLogEntity.setUpdatedBy(deaccessionUser);
            itemChangeLogEntity.setUpdatedDate(updatedDate);
            itemChangeLogEntity.setOperationType(operationType);
            itemChangeLogEntity.setRecordId(itemId);
            itemChangeLogEntity.setNotes(itemIdAndMessageMap.get(itemId) + notes);
            itemChangeLogEntities.add(itemChangeLogEntity);
        }
        itemChangeLogDetailsRepository.save(itemChangeLogEntities);
    }

    private int getHoldQueueLength(ItemInformationResponse itemInformationResponse) {
        int iholdQueue = 0;
        if (StringUtils.isNotBlank(itemInformationResponse.getHoldQueueLength())) {
            iholdQueue = Integer.parseInt(itemInformationResponse.getHoldQueueLength());
        }
        return iholdQueue;
    }

    /**
     * De accession items in db.
     *
     * @param barcodeAndStopCodeMap         the barcode and stop code map
     * @param deAccessionDBResponseEntities the de accession db response entities
     * @param username                      the username
     */
    public void deAccessionItemsInDB(Map<String, String> barcodeAndStopCodeMap, List<DeAccessionDBResponseEntity> deAccessionDBResponseEntities, String username) {
        DeAccessionDBResponseEntity deAccessionDBResponseEntity;
        Date currentDate = new Date();
        Set<String> itemBarcodeList = barcodeAndStopCodeMap.keySet();
        List<ItemEntity> itemEntityList = itemDetailsRepository.findByBarcodeIn(new ArrayList<>(itemBarcodeList));
        try {
            String barcode = null;
            String deliveryLocation = null;
            for(ItemEntity itemEntity : itemEntityList) {
                try {
                    barcode = itemEntity.getBarcode();
                    deliveryLocation = barcodeAndStopCodeMap.get(barcode);
                    List<HoldingsEntity> holdingsEntities = itemEntity.getHoldingsEntities();
                    List<BibliographicEntity> bibliographicEntities = itemEntity.getBibliographicEntities();
                    Integer itemId = itemEntity.getItemId();
                    List<Integer> holdingsIds = processHoldings(holdingsEntities, username);
                    List<Integer> bibliographicIds = processBibs(bibliographicEntities, username);
                    itemDetailsRepository.markItemAsDeleted(itemId, username, currentDate);
                    deAccessionDBResponseEntity = prepareSuccessResponse(barcode, deliveryLocation, itemEntity, holdingsIds, bibliographicIds);
                    deAccessionDBResponseEntities.add(deAccessionDBResponseEntity);
                } catch (Exception ex) {
                    logger.error(ReCAPConstants.LOG_ERROR, ex);
                    deAccessionDBResponseEntity = prepareFailureResponse(barcode, deliveryLocation, "Exception" + ex, null);
                    deAccessionDBResponseEntities.add(deAccessionDBResponseEntity);
                }
            }
        } catch (Exception ex) {
            logger.error(ReCAPConstants.LOG_ERROR,ex);
        }
    }

    /**
     * Process and save list.
     *
     * @param deAccessionDBResponseEntities the de accession db response entities
     * @return the list
     */
    public List<ReportEntity> processAndSaveReportEntities(List<DeAccessionDBResponseEntity> deAccessionDBResponseEntities) {
        List<ReportEntity> reportEntities = new ArrayList<>();
        ReportEntity reportEntity = null;
        if (CollectionUtils.isNotEmpty(deAccessionDBResponseEntities)) {
            for (DeAccessionDBResponseEntity deAccessionDBResponseEntity : deAccessionDBResponseEntities) {
                List<String> owningInstitutionBibIds = deAccessionDBResponseEntity.getOwningInstitutionBibIds();
                if (CollectionUtils.isNotEmpty(owningInstitutionBibIds)) {
                    for (String owningInstitutionBibId : owningInstitutionBibIds) {
                        reportEntity = generateReportEntity(deAccessionDBResponseEntity, owningInstitutionBibId);
                        reportEntities.add(reportEntity);
                    }
                } else {
                    reportEntity = generateReportEntity(deAccessionDBResponseEntity, null);
                    reportEntities.add(reportEntity);
                }
            }
            if (!CollectionUtils.isEmpty(reportEntities)) {
                reportDetailRepository.save(reportEntities);
            }
        }
        return reportEntities;
    }

    private ReportEntity generateReportEntity(DeAccessionDBResponseEntity deAccessionDBResponseEntity, String owningInstitutionBibId) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(ReCAPConstants.DEACCESSION_REPORT);
        reportEntity.setType(ReCAPConstants.DEACCESSION_SUMMARY_REPORT);
        reportEntity.setCreatedDate(new Date());

        List<ReportDataEntity> reportDataEntities = new ArrayList<>();

        ReportDataEntity dateReportDataEntity = new ReportDataEntity();
        dateReportDataEntity.setHeaderName(ReCAPConstants.DATE_OF_DEACCESSION);
        dateReportDataEntity.setHeaderValue(formatter.format(new Date()));
        reportDataEntities.add(dateReportDataEntity);

        if (!org.springframework.util.StringUtils.isEmpty(deAccessionDBResponseEntity.getInstitutionCode())) {
            reportEntity.setInstitutionName(deAccessionDBResponseEntity.getInstitutionCode());

            ReportDataEntity owningInstitutionReportDataEntity = new ReportDataEntity();
            owningInstitutionReportDataEntity.setHeaderName(ReCAPConstants.OWNING_INSTITUTION);
            owningInstitutionReportDataEntity.setHeaderValue(deAccessionDBResponseEntity.getInstitutionCode());
            reportDataEntities.add(owningInstitutionReportDataEntity);
        } else {
            reportEntity.setInstitutionName("NA");
        }

        ReportDataEntity barcodeReportDataEntity = new ReportDataEntity();
        barcodeReportDataEntity.setHeaderName(ReCAPConstants.BARCODE);
        barcodeReportDataEntity.setHeaderValue(deAccessionDBResponseEntity.getBarcode());
        reportDataEntities.add(barcodeReportDataEntity);

        if (!org.springframework.util.StringUtils.isEmpty(owningInstitutionBibId)) {
            ReportDataEntity owningInstitutionBibIdReportDataEntity = new ReportDataEntity();
            owningInstitutionBibIdReportDataEntity.setHeaderName(ReCAPConstants.OWNING_INST_BIB_ID);
            owningInstitutionBibIdReportDataEntity.setHeaderValue(owningInstitutionBibId);
            reportDataEntities.add(owningInstitutionBibIdReportDataEntity);
        }

        if (!org.springframework.util.StringUtils.isEmpty(deAccessionDBResponseEntity.getCollectionGroupCode())) {
            ReportDataEntity collectionGroupCodeReportDataEntity = new ReportDataEntity();
            collectionGroupCodeReportDataEntity.setHeaderName(ReCAPConstants.COLLECTION_GROUP_CODE);
            collectionGroupCodeReportDataEntity.setHeaderValue(deAccessionDBResponseEntity.getCollectionGroupCode());
            reportDataEntities.add(collectionGroupCodeReportDataEntity);
        }

        ReportDataEntity statusReportDataEntity = new ReportDataEntity();
        statusReportDataEntity.setHeaderName(ReCAPConstants.STATUS);
        statusReportDataEntity.setHeaderValue(deAccessionDBResponseEntity.getStatus());
        reportDataEntities.add(statusReportDataEntity);

        if (!org.springframework.util.StringUtils.isEmpty(deAccessionDBResponseEntity.getReasonForFailure())) {
            ReportDataEntity reasonForFailureReportDataEntity = new ReportDataEntity();
            reasonForFailureReportDataEntity.setHeaderName(ReCAPConstants.REASON_FOR_FAILURE);
            reasonForFailureReportDataEntity.setHeaderValue(deAccessionDBResponseEntity.getReasonForFailure());
            reportDataEntities.add(reasonForFailureReportDataEntity);
        }

        reportEntity.setReportDataEntities(reportDataEntities);
        return reportEntity;
    }

    private DeAccessionDBResponseEntity prepareSuccessResponse(String itemBarcode, String deliveryLocation, ItemEntity itemEntity, List<Integer> holdingIds, List<Integer> bibliographicIds) throws JSONException {
        DeAccessionDBResponseEntity deAccessionDBResponseEntity = new DeAccessionDBResponseEntity();
        deAccessionDBResponseEntity.setBarcode(itemBarcode);
        deAccessionDBResponseEntity.setDeliveryLocation(deliveryLocation);
        deAccessionDBResponseEntity.setStatus(ReCAPConstants.SUCCESS);
        populateDeAccessionDBResponseEntity(itemEntity, deAccessionDBResponseEntity);
        deAccessionDBResponseEntity.setHoldingIds(holdingIds);
        deAccessionDBResponseEntity.setBibliographicIds(bibliographicIds);
        return deAccessionDBResponseEntity;
    }

    private DeAccessionDBResponseEntity prepareFailureResponse(String itemBarcode, String deliveryLocation, String reasonForFailure, ItemEntity itemEntity) {
        DeAccessionDBResponseEntity deAccessionDBResponseEntity = new DeAccessionDBResponseEntity();
        deAccessionDBResponseEntity.setBarcode(itemBarcode);
        deAccessionDBResponseEntity.setDeliveryLocation(deliveryLocation);
        deAccessionDBResponseEntity.setStatus(ReCAPConstants.FAILURE);
        deAccessionDBResponseEntity.setReasonForFailure(reasonForFailure);
        if (itemEntity != null) {
            try {
                populateDeAccessionDBResponseEntity(itemEntity, deAccessionDBResponseEntity);
            } catch (JSONException e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
        return deAccessionDBResponseEntity;
    }

    private void populateDeAccessionDBResponseEntity(ItemEntity itemEntity, DeAccessionDBResponseEntity deAccessionDBResponseEntity) throws JSONException {
        ItemStatusEntity itemStatusEntity = itemEntity.getItemStatusEntity();
        if (itemStatusEntity != null) {
            deAccessionDBResponseEntity.setItemStatus(itemStatusEntity.getStatusCode());
        }
        InstitutionEntity institutionEntity = itemEntity.getInstitutionEntity();
        if (institutionEntity != null) {
            deAccessionDBResponseEntity.setInstitutionCode(institutionEntity.getInstitutionCode());
        }
        CollectionGroupEntity collectionGroupEntity = itemEntity.getCollectionGroupEntity();
        if (collectionGroupEntity != null) {
            deAccessionDBResponseEntity.setCollectionGroupCode(collectionGroupEntity.getCollectionGroupCode());
        }
        deAccessionDBResponseEntity.setCustomerCode(itemEntity.getCustomerCode());
        deAccessionDBResponseEntity.setItemId(itemEntity.getItemId());
        List<BibliographicEntity> bibliographicEntities = itemEntity.getBibliographicEntities();
        List<String> owningInstitutionBibIds = new ArrayList<>();
        for (BibliographicEntity bibliographicEntity : bibliographicEntities) {
            String owningInstitutionBibId = bibliographicEntity.getOwningInstitutionBibId();
            owningInstitutionBibIds.add(owningInstitutionBibId);
        }
        deAccessionDBResponseEntity.setOwningInstitutionBibIds(owningInstitutionBibIds);
    }

    private List<Integer> processBibs(List<BibliographicEntity> bibliographicEntities, String username) throws JSONException {
        List<Integer> bibliographicIds = new ArrayList<>();
        for (BibliographicEntity bibliographicEntity : bibliographicEntities) {
            Integer owningInstitutionId = bibliographicEntity.getOwningInstitutionId();
            String owningInstitutionBibId = bibliographicEntity.getOwningInstitutionBibId();
            Long nonDeletedItemsCount = bibliographicDetailsRepository.getNonDeletedItemsCount(owningInstitutionId, owningInstitutionBibId);
            if (nonDeletedItemsCount == 1) {
                bibliographicIds.add(bibliographicEntity.getBibliographicId());
            }
        }
        if (CollectionUtils.isNotEmpty(bibliographicIds)) {
            bibliographicDetailsRepository.markBibsAsDeleted(bibliographicIds, username, new Date());
        }
        return bibliographicIds;
    }

    private List<Integer> processHoldings(List<HoldingsEntity> holdingsEntities, String username) throws JSONException {
        List<Integer> holdingIds = new ArrayList<>();
        for (HoldingsEntity holdingsEntity : holdingsEntities) {
            Integer owningInstitutionId = holdingsEntity.getOwningInstitutionId();
            String owningInstitutionHoldingsId = holdingsEntity.getOwningInstitutionHoldingsId();
            Long nonDeletedItemsCount = holdingsDetailsRepository.getNonDeletedItemsCount(owningInstitutionId, owningInstitutionHoldingsId);
            if (nonDeletedItemsCount == 1) {
                holdingIds.add(holdingsEntity.getHoldingsId());
            }
        }
        if (CollectionUtils.isNotEmpty(holdingIds)) {
            holdingsDetailsRepository.markHoldingsAsDeleted(holdingIds, username, new Date());
        }
        return holdingIds;
    }

    /**
     * De accession items in solr
     * @param deAccessionDBResponseEntities
     * @param resultMap
     */
    public void deAccessionItemsInSolr(List<DeAccessionDBResponseEntity> deAccessionDBResponseEntities, Map<String, String> resultMap) {
        try {
            if (CollectionUtils.isNotEmpty(deAccessionDBResponseEntities)) {
                List<Integer> bibIds = new ArrayList<>();
                List<Integer> holdingsIds = new ArrayList<>();
                List<Integer> itemIds = new ArrayList<>();
                for (DeAccessionDBResponseEntity deAccessionDBResponseEntity : deAccessionDBResponseEntities) {
                    if (deAccessionDBResponseEntity.getStatus().equalsIgnoreCase(ReCAPConstants.FAILURE)) {
                        resultMap.put(deAccessionDBResponseEntity.getBarcode(), deAccessionDBResponseEntity.getStatus() + " - " + deAccessionDBResponseEntity.getReasonForFailure());
                    } else if (deAccessionDBResponseEntity.getStatus().equalsIgnoreCase(ReCAPConstants.SUCCESS)) {
                        resultMap.put(deAccessionDBResponseEntity.getBarcode(), deAccessionDBResponseEntity.getStatus());
                        bibIds.addAll(deAccessionDBResponseEntity.getBibliographicIds());
                        holdingsIds.addAll(deAccessionDBResponseEntity.getHoldingIds());
                        itemIds.add(deAccessionDBResponseEntity.getItemId());
                    }
                }
                if (CollectionUtils.isNotEmpty(bibIds) || CollectionUtils.isNotEmpty(holdingsIds) || CollectionUtils.isNotEmpty(itemIds)) {
                    String deAccessionSolrClientUrl = scsbSolrClientUrl + ReCAPConstants.DEACCESSION_IN_SOLR_URL;
                    DeAccessionSolrRequest deAccessionSolrRequest = new DeAccessionSolrRequest();
                    deAccessionSolrRequest.setBibIds(bibIds);
                    deAccessionSolrRequest.setHoldingsIds(holdingsIds);
                    deAccessionSolrRequest.setItemIds(itemIds);

                    RestTemplate restTemplate = new RestTemplate();
                    HttpEntity<DeAccessionSolrRequest> requestEntity = new HttpEntity(deAccessionSolrRequest, getRestHeaderService().getHttpHeaders());
                    ResponseEntity<String> responseEntity = restTemplate.exchange(deAccessionSolrClientUrl, HttpMethod.POST, requestEntity, String.class);
                    logger.info("Deaccession Item Solr update status : " + responseEntity.getBody());
                }
            }
        } catch (Exception e) {
            logger.error("Exception : ", e);
        }
    }
}
