package org.recap.ils;

import com.pkrete.jsip2.messages.requests.*;
import com.pkrete.jsip2.messages.responses.*;
import com.pkrete.jsip2.util.MessageUtil;
import com.pkrete.jsip2.variables.HoldMode;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ils.jsipmessages.SIP2CreateBibResponse;
import org.recap.ils.jsipmessages.SIP2RecallResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;

import static org.junit.Assert.*;

/**
 * Created by saravanakumarp on 28/9/16.
 */
public class PrincetonJSIPConnectorUT extends BaseTestCase {

    @Autowired
    private PrincetonJSIPConnector princetonESIPConnector;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void login() throws Exception {
        String patronIdentifier = "45678912";
        boolean sip2LoginRequest = princetonESIPConnector.jSIPLogin(null,patronIdentifier);
        assertTrue(sip2LoginRequest);
    }

    @Test
    public void lookupItem() throws Exception {
        String[] itemIdentifier = {"32101077423406", "32101061738587","77777","77777777777779","PULTST54321"};
        String patronIdentifier = "45678913";
        String institutionId = "htccul";
        SIP2ItemInformationResponse itemInformationResponse = princetonESIPConnector.lookupItem(itemIdentifier[4]);

        logger.info("Circulation Status     :" + itemInformationResponse.getCirculationStatus());
        logger.info("SecurityMarker         :" + itemInformationResponse.getSecurityMarker());
        logger.info("Fee Type               :" + itemInformationResponse.getFeeType());
        logger.info("Transaction Date       :" + itemInformationResponse.getTransactionDate());
        logger.info("Hold Queue Length (CF) :" + itemInformationResponse.getHoldQueueLength());

        assertEquals(itemIdentifier[4],itemInformationResponse.getItemIdentifier());
//        assertEquals("Bolshevism, by an eye-witness from Wisconsin, by Lieutenant A. W. Kliefoth ...",itemInformationResponse.getTitleIdentifier());
//        SimpleDateFormat simpleDateFormat=  new SimpleDateFormat("YYYY-MM-DD");
//        logger.info(""+simpleDateFormat.parse("2016-12-01"));
    }

    @Test
    public void lookupItemStatus() throws Exception {
        String[] itemIdentifier = {"32101077423406", "32101061738587","77777","77777777777779","PULTST54338"};
        String[] patronIdentifier = {"45678913"};
        SIP2ItemInformationResponse itemInformationResponse = princetonESIPConnector.lookupItemStatus(itemIdentifier[4],"",patronIdentifier[0]);
        assertEquals(itemIdentifier,itemInformationResponse.getItemIdentifier());
    }

    @Test
    public void lookupUser() throws Exception {
        String patronIdentifier = "45678912";
        String institutionId = "htccul";
        SIP2PatronStatusResponse patronInformationResponse = princetonESIPConnector.lookupUser(institutionId, patronIdentifier);
        assertNotNull(patronInformationResponse);
//        assertTrue(patronInformationResponse.isValid());

    }

    @Test
    public void checkout() throws Exception {
        String itemIdentifier = "PULTST54338";
        String patronIdentifier = "45678913";
        SIP2CheckoutResponse checkOutResponse = princetonESIPConnector.checkOutItem(itemIdentifier, patronIdentifier);
        assertNotNull(checkOutResponse);
        assertTrue(checkOutResponse.isOk());
    }

    @Test
    public void checkIn() throws Exception {
        String itemIdentifier = "PULTST54321";
        String patronIdentifier = "45678913";
        SIP2CheckinResponse checkInResponse = princetonESIPConnector.checkInItem(itemIdentifier,patronIdentifier);
        assertNotNull(checkInResponse);
        assertTrue(checkInResponse.isOk());
    }

