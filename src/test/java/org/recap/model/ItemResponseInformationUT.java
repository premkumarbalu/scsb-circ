package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 20/3/17.
 */
public class ItemResponseInformationUT extends BaseTestCase{


    @Test
    public void testItemResponseInformation(){

        ItemResponseInformation itemResponseInformation = new ItemResponseInformation();
        itemResponseInformation.setPatronBarcode("45632985");
        itemResponseInformation.setItemBarcode("312365545522554");
        itemResponseInformation.setRequestType("Recall");
        itemResponseInformation.setDeliveryLocation("PB");
        itemResponseInformation.setRequestingInstitution("CUL");
        itemResponseInformation.setBibliographicId("12");
        itemResponseInformation.setExpirationDate(new Date().toString());
        itemResponseInformation.setScreenMessage("Success");
        itemResponseInformation.setSuccess(true);
        itemResponseInformation.setEmailAddress("hemalaths.s@htcindia.com");
        itemResponseInformation.setStartPage(1);
        itemResponseInformation.setEndPage(10);
        itemResponseInformation.setTitleIdentifier("test");
        itemResponseInformation.setBibiid("236");
        itemResponseInformation.setDueDate(new Date().toString());
        assertNotNull(itemResponseInformation.getPatronBarcode());
        assertNotNull(itemResponseInformation.getItemBarcode());
        assertNotNull(itemResponseInformation.getRequestType());
        assertNotNull(itemResponseInformation.getDeliveryLocation());
        assertNotNull(itemResponseInformation.getRequestingInstitution());
        assertNotNull(itemResponseInformation.getBibliographicId());
        assertNotNull(itemResponseInformation.getExpirationDate());
        assertNotNull(itemResponseInformation.getScreenMessage());
        assertNotNull(itemResponseInformation.isSuccess());
        assertNotNull(itemResponseInformation.getEmailAddress());
        assertNotNull(itemResponseInformation.getStartPage());
        assertNotNull(itemResponseInformation.getEndPage());
        assertNotNull(itemResponseInformation.getTitleIdentifier());
        assertNotNull(itemResponseInformation.getBibiid());
        assertNotNull(itemResponseInformation.getDueDate());
    }

}