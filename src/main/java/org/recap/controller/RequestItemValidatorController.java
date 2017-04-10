package org.recap.controller;

import org.recap.ReCAPConstants;
import org.recap.ils.JSIPConnectorFactory;
import org.recap.model.ItemRequestInformation;
import org.recap.request.ItemValidatorService;
import org.recap.request.RequestParamaterValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by hemalathas on 10/11/16.
 */
@RestController
@RequestMapping("/requestItem")
public class RequestItemValidatorController {

    private static final Logger logger = LoggerFactory.getLogger(RequestItemValidatorController.class);
    /**
     * The Request paramater validator service.
     */
    @Autowired
    RequestParamaterValidatorService requestParamaterValidatorService;

    /**
     * The Jsip connector factory.
     */
    @Autowired
    JSIPConnectorFactory jsipConnectorFactory;

    /**
     * The Item validator service.
     */
    @Autowired
    ItemValidatorService itemValidatorService;

    /**
     * Validate item request informations response entity.
     *
     * @param itemRequestInformation the item request information
     * @return the response entity
     */
    @RequestMapping(value = "/validateItemRequestInformations", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity validateItemRequestInformations(@RequestBody ItemRequestInformation itemRequestInformation) {
        ResponseEntity responseEntity;
        responseEntity = requestParamaterValidatorService.validateItemRequestParameters(itemRequestInformation);
        if (responseEntity == null) {
            responseEntity = itemValidatorService.itemValidation(itemRequestInformation);
            if (responseEntity.getStatusCode() == HttpStatus.OK && !jsipConnectorFactory.getJSIPConnector(itemRequestInformation.getRequestingInstitution()).patronValidation(itemRequestInformation.getRequestingInstitution(), itemRequestInformation.getPatronBarcode())) {
                    responseEntity = new ResponseEntity(ReCAPConstants.INVALID_PATRON, requestParamaterValidatorService.getHttpHeaders(), HttpStatus.BAD_REQUEST);
                }
        }
        logger.info(String.format("Request Validation: %s - %s",responseEntity.getStatusCode(), responseEntity.getBody()));
        return responseEntity;
    }

    /**
     * Validate item request response entity.
     *
     * @param itemRequestInformation the item request information
     * @return the response entity
     */
    @RequestMapping(value = "/validateItemRequest", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity validateItemRequest(@RequestBody ItemRequestInformation itemRequestInformation) {
        ResponseEntity responseEntity;
        logger.info("Request Validation: Start");
        responseEntity = requestParamaterValidatorService.validateItemRequestParameters(itemRequestInformation);
        if (responseEntity == null) {
            responseEntity = itemValidatorService.itemValidation(itemRequestInformation);
        }
        logger.info(String.format("Request Validation: %s - %s",responseEntity.getStatusCode(), responseEntity.getBody()));
        return responseEntity;
    }
}