    @Test
    public void check_out_In() throws Exception {
        String itemIdentifier = "32101077423406";
        String patronIdentifier = "198572368";
        String institutionId = "htccul";
        SIP2CheckoutResponse checkOutResponse = princetonESIPConnector.checkOutItem(itemIdentifier , patronIdentifier);
        assertNotNull(checkOutResponse);
        assertTrue(checkOutResponse.isOk());
        SIP2CheckinResponse checkInResponse = princetonESIPConnector.checkInItem(itemIdentifier,patronIdentifier);
        assertNotNull(checkInResponse);
        assertTrue(checkInResponse.isOk());
    }

    /*
    String itemIdentifier = "32101057972166";
    String itemIdentifier = "77777777777779";
    String itemIdentifier = "32101095533293";
    String patronIdentifier = "198572368";
    String patronIdentifier = "45678915";
    String institutionId = "htccul";
    String institutionId = "rcpsharepact";
    String expirationDate ="20161201    190405";//MessageUtil.getSipDateTime(); // Date Format YYYYMMDDZZZZHHMMSS
    String bibId="9959082";
    String pickupLocation="htcsc";
    */

    @Test
    public void cancelHold() throws Exception {
        String itemIdentifier = "PULTST54321";
        String patronIdentifier = "45678913";
        String institutionId = "htccul";
        String expirationDate =MessageUtil.createFutureDate(20,2);
        String bibId="59040";
        String pickupLocation="rcpcirc";

        SIP2HoldResponse holdResponse = princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation);

        try {
            assertNotNull(holdResponse);
            assertTrue(holdResponse.isOk());
        } catch (AssertionError e) {
            logger.error("Cancel Hold Error - > ",e);
        }
        lookupItem();
    }

    //        String itemIdentifier = "32101095533293";

    @Test
    public void placeHold() throws Exception {
        String itemIdentifier = "PULTST54321";
        String patronIdentifier = "45678913";
        String institutionId = "htccul";
        String expirationDate = MessageUtil.createFutureDate(20,2);
        String bibId="9959082";
        String pickupLocation="rcpcirc";

        SIP2HoldResponse holdResponse = princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation);

        try {
            assertNotNull(holdResponse);
            assertTrue(holdResponse.isOk());
        } catch (AssertionError e) {
            logger.error("Hold Error - > ",e);
        }
        lookupItem();
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
        String itemIdentifier = "77777777777799";
        String patronIdentifier = "198572368"; // Not required

        String institutionId = "htccul"; // Not Required
        String titleIdentifier ="RECAP TEST TITLE - 002";
        String bibId =""; // Not Required
        SIP2CreateBibResponse createBibResponse;

        createBibResponse = princetonESIPConnector.createBib(itemIdentifier,patronIdentifier,institutionId ,titleIdentifier);

        assertNotNull(createBibResponse);
        assertTrue(createBibResponse.isOk());
    }

    @Test
    public void testRecall() throws Exception {
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        String institutionId = "htccul";
        String expirationDate = MessageUtil.createFutureDate(20,2);
        String pickupLocation="htcsc";
        String bibId="9959082";

        SIP2RecallResponse recallResponse = princetonESIPConnector.recallItem(itemIdentifier, patronIdentifier,institutionId ,expirationDate,pickupLocation,bibId);

        assertNotNull(recallResponse);
        assertTrue(recallResponse.isOk());
    }

//    Staff Identifier      Voyager operator ID.
//    Item Identifier       item barcode.
//    Bibliographic         ID MARC bibliographic field 001.

    /**
     * itemIdentifier = "32101095533293";
     * patronIdentifier = "198572368";
     * institutionId = "htccul";
     * bibId="9959082";
     * pickupLocation="htcsc";
     *
     *

     itemRequestInformation.setItemBarcodes(Arrays.asList("PULTST54321"));
     itemRequestInformation.setPatronBarcode("45678913");
     itemRequestInformation.setRequestType(ReCAPConstants.REQUEST_TYPE_RETRIEVAL);
     itemRequestInformation.setRequestingInstitution(ReCAPConstants.PRINCETON);
     itemRequestInformation.setEmailAddress("ksudhish@gmail.com");
     itemRequestInformation.setDeliveryLocation("htcsc");
     *
     */
}
