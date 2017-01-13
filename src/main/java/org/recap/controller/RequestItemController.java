package org.recap.controller;

import org.recap.ils.JSIPConnectorFactory;
import org.recap.ils.model.response.*;
import org.recap.model.ItemRequestInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
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
        ItemCheckoutResponse itemCheckoutResponse = new ItemCheckoutResponse();
        String itembarcode = "";
        try {
            if (callInstitition == null) {
                callInstitition = itemRequestInformation.getItemOwningInstitution();
            }
            if (itemRequestInformation.getItemBarcodes().size() > 0) {
                itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
                itemCheckoutResponse = (ItemCheckoutResponse) jsipConectorFactory.getJSIPConnector(callInstitition).checkOutItem(itembarcode, itemRequestInformation.getPatronBarcode());
            } else {
                itemCheckoutResponse.setSuccess(false);
                itemCheckoutResponse.setScreenMessage("Item Id not found");
            }
        } catch (Exception e) {
            itemCheckoutResponse.setSuccess(false);
            itemCheckoutResponse.setScreenMessage(e.getMessage());
        }

        return itemCheckoutResponse;
    }

    @RequestMapping(value = "/checkinItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem checkinItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemCheckinResponse itemCheckinResponse = null;
        String itembarcode = "";
        try {
            if (callInstitition == null) {
                callInstitition = itemRequestInformation.getItemOwningInstitution();
            }
            if (itemRequestInformation.getItemBarcodes().size() > 0) {
                itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
                itemCheckinResponse = (ItemCheckinResponse) jsipConectorFactory.getJSIPConnector(itemRequestInformation.getRequestingInstitution()).checkInItem(itembarcode, itemRequestInformation.getPatronBarcode());
            } else {
                itemCheckinResponse = new ItemCheckinResponse();
                itemCheckinResponse.setSuccess(false);
                itemCheckinResponse.setScreenMessage("Item Id not found");
            }
        } catch (Exception e) {
            itemCheckinResponse.setSuccess(false);
            itemCheckinResponse.setScreenMessage(e.getMessage());
        }
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

            itemHoldResponse = (ItemHoldResponse) jsipConectorFactory.getJSIPConnector(callInstitition).placeHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                    itemRequestInformation.getRequestingInstitution(),
                    itemRequestInformation.getItemOwningInstitution(),
                    itemRequestInformation.getExpirationDate(),
                    itemRequestInformation.getBibId(),
                    itemRequestInformation.getDeliveryLocation(),
                    itemRequestInformation.getTrackingId(),
                    itemRequestInformation.getTitle(),
                    itemRequestInformation.getAuthor(),
                    itemRequestInformation.getCallNumber());

        } catch (Exception e) {
            logger.info("Exception", e);
            itemHoldResponse.setSuccess(false);
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
        itemHoldCancelResponse = (ItemHoldResponse)  jsipConectorFactory.getJSIPConnector(itemRequestInformation.getRequestingInstitution()).cancelHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation(),itemRequestInformation.getTrackingId());
        return itemHoldCancelResponse;
    }

    @RequestMapping(value = "/createBib", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem createBibliogrphicItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition)  throws Exception{
        ItemCreateBibResponse itemCreateBibResponse = null;
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        itemCreateBibResponse = (ItemCreateBibResponse) jsipConectorFactory.getJSIPConnector(callInstitition).createBib(itembarcode, itemRequestInformation.getPatronBarcode(), itemRequestInformation.getRequestingInstitution(), itemRequestInformation.getTitleIdentifier());

        return itemCreateBibResponse;
    }

    @RequestMapping(value = "/itemInformation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem itemInformation(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();

        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);

        itemInformationResponse = (ItemInformationResponse) jsipConectorFactory.getJSIPConnector(callInstitition).lookupItem(itembarcode,itemRequestInformation.getSource());
        return itemInformationResponse;
    }

    @RequestMapping(value = "/recallItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem recallItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemRecallResponse itemRecallResponse = new ItemRecallResponse();
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = (String) itemRequestInformation.getItemBarcodes().get(0);
        itemRecallResponse = (ItemRecallResponse) jsipConectorFactory.getJSIPConnector(callInstitition).recallItem(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation());

        return itemRecallResponse;
    }

    @RequestMapping(value = "/patronInformation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem patronInformation(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        PatronInformationResponse patronInformationResponse=null;
        String itembarcode ="";
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        patronInformationResponse = (PatronInformationResponse) jsipConectorFactory.getJSIPConnector(callInstitition).lookupPatron(itemRequestInformation.getPatronBarcode());
        return patronInformationResponse;
    }

    private String formatFromSipDate(String sipDate) {
        SimpleDateFormat sipFormat = new SimpleDateFormat("yyyyMMdd    HHmmss");
        SimpleDateFormat requiredFormat = new SimpleDateFormat("dd-MMM-YYYY HH:mm:ss");
        String reformattedStr = "";
        try {
            if (sipDate != null && sipDate.trim().length() > 0) {
                reformattedStr = requiredFormat.format(sipFormat.parse(sipDate));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return reformattedStr;
    }

    public void logMessages(Logger logger, Object clsObject){
        try {
            for (Field field : clsObject.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(clsObject);
                logger.info("Field name: "+ name+"Filed Value :"+value);
            }
        } catch (IllegalAccessException e) {
            logger.error("",e);
        }
    }
}
