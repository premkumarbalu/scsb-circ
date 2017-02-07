package org.recap.controller;

import io.swagger.annotations.ApiParam;
import org.recap.ReCAPConstants;
import org.recap.ils.JSIPConnectorFactory;
import org.recap.ils.model.response.*;
import org.recap.model.ItemRefileRequest;
import org.recap.model.ItemRefileResponse;
import org.recap.model.ItemRequestInformation;
import org.recap.request.ItemRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;

/**
 * Created by sudhishk on 16/11/16.
 */
@RestController
@RequestMapping("/requestItem")
public class RequestItemController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    JSIPConnectorFactory jsipConectorFactory;

    @Autowired
    ItemRequestService itemRequestService;

    @RequestMapping(value = "/checkoutItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem checkoutItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemCheckoutResponse itemCheckoutResponse = new ItemCheckoutResponse();
        String itembarcode = "";
        try {
            if (callInstitition == null) {
                callInstitition = itemRequestInformation.getItemOwningInstitution();
            }
            if (!itemRequestInformation.getItemBarcodes().isEmpty()) {
                itembarcode = itemRequestInformation.getItemBarcodes().get(0);
                itemCheckoutResponse = (ItemCheckoutResponse) jsipConectorFactory.getJSIPConnector(callInstitition).checkOutItem(itembarcode, itemRequestInformation.getPatronBarcode());
            } else {
                itemCheckoutResponse.setSuccess(false);
                itemCheckoutResponse.setScreenMessage("Item Id not found");
            }
        } catch (Exception e) {
            itemCheckoutResponse.setSuccess(false);
            itemCheckoutResponse.setScreenMessage(e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
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
            if (!itemRequestInformation.getItemBarcodes().isEmpty()) {
                itembarcode = itemRequestInformation.getItemBarcodes().get(0);
                itemCheckinResponse = (ItemCheckinResponse) jsipConectorFactory.getJSIPConnector(callInstitition).checkInItem(itembarcode, itemRequestInformation.getPatronBarcode());
            } else {
                itemCheckinResponse = new ItemCheckinResponse();
                itemCheckinResponse.setSuccess(false);
                itemCheckinResponse.setScreenMessage("Item Id not found");
            }
        } catch (Exception e) {
            itemCheckinResponse = new ItemCheckinResponse();
            itemCheckinResponse.setSuccess(false);
            itemCheckinResponse.setScreenMessage(e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
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
            String itembarcode = itemRequestInformation.getItemBarcodes().get(0);

            itemHoldResponse = (ItemHoldResponse) jsipConectorFactory.getJSIPConnector(callInstitition).placeHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                    itemRequestInformation.getRequestingInstitution(),
                    itemRequestInformation.getItemOwningInstitution(),
                    itemRequestInformation.getExpirationDate(),
                    itemRequestInformation.getBibId(),
                    itemRequestInformation.getDeliveryLocation(),
                    itemRequestInformation.getTrackingId(),
                    itemRequestInformation.getTitleIdentifier(),
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
        ItemHoldResponse itemHoldCancelResponse;
        String itembarcode = "";
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }

        if (!itemRequestInformation.getItemBarcodes().isEmpty()) {
            itembarcode = itemRequestInformation.getItemBarcodes().get(0);
        }
        itemHoldCancelResponse = (ItemHoldResponse) jsipConectorFactory.getJSIPConnector(callInstitition).cancelHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation(), itemRequestInformation.getTrackingId());
        return itemHoldCancelResponse;
    }

    @RequestMapping(value = "/createBib", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem createBibliogrphicItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) throws Exception {
        ItemCreateBibResponse itemCreateBibResponse;
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = itemRequestInformation.getItemBarcodes().get(0);
        itemCreateBibResponse = (ItemCreateBibResponse) jsipConectorFactory.getJSIPConnector(callInstitition).createBib(itembarcode, itemRequestInformation.getPatronBarcode(), itemRequestInformation.getRequestingInstitution(), itemRequestInformation.getTitleIdentifier());

        return itemCreateBibResponse;
    }

    @RequestMapping(value = "/itemInformation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem itemInformation(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemInformationResponse itemInformationResponse;

        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = itemRequestInformation.getItemBarcodes().get(0);

        itemInformationResponse = (ItemInformationResponse) jsipConectorFactory.getJSIPConnector(callInstitition).lookupItem(itembarcode);
        return itemInformationResponse;
    }

    @RequestMapping(value = "/recallItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem recallItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemRecallResponse itemRecallResponse;
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        String itembarcode = itemRequestInformation.getItemBarcodes().get(0);
        itemRecallResponse = (ItemRecallResponse) jsipConectorFactory.getJSIPConnector(callInstitition).recallItem(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                itemRequestInformation.getDeliveryLocation());

        return itemRecallResponse;
    }

    @RequestMapping(value = "/patronInformation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem patronInformation(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        PatronInformationResponse patronInformationResponse;
        if (callInstitition == null) {
            callInstitition = itemRequestInformation.getItemOwningInstitution();
        }
        patronInformationResponse = (PatronInformationResponse) jsipConectorFactory.getJSIPConnector(callInstitition).lookupPatron(itemRequestInformation.getPatronBarcode());
        return patronInformationResponse;
    }

    @RequestMapping(value = "/refile", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ItemRefileResponse refileItem(@ApiParam(value = "Parameters for requesting re-file", required = true, name = "refileInformation") @RequestBody ItemRefileRequest itemRefileRequest) {
        boolean bSuccess = itemRequestService.reFileItem(itemRefileRequest);
        ItemRefileResponse itemRefileResponse = new ItemRefileResponse();
        itemRefileResponse.setSuccess(bSuccess);
        if (bSuccess) {
            itemRefileResponse.setScreenMessage("Successfully Refiled");
        } else {
            itemRefileResponse.setScreenMessage("Failed to Refile");
        }

        return itemRefileResponse;
    }

    public void logMessages(Logger logger, Object clsObject) {
        try {
            for (Field field : clsObject.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(clsObject);
                logger.info(String.format("Field name: %sFiled Value :%s", name, value));
            }
        } catch (IllegalAccessException e) {
            logger.error("", e);
        }
    }
}
