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
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        ItemCheckoutResponse itemCheckoutResponse = (ItemCheckoutResponse) jsipConectorFactory.getJSIPConnector(callInstitition).checkOutItem(itembarcode, itemRequestInformation.getPatronBarcode());
        return itemCheckoutResponse;
    }

    @RequestMapping(value = "/checkinItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem checkinItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        ItemCheckinResponse itemCheckinResponse = (ItemCheckinResponse) jsipConectorFactory.getJSIPConnector(callInstitition).checkInItem(itembarcode, itemRequestInformation.getPatronBarcode());
        return itemCheckinResponse;
    }

    @RequestMapping(value = "/holdItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem holdItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        ItemHoldResponse itemHoldResponse = (ItemHoldResponse) jsipConectorFactory.getJSIPConnector(callInstitition).placeHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation(),
                itemRequestInformation.getTrackingId(),
                itemRequestInformation.getTitle(),
                itemRequestInformation.getAuthor(),
                itemRequestInformation.getCallNumber());
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
        ItemHoldResponse itemHoldResponse = (ItemHoldResponse) jsipConectorFactory.getJSIPConnector(callInstitition).cancelHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation(),
                itemRequestInformation.getTrackingId());
        return itemHoldResponse;
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
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        String source = itemRequestInformation.getSource();

        ItemInformationResponse itemInformationResponse = (ItemInformationResponse) jsipConectorFactory.getJSIPConnector(callInstitition).lookupItem(itembarcode, source);
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
