package org.recap.controller;

import org.apache.commons.lang3.StringUtils;
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
 * Class for all service part of Requesting Item Functionality
 */
@RestController
@RequestMapping("/requestItem")
public class RequestItemController {

    private static final Logger logger = LoggerFactory.getLogger(RequestItemController.class);

    @Autowired
    private JSIPConnectorFactory jsipConectorFactory;

    @Autowired
    private ItemRequestService itemRequestService;

    public JSIPConnectorFactory getJsipConectorFactory() {
        return jsipConectorFactory;
    }

    public ItemRequestService getItemRequestService() {
        return itemRequestService;
    }

    @RequestMapping(value = "/checkoutItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem checkoutItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemCheckoutResponse itemCheckoutResponse = new ItemCheckoutResponse();
        String itemBarcode;
        try {
            String callInst = callingInsttution(callInstitition, itemRequestInformation);
            if (!itemRequestInformation.getItemBarcodes().isEmpty()) {
                itemBarcode = itemRequestInformation.getItemBarcodes().get(0);
                itemCheckoutResponse = (ItemCheckoutResponse) getJsipConectorFactory().getJSIPConnector(callInst).checkOutItem(itemBarcode, itemRequestInformation.getPatronBarcode());
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
        ItemCheckinResponse itemCheckinResponse;
        String itemBarcode;
        try {
            String callInst = callingInsttution(callInstitition, itemRequestInformation);
            if (!itemRequestInformation.getItemBarcodes().isEmpty()) {
                itemBarcode = itemRequestInformation.getItemBarcodes().get(0);
                itemCheckinResponse = (ItemCheckinResponse) getJsipConectorFactory().getJSIPConnector(callInst).checkInItem(itemBarcode, itemRequestInformation.getPatronBarcode());
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
            String callInst = callingInsttution(callInstitition, itemRequestInformation);
            String itembarcode = itemRequestInformation.getItemBarcodes().get(0);
            itemHoldResponse = (ItemHoldResponse) getJsipConectorFactory().getJSIPConnector(callInst).placeHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                    itemRequestInformation.getRequestingInstitution(),
                    itemRequestInformation.getItemOwningInstitution(),
                    itemRequestInformation.getExpirationDate(),
                    itemRequestInformation.getBibId(),
                    getpickupLoacation(callInstitition),
                    itemRequestInformation.getTrackingId(),
                    itemRequestInformation.getTitleIdentifier(),
                    itemRequestInformation.getAuthor(),
                    itemRequestInformation.getCallNumber());

        } catch (Exception e) {
            logger.info(ReCAPConstants.REQUEST_EXCEPTION, e);
            itemHoldResponse.setSuccess(false);
            itemHoldResponse.setScreenMessage("ILS returned a invalid response");
        }
        return itemHoldResponse;
    }

    @RequestMapping(value = "/cancelHoldItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem cancelHoldItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemHoldResponse itemHoldCancelResponse;
        String itembarcode = "";
        String callInst = callingInsttution(callInstitition, itemRequestInformation);
        if (!itemRequestInformation.getItemBarcodes().isEmpty()) {
            itembarcode = itemRequestInformation.getItemBarcodes().get(0);
        }
        itemHoldCancelResponse = (ItemHoldResponse) getJsipConectorFactory().getJSIPConnector(callInst).cancelHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                getpickupLoacation(callInstitition), itemRequestInformation.getTrackingId());
        return itemHoldCancelResponse;
    }

    @RequestMapping(value = "/createBib", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem createBibliogrphicItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemCreateBibResponse itemCreateBibResponse;
        String itemBarcode;
        String callInst = callingInsttution(callInstitition, itemRequestInformation);
        if (!itemRequestInformation.getItemBarcodes().isEmpty()) {
            itemBarcode = itemRequestInformation.getItemBarcodes().get(0);
            ItemInformationResponse itemInformation = (ItemInformationResponse) itemInformation(itemRequestInformation, itemRequestInformation.getRequestingInstitution());
            if (itemInformation.getScreenMessage().toUpperCase().contains(ReCAPConstants.REQUEST_ITEM_BARCODE_NOT_FOUND)) {
                itemCreateBibResponse = (ItemCreateBibResponse) getJsipConectorFactory().getJSIPConnector(callInst).createBib(itemBarcode, itemRequestInformation.getPatronBarcode(), itemRequestInformation.getRequestingInstitution(), itemRequestInformation.getTitleIdentifier());
            } else {
                itemCreateBibResponse = new ItemCreateBibResponse();
                itemCreateBibResponse.setSuccess(true);
                itemCreateBibResponse.setScreenMessage("Item Barcode already Exist");
                itemCreateBibResponse.setItemBarcode(itemBarcode);
                itemCreateBibResponse.setBibId(itemInformation.getBibID());
            }
        } else {
            itemCreateBibResponse = new ItemCreateBibResponse();
        }
        return itemCreateBibResponse;
    }

    @RequestMapping(value = "/itemInformation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem itemInformation(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        AbstractResponseItem itemInformationResponse;
        String callInst = callingInsttution(callInstitition, itemRequestInformation);
        String itembarcode = itemRequestInformation.getItemBarcodes().get(0);
        itemInformationResponse = getJsipConectorFactory().getJSIPConnector(callInst).lookupItem(itembarcode);
        return itemInformationResponse;
    }

    @RequestMapping(value = "/recallItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem recallItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemRecallResponse itemRecallResponse;
        String callInst = callingInsttution(callInstitition, itemRequestInformation);
        String itembarcode = itemRequestInformation.getItemBarcodes().get(0);
        itemRecallResponse = (ItemRecallResponse) getJsipConectorFactory().getJSIPConnector(callInst).recallItem(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                getpickupLoacation(callInstitition));

        return itemRecallResponse;
    }

    @RequestMapping(value = "/patronInformation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem patronInformation(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        PatronInformationResponse patronInformationResponse;
        String callInst = callingInsttution(callInstitition, itemRequestInformation);
        patronInformationResponse = (PatronInformationResponse) getJsipConectorFactory().getJSIPConnector(callInst).lookupPatron(itemRequestInformation.getPatronBarcode());
        return patronInformationResponse;
    }

    @RequestMapping(value = "/refile", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ItemRefileResponse refileItem(@RequestBody ItemRefileRequest itemRefileRequest) {
        boolean bSuccess = getItemRequestService().reFileItem(itemRefileRequest);
        ItemRefileResponse itemRefileResponse = new ItemRefileResponse();
        itemRefileResponse.setSuccess(bSuccess);
        if (bSuccess) {
            itemRefileResponse.setScreenMessage("Successfully Refiled");
        } else {
            itemRefileResponse.setScreenMessage("Cannot refile a already available Item");
        }

        return itemRefileResponse;
    }

    public String getpickupLoacation(String institution) {
        String pickUpLocation = "";
        if (institution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            pickUpLocation = ReCAPConstants.DEFAULT_PICK_UP_LOCATION_PUL;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            pickUpLocation = ReCAPConstants.DEFAULT_PICK_UP_LOCATION_CUL;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
            pickUpLocation = ReCAPConstants.DEFAULT_PICK_UP_LOCATION_NYPL;
        }
        return pickUpLocation;
    }

    public void logMessages(Logger logger, Object clsObject) {
        try {
            for (Field field : clsObject.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(clsObject);
                if (!StringUtils.isBlank(name) && value != null) {
                    logger.info("Field name: {} Filed Value : {} ", name, value);
                }
            }
        } catch (IllegalAccessException e) {
            logger.error("", e);
        }
    }

    private String callingInsttution(String callingInst, ItemRequestInformation itemRequestInformation) {
        String inst;
        if (callingInst == null) {
            inst = itemRequestInformation.getItemOwningInstitution();
        } else {
            inst = callingInst;
        }
        return inst;
    }
}
