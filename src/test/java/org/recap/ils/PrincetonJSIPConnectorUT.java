package org.recap.ils;

import com.pkrete.jsip2.messages.requests.*;
import com.pkrete.jsip2.messages.responses.*;
import com.pkrete.jsip2.util.MessageUtil;
import com.pkrete.jsip2.variables.HoldMode;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ils.jsipmessages.SIP2CreateBibResponse;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by saravanakumarp on 28/9/16.
 */
public class PrincetonJSIPConnectorUT extends BaseTestCase {

    @Autowired
    private PrincetonJSIPConnector princetonESIPConnector;

    @Test
    public void lookupItem() throws Exception {
        String[] itemIdentifier = {"32101077423406", "32101061738587","77777","77777777777779"};

        SIP2ItemInformationResponse itemInformationResponse = princetonESIPConnector.lookupItem(itemIdentifier[1]);

//        assertEquals(itemIdentifier,itemInformationResponse.getItemIdentifier());
//        assertEquals("Bolshevism, by an eye-witness from Wisconsin, by Lieutenant A. W. Kliefoth ...",itemInformationResponse.getTitleIdentifier());
    }

//    @Test
//    public void lookupUser() throws Exception {
//        String patronIdentifier = "45678915";
//        PatronInformationResponse patronInformationResponse = princetonESIPConnector.lookupUser(patronIdentifier);
//        assertNotNull(patronInformationResponse);
//        assertTrue(patronInformationResponse.isValidPatron());
//        assertTrue(patronInformationResponse.isValidPatronPassword());
//    }

    @Test
    public void checkout() throws Exception {
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        String institutionId = "htccul";
        SIP2CheckoutResponse checkOutResponse = princetonESIPConnector.checkOutItem(itemIdentifier, institutionId, patronIdentifier);
        assertNotNull(checkOutResponse);
        assertTrue(checkOutResponse.isOk());
    }

    @Test
    public void checkIn() throws Exception {
        String itemIdentifier = "32101077423406";
        String patronIdentifier = "198572368";
        String institutionId = "htccul";
        SIP2CheckinResponse checkInResponse = princetonESIPConnector.checkInItem(itemIdentifier,institutionId,patronIdentifier);
        assertNotNull(checkInResponse);
        assertTrue(checkInResponse.isOk());
    }

    @Test
    public void check_out_In() throws Exception {
        String itemIdentifier = "32101077423406";
        String patronIdentifier = "198572368";
        String institutionId = "htccul";
        SIP2CheckoutResponse checkOutResponse = princetonESIPConnector.checkOutItem(itemIdentifier, institutionId, patronIdentifier);
        assertNotNull(checkOutResponse);
        assertTrue(checkOutResponse.isOk());
        SIP2CheckinResponse checkInResponse = princetonESIPConnector.checkInItem(itemIdentifier,institutionId,patronIdentifier);
        assertNotNull(checkInResponse);
        assertTrue(checkInResponse.isOk());
    }

    @Test
    public void cancelHold() throws Exception {
//        String itemIdentifier = "32101057972166";
        String itemIdentifier = "77777777777779";
//        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
//        String patronIdentifier = "45678915";
        String institutionId = "htccul";
//        String institutionId = "rcpsharepact";
        String expirationDate ="20161201    190405";//MessageUtil.getSipDateTime(); // Date Format YYYYMMDDZZZZHHMMSS
        String bibId="9959082";
        String pickupLocation="htcsc";

        SIP2HoldResponse holdResponse = princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation);

        assertNotNull(holdResponse);
        assertTrue(holdResponse.isOk());
    }

    // Test itemIdentifier I77777777777778
    // bibId="9959081";
    //9959082
    @Test
    public void placeHold() throws Exception {
        String itemIdentifier = "77777777777779";
//        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        String institutionId = "htccul";
        String expirationDate = "20161201    190405";//MessageUtil.getSipDateTime(); // Date Format YYYYMMDDZZZZHHMMSS
//        String bibId="100001";
        String bibId="9959082";
        String pickupLocation="htcsc";

        SIP2HoldResponse holdResponse = princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation);

        assertNotNull(holdResponse);
        assertTrue(holdResponse.isOk());
    }

    @Test
    public void bothHold() throws Exception {
//        String itemIdentifier = "32101057972166";
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        String institutionId = "htccul";
        String expirationDate =MessageUtil.getSipDateTime(); // Date Format YYYYMMDDZZZZHHMMSS
        String bibId="100001";
        String pickupLocation="htcsc";
        SIP2HoldResponse holdResponse;

        holdResponse = princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation);
        holdResponse = princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation);

        assertNotNull(holdResponse);
        assertTrue(holdResponse.isOk());
    }

    @Test
    public void createBib() throws Exception {
        String itemIdentifier = "77777777777779";
        String patronIdentifier = "198572368"; //Not required
        String institutionId = "htccul"; // Not Required
        String titleIdentifier ="RECAP TEST TITLE - 001";
        String bibId =""; //Not Required
        SIP2CreateBibResponse createBibResponse;

        createBibResponse = princetonESIPConnector.createBib(itemIdentifier,patronIdentifier,institutionId ,titleIdentifier,bibId);

        assertNotNull(createBibResponse);
        assertTrue(createBibResponse.isOk());
    }

    @Test
    public void deleteBib() throws Exception {
        String itemIdentifier = "77777777777777";
        String patronIdentifier = "198572368";
        String institutionId = "htccul";
        String titleIdentifier ="RECAP TEST TITLE";
        String bibId ="10001";

        SIP2DeleteBibResponse deleteBibResponse;

        deleteBibResponse = princetonESIPConnector.deleteBib(itemIdentifier,patronIdentifier,institutionId ,titleIdentifier,bibId);

        assertNotNull(deleteBibResponse);
        assertTrue(deleteBibResponse.isOk());
    }


//    Staff Identifier      Voyager operator ID.
//    Item Identifier       item barcode.
//    Bibliographic         ID MARC bibliographic field 001.
}
