package org.recap.controller;

import com.pkrete.jsip2.messages.responses.SIP2CheckinResponse;
import com.pkrete.jsip2.messages.responses.SIP2CheckoutResponse;
import com.pkrete.jsip2.messages.responses.SIP2HoldResponse;

import com.pkrete.jsip2.messages.responses.SIP2ItemInformationResponse;
import com.pkrete.jsip2.messages.response.SIP2CreateBibResponse;
import org.recap.ils.model.*;
import org.recap.model.ItemRequestInformation;
import org.recap.model.ItemResponseInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.recap.ils.JSIPConnectorFactory;

/**
 * Created by sudhishk on 16/11/16.
 */
@RestController
@RequestMapping("/requestItem")
public class RequestItemController {

    @Autowired
    JSIPConnectorFactory jsipConectorFactory;

    @RequestMapping(value = "/checkoutItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem checkoutItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemResponseInformation itemResponseInformation = new ItemResponseInformation();
        ItemCheckoutResponse itemCheckoutResponse= new ItemCheckoutResponse();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2CheckoutResponse sip2CheckoutResponse = (SIP2CheckoutResponse) jsipConectorFactory.getJSIPConnector(callInstitition).checkOutItem(itembarcode, itemRequestInformation.getPatronBarcode());

        itemCheckoutResponse.setItemBarcode(sip2CheckoutResponse.getItemIdentifier());
        itemCheckoutResponse.setScreenMessage(sip2CheckoutResponse.getScreenMessage().get(0));
        itemCheckoutResponse.setSuccess(sip2CheckoutResponse.isOk());
        itemCheckoutResponse.setTitleIdentifier(sip2CheckoutResponse.getTitleIdentifier());
        itemCheckoutResponse.setDesensitize(sip2CheckoutResponse.isDesensitizeSupported());
        itemCheckoutResponse.setRenewal(sip2CheckoutResponse.isRenewalOk());
        itemCheckoutResponse.setMagneticMedia(sip2CheckoutResponse.isMagneticMedia());
        itemCheckoutResponse.setDueDate(sip2CheckoutResponse.getDueDate());
        itemCheckoutResponse.setTransactionDate(sip2CheckoutResponse.getTransactionDate());
        itemCheckoutResponse.setInstitutionID(sip2CheckoutResponse.getInstitutionId());
        itemCheckoutResponse.setItemOwningInstitution(sip2CheckoutResponse.getInstitutionId());
        itemCheckoutResponse.setPatronIdentifier(sip2CheckoutResponse.getPatronIdentifier());

        itemCheckoutResponse.setMediaType((sip2CheckoutResponse.getMediaType() != null)? sip2CheckoutResponse.getMediaType().name():"");
        itemCheckoutResponse.setBibId(sip2CheckoutResponse.getBibId());
        return itemCheckoutResponse;
    }

    @RequestMapping(value = "/checkinItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem checkinItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemCheckinResponse itemCheckinResponse = new ItemCheckinResponse();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2CheckinResponse sip2CheckinResponse = (SIP2CheckinResponse) jsipConectorFactory.getJSIPConnector(itemRequestInformation.getRequestingInstitution()).checkInItem(itembarcode, itemRequestInformation.getPatronBarcode());
        itemCheckinResponse.setItemBarcode(sip2CheckinResponse.getItemIdentifier());
        itemCheckinResponse.setScreenMessage(sip2CheckinResponse.getScreenMessage().get(0));
        itemCheckinResponse.setSuccess(sip2CheckinResponse.isOk());
        itemCheckinResponse.setTitleIdentifier(sip2CheckinResponse.getTitleIdentifier());
        itemCheckinResponse.setDueDate(sip2CheckinResponse.getDueDate());
        itemCheckinResponse.setResensitize(sip2CheckinResponse.isResensitize());
        itemCheckinResponse.setAlert(sip2CheckinResponse.isAlert());
        itemCheckinResponse.setMagneticMedia(sip2CheckinResponse.isMagneticMedia());
        itemCheckinResponse.setTransactionDate(sip2CheckinResponse.getTransactionDate());
        itemCheckinResponse.setInstitutionID(sip2CheckinResponse.getInstitutionId());
        itemCheckinResponse.setItemOwningInstitution(sip2CheckinResponse.getInstitutionId());
        itemCheckinResponse.setPatronIdentifier(sip2CheckinResponse.getPatronIdentifier());
        itemCheckinResponse.setMediaType((sip2CheckinResponse.getMediaType() != null)? sip2CheckinResponse.getMediaType().name():"");
        itemCheckinResponse.setBibId(sip2CheckinResponse.getBibId());
        itemCheckinResponse.setPermanentLocation(sip2CheckinResponse.getPermanentLocation());
        itemCheckinResponse.setCollectionCode(sip2CheckinResponse.getCollectionCode());
        itemCheckinResponse.setSortBin(sip2CheckinResponse.getSortBin());
        itemCheckinResponse.setCallNumber(sip2CheckinResponse.getCallNumber());
        itemCheckinResponse.setDestinationLocation(sip2CheckinResponse.getDestinationLocation());
        itemCheckinResponse.setAlertType((sip2CheckinResponse.getAlertType()!=null)? sip2CheckinResponse.getAlertType().name():"");
        itemCheckinResponse.setHoldPatronId(sip2CheckinResponse.getHoldPatronId());
        itemCheckinResponse.setHoldPatronName(sip2CheckinResponse.getHoldPatronName());

        return itemCheckinResponse;
    }

