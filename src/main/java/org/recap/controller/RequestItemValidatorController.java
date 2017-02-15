package org.recap.controller;

import org.recap.ReCAPConstants;
import org.recap.ils.JSIPConnectorFactory;
import org.recap.model.ItemRequestInformation;
import org.recap.request.ItemValidatorService;
import org.recap.request.RequestParamaterValidatorService;
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

    @Autowired
    RequestParamaterValidatorService requestParamaterValidatorService;

    @Autowired
    JSIPConnectorFactory jsipConnectorFactory;

    @Autowired
    ItemValidatorService itemValidatorService;

    @RequestMapping(value = "/validateItemRequestInformations", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity validateItemRequestInformations(@RequestBody ItemRequestInformation itemRequestInformation) {
        ResponseEntity responseEntity;
        responseEntity = requestParamaterValidatorService.validateItemRequestParameters(itemRequestInformation);
        if (responseEntity == null) {
            responseEntity = itemValidatorService.itemValidation(itemRequestInformation);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                if (!jsipConnectorFactory.getJSIPConnector(itemRequestInformation.getRequestingInstitution()).patronValidation(itemRequestInformation.getRequestingInstitution(), itemRequestInformation.getPatronBarcode())) {
                    responseEntity = new ResponseEntity(ReCAPConstants.INVALID_PATRON, requestParamaterValidatorService.getHttpHeaders(), HttpStatus.BAD_REQUEST);
                }
            }
        }
        return responseEntity;
    }

}
