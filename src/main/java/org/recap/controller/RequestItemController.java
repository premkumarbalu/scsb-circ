package org.recap.controller;

import com.pkrete.jsip2.messages.responses.SIP2CheckinResponse;
import com.pkrete.jsip2.messages.responses.SIP2CheckoutResponse;
import com.pkrete.jsip2.messages.responses.SIP2HoldResponse;

import com.pkrete.jsip2.messages.responses.SIP2ItemInformationResponse;
import com.pkrete.jsip2.messages.response.SIP2CreateBibResponse;
import org.recap.ils.model.ItemInformationRequest;
import org.recap.ils.model.ItemInformationResponse;
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
    public ItemResponseInformation checkoutItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemResponseInformation itemResponseInformation = new ItemResponseInformation();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2CheckoutResponse sip2CheckoutResponse = (SIP2CheckoutResponse) jsipConectorFactory.getJSIPConnector(callInstitition).checkOutItem(itembarcode, itemRequestInformation.getPatronBarcode());
        itemResponseInformation.setItemBarcode(sip2CheckoutResponse.getItemIdentifier());
        itemResponseInformation.setScreenMessage(sip2CheckoutResponse.getScreenMessage().get(0));
        itemResponseInformation.setSuccess(sip2CheckoutResponse.isOk());
        itemResponseInformation.setTitleIdentifier(sip2CheckoutResponse.getTitleIdentifier());
        itemResponseInformation.setDueDate(sip2CheckoutResponse.getDueDate());
        return itemResponseInformation;
    }

    @RequestMapping(value = "/checkinItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ItemResponseInformation checkinItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemResponseInformation itemResponseInformation = new ItemResponseInformation();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2CheckinResponse sip2CheckinResponse = (SIP2CheckinResponse) jsipConectorFactory.getJSIPConnector(itemRequestInformation.getRequestingInstitution()).checkInItem(itembarcode, itemRequestInformation.getPatronBarcode());
        itemResponseInformation.setItemBarcode(sip2CheckinResponse.getItemIdentifier());
        itemResponseInformation.setScreenMessage(sip2CheckinResponse.getScreenMessage().get(0));
        itemResponseInformation.setSuccess(sip2CheckinResponse.isOk());
        itemResponseInformation.setTitleIdentifier(sip2CheckinResponse.getTitleIdentifier());
        itemResponseInformation.setDueDate(sip2CheckinResponse.getDueDate());
        return itemResponseInformation;
    }

    @RequestMapping(value = "/holdItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ItemResponseInformation holdItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemResponseInformation itemResponseInformation = new ItemResponseInformation();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2HoldResponse sip2SIP2HoldResponse = (SIP2HoldResponse) jsipConectorFactory.getJSIPConnector(callInstitition).placeHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation());
        itemResponseInformation.setItemBarcode(sip2SIP2HoldResponse.getItemIdentifier());
        itemResponseInformation.setScreenMessage(sip2SIP2HoldResponse.getScreenMessage().get(0));
        itemResponseInformation.setSuccess(sip2SIP2HoldResponse.isOk());
        itemResponseInformation.setTitleIdentifier(sip2SIP2HoldResponse.getTitleIdentifier());
        itemResponseInformation.setDueDate(sip2SIP2HoldResponse.getDueDate());
        return itemResponseInformation;
    }

    @RequestMapping(value = "/cancelHoldItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ItemResponseInformation cancelHoldItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemResponseInformation itemResponseInformation = new ItemResponseInformation();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2HoldResponse sip2SIP2HoldResponse = (SIP2HoldResponse) jsipConectorFactory.getJSIPConnector(itemRequestInformation.getRequestingInstitution()).cancelHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation());
        itemResponseInformation.setItemBarcode(sip2SIP2HoldResponse.getItemIdentifier());
        itemResponseInformation.setScreenMessage(sip2SIP2HoldResponse.getScreenMessage().get(0));
        itemResponseInformation.setSuccess(sip2SIP2HoldResponse.isOk());
        itemResponseInformation.setTitleIdentifier(sip2SIP2HoldResponse.getTitleIdentifier());
        itemResponseInformation.setDueDate(sip2SIP2HoldResponse.getDueDate());
        return itemResponseInformation;
    }

    @RequestMapping(value = "/createBib", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ItemResponseInformation createBibliogrphicItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemResponseInformation itemResponseInformation = new ItemResponseInformation();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2CreateBibResponse sip2CreateBibResponse = (SIP2CreateBibResponse) jsipConectorFactory.getJSIPConnector(callInstitition).createBib(itembarcode, itemRequestInformation.getPatronBarcode(), itemRequestInformation.getRequestingInstitution(), itemRequestInformation.getTitleIdentifier());
        itemResponseInformation.setItemBarcode(sip2CreateBibResponse.getItemIdentifier());
        itemResponseInformation.setScreenMessage(sip2CreateBibResponse.getScreenMessage().get(0));
        itemResponseInformation.setSuccess(sip2CreateBibResponse.isOk());
        itemResponseInformation.setTitleIdentifier(sip2CreateBibResponse.getTitleIdentifier());
        itemRequestInformation.setBibId(sip2CreateBibResponse.getBibId());
        return itemResponseInformation;
    }

    @RequestMapping(value = "/itemInformation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ItemInformationResponse itemInformation(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
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
    public ItemResponseInformation recallItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemResponseInformation itemResponseInformation = new ItemResponseInformation();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        SIP2HoldResponse sip2SIP2HoldResponse = (SIP2HoldResponse) jsipConectorFactory.getJSIPConnector(callInstitition).recallItem(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation());
        itemResponseInformation.setItemBarcode(sip2SIP2HoldResponse.getItemIdentifier());
        itemResponseInformation.setScreenMessage(sip2SIP2HoldResponse.getScreenMessage().get(0));
        itemResponseInformation.setSuccess(sip2SIP2HoldResponse.isOk());
        itemResponseInformation.setTitleIdentifier(sip2SIP2HoldResponse.getTitleIdentifier());
        itemResponseInformation.setDueDate(sip2SIP2HoldResponse.getDueDate());
        return itemResponseInformation;
    }
}
