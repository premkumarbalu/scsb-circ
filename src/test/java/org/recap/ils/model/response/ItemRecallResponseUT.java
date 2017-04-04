package org.recap.ils.model.response;

import org.junit.Test;
import org.recap.BaseTestCase;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 3/4/17.
 */
public class ItemRecallResponseUT extends BaseTestCase{

    @Test
    public void testItemRecallResponse(){
        ItemRecallResponse itemRecallResponse = new ItemRecallResponse();
        itemRecallResponse.setAvailable(true);
        itemRecallResponse.setTransactionDate(new Date().toString());
        itemRecallResponse.setInstitutionID("1");
        itemRecallResponse.setPatronIdentifier("456852345");
        itemRecallResponse.setTitleIdentifier("test");
        itemRecallResponse.setExpirationDate(new Date().toString());
        itemRecallResponse.setPickupLocation("PB");
        itemRecallResponse.setQueuePosition("1");
        itemRecallResponse.setBibId("4564645");
        itemRecallResponse.setISBN("4545");
        itemRecallResponse.setLCCN("7854");

        assertNotNull(itemRecallResponse.getAvailable());
        assertNotNull(itemRecallResponse.getTransactionDate());
        assertNotNull(itemRecallResponse.getInstitutionID());
        assertNotNull(itemRecallResponse.getPatronIdentifier());
        assertNotNull(itemRecallResponse.getTitleIdentifier());
        assertNotNull(itemRecallResponse.getExpirationDate());
        assertNotNull(itemRecallResponse.getPickupLocation());
        assertNotNull(itemRecallResponse.getQueuePosition());
        assertNotNull(itemRecallResponse.getBibId());
        assertNotNull(itemRecallResponse.getISBN());
        assertNotNull(itemRecallResponse.getLCCN());
    }

}