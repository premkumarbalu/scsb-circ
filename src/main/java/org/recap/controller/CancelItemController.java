package org.recap.controller;

import io.swagger.annotations.ApiParam;
import org.recap.ReCAPConstants;
import org.recap.ils.model.response.ItemHoldResponse;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.*;
import org.recap.repository.RequestItemDetailsRepository;
import org.recap.repository.RequestItemStatusDetailsRepository;
import org.recap.request.ItemRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * Created by sudhishk on 31/01/17.
 */
@RestController
@RequestMapping("/cancelRequest")
public class CancelItemController {

    public static final String REQUEST_CANCELLATION_NOT_ACTIVE = "RequestId is not active status to be canceled";
    public static final String REQUEST_CANCELLATION_DOES_NOT_EXIST = "RequestId does not exist";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RequestItemController requestItemController;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    private RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Autowired
    private ItemRequestService itemRequestService;

    @RequestMapping(value = "/cancel", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CancelRequestResponse cancelRequest(@ApiParam(value = "Parameters for cancelling", required = true, name = "requestId") @RequestParam Integer requestId) {
        CancelRequestResponse cancelRequestResponse = new CancelRequestResponse();
        ItemHoldResponse itemCanceHoldResponse=null;
        try {
            RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByRequestId(requestId);
            if (requestItemEntity != null) {
                ItemEntity itemEntity = requestItemEntity.getItemEntity();

                ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
                itemRequestInformation.setItemBarcodes(Arrays.asList(itemEntity.getBarcode()));
                itemRequestInformation.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                itemRequestInformation.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                itemRequestInformation.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());
                itemRequestInformation.setPatronBarcode(requestItemEntity.getPatronEntity().getInstitutionIdentifier());
                itemRequestInformation.setDeliveryLocation(requestItemEntity.getStopCode());

                String requestStatus = requestItemEntity.getRequestStatusEntity().getRequestStatusCode();
                ItemInformationResponse itemInformationResponse = (ItemInformationResponse) requestItemController.itemInformation(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
                itemRequestInformation.setBibId(itemInformationResponse.getBibID());

                if (requestStatus.equalsIgnoreCase(ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED)) {
                    itemCanceHoldResponse = processCancelRequest(itemRequestInformation, itemInformationResponse, requestItemEntity);
                } else if (requestStatus.equalsIgnoreCase(ReCAPConstants.REQUEST_STATUS_RECALLED)) {
                    itemCanceHoldResponse = processRecall(itemRequestInformation, itemInformationResponse, requestItemEntity);
                } else if (requestStatus.equalsIgnoreCase(ReCAPConstants.REQUEST_STATUS_EDD)) {
                    itemCanceHoldResponse =processEDD(requestItemEntity);
                } else {
                    itemCanceHoldResponse = new ItemHoldResponse();
                    itemCanceHoldResponse.setSuccess(false);
                    itemCanceHoldResponse.setScreenMessage(REQUEST_CANCELLATION_NOT_ACTIVE);
                }
            } else {
                itemCanceHoldResponse = new ItemHoldResponse();
                itemCanceHoldResponse.setSuccess(false);
                itemCanceHoldResponse.setScreenMessage(REQUEST_CANCELLATION_DOES_NOT_EXIST);
            }
        } catch (Exception e) {
            itemCanceHoldResponse = new ItemHoldResponse();
            itemCanceHoldResponse.setSuccess(false);
            itemCanceHoldResponse.setScreenMessage(e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        } finally {
            if(itemCanceHoldResponse ==null){
                itemCanceHoldResponse = new ItemHoldResponse();
            }
            cancelRequestResponse.setSuccess(itemCanceHoldResponse.isSuccess());
            cancelRequestResponse.setScreenMessage(itemCanceHoldResponse.getScreenMessage());
        }
        return cancelRequestResponse;
    }

    private ItemHoldResponse processCancelRequest(ItemRequestInformation itemRequestInformation, ItemInformationResponse itemInformationResponse, RequestItemEntity requestItemEntity) {
        ItemHoldResponse itemCanceHoldResponse;
        if (getHoldQueueLength(itemInformationResponse) > 0 && (itemInformationResponse.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_OTHER) || itemInformationResponse.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_IN_TRANSIT))) {
            itemCanceHoldResponse = (ItemHoldResponse) requestItemController.cancelHoldItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
            if (itemCanceHoldResponse.isSuccess()) {
                if (!itemRequestInformation.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                    requestItemController.checkinItem(itemRequestInformation, itemRequestInformation.getItemOwningInstitution());
                }
                RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_CANCELED);
                requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                if (requestItemEntity.getRequestTypeEntity().getRequestTypeCode().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
                    requestItemEntity.getItemEntity().setItemAvailabilityStatusId(1);
                }
                RequestItemEntity savedRequestItemEntity = requestItemDetailsRepository.save(requestItemEntity);
                itemRequestService.saveItemChangeLogEntity(savedRequestItemEntity.getRequestId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_CANCEL_ITEM_AVAILABILITY_STATUS, ReCAPConstants.REQUEST_STATUS_CANCELED + savedRequestItemEntity.getItemId());
                itemRequestService.updateSolrIndex(savedRequestItemEntity.getItemEntity());
                itemCanceHoldResponse.setSuccess(true);
                itemCanceHoldResponse.setScreenMessage(ReCAPConstants.REQUEST_CANCELLATION_SUCCCESS);
            } else {
                itemCanceHoldResponse.setSuccess(false);
                itemCanceHoldResponse.setScreenMessage(itemCanceHoldResponse.getScreenMessage());
            }
        } else {
            itemCanceHoldResponse = new ItemHoldResponse();
            itemCanceHoldResponse.setSuccess(false);
            itemCanceHoldResponse.setScreenMessage(ReCAPConstants.REQUEST_CANCELLATION_NOT_ON_HOLD_IN_ILS);
        }
        return itemCanceHoldResponse;
    }

    private ItemHoldResponse processRecall(ItemRequestInformation itemRequestInformation, ItemInformationResponse itemInformationResponse, RequestItemEntity requestItemEntity) {
        ItemHoldResponse itemCanceHoldResponse;
        if (getHoldQueueLength(itemInformationResponse) > 0) {
            itemRequestInformation.setBibId(itemInformationResponse.getBibID());
            itemCanceHoldResponse = (ItemHoldResponse) requestItemController.cancelHoldItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
            if (itemCanceHoldResponse.isSuccess()) {
                RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_CANCELED);
                requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                RequestItemEntity savedRequestItemEntity = requestItemDetailsRepository.save(requestItemEntity);
                itemRequestService.saveItemChangeLogEntity(savedRequestItemEntity.getRequestId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_CANCEL_ITEM_AVAILABILITY_STATUS, ReCAPConstants.REQUEST_STATUS_CANCELED + savedRequestItemEntity.getItemId());
                itemCanceHoldResponse.setSuccess(true);
                itemCanceHoldResponse.setScreenMessage(ReCAPConstants.RECALL_CANCELLATION_SUCCCESS);
            } else {
                itemCanceHoldResponse.setSuccess(false);
                itemCanceHoldResponse.setScreenMessage(itemCanceHoldResponse.getScreenMessage());
            }
        } else {
            itemCanceHoldResponse = new ItemHoldResponse();
            itemCanceHoldResponse.setSuccess(false);
            itemCanceHoldResponse.setScreenMessage(ReCAPConstants.REQUEST_CANCELLATION_NOT_ON_HOLD_IN_ILS);
        }
        return itemCanceHoldResponse;
    }

    private ItemHoldResponse processEDD(RequestItemEntity requestItemEntity) {
        ItemHoldResponse itemCanceHoldResponse = new ItemHoldResponse();
        RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_CANCELED);
        requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
        RequestItemEntity savedRequestItemEntity = requestItemDetailsRepository.save(requestItemEntity);
        itemRequestService.saveItemChangeLogEntity(savedRequestItemEntity.getRequestId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_CANCEL_ITEM_AVAILABILITY_STATUS, ReCAPConstants.REQUEST_STATUS_CANCELED + savedRequestItemEntity.getItemId());
        itemCanceHoldResponse.setSuccess(true);
        itemCanceHoldResponse.setScreenMessage(ReCAPConstants.REQUEST_CANCELLATION_EDD_SUCCCESS);
        return itemCanceHoldResponse;
    }

    private int getHoldQueueLength(ItemInformationResponse itemInformationResponse) {
        int iholdQueue = 0;
        if (itemInformationResponse.getHoldQueueLength().trim().length() > 0) {
            iholdQueue = Integer.parseInt(itemInformationResponse.getHoldQueueLength());
        }
        return iholdQueue;
    }
}
