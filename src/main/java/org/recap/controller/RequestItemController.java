package org.recap.controller;

import com.pkrete.jsip2.messages.response.SIP2RecallResponse;
import com.pkrete.jsip2.messages.responses.SIP2CheckinResponse;
import com.pkrete.jsip2.messages.responses.SIP2CheckoutResponse;
import com.pkrete.jsip2.messages.responses.SIP2HoldResponse;

import com.pkrete.jsip2.messages.responses.SIP2ItemInformationResponse;
import com.pkrete.jsip2.messages.response.SIP2CreateBibResponse;
import org.recap.ils.model.*;
import org.recap.model.ItemRequestInformation;
import org.recap.model.ItemResponseInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.recap.ils.JSIPConnectorFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by sudhishk on 16/11/16.
 */
@RestController
@RequestMapping("/requestItem")
public class RequestItemController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    JSIPConnectorFactory jsipConectorFactory;

    @RequestMapping(value = "/checkoutItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem checkoutItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemResponseInformation itemResponseInformation = new ItemResponseInformation();
        ItemCheckoutResponse itemCheckoutResponse = new ItemCheckoutResponse();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2CheckoutResponse sip2CheckoutResponse = (SIP2CheckoutResponse) jsipConectorFactory.getJSIPConnector(callInstitition).checkOutItem(itembarcode, itemRequestInformation.getPatronBarcode());

        itemCheckoutResponse.setItemBarcode(sip2CheckoutResponse.getItemIdentifier());
        itemCheckoutResponse.setScreenMessage((sip2CheckoutResponse.getScreenMessage().size() > 0) ? sip2CheckoutResponse.getScreenMessage().get(0) : "");
        itemCheckoutResponse.setSuccess(sip2CheckoutResponse.isOk());
        itemCheckoutResponse.setTitleIdentifier(sip2CheckoutResponse.getTitleIdentifier());
        itemCheckoutResponse.setDesensitize(sip2CheckoutResponse.isDesensitizeSupported());
        itemCheckoutResponse.setRenewal(sip2CheckoutResponse.isRenewalOk());
        itemCheckoutResponse.setMagneticMedia(sip2CheckoutResponse.isMagneticMedia());
        itemCheckoutResponse.setDueDate(formatFromSipDate(sip2CheckoutResponse.getDueDate()));
        itemCheckoutResponse.setTransactionDate(formatFromSipDate(sip2CheckoutResponse.getTransactionDate()));
        itemCheckoutResponse.setInstitutionID(sip2CheckoutResponse.getInstitutionId());
        itemCheckoutResponse.setItemOwningInstitution(sip2CheckoutResponse.getInstitutionId());
        itemCheckoutResponse.setPatronIdentifier(sip2CheckoutResponse.getPatronIdentifier());

        itemCheckoutResponse.setMediaType((sip2CheckoutResponse.getMediaType() != null) ? sip2CheckoutResponse.getMediaType().name() : "");
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
        itemCheckinResponse.setScreenMessage((sip2CheckinResponse.getScreenMessage().size() > 0) ? sip2CheckinResponse.getScreenMessage().get(0) : "");
        itemCheckinResponse.setSuccess(sip2CheckinResponse.isOk());
        itemCheckinResponse.setTitleIdentifier(sip2CheckinResponse.getTitleIdentifier());
        itemCheckinResponse.setDueDate(formatFromSipDate(sip2CheckinResponse.getDueDate()));
        itemCheckinResponse.setResensitize(sip2CheckinResponse.isResensitize());
        itemCheckinResponse.setAlert(sip2CheckinResponse.isAlert());
        itemCheckinResponse.setMagneticMedia(sip2CheckinResponse.isMagneticMedia());
        itemCheckinResponse.setTransactionDate(formatFromSipDate(sip2CheckinResponse.getTransactionDate()));
        itemCheckinResponse.setInstitutionID(sip2CheckinResponse.getInstitutionId());
        itemCheckinResponse.setItemOwningInstitution(sip2CheckinResponse.getInstitutionId());
        itemCheckinResponse.setPatronIdentifier(sip2CheckinResponse.getPatronIdentifier());
        itemCheckinResponse.setMediaType((sip2CheckinResponse.getMediaType() != null) ? sip2CheckinResponse.getMediaType().name() : "");
        itemCheckinResponse.setBibId(sip2CheckinResponse.getBibId());
        itemCheckinResponse.setPermanentLocation(sip2CheckinResponse.getPermanentLocation());
        itemCheckinResponse.setCollectionCode(sip2CheckinResponse.getCollectionCode());
        itemCheckinResponse.setSortBin(sip2CheckinResponse.getSortBin());
        itemCheckinResponse.setCallNumber(sip2CheckinResponse.getCallNumber());
        itemCheckinResponse.setDestinationLocation(sip2CheckinResponse.getDestinationLocation());
        itemCheckinResponse.setAlertType((sip2CheckinResponse.getAlertType() != null) ? sip2CheckinResponse.getAlertType().name() : "");
        itemCheckinResponse.setHoldPatronId(sip2CheckinResponse.getHoldPatronId());
        itemCheckinResponse.setHoldPatronName(sip2CheckinResponse.getHoldPatronName());

        return itemCheckinResponse;
    }

    @RequestMapping(value = "/holdItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem holdItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        try {


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
            itemHoldResponse.setScreenMessage((sip2SIP2HoldResponse.getScreenMessage().size() > 0) ? sip2SIP2HoldResponse.getScreenMessage().get(0) : "");
            itemHoldResponse.setSuccess(sip2SIP2HoldResponse.isOk());
            itemHoldResponse.setTitleIdentifier(sip2SIP2HoldResponse.getTitleIdentifier());
            itemHoldResponse.setExpirationDate(formatFromSipDate(sip2SIP2HoldResponse.getExpirationDate()));
            itemHoldResponse.setTransactionDate(formatFromSipDate(sip2SIP2HoldResponse.getTransactionDate()));
            itemHoldResponse.setInstitutionID(sip2SIP2HoldResponse.getInstitutionId());
            itemHoldResponse.setItemOwningInstitution(itemRequestInformation.getItemOwningInstitution());
            itemHoldResponse.setPatronIdentifier(sip2SIP2HoldResponse.getPatronIdentifier());
            itemHoldResponse.setBibId(sip2SIP2HoldResponse.getBibId());
            itemHoldResponse.setQueuePosition(sip2SIP2HoldResponse.getQueuePosition());
            itemHoldResponse.setLCCN(sip2SIP2HoldResponse.getLccn());
            itemHoldResponse.setISBN(sip2SIP2HoldResponse.getIsbn());
            itemHoldResponse.setAvailable(sip2SIP2HoldResponse.isAvailable());
        } catch (Exception e) {
            logger.info("Exception", e);
            itemHoldResponse.setScreenMessage("ILS returned a invalid response");
        }

        return itemHoldResponse;
    }

    @RequestMapping(value = "/cancelHoldItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem cancelHoldItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemHoldResponse itemHoldCancelResponse = new ItemHoldResponse();
        String itembarcode = "";
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }

        if (itemRequestInformation.getItemBarcodes().size() > 0) {
            itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        }
        SIP2HoldResponse sip2SIP2HoldResponse = (SIP2HoldResponse) jsipConectorFactory.getJSIPConnector(itemRequestInformation.getRequestingInstitution()).cancelHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation());

        itemHoldCancelResponse.setItemBarcode(sip2SIP2HoldResponse.getItemIdentifier());
        itemHoldCancelResponse.setScreenMessage((sip2SIP2HoldResponse.getScreenMessage().size() > 0) ? sip2SIP2HoldResponse.getScreenMessage().get(0) : "");
        itemHoldCancelResponse.setSuccess(sip2SIP2HoldResponse.isOk());
        itemHoldCancelResponse.setTitleIdentifier(sip2SIP2HoldResponse.getTitleIdentifier());
        itemHoldCancelResponse.setTransactionDate(formatFromSipDate(sip2SIP2HoldResponse.getTransactionDate()));
        itemHoldCancelResponse.setExpirationDate(formatFromSipDate(sip2SIP2HoldResponse.getExpirationDate()));
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
        itemCreateBibResponse.setScreenMessage((sip2CreateBibResponse.getScreenMessage().size() > 0) ? sip2CreateBibResponse.getScreenMessage().get(0) : "");
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

        itemInformationResponse = (ItemInformationResponse) jsipConectorFactory.getJSIPConnector(callInstitition).lookupItem(itembarcode);
        return itemInformationResponse;
    }

    @RequestMapping(value = "/recallItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem recallItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemRecallResponse itemRecallResponse = new ItemRecallResponse();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2RecallResponse sip2RecallResponse = (SIP2RecallResponse) jsipConectorFactory.getJSIPConnector(callInstitition).recallItem(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation());

        itemRecallResponse.setItemBarcode(sip2RecallResponse.getItemIdentifier());
        itemRecallResponse.setScreenMessage((sip2RecallResponse.getScreenMessage().size() > 0) ? sip2RecallResponse.getScreenMessage().get(0) : "");
        itemRecallResponse.setSuccess(sip2RecallResponse.isOk());
        itemRecallResponse.setTitleIdentifier(sip2RecallResponse.getTitleIdentifier());
        itemRecallResponse.setTransactionDate(formatFromSipDate(sip2RecallResponse.getDueDate()));
        itemRecallResponse.setExpirationDate(formatFromSipDate(sip2RecallResponse.getExpirationDate()));
        itemRecallResponse.setInstitutionID(sip2RecallResponse.getInstitutionId());
        itemRecallResponse.setPickupLocation(sip2RecallResponse.getPickupLocation());
        itemRecallResponse.setPatronIdentifier(sip2RecallResponse.getPatronIdentifier());
        return itemRecallResponse;
    }

    private String formatFromSipDate(String sipDate) {
        SimpleDateFormat sipFormat = new SimpleDateFormat("yyyyMMdd    HHmmss");
        SimpleDateFormat requiredFormat = new SimpleDateFormat("dd-MMM-YYYY HH:mm:ss");
        String reformattedStr = "";
        try {
            if(sipDate != null && sipDate.trim().length()>0) {
                reformattedStr = requiredFormat.format(sipFormat.parse(sipDate));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return reformattedStr;
    }

}
