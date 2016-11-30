package org.recap.controller;

import org.recap.ReCAPConstants;
import org.recap.ils.JSIPConnector;
import org.recap.model.ItemRequestInformation;
import org.recap.request.PatronValidatorService;
import org.recap.request.RequestParamaterValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Created by hemalathas on 10/11/16.
 */
@RestController
@RequestMapping("/requestItem")
public class RequestItemValidatorController{

    @Autowired
    RequestParamaterValidatorService requestParamaterValidatorService;

    @Autowired
    PatronValidatorService patronValidatorService;

    @Autowired
    JSIPConnector jsipConnector;

    @RequestMapping(value = "/validateItemRequestInformations" , method = RequestMethod.POST ,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity validateItemRequestInformations(@RequestBody ItemRequestInformation itemRequestInformation){
        ResponseEntity responseEntity = null;
        responseEntity = requestParamaterValidatorService.validateItemRequestParameters(itemRequestInformation);
        if(responseEntity == null){
            if(jsipConnector.patronValidation(itemRequestInformation.getRequestingInstitution(),itemRequestInformation.getPatronBarcode())){
                responseEntity = new ResponseEntity(ReCAPConstants.VALID_REQUEST,getHttpHeaders(), HttpStatus.OK);
            }else{
                responseEntity = new ResponseEntity(ReCAPConstants.INVALID_PATRON,getHttpHeaders(), HttpStatus.OK);
            }
        }
        return responseEntity;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ReCAPConstants.RESPONSE_DATE, new Date().toString());
        return responseHeaders;
    }
}
