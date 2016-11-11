package org.recap.ils;

import com.ceridwen.circulation.SIP.messages.*;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by saravanakumarp on 28/9/16.
 */
public class PrincetonESIPConnectorUT extends BaseTestCase {

    @Autowired
    private PrincetonESIPConnector princetonESIPConnector;

    @Test
    public void lookupItem() throws Exception {
        String itemIdentifier = "32101077423406";
        ItemInformationResponse itemInformationResponse = princetonESIPConnector.lookupItem(itemIdentifier);
        assertEquals(itemIdentifier,itemInformationResponse.getItemIdentifier());
        assertEquals("Bolshevism, by an eye-witness from Wisconsin, by Lieutenant A. W. Kliefoth ...",itemInformationResponse.getTitleIdentifier());
    }

    @Test
    public void lookupUser() throws Exception {
        String patronIdentifier = "45678915";
        PatronInformationResponse patronInformationResponse = princetonESIPConnector.lookupUser(patronIdentifier);
        assertNotNull(patronInformationResponse);
        assertTrue(patronInformationResponse.isValidPatron());
        assertTrue(patronInformationResponse.isValidPatronPassword());
    }

    @Test
    public void checkout() throws Exception {
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        CheckOutResponse checkOutResponse = princetonESIPConnector.checkoutItem(itemIdentifier, patronIdentifier);
        assertNotNull(checkOutResponse);
        assertTrue(checkOutResponse.isOk());
    }

    @Test
    public void checkIn() throws Exception {
        String itemIdentifier = "32101077423406";
        CheckInResponse checkInResponse = princetonESIPConnector.checkInItem(itemIdentifier);
        assertNotNull(checkInResponse);
        assertTrue(checkInResponse.isOk());
    }

    @Test
    public void placeHold() throws Exception {
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        HoldResponse holdResponse = princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier);
        assertNotNull(holdResponse);
        assertTrue(holdResponse.isOk());
    }

    @Test
    public void cancelHold() throws Exception {
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        HoldResponse holdResponse = princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier);
        assertNotNull(holdResponse);
        assertTrue(holdResponse.isOk());
    }
}
