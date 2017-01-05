package org.recap.controller;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.ItemRequestInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hemalathas on 11/11/16.
 */
public class RequestItemValidatorControllerUT extends BaseTestCase{
    @Autowired
    RequestItemValidatorController requestItemValidatorController;

    @Test
    public void testValidRequest(){
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setPatronBarcode("45678915");
        itemRequestInformation.setRequestType(ReCAPConstants.REQUEST_TYPE_HOLD);
        itemRequestInformation.setDeliveryLocation("AC");
        itemRequestInformation.setEmailAddress("hemalatha.s@htcindia.com");
        itemRequestInformation.setRequestingInstitution("PUL");
        itemRequestInformation.setItemBarcodes(Arrays.asList("33433003796665","33433003796673"));
        ResponseEntity responseEntity = requestItemValidatorController.validateItemRequestInformations(itemRequestInformation);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getBody(), ReCAPConstants.VALID_REQUEST);

    }

    @Test
    public void testInValidRequest(){
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setPatronBarcode("4567gfdr8915");
        itemRequestInformation.setRequestType(ReCAPConstants.REQUEST_TYPE_HOLD);
        itemRequestInformation.setDeliveryLocation("AC");
        itemRequestInformation.setEmailAddress("hemalatha.s@htcindia.com");
        itemRequestInformation.setRequestingInstitution("PUL");
        itemRequestInformation.setItemBarcodes(Arrays.asList("33433003796665","33433003796673"));
        ResponseEntity responseEntity = requestItemValidatorController.validateItemRequestInformations(itemRequestInformation);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getBody(), ReCAPConstants.INVALID_PATRON);

    }

}