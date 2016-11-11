package org.recap.ils;

import com.ceridwen.circulation.SIP.messages.*;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by chenchulakshmig on 6/10/16.
 */
public class ColumbiaESIPConnectorUT extends BaseTestCase {

    @Autowired
    ColumbiaESIPConnector columbiaESIPConnector;

    @Test
    public void lookupItem() throws Exception {
        String itemIdentifier = "32101077423406";
        ItemInformationResponse itemInformationResponse = columbiaESIPConnector.lookupItem(itemIdentifier);
        assertEquals(itemIdentifier,itemInformationResponse.getItemIdentifier());
        assertEquals("Bolshevism, by an eye-witness from Wisconsin, by Lieutenant A. W. Kliefoth ...",itemInformationResponse.getTitleIdentifier());
    }

    @Test
    public void lookupUser() throws Exception {
        String patronIdentifier = "45678915";
        PatronInformationResponse patronInformationResponse = columbiaESIPConnector.lookupUser(patronIdentifier);
        assertNotNull(patronInformationResponse);
        assertTrue(patronInformationResponse.isValidPatron());
        assertTrue(patronInformationResponse.isValidPatronPassword());
    }

    @Test
    public void checkout() throws Exception {
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        CheckOutResponse checkOutResponse = columbiaESIPConnector.checkoutItem(itemIdentifier, patronIdentifier);
        assertNotNull(checkOutResponse);
        assertTrue(checkOutResponse.isOk());
    }

    @Test
    public void checkIn() throws Exception {
        String itemIdentifier = "32101077423406";
        CheckInResponse checkInResponse = columbiaESIPConnector.checkInItem(itemIdentifier);
        assertNotNull(checkInResponse);
        assertTrue(checkInResponse.isOk());
    }

    @Test
    public void placeHold() throws Exception {
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        HoldResponse holdResponse = columbiaESIPConnector.placeHold(itemIdentifier, patronIdentifier);
        assertNotNull(holdResponse);
        assertTrue(holdResponse.isOk());
    }

    @Test
    public void cancelHold() throws Exception {
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        HoldResponse holdResponse = columbiaESIPConnector.cancelHold(itemIdentifier, patronIdentifier);
        assertNotNull(holdResponse);
        assertTrue(holdResponse.isOk());
    }

}