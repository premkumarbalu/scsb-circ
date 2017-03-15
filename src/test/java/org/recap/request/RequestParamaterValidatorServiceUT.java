package org.recap.request;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.ItemRequestInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hemalathas on 3/11/16.
 */
public class RequestParamaterValidatorServiceUT extends BaseTestCase{

    @Autowired
    RequestParamaterValidatorService requestParamaterValidatorService;

    @Test
    public void testForValidatingInvalidRequestingInstitution(){
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        List<String> itemBarcodeList = new ArrayList<>();
        itemBarcodeList.add("33433014514719");
        itemBarcodeList.add("33433012968222");
        itemRequestInformation.setItemBarcodes(itemBarcodeList);
        itemRequestInformation.setPatronBarcode("45678915");
        itemRequestInformation.setRequestType(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT);
        itemRequestInformation.setRequestingInstitution("PULd");
        itemRequestInformation.setEmailAddress("hemalatha.s@htcindia.com");
        ResponseEntity responseEntity = requestParamaterValidatorService.validateItemRequestParameters(itemRequestInformation);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getBody(), ReCAPConstants.INVALID_REQUEST_INSTITUTION+"\n");
    }

    @Test
    public void testForValidatingInvalidEmailAddress(){
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        List<String> itemBarcodeList = new ArrayList<>();
        itemBarcodeList.add("33433014514719");
        itemBarcodeList.add("33433012968222");
        itemRequestInformation.setItemBarcodes(itemBarcodeList);
        itemRequestInformation.setPatronBarcode("45678915");
        itemRequestInformation.setRequestType(ReCAPConstants.REQUEST_TYPE_BORROW_DIRECT);
        itemRequestInformation.setRequestingInstitution("PUL");
        itemRequestInformation.setEmailAddress("hemalatha.shtcindia.com");
        ResponseEntity responseEntity = requestParamaterValidatorService.validateItemRequestParameters(itemRequestInformation);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getBody(), ReCAPConstants.INVALID_EMAIL_ADDRESS+"\n");
    }

    @Test
    public void testForValidatingInvalidRequestType(){
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        List<String> itemBarcodeList = new ArrayList<>();
        itemBarcodeList.add("33433014514719");
        itemBarcodeList.add("33433012968222");
        itemRequestInformation.setItemBarcodes(itemBarcodeList);
        itemRequestInformation.setPatronBarcode("45678915");
        itemRequestInformation.setRequestType(null);
        itemRequestInformation.setRequestingInstitution("PUL");
        itemRequestInformation.setEmailAddress("hemalatha.s@htcindia.com");
        ResponseEntity responseEntity = requestParamaterValidatorService.validateItemRequestParameters(itemRequestInformation);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getBody(), ReCAPConstants.INVALID_REQUEST_TYPE+"\n");
    }

    @Test
    public void testForValidatingEDDRequestType(){
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        List<String> itemBarcodeList = new ArrayList<>();
        itemBarcodeList.add("33433014514719");
        itemRequestInformation.setItemBarcodes(itemBarcodeList);
        itemRequestInformation.setPatronBarcode("45678915");
        itemRequestInformation.setChapterTitle("title");
        itemRequestInformation.setRequestType(ReCAPConstants.EDD_REQUEST);
        itemRequestInformation.setDeliveryLocation("AC");
        itemRequestInformation.setRequestingInstitution("PUL");
        itemRequestInformation.setEmailAddress("hemalatha.s@htcindia.com");
        itemRequestInformation.setStartPage("0");
        itemRequestInformation.setEndPage("0");
        ResponseEntity responseEntity = requestParamaterValidatorService.validateItemRequestParameters(itemRequestInformation);
        assertNull(responseEntity);
    }







}