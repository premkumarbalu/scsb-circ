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

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    HoldingsDetailsRepository holdingsDetailsRepository;

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Autowired
    ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    @Autowired
    RequestItemController requestItemController;

    @Autowired
    GFAService gfaService;

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.solr.client.url}")
    String scsbSolrClientUrl;

    @Value("${gfa.item.permanent.withdrawl.direct}")
    private String gfaItemPermanentWithdrawlDirect;

    @Value("${gfa.item.permanent.withdrawl.indirect}")
    private String gfaItemPermanentWithdrawlInDirect;

    @Value("${request.cancel.email.recap.to}")
    private String gfaAssistanceEmailTo;


    public Map<String, String> deAccession(DeAccessionRequest deAccessionRequest) {
        Map<String, String> resultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(deAccessionRequest.getDeAccessionItems())) {
            Map<String, String> barcodeAndStopCodeMap = new HashMap<>();
            List<DeAccessionDBResponseEntity> deAccessionDBResponseEntities = new ArrayList<>();
            String username = StringUtils.isNotBlank(deAccessionRequest.getUsername()) ? deAccessionRequest.getUsername() : ReCAPConstants.DISCOVERY;
            checkGfaItemStatusForPWI(deAccessionRequest.getDeAccessionItems(), deAccessionDBResponseEntities, barcodeAndStopCodeMap);
            checkAndCancelHolds(barcodeAndStopCodeMap, deAccessionDBResponseEntities, username);
            deAccessionItemsInDB(barcodeAndStopCodeMap, deAccessionDBResponseEntities, username);
            processAndSave(deAccessionDBResponseEntities);
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
                deAccessionItemsInSolr(bibIds, holdingsIds, itemIds);
                callGfaDeaccessionService(deAccessionDBResponseEntities, username);
                return resultMap;
            }
        } else {
            resultMap.put("", ReCAPConstants.DEACCESSION_NO_BARCODE_ERROR);
            return resultMap;
        }
        return resultMap;
    }

    private void checkGfaItemStatusForPWI(List<DeAccessionItem> deAccessionItems, List<DeAccessionDBResponseEntity> deAccessionDBResponseEntities, Map<String, String> barcodeAndStopCodeMap) {
        try {
            for (DeAccessionItem deAccessionItem : deAccessionItems) {
                String itemBarcode = deAccessionItem.getItemBarcode();
                if (StringUtils.isNotBlank(itemBarcode)) {
                    List<ItemEntity> itemEntities = itemDetailsRepository.findByBarcode(itemBarcode.trim());
                    if (CollectionUtils.isNotEmpty(itemEntities)) {
                        ItemEntity itemEntity = itemEntities.get(0);
                        if (itemEntity.isDeleted()) {
                            deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deAccessionItem.getDeliveryLocation(), ReCAPConstants.REQUESTED_ITEM_DEACCESSIONED, itemEntity));
                        } else {
                            ItemStatusEntity itemStatusEntity = itemEntity.getItemStatusEntity();
                            if (ReCAPConstants.NOT_AVAILABLE.equals(itemStatusEntity.getStatusCode())) {
                                String gfaItemStatus = callGfaItemStatus(itemBarcode);
                                if (StringUtils.isNotBlank(gfaItemStatus) && ReCAPConstants.getGFAStatusNotAvailableList().contains(gfaItemStatus) && !ReCAPConstants.GFA_STATUS_NOT_ON_FILE.equalsIgnoreCase(gfaItemStatus)) {
                                    barcodeAndStopCodeMap.put(itemBarcode.trim(), deAccessionItem.getDeliveryLocation());
                                } else {
                                    deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deAccessionItem.getDeliveryLocation(), MessageFormat.format(ReCAPConstants.GFA_ITEM_STATUS_MISMATCH, gfaAssistanceEmailTo), itemEntity));
                                }
                            } else {
                                barcodeAndStopCodeMap.put(itemBarcode.trim(), deAccessionItem.getDeliveryLocation());
                            }
                        }
                    } else {
                        deAccessionDBResponseEntities.add(prepareFailureResponse(itemBarcode, deAccessionItem.getDeliveryLocation(), ReCAPConstants.ITEM_BARCDE_DOESNOT_EXIST, null));
                    }
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
                    gfaService.gfaPermanentWithdrawlDirect(gfaPwdRequest);
                } else if (ReCAPConstants.SUCCESS.equalsIgnoreCase(deAccessionDBResponseEntity.getStatus()) && ReCAPConstants.NOT_AVAILABLE.equalsIgnoreCase(deAccessionDBResponseEntity.getItemStatus())) {
                    GFAPwiRequest gfaPwiRequest = new GFAPwiRequest();
                    GFAPwiDsItemRequest gfaPwiDsItemRequest = new GFAPwiDsItemRequest();
                    GFAPwiTtItemRequest gfaPwiTtItemRequest = new GFAPwiTtItemRequest();
                    gfaPwiTtItemRequest.setCustomerCode(deAccessionDBResponseEntity.getCustomerCode());
                    gfaPwiTtItemRequest.setItemBarcode(deAccessionDBResponseEntity.getBarcode());
                    gfaPwiDsItemRequest.setTtitem(Arrays.asList(gfaPwiTtItemRequest));
                    gfaPwiRequest.setDsitem(gfaPwiDsItemRequest);
                    gfaService.gfaPermanentWithdrawlInDirect(gfaPwiRequest);
                }
            }
        }
    }

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
            RequestItemEntity savedRequestItemEntity = requestItemDetailsRepository.save(requestItemEntity);
            saveItemChangeLogEntity(savedRequestItemEntity.getRequestId(), username, ReCAPConstants.REQUEST_ITEM_CANCEL_DEACCESSION_ITEM, ReCAPConstants.REQUEST_ITEM_CANCELED_FOR_DEACCESSION + savedRequestItemEntity.getItemId());
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

    private int getHoldQueueLength(ItemInformationResponse itemInformationResponse) {
        int iholdQueue = 0;
        if (StringUtils.isNotBlank(itemInformationResponse.getHoldQueueLength())) {
            iholdQueue = Integer.parseInt(itemInformationResponse.getHoldQueueLength());
        }
        return iholdQueue;
    }

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
                    updateBibliographicWithLastUpdatedDate(itemId, username,currentDate);
                    deAccessionDBResponseEntity = prepareSuccessResponse(barcode, deliveryLocation, itemEntity, holdingsIds, bibliographicIds);
                    deAccessionDBResponseEntities.add(deAccessionDBResponseEntity);
                } catch (Exception ex) {
                    deAccessionDBResponseEntity = prepareFailureResponse(barcode, deliveryLocation, "Exception" + ex, null);
                    deAccessionDBResponseEntities.add(deAccessionDBResponseEntity);
                }
            }
        } catch (Exception ex) {
            logger.error(ReCAPConstants.LOG_ERROR,ex);
        }
    }

    public List<ReportEntity> processAndSave(List<DeAccessionDBResponseEntity> deAccessionDBResponseEntities) {
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

    public void deAccessionItemsInSolr(List<Integer> bibIds, List<Integer> holdingsIds, List<Integer> itemIds) {
        try {
            if (CollectionUtils.isNotEmpty(bibIds) || CollectionUtils.isNotEmpty(holdingsIds) || CollectionUtils.isNotEmpty(itemIds)) {
                String deAccessionSolrClientUrl = serverProtocol + scsbSolrClientUrl + ReCAPConstants.DEACCESSION_IN_SOLR_URL;
                DeAccessionSolrRequest deAccessionSolrRequest = new DeAccessionSolrRequest();
                deAccessionSolrRequest.setBibIds(bibIds);
                deAccessionSolrRequest.setHoldingsIds(holdingsIds);
                deAccessionSolrRequest.setItemIds(itemIds);

                RestTemplate restTemplate = new RestTemplate();
                HttpEntity<DeAccessionSolrRequest> requestEntity = new HttpEntity(deAccessionSolrRequest, getHttpHeaders());
                ResponseEntity<String> responseEntity = restTemplate.exchange(deAccessionSolrClientUrl, HttpMethod.POST, requestEntity, String.class);
                logger.info(responseEntity.getBody());
            }
        } catch (Exception e) {
            logger.error("Exception : ", e);
        }
    }

    public void updateBibliographicWithLastUpdatedDate(Integer itemId,String userName,Date lastUpdatedDate){
        ItemEntity itemEntity = itemDetailsRepository.findByItemId(itemId);
        List<BibliographicEntity> bibliographicEntityList = itemEntity.getBibliographicEntities();
        List<Integer> bibliographicIdList = new ArrayList<>();
        for(BibliographicEntity bibliographicEntity : bibliographicEntityList){
            bibliographicIdList.add(bibliographicEntity.getBibliographicId());
        }
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(ReCAPConstants.API_KEY, ReCAPConstants.RECAP);
        return headers;
    }
}
