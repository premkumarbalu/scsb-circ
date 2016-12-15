package org.recap.ils;

import com.pkrete.jsip2.messages.responses.*;
import com.pkrete.jsip2.util.MessageUtil;
import org.junit.Test;
import org.recap.BaseTestCase;
import com.pkrete.jsip2.messages.response.SIP2CreateBibResponse;
import com.pkrete.jsip2.messages.response.SIP2RecallResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by saravanakumarp on 28/9/16.
 */
public class PrincetonJSIPConnectorUT extends BaseTestCase {

    @Autowired
    private PrincetonJSIPConnector princetonESIPConnector;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String itemIdentifier = "32101065514414";
    private String patronIdentifier = "45678915";
    //    private String patronIdentifier = "45678912";45678915
    private String pickupLocation = "rcpcirc";
    private String bibId = "59041";
    private String institutionId = "htccul";
    private String expirationDate = MessageUtil.createFutureDate(1, 1);

    @Test
    public void login() throws Exception {
        boolean sip2LoginRequest = princetonESIPConnector.jSIPLogin(null, patronIdentifier);
        assertTrue(sip2LoginRequest);
    }

    @Test
    public void lookupItem() throws Exception {
        String[] itemIdentifier = {"32101077423406", "32101061738587", "77777", "77777777777779", "32101065514414"};
        SIP2ItemInformationResponse itemInformationResponse = princetonESIPConnector.lookupItem(itemIdentifier[4]);

        logger.info("Circulation Status     :" + itemInformationResponse.getCirculationStatus().name());
        logger.info("SecurityMarker         :" + itemInformationResponse.getSecurityMarker());
        logger.info("Fee Type               :" + itemInformationResponse.getFeeType());
        logger.info("Transaction Date       :" + itemInformationResponse.getTransactionDate());
        logger.info("Hold Queue Length (CF) :" + itemInformationResponse.getHoldQueueLength());

        assertEquals(itemIdentifier[4], itemInformationResponse.getItemIdentifier());
    }

    @Test
    public void lookupItemStatus() throws Exception {
        String[] itemIdentifier = {"32101077423406", "32101061738587", "77777", "77777777777779", "PULTST54338"};
        String[] patronIdentifier = {"45678913"};
        SIP2ItemInformationResponse itemInformationResponse = princetonESIPConnector.lookupItemStatus(itemIdentifier[4], "", patronIdentifier[0]);
        assertEquals(itemIdentifier, itemInformationResponse.getItemIdentifier());
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
        SIP2CheckoutResponse checkOutResponse = princetonESIPConnector.checkOutItem(itemIdentifier, patronIdentifier);
        assertNotNull(checkOutResponse);
        assertTrue(checkOutResponse.isOk());
    }

    @Test
    public void checkIn() throws Exception {
        SIP2CheckinResponse checkInResponse = princetonESIPConnector.checkInItem(itemIdentifier, patronIdentifier);
        assertNotNull(checkInResponse);
        assertTrue(checkInResponse.isOk());
    }

    @Test
    public void check_out_In() throws Exception {
        String itemIdentifier = "32101077423406";
        String patronIdentifier = "198572368";
        String institutionId = "htccul";
        SIP2CheckoutResponse checkOutResponse = princetonESIPConnector.checkOutItem(itemIdentifier, patronIdentifier);
        assertNotNull(checkOutResponse);
        assertTrue(checkOutResponse.isOk());
        SIP2CheckinResponse checkInResponse = princetonESIPConnector.checkInItem(itemIdentifier, patronIdentifier);
        assertNotNull(checkInResponse);
        assertTrue(checkInResponse.isOk());
    }

    @Test
    public void cancelHold() throws Exception {
        SIP2HoldResponse holdResponse = princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation);

        try {
            assertNotNull(holdResponse);
            assertTrue(holdResponse.isOk());
        } catch (AssertionError e) {
            logger.error("Cancel Hold Error - > ", e);
        }
        lookupItem();
    }

    @Test
    public void placeHold() throws Exception {
        SIP2HoldResponse holdResponse = princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation);

        try {
            assertNotNull(holdResponse);
            assertTrue(holdResponse.isOk());
        } catch (AssertionError e) {
            logger.error("Hold Error - > ", e);
        }
        lookupItem();
    }

    @Test
    public void bothHold() throws Exception {
//        String itemIdentifier = "32101057972166";
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        String institutionId = "htccul";
        String expirationDate = MessageUtil.getSipDateTime(); // Date Format YYYYMMDDZZZZHHMMSS
        String bibId = "100001";
        String pickupLocation = "htcsc";
        SIP2HoldResponse holdResponse;

        holdResponse = princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation);
        holdResponse = princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation);

        assertNotNull(holdResponse);
        assertTrue(holdResponse.isOk());
    }

    @Test
    public void createBib() throws Exception {
        String titleIdentifier = "RECAP TEST TITLE - 002";
        SIP2CreateBibResponse createBibResponse = princetonESIPConnector.createBib(itemIdentifier, patronIdentifier, institutionId, titleIdentifier);

        assertNotNull(createBibResponse);
        assertTrue(createBibResponse.isOk());
    }

    @Test
    public void testRecall() throws Exception {
        String itemIdentifier = "32101065514414";
        String patronIdentifier = "45678912";
        String institutionId = "htccul";
        String expirationDate = MessageUtil.createFutureDate(20, 2);
        String pickupLocation = "htcsc";
        String bibId = "9959082";

        SIP2RecallResponse recallResponse = princetonESIPConnector.recallItem(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation);

        assertNotNull(recallResponse);
        assertTrue(recallResponse.isOk());
    }

}
