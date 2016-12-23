package org.recap.ils;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ils.model.ItemCheckinResponse;
import org.recap.ils.model.ItemCheckoutResponse;
import org.recap.ils.model.ItemHoldResponse;
import org.recap.ils.model.ItemInformationResponse;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by rajeshbabuk on 19/12/16.
 */
public class NyplApiServiceConnectorUT extends BaseTestCase {

    @Autowired
    NyplApiServiceConnector nyplApiServiceConnector;

    @Test
    public void lookupItem() throws Exception {
        String itemId = "2322222222";
        String source = "nypl-sierra";
        ItemInformationResponse itemInformationResponse = nyplApiServiceConnector.lookupItem(itemId, source);
        assertNotNull(itemInformationResponse);
        assertNotNull(itemInformationResponse.getItemBarcode());
        assertNotNull(itemInformationResponse.getBibID());
        assertTrue(itemInformationResponse.isSuccess());
    }

    @Test
    public void checkoutItem() throws Exception {
        String itemBarcode = "33433001888415";
        String patronBarcode = "23333095887111";
        ItemCheckoutResponse itemCheckoutResponse = nyplApiServiceConnector.checkOutItem(itemBarcode, patronBarcode);
        assertNotNull(itemCheckoutResponse);
    }

    @Test
    public void checkinItem() throws Exception {
        String itemBarcode = "33433001888415";
        ItemCheckinResponse itemCheckinResponse = nyplApiServiceConnector.checkInItem(itemBarcode, null);
        assertNotNull(itemCheckinResponse);
    }

    @Test
    public void placeHold() throws Exception {
        String itemBarcode = "33433001888415";
        String patronBarcode = "23333095887111";
        String institutionId = "NYPL";
        String expirationDate = "";
        String bibId = "";
        String pickupLocation = "";
        String trackingId = "";
        String title = "";
        String author = "";
        String callNumber = "";
        ItemHoldResponse itemHoldResponse = nyplApiServiceConnector.placeHold(itemBarcode, patronBarcode, institutionId, expirationDate, bibId, pickupLocation, trackingId, title, author, callNumber);
        assertNotNull(itemHoldResponse);
    }

    @Test
    public void cancelHold() throws Exception {
        String itemBarcode = "33433001888415";
        String patronBarcode = "23333095887111";
        String institutionId = "NYPL";
        String expirationDate = "";
        String bibId = "";
        String pickupLocation = "";
        String trackingId = "";
        ItemHoldResponse itemHoldResponse = nyplApiServiceConnector.cancelHold(itemBarcode, patronBarcode, institutionId, expirationDate, bibId, pickupLocation, trackingId);
        assertNotNull(itemHoldResponse);
    }
}
