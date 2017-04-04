package org.recap.ils.model.response;

import org.junit.Test;
import org.recap.BaseTestCase;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 3/4/17.
 */
public class ItemInformationResponseUT extends BaseTestCase{

    @Test
    public void testItemInformationResponse(){
        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
        itemInformationResponse.setExpirationDate(new Date().toString());
        itemInformationResponse.setTitleIdentifier("test");
        itemInformationResponse.setDueDate(new Date().toString());
        itemInformationResponse.setCirculationStatus("test");
        itemInformationResponse.setSecurityMarker("test");
        itemInformationResponse.setFeeType("test");
        itemInformationResponse.setTransactionDate(new Date().toString());
        itemInformationResponse.setHoldQueueLength("1");
        itemInformationResponse.setHoldPickupDate(new Date().toString());
        itemInformationResponse.setRecallDate(new Date().toString());
        itemInformationResponse.setOwner("PUL");
        itemInformationResponse.setMediaType("test");
        itemInformationResponse.setPermanentLocation("test");
        itemInformationResponse.setCurrentLocation("test");
        itemInformationResponse.setBibID("1");
        itemInformationResponse.setISBN("254564");
        itemInformationResponse.setLCCN("524545578");
        itemInformationResponse.setCurrencyType("test");
        itemInformationResponse.setCallNumber("56465");
        itemInformationResponse.setItemType("test");
        itemInformationResponse.setBibIds(Arrays.asList("254"));
        itemInformationResponse.setSource("test");
        itemInformationResponse.setCreatedDate(new Date().toString());
        itemInformationResponse.setUpdatedDate(new Date().toString());
        itemInformationResponse.setDeletedDate(new Date().toString());
        itemInformationResponse.setDeleted(false);
        itemInformationResponse.setRequestId(1);
        itemInformationResponse.setPatronBarcode("456686528");
        itemInformationResponse.setRequestingInstitution("CUL");
        itemInformationResponse.setEmailAddress("test@gmail.com");
        itemInformationResponse.setRequestType("Recall");
        itemInformationResponse.setDeliveryLocation("PB");
        itemInformationResponse.setRequestNotes("test");
        itemInformationResponse.setItemId(1);
        itemInformationResponse.setUsername("test");


        assertNotNull(itemInformationResponse.getExpirationDate());
        assertNotNull(itemInformationResponse.getTitleIdentifier());
        assertNotNull(itemInformationResponse.getDueDate());
        assertNotNull(itemInformationResponse.getCirculationStatus());
        assertNotNull(itemInformationResponse.getSecurityMarker());
        assertNotNull(itemInformationResponse.getFeeType());
        assertNotNull(itemInformationResponse.getTransactionDate());
        assertNotNull(itemInformationResponse.getHoldQueueLength());
        assertNotNull(itemInformationResponse.getRecallDate());
        assertNotNull(itemInformationResponse.getOwner());
        assertNotNull(itemInformationResponse.getMediaType());
        assertNotNull(itemInformationResponse.getPermanentLocation());
        assertNotNull(itemInformationResponse.getCurrentLocation());
        assertNotNull(itemInformationResponse.getBibID());
        assertNotNull(itemInformationResponse.getISBN());
        assertNotNull(itemInformationResponse.getLCCN());
        assertNotNull(itemInformationResponse.getHoldPickupDate());
        assertNotNull(itemInformationResponse.getCurrencyType());
        assertNotNull(itemInformationResponse.getCallNumber());
        assertNotNull(itemInformationResponse.getItemType());
        assertNotNull(itemInformationResponse.getBibIds());
        assertNotNull(itemInformationResponse.getSource());
        assertNotNull(itemInformationResponse.getCreatedDate());
        assertNotNull(itemInformationResponse.getUpdatedDate());
        assertNotNull(itemInformationResponse.getDeletedDate());
        assertNotNull(itemInformationResponse.isDeleted());
        assertNotNull(itemInformationResponse.getRequestId());
        assertNotNull(itemInformationResponse.getPatronBarcode());
        assertNotNull(itemInformationResponse.getRequestingInstitution());
        assertNotNull(itemInformationResponse.getEmailAddress());
        assertNotNull(itemInformationResponse.getRequestType());
        assertNotNull(itemInformationResponse.getDeliveryLocation());
        assertNotNull(itemInformationResponse.getRequestNotes());
        assertNotNull(itemInformationResponse.getItemId());
        assertNotNull(itemInformationResponse.getUsername());
    }

}