    @RequestMapping(value = "/holdItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem holdItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2HoldResponse sip2SIP2HoldResponse = (SIP2HoldResponse) jsipConectorFactory.getJSIPConnector(callInstitition).placeHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation());

        itemHoldResponse.setItemBarcode(sip2SIP2HoldResponse.getItemIdentifier());
        itemHoldResponse.setScreenMessage(sip2SIP2HoldResponse.getScreenMessage().get(0));
        itemHoldResponse.setSuccess(sip2SIP2HoldResponse.isOk());
        itemHoldResponse.setTitleIdentifier(sip2SIP2HoldResponse.getTitleIdentifier());
        itemHoldResponse.setExpirationDate(sip2SIP2HoldResponse.getExpirationDate());
        itemHoldResponse.setTransactionDate(sip2SIP2HoldResponse.getTransactionDate());
        itemHoldResponse.setInstitutionID(sip2SIP2HoldResponse.getInstitutionId());
        itemHoldResponse.setItemOwningInstitution(itemRequestInformation.getItemOwningInstitution());
        itemHoldResponse.setPatronIdentifier(sip2SIP2HoldResponse.getPatronIdentifier());
        itemHoldResponse.setBibId(sip2SIP2HoldResponse.getBibId());
        itemHoldResponse.setQueuePosition(sip2SIP2HoldResponse.getQueuePosition());
        itemHoldResponse.setLCCN(sip2SIP2HoldResponse.getLccn());
        itemHoldResponse.setISBN(sip2SIP2HoldResponse.getIsbn());
        itemHoldResponse.setAvailable(sip2SIP2HoldResponse.isAvailable());


        return itemHoldResponse;
    }

    @RequestMapping(value = "/cancelHoldItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem cancelHoldItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemHoldResponse itemHoldCancelResponse = new ItemHoldResponse();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2HoldResponse sip2SIP2HoldResponse = (SIP2HoldResponse) jsipConectorFactory.getJSIPConnector(itemRequestInformation.getRequestingInstitution()).cancelHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation());

        itemHoldCancelResponse.setItemBarcode(sip2SIP2HoldResponse.getItemIdentifier());
        itemHoldCancelResponse.setScreenMessage(sip2SIP2HoldResponse.getScreenMessage().get(0));
        itemHoldCancelResponse.setSuccess(sip2SIP2HoldResponse.isOk());
        itemHoldCancelResponse.setTitleIdentifier(sip2SIP2HoldResponse.getTitleIdentifier());
        itemHoldCancelResponse.setTransactionDate(sip2SIP2HoldResponse.getTransactionDate());
        itemHoldCancelResponse.setExpirationDate(sip2SIP2HoldResponse.getExpirationDate());
        itemHoldCancelResponse.setInstitutionID(sip2SIP2HoldResponse.getInstitutionId());
        itemHoldCancelResponse.setItemOwningInstitution(itemRequestInformation.getItemOwningInstitution());
        itemHoldCancelResponse.setPatronIdentifier(sip2SIP2HoldResponse.getPatronIdentifier());
        itemHoldCancelResponse.setBibId(sip2SIP2HoldResponse.getBibId());
        itemHoldCancelResponse.setQueuePosition(sip2SIP2HoldResponse.getQueuePosition());
        itemHoldCancelResponse.setLCCN(sip2SIP2HoldResponse.getLccn());
        itemHoldCancelResponse.setISBN(sip2SIP2HoldResponse.getIsbn());
        itemHoldCancelResponse.setAvailable(sip2SIP2HoldResponse.isAvailable());

        return itemHoldCancelResponse;
    }

