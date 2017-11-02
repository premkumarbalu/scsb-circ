package org.recap.controller;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by sudhishk on 31/01/17.
 */
@RestController
@RequestMapping("/cancelRequest")
public class CancelItemController {

    private static final Logger logger = LoggerFactory.getLogger(CancelItemController.class);

    @Autowired
    private RequestItemController requestItemController;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    private RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Autowired
    private ItemRequestService itemRequestService;

    /**
     * This is rest service  method, for cancel requested item.
     *
     * @param requestId the request id that already exist in SCSB database.
     * @return CancelRequestResponse custom java object, with information of success and failure.
     * @Exception
     *
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CancelRequestResponse cancelRequest(@RequestParam Integer requestId) {
        CancelRequestResponse cancelRequestResponse = new CancelRequestResponse();
        ItemHoldResponse itemCanceHoldResponse = null;
        try {
            RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByRequestId(requestId);
            if (requestItemEntity != null) {
                ItemEntity itemEntity = requestItemEntity.getItemEntity();

                ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
                itemRequestInformation.setItemBarcodes(Arrays.asList(itemEntity.getBarcode()));
                itemRequestInformation.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                itemRequestInformation.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                itemRequestInformation.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());
                itemRequestInformation.setPatronBarcode(requestItemEntity.getPatronId());
                itemRequestInformation.setDeliveryLocation(requestItemEntity.getStopCode());

                String requestStatus = requestItemEntity.getRequestStatusEntity().getRequestStatusCode();
                ItemInformationResponse itemInformationResponse = (ItemInformationResponse) requestItemController.itemInformation(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
                itemRequestInformation.setBibId(itemInformationResponse.getBibID());

                if (requestStatus.equalsIgnoreCase(ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED)) {
                    itemCanceHoldResponse = processCancelRequest(itemRequestInformation, itemInformationResponse, requestItemEntity);
                } else if (requestStatus.equalsIgnoreCase(ReCAPConstants.REQUEST_STATUS_RECALLED)) {
                    itemCanceHoldResponse = processRecall(itemRequestInformation, itemInformationResponse, requestItemEntity);
                } else if (requestStatus.equalsIgnoreCase(ReCAPConstants.REQUEST_STATUS_EDD)) {
                    itemCanceHoldResponse = processEDD(requestItemEntity);
                } else {
                    itemCanceHoldResponse = new ItemHoldResponse();
                    itemCanceHoldResponse.setSuccess(false);
                    itemCanceHoldResponse.setScreenMessage(ReCAPConstants.REQUEST_CANCELLATION_NOT_ACTIVE);
                }
            } else {
                itemCanceHoldResponse = new ItemHoldResponse();
                itemCanceHoldResponse.setSuccess(false);
                itemCanceHoldResponse.setScreenMessage(ReCAPConstants.REQUEST_CANCELLATION_DOES_NOT_EXIST);
            }
        } catch (Exception e) {
            itemCanceHoldResponse = new ItemHoldResponse();
            itemCanceHoldResponse.setSuccess(false);
            itemCanceHoldResponse.setScreenMessage(e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        } finally {
            if (itemCanceHoldResponse == null) {
                itemCanceHoldResponse = new ItemHoldResponse();
            }
            cancelRequestResponse.setSuccess(itemCanceHoldResponse.isSuccess());
            cancelRequestResponse.setScreenMessage(itemCanceHoldResponse.getScreenMessage());
        }
        return cancelRequestResponse;
    }

    private ItemHoldResponse processCancelRequest(ItemRequestInformation itemRequestInformation, ItemInformationResponse itemInformationResponse, RequestItemEntity requestItemEntity) {
        ItemHoldResponse itemCanceHoldResponse;
        if ((getHoldQueueLength(itemInformationResponse) > 0 && (itemInformationResponse.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_OTHER) || itemInformationResponse.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_IN_TRANSIT)))
                || (itemInformationResponse.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_ON_HOLDSHELF) || itemInformationResponse.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_IN_TRANSIT_NYPL))) {
            itemCanceHoldResponse = (ItemHoldResponse) requestItemController.cancelHoldItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
            if (itemCanceHoldResponse.isSuccess()) {
                if (!itemRequestInformation.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                    requestItemController.checkinItem(itemRequestInformation, itemRequestInformation.getItemOwningInstitution());
                }
                changeRetrievalToCancelStatus(requestItemEntity, itemCanceHoldResponse);
            } else {
                itemCanceHoldResponse.setSuccess(false);
                itemCanceHoldResponse.setScreenMessage(itemCanceHoldResponse.getScreenMessage());
            }
        } else {
            itemCanceHoldResponse = new ItemHoldResponse();
            changeRetrievalToCancelStatus(requestItemEntity,itemCanceHoldResponse);
        }
        return itemCanceHoldResponse;
    }


    private ItemHoldResponse processRecall(ItemRequestInformation itemRequestInformation, ItemInformationResponse itemInformationResponse, RequestItemEntity requestItemEntity) {
        ItemHoldResponse itemCanceHoldResponse;
        if (getHoldQueueLength(itemInformationResponse) > 0 || (itemInformationResponse.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_ON_HOLDSHELF) || itemInformationResponse.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_IN_TRANSIT_NYPL))) {
            itemRequestInformation.setBibId(itemInformationResponse.getBibID());
            itemCanceHoldResponse = (ItemHoldResponse) requestItemController.cancelHoldItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
            if (itemCanceHoldResponse.isSuccess()) {
                changeRecallToCancelStatus(requestItemEntity, itemCanceHoldResponse);
            } else {
                itemCanceHoldResponse.setSuccess(false);
                itemCanceHoldResponse.setScreenMessage(itemCanceHoldResponse.getScreenMessage());
            }
        } else {
            itemCanceHoldResponse = new ItemHoldResponse();
            changeRecallToCancelStatus(requestItemEntity, itemCanceHoldResponse);
        }
        return itemCanceHoldResponse;
    }

    private ItemHoldResponse processEDD(RequestItemEntity requestItemEntity) {
        ItemHoldResponse itemCanceHoldResponse = new ItemHoldResponse();
        RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_CANCELED);
        requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
        requestItemEntity.setLastUpdatedDate(new Date());
        requestItemEntity.setNotes(appendCancelMessageToNotes(requestItemEntity));
        RequestItemEntity savedRequestItemEntity = requestItemDetailsRepository.save(requestItemEntity);
        itemRequestService.saveItemChangeLogEntity(savedRequestItemEntity.getRequestId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_CANCEL_ITEM_AVAILABILITY_STATUS, ReCAPConstants.REQUEST_STATUS_CANCELED + savedRequestItemEntity.getItemId());
        itemCanceHoldResponse.setSuccess(true);
        itemCanceHoldResponse.setScreenMessage(ReCAPConstants.REQUEST_CANCELLATION_EDD_SUCCCESS);
        sendEmail(requestItemEntity.getItemEntity().getCustomerCode(), requestItemEntity.getItemEntity().getBarcode(), requestItemEntity.getPatronId());
        return itemCanceHoldResponse;
    }

    private int getHoldQueueLength(ItemInformationResponse itemInformationResponse) {
        int iholdQueue = 0;
        if (itemInformationResponse.getHoldQueueLength().trim().length() > 0) {
            iholdQueue = Integer.parseInt(itemInformationResponse.getHoldQueueLength());
        }
        return iholdQueue;
    }

    private void sendEmail(String customerCode, String itemBarcode, String patronBarcode) {
        itemRequestService.getEmailService().sendEmail(customerCode, itemBarcode, ReCAPConstants.REQUEST_CANCELLED_NO_REFILED, patronBarcode, ReCAPConstants.GFA,ReCAPConstants.REQUEST_CANCELLED_SUBJECT);
    }

    private void changeRetrievalToCancelStatus(RequestItemEntity requestItemEntity, ItemHoldResponse itemCanceHoldResponse) {
        RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_CANCELED);
        requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
        requestItemEntity.setLastUpdatedDate(new Date());
        requestItemEntity.setNotes(appendCancelMessageToNotes(requestItemEntity));
        RequestItemEntity savedRequestItemEntity = requestItemDetailsRepository.save(requestItemEntity);
        itemRequestService.saveItemChangeLogEntity(savedRequestItemEntity.getRequestId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_CANCEL_ITEM_AVAILABILITY_STATUS, ReCAPConstants.REQUEST_STATUS_CANCELED + savedRequestItemEntity.getItemId());
        itemCanceHoldResponse.setSuccess(true);
        itemCanceHoldResponse.setScreenMessage(ReCAPConstants.REQUEST_CANCELLATION_SUCCCESS);
        logger.info("Send Mail");
        sendEmail(requestItemEntity.getItemEntity().getCustomerCode(), requestItemEntity.getItemEntity().getBarcode(), requestItemEntity.getPatronId());
        logger.info("Send Mail Done");
    }

    private void changeRecallToCancelStatus(RequestItemEntity requestItemEntity, ItemHoldResponse itemCanceHoldResponse) {
        RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_CANCELED);
        requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
        requestItemEntity.setLastUpdatedDate(new Date());
        requestItemEntity.setNotes(appendCancelMessageToNotes(requestItemEntity));
        RequestItemEntity savedRequestItemEntity = requestItemDetailsRepository.save(requestItemEntity);
        itemRequestService.saveItemChangeLogEntity(savedRequestItemEntity.getRequestId(), ReCAPConstants.GUEST_USER, ReCAPConstants.REQUEST_ITEM_CANCEL_ITEM_AVAILABILITY_STATUS, ReCAPConstants.REQUEST_STATUS_CANCELED + savedRequestItemEntity.getItemId());
        itemCanceHoldResponse.setSuccess(true);
        itemCanceHoldResponse.setScreenMessage(ReCAPConstants.RECALL_CANCELLATION_SUCCCESS);
    }

    private String appendCancelMessageToNotes(RequestItemEntity requestItemEntity) {
        DateFormat cancelRequestDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return requestItemEntity.getNotes() + "\nCancel requested ["+cancelRequestDateFormat.format(new Date())+"]";
    }
}
