package org.recap.ils;

import com.pkrete.jsip2.messages.response.SIP2CreateBibResponse;
import com.pkrete.jsip2.messages.responses.SIP2ItemInformationResponse;
import com.pkrete.jsip2.util.MessageUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ils.model.response.*;
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

    String[] itemIds = {"32101077423406", "32101061738587", "77777", "77777777777779", "32101065514414","32101057972166","PULTST54329"};
    private String itemIdentifier = "32101077423406";
    private String patronIdentifier = "45678912";
    private String[] patronId = {"45678912","45678913","45678915"};
    private String pickupLocation = "rcpcirc";
    private String bibId = "9959052";
    private String institutionId = "htccul";
    private String itemInstitutionId = "";
    private String expirationDate = MessageUtil.createFutureDate(1, 1);

    @Test
    public void login() throws Exception {
        boolean sip2LoginRequest = princetonESIPConnector.jSIPLogin(null, patronIdentifier);
        assertTrue(sip2LoginRequest);
    }

    @Test
    public void lookupItem() throws Exception {
        String[] itemIdentifiers = {"32101077423406", "32101061738587", "77777", "77777777777779", "32101065514414","32101057972166"};
        ItemInformationResponse itemInformationResponse = (ItemInformationResponse)princetonESIPConnector.lookupItem(this.itemIdentifier);

        logger.info("\n\n");
        logger.info("Item barcode           : " + itemInformationResponse.getItemBarcode());
        logger.info("Circulation Status     : " + itemInformationResponse.getCirculationStatus());
        logger.info("SecurityMarker         : " + itemInformationResponse.getSecurityMarker());
        logger.info("Fee Type               : " + itemInformationResponse.getFeeType());
        logger.info("Transaction Date       : " + itemInformationResponse.getTransactionDate());
        logger.info("Hold Queue Length (CF) : " + itemInformationResponse.getHoldQueueLength());

        assertEquals(this.itemIdentifier,itemInformationResponse.getItemBarcode());
    }

    @Ignore
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
        PatronInformationResponse patronInformationResponse =  (PatronInformationResponse) princetonESIPConnector.lookupPatron(patronIdentifier);
        assertNotNull(patronInformationResponse);
    }

    @Test
    public void checkout() throws Exception {
        ItemCheckoutResponse itemCheckoutResponse =(ItemCheckoutResponse)princetonESIPConnector.checkOutItem(itemIdentifier, patronIdentifier);
        assertNotNull(itemCheckoutResponse);
        assertTrue(itemCheckoutResponse.isSuccess());
        lookupItem();
    }

    @Test
    public void checkIn() throws Exception {
        ItemCheckinResponse itemCheckinResponse = (ItemCheckinResponse)princetonESIPConnector.checkInItem(itemIdentifier, patronIdentifier);
        assertNotNull(itemCheckinResponse);
        assertTrue(itemCheckinResponse.isSuccess());
        lookupItem();
    }

    @Test
    public void check_out_In() throws Exception {
        String itemIdentifier = "32101077423406";
        String patronIdentifier = "198572368";
        ItemCheckoutResponse itemCheckoutResponse = (ItemCheckoutResponse)princetonESIPConnector.checkOutItem(this.itemIdentifier, this.patronIdentifier);
        assertNotNull(itemCheckoutResponse);
        assertTrue(itemCheckoutResponse.isSuccess());
        lookupItem();
        ItemCheckinResponse itemCheckinResponse = (ItemCheckinResponse)princetonESIPConnector.checkInItem(this.itemIdentifier, this.patronIdentifier);
        assertNotNull(itemCheckinResponse);
        assertTrue(itemCheckinResponse.isSuccess());
        lookupItem();
    }

    @Test
    public void cancelHold() throws Exception {
        ItemHoldResponse holdResponse = (ItemHoldResponse) princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation,null);

        try {
            assertNotNull(holdResponse);
            assertTrue(holdResponse.isSuccess());
        } catch (AssertionError e) {
            logger.error("Cancel Hold Error - > ", e);
        }
        lookupItem();
    }

    @Test
    public void placeHold() throws Exception {
        ItemHoldResponse holdResponse = (ItemHoldResponse)  princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier, institutionId, itemInstitutionId,  expirationDate, bibId, pickupLocation,null,null,null,null);

        try {
            assertNotNull(holdResponse);
            assertTrue(holdResponse.isSuccess());
        } catch (AssertionError e) {
            logger.error("Hold Error - > ", e);
        }
        lookupItem();
    }

    @Test
    public void bothHold() throws Exception {
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        String institutionId = "htccul";
        String itemInstitutionId = "";
        String expirationDate = MessageUtil.getSipDateTime(); // Date Format YYYYMMDDZZZZHHMMSS
        String bibId = "100001";
        String pickupLocation = "htcsc";
        ItemHoldResponse holdResponse;

        holdResponse = (ItemHoldResponse) princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation,null);
        holdResponse = (ItemHoldResponse)  princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier, institutionId, itemInstitutionId, expirationDate, bibId, pickupLocation,null,null,null,null);

        assertNotNull(holdResponse);
        assertTrue(holdResponse.isSuccess());
    }

    @Test
    public void createBib() throws Exception {
        String itemIdentifier = " CU69277435";
        String patronIdentifier = "45678915";
        String titleIdentifier = "";
        String institutionId = "htccul";
        ItemCreateBibResponse itemCreateBibResponse = princetonESIPConnector.createBib(itemIdentifier, patronIdentifier, institutionId, titleIdentifier);

        assertNotNull(itemCreateBibResponse);
        assertTrue(itemCreateBibResponse.isSuccess());
    }

    @Test
    public void testRecall() throws Exception {
        String itemIdentifier = "32101065514414";
        String patronIdentifier = "45678912";
        String institutionId = "htccul";
        String expirationDate = MessageUtil.createFutureDate(20, 2);
        String pickupLocation = "htcsc";
        String bibId = "9959082";

        ItemRecallResponse itemRecallResponse= princetonESIPConnector.recallItem(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation);

        assertNotNull(itemRecallResponse);
        assertTrue(itemRecallResponse.isSuccess());
    }

}
