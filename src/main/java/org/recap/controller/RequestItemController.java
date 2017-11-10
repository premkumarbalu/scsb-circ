package org.recap.controller;

import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.ils.JSIPConnectorFactory;
import org.recap.ils.model.response.*;
import org.recap.model.BulkRequestInformation;
import org.recap.model.ItemRefileRequest;
import org.recap.model.ItemRefileResponse;
import org.recap.model.ItemRequestInformation;
import org.recap.request.ItemRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Gets JSIPConectorFactory object.
     *
     * @return the jsip conector factory
     */
    public JSIPConnectorFactory getJsipConectorFactory() {
        return jsipConectorFactory;
    }

    /**
     * Gets ItemRequestService object.
     *
     * @return the item request service
     */
    public ItemRequestService getItemRequestService() {
        return itemRequestService;
    }

    /**
     * Checkout item method is for processing SIP2 protocol function check out, This function converts SIP data to JSON format.
     *
     * @param itemRequestInformation the item request information
     * @param callInstitition        the call institition
     * @return the abstract response item
     */
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

    /**
     * This method checkinItem is for processing SIP2 protocol function check in. This function converts SIP data to JSON format.
     *
     * @param itemRequestInformation the item request information
     * @param callInstitition        the call institition
     * @return the abstract response item
     */
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

    /**
     * Hold item abstract response item.
     *
     * @param itemRequestInformation the item request information
     * @param callInstitition        the call institition
     * @return the abstract response item
     */
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
                    getPickupLocationDB(itemRequestInformation, callInst),
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

    /**
     * Cancel hold item abstract response item.
     *
     * @param itemRequestInformation the item request information
     * @param callInstitition        the call institition
     * @return the abstract response item
     */
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
                getPickupLocationDB(itemRequestInformation, callInst), itemRequestInformation.getTrackingId());
        return itemHoldCancelResponse;
    }

    /**
     * Create bibliogrphic item abstract response item.
     *
     * @param itemRequestInformation the item request information
     * @param callInstitition        the call institition
     * @return the abstract response item
     */
    @RequestMapping(value = "/createBib", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem createBibliogrphicItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemCreateBibResponse itemCreateBibResponse;
        String itemBarcode;
        logger.info("ESIP CALL FOR -> " + callInstitition);
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

    /**
     * Item information abstract response item.
     *
     * @param itemRequestInformation the item request information
     * @param callInstitition        the call institition
     * @return the abstract response item
     */
    @RequestMapping(value = "/itemInformation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem itemInformation(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        AbstractResponseItem itemInformationResponse;
        String callInst = callingInsttution(callInstitition, itemRequestInformation);
        String itembarcode = itemRequestInformation.getItemBarcodes().get(0);
        itemInformationResponse = getJsipConectorFactory().getJSIPConnector(callInst).lookupItem(itembarcode);
        return itemInformationResponse;
    }

    /**
     * Recall item abstract response item.
     *
     * @param itemRequestInformation the item request information
     * @param callInstitition        the call institition
     * @return the abstract response item
     */
    @RequestMapping(value = "/recallItem", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem recallItem(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemRecallResponse itemRecallResponse;
        logger.info("ESIP CALL FOR -> " + callInstitition);
        String callInst = callingInsttution(callInstitition, itemRequestInformation);
        String itembarcode = itemRequestInformation.getItemBarcodes().get(0);
        itemRecallResponse = (ItemRecallResponse) getJsipConectorFactory().getJSIPConnector(callInst).recallItem(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                getPickupLocationDB(itemRequestInformation, callInst));
        return itemRecallResponse;
    }

    /**
     * Patron information abstract response item.
     *
     * @param itemRequestInformation the item request information
     * @param callInstitition        the call institition
     * @return the abstract response item
     */
    @RequestMapping(value = "/patronInformation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem patronInformation(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        PatronInformationResponse patronInformationResponse;
        String callInst = callingInsttution(callInstitition, itemRequestInformation);
        patronInformationResponse = (PatronInformationResponse) getJsipConectorFactory().getJSIPConnector(callInst).lookupPatron(itemRequestInformation.getPatronBarcode());
        return patronInformationResponse;
    }

    /**
     * Refile item item refile response.
     *
     * @param itemRefileRequest the item refile request
     * @return the item refile response
     */
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


    @PostMapping("/patronValidationBulkRequest")
    public Boolean patronValidationBulkRequest(@RequestBody BulkRequestInformation bulkRequestInformation) {
        return jsipConectorFactory.getJSIPConnector(bulkRequestInformation.getRequestingInstitution()).patronValidation(bulkRequestInformation.getRequestingInstitution(), bulkRequestInformation.getPatronBarcode());
    }

    /**
     * This method refiles the item in ILS. Currently only NYPL has the refile endpoint.
     *
     * @param itemRequestInformation the item request information
     * @param callInstitition        the call institition
     * @return the abstract response item
     */
    @RequestMapping(value = "/refileItemInILS", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AbstractResponseItem refileItemInILS(@RequestBody ItemRequestInformation itemRequestInformation, String callInstitition) {
        ItemRefileResponse itemRefileResponse;
        String itemBarcode;
        try {
            String callInst = callingInsttution(callInstitition, itemRequestInformation);
            if (!itemRequestInformation.getItemBarcodes().isEmpty()) {
                itemBarcode = itemRequestInformation.getItemBarcodes().get(0);
                itemRefileResponse = (ItemRefileResponse) getJsipConectorFactory().getJSIPConnector(callInst).refileItem(itemBarcode);
            } else {
                itemRefileResponse = new ItemRefileResponse();
                itemRefileResponse.setSuccess(false);
                itemRefileResponse.setScreenMessage(ReCAPConstants.REQUEST_ITEM_BARCODE_NOT_FOUND);
            }
        } catch (Exception e) {
            itemRefileResponse = new ItemRefileResponse();
            itemRefileResponse.setSuccess(false);
            itemRefileResponse.setScreenMessage(e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return itemRefileResponse;
    }

    @RequestMapping(value = "/lasItemStatus", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String lasItemStatusUri(@RequestParam(name = "uriEnable", required = true) boolean uriEnable) {
        String msg = "Uri Enabled = " + uriEnable;
        if (uriEnable) {
            ReCAPConstants.LAS_ITEM_STATUS_REST_SERVICE = "http://recapgfa-dev.princeton.edu:9092/lasapi/rest/lasapiSvc/itemStatus";
        } else {
            ReCAPConstants.LAS_ITEM_STATUS_REST_SERVICE = "http://recapgfa-dev.princeton.edu:9092/lasapi/rest/lasapiSvc/itemStatu";
        }
        return msg;
    }

    /**
     * Gets pickup location.
     *
     * @param institution the institution
     * @return the pickup location
     */
    public String getPickupLocation(String institution) {
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

    /**
     * Log messages.
     *
     * @param logger    the logger
     * @param clsObject the cls object
     */
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

    private String getPickupLocationDB(ItemRequestInformation itemRequestInformation, String callInstitution) {
        if (ReCAPConstants.NYPL.equalsIgnoreCase(callInstitution)) {
            return itemRequestInformation.getDeliveryLocation();
        }
        return (StringUtils.isBlank(itemRequestInformation.getPickupLocation())) ? getPickupLocation(callInstitution) : itemRequestInformation.getPickupLocation();
    }
}
