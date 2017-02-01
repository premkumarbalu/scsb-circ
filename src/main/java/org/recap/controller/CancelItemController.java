package org.recap.controller;

import io.swagger.annotations.ApiParam;
import org.recap.ReCAPConstants;
import org.recap.ils.model.response.ItemCheckinResponse;
import org.recap.ils.model.response.ItemHoldResponse;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.*;
import org.recap.repository.RequestItemDetailsRepository;
import org.recap.repository.RequestItemStatusDetailsRepository;
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RequestItemController requestItemController;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @RequestMapping(value = "/cancel", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CancelRequestResponse cancelRequest(@ApiParam(value = "Parameters for cancelling", required = true, name = "requestId") @RequestParam Integer requestId) {
        CancelRequestResponse cancelRequestResponse = new CancelRequestResponse();
        ItemHoldResponse itemCanceHoldResponse = null;
        ItemCheckinResponse itemCheckinResponse = null;
        boolean bSuccess = false;
        String screenMessage = "";

        try {
            RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByRequestId(requestId);
            ItemEntity itemEntity = requestItemEntity.getItemEntity();

            ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
            itemRequestInformation.setItemBarcodes(Arrays.asList(itemEntity.getBarcode()));
            itemRequestInformation.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
            itemRequestInformation.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());

            itemRequestInformation.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());
            itemRequestInformation.setRequestingInstitution(requestItemEntity.getPatronEntity().getInstitutionIdentifier());
            itemRequestInformation.setDeliveryLocation(requestItemEntity.getStopCode());

            ItemInformationResponse itemInformationResponse = (ItemInformationResponse) requestItemController.itemInformation(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
            int iholdQueue = 0;
            if (itemInformationResponse.getHoldQueueLength().trim().length() > 0) {
                iholdQueue = Integer.parseInt(itemInformationResponse.getHoldQueueLength());
            }
            if (iholdQueue > 0 && (itemInformationResponse.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_OTHER) || itemInformationResponse.getCirculationStatus().equalsIgnoreCase(ReCAPConstants.CIRCULATION_STATUS_IN_TRANSIT))) {
                itemCanceHoldResponse = (ItemHoldResponse) requestItemController.holdItem(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
                if (itemCanceHoldResponse.isSuccess()) {
                    if (!itemRequestInformation.getItemOwningInstitution().equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
                        itemCheckinResponse = (ItemCheckinResponse) requestItemController.checkinItem(itemRequestInformation, itemRequestInformation.getItemOwningInstitution());
                    }
                    RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_CANCELED);
                    requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
                    if(requestItemEntity.getRequestTypeEntity().getRequestTypeCode().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL)) {
                        requestItemEntity.getItemEntity().setItemAvailabilityStatusId(1);
                    }
                    bSuccess = true;
                    screenMessage = "Request cancellation succcessfully processed";
                } else {
                    bSuccess = false;
                    screenMessage = "Cancel hold request failed from ILS";
                }
            } else {
                bSuccess = false;
                screenMessage = "Hold queue length in ILS is zero or empty";
            }
            cancelRequestResponse.setSuccess(bSuccess);
            cancelRequestResponse.setScreenMessage(screenMessage);
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
        return cancelRequestResponse;
    }
}