    @RequestMapping(value = "/createBib", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem createBibliogrphicItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemCreateBibResponse itemCreateBibResponse = new ItemCreateBibResponse();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2CreateBibResponse sip2CreateBibResponse = (SIP2CreateBibResponse) jsipConectorFactory.getJSIPConnector(callInstitition).createBib(itembarcode, itemRequestInformation.getPatronBarcode(), itemRequestInformation.getRequestingInstitution(), itemRequestInformation.getTitleIdentifier());

        itemCreateBibResponse.setItemBarcode(sip2CreateBibResponse.getItemIdentifier());
        itemCreateBibResponse.setScreenMessage(sip2CreateBibResponse.getScreenMessage().get(0));
        itemCreateBibResponse.setSuccess(sip2CreateBibResponse.isOk());
        itemCreateBibResponse.setBibId(sip2CreateBibResponse.getBibId());
        itemCreateBibResponse.setItemId(sip2CreateBibResponse.getItemIdentifier());

        return itemCreateBibResponse;
    }

    @RequestMapping(value = "/itemInformation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem itemInformation(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();

        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);

        SIP2ItemInformationResponse sip2ItemInformationResponse = (SIP2ItemInformationResponse) jsipConectorFactory.getJSIPConnector(callInstitition).lookupItem(itembarcode);

        itemInformationResponse.setItemBarcode(sip2ItemInformationResponse.getItemIdentifier());
        itemInformationResponse.setScreenMessage(sip2ItemInformationResponse.getScreenMessage().get(0));
        itemInformationResponse.setSuccess(sip2ItemInformationResponse.isOk());
        itemInformationResponse.setTitleIdentifier(sip2ItemInformationResponse.getTitleIdentifier());

        itemInformationResponse.setDueDate(sip2ItemInformationResponse.getDueDate());
        itemInformationResponse.setRecallDate(sip2ItemInformationResponse.getRecallDate());
        itemInformationResponse.setHoldPickupDate(sip2ItemInformationResponse.getHoldPickupDate());
        itemInformationResponse.setTransactionDate(sip2ItemInformationResponse.getTransactionDate());
        itemInformationResponse.setExpirationDate(sip2ItemInformationResponse.getExpirationDate());

        itemInformationResponse.setCirculationStatus(sip2ItemInformationResponse.getCirculationStatus().name());
        itemInformationResponse.setCurrentLocation(sip2ItemInformationResponse.getCurrentLocation());
        itemInformationResponse.setPermanentLocation(sip2ItemInformationResponse.getPermanentLocation());
        itemInformationResponse.setFeeType(sip2ItemInformationResponse.getFeeType().name());
        itemInformationResponse.setHoldQueueLength(sip2ItemInformationResponse.getHoldQueueLength());
        itemInformationResponse.setOwner(sip2ItemInformationResponse.getOwner());
        itemInformationResponse.setSecurityMarker(sip2ItemInformationResponse.getSecurityMarker().name());
        itemInformationResponse.setCurrencyType((sip2ItemInformationResponse.getCurrencyType() != null) ? sip2ItemInformationResponse.getCurrencyType().name() : "");

        return itemInformationResponse;
    }

    @RequestMapping(value = "/recallItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem recallItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemRecallResponse itemRecallResponse = new ItemRecallResponse();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2HoldResponse sip2SIP2HoldResponse = (SIP2HoldResponse) jsipConectorFactory.getJSIPConnector(callInstitition).recallItem(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation());

        itemRecallResponse.setItemBarcode(sip2SIP2HoldResponse.getItemIdentifier());
        itemRecallResponse.setScreenMessage(sip2SIP2HoldResponse.getScreenMessage().get(0));
        itemRecallResponse.setSuccess(sip2SIP2HoldResponse.isOk());
        itemRecallResponse.setTitleIdentifier(sip2SIP2HoldResponse.getTitleIdentifier());
        itemRecallResponse.setTransactionDate(sip2SIP2HoldResponse.getDueDate());
        itemRecallResponse.setExpirationDate(sip2SIP2HoldResponse.getExpirationDate());
        itemRecallResponse.setInstitutionID(sip2SIP2HoldResponse.getInstitutionId());
        itemRecallResponse.setPickupLocation(sip2SIP2HoldResponse.getPickupLocation());
        itemRecallResponse.setPatronIdentifier(sip2SIP2HoldResponse.getPatronIdentifier());
        itemRecallResponse.setISBN(sip2SIP2HoldResponse.getIsbn());
        itemRecallResponse.setLCCN(sip2SIP2HoldResponse.getLccn());


        return itemRecallResponse;
    }
}
