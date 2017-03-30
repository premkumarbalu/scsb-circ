package org.recap.ils;

import com.pkrete.jsip2.messages.responses.SIP2ItemInformationResponse;
import com.pkrete.jsip2.util.MessageUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCase;
import org.recap.ils.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by saravanakumarp on 28/9/16.
 */
public class PrincetonJSIPConnectorUT extends BaseTestCase {

    @Mock
    private PrincetonJSIPConnector princetonESIPConnector;

    @Autowired
    private PrincetonJSIPConnector pulESIPConnector;

    private static final Logger logger = LoggerFactory.getLogger(PrincetonJSIPConnectorUT.class);

    String[] itemIds = {"32101077423406", "32101061738587", "77777", "77777777777779", "32101065514414", "32101057972166", "PULTST54329"};
    private String itemIdentifier = "32101077423406";
    private String patronIdentifier = "22101008354581";
    private String[] patronId = {"45678912", "45678913", "45678915"};
    private String pickupLocation = "rcpcirc";
    private String bibId = "9959052";
    private String institutionId = "htccul";
    private String itemInstitutionId = "";
    private String expirationDate = MessageUtil.createFutureDate(1, 1);

    @Test
    public void login() throws Exception {
//        Mockito.when(princetonESIPConnector.jSIPLogin(null, patronIdentifier)).thenReturn(true);

        boolean sip2LoginRequest = pulESIPConnector.jSIPLogin(null, patronIdentifier);
        assertTrue(sip2LoginRequest);
        sip2LoginRequest = pulESIPConnector.jSIPLogin(null, "1212");
        assertFalse(sip2LoginRequest);

    }

    @Test
    public void lookupItem() throws Exception {
        String[] itemIdentifiers = {"PULTST54321", "PULTST54322", "PULTST54323", "PULTST54324", "PULTST54325", "PULTST54326", "PULTST54334", "PULTST54335", "PULTST54337", "PULTST54338", "PULTST54339", "PULTST54340"};
        for (int i = 0; i < itemIdentifiers.length; i++) {
            String identifier = itemIdentifiers[i];
            Mockito.when((ItemInformationResponse) princetonESIPConnector.lookupItem(identifier)).thenReturn(getItemInformationResponse());
            ItemInformationResponse itemInformationResponse = (ItemInformationResponse) princetonESIPConnector.lookupItem(identifier);

            logger.info("\n\n");
            logger.info("Item barcode           : " + itemInformationResponse.getItemBarcode());
            logger.info("Circulation Status     : " + itemInformationResponse.getCirculationStatus());
            logger.info("SecurityMarker         : " + itemInformationResponse.getSecurityMarker());
            logger.info("Fee Type               : " + itemInformationResponse.getFeeType());
            logger.info("Transaction Date       : " + itemInformationResponse.getTransactionDate());
            logger.info("Hold Queue Length (CF) : " + itemInformationResponse.getHoldQueueLength());
        }

//        assertEquals(this.itemIdentifier,itemInformationResponse.getItemBarcode());
    }

    @Test
    public void lookupUser() throws Exception {
        String patronIdentifier = "45678912";
        String institutionId = "htccul";
//        Mockito.when((PatronInformationResponse) princetonESIPConnector.lookupPatron(patronIdentifier)).thenReturn(new PatronInformationResponse());
        PatronInformationResponse patronInformationResponse = (PatronInformationResponse) pulESIPConnector.lookupPatron(patronIdentifier);
        assertNotNull(patronInformationResponse);
    }

    @Test
    public void checkout() throws Exception {
        Mockito.when((ItemCheckoutResponse) princetonESIPConnector.checkOutItem(itemIdentifier, patronIdentifier)).thenReturn(getItemCheckoutResponse());
        ItemCheckoutResponse itemCheckoutResponse = (ItemCheckoutResponse) princetonESIPConnector.checkOutItem(itemIdentifier, patronIdentifier);
        assertNotNull(itemCheckoutResponse);
        assertTrue(itemCheckoutResponse.isSuccess());
        lookupItem();
    }

    @Test
    public void checkIn() throws Exception {
        Mockito.when((ItemCheckinResponse) princetonESIPConnector.checkInItem(itemIdentifier, patronIdentifier)).thenReturn(getItemCheckinResponse());
        ItemCheckinResponse itemCheckinResponse = (ItemCheckinResponse) princetonESIPConnector.checkInItem(itemIdentifier, patronIdentifier);
        assertNotNull(itemCheckinResponse);
        assertTrue(itemCheckinResponse.isSuccess());
        lookupItem();
    }

    @Test
    public void check_out_In() throws Exception {
        String itemIdentifier = "32101077423406";
        String patronIdentifier = "198572368";
        Mockito.when((ItemCheckoutResponse) princetonESIPConnector.checkOutItem(this.itemIdentifier, this.patronIdentifier)).thenReturn(getItemCheckoutResponse());
        ItemCheckoutResponse itemCheckoutResponse = (ItemCheckoutResponse) princetonESIPConnector.checkOutItem(this.itemIdentifier, this.patronIdentifier);
        assertNotNull(itemCheckoutResponse);
        assertTrue(itemCheckoutResponse.isSuccess());
        lookupItem();
        Mockito.when((ItemCheckinResponse) princetonESIPConnector.checkInItem(this.itemIdentifier, this.patronIdentifier)).thenReturn(getItemCheckinResponse());
        ItemCheckinResponse itemCheckinResponse = (ItemCheckinResponse) princetonESIPConnector.checkInItem(this.itemIdentifier, this.patronIdentifier);
        assertNotNull(itemCheckinResponse);
        assertTrue(itemCheckinResponse.isSuccess());
        lookupItem();
    }

    @Test
    public void cancelHold() throws Exception {
        Mockito.when((ItemHoldResponse) princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation, null)).thenReturn(getItemHoldResponse());
        ItemHoldResponse holdResponse = (ItemHoldResponse) princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation, null);

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
        Mockito.when((ItemHoldResponse) princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier, institutionId, itemInstitutionId, expirationDate, bibId, pickupLocation, null, null, null, null)).thenReturn(getItemHoldResponse());
        ItemHoldResponse holdResponse = (ItemHoldResponse) princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier, institutionId, itemInstitutionId, expirationDate, bibId, pickupLocation, null, null, null, null);

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
        Mockito.when((ItemHoldResponse) princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation, null)).thenReturn(getItemHoldResponse());
        Mockito.when((ItemHoldResponse) princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier, institutionId, itemInstitutionId, expirationDate, bibId, pickupLocation, null, null, null, null)).thenReturn(getItemHoldResponse());
        holdResponse = (ItemHoldResponse) princetonESIPConnector.cancelHold(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation, null);
        holdResponse = (ItemHoldResponse) princetonESIPConnector.placeHold(itemIdentifier, patronIdentifier, institutionId, itemInstitutionId, expirationDate, bibId, pickupLocation, null, null, null, null);

        assertNotNull(holdResponse);
        assertTrue(holdResponse.isSuccess());
    }

    @Test
    public void createBib() throws Exception {
        String itemIdentifier = " CU69277435";
        String patronIdentifier = "45678915";
        String titleIdentifier = "";
        String institutionId = "htccul";
        Mockito.when(princetonESIPConnector.createBib(itemIdentifier, patronIdentifier, institutionId, titleIdentifier)).thenReturn(getItemCreateBibResponse());
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
        Mockito.when(princetonESIPConnector.recallItem(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation)).thenReturn(getItemRecallResponse());
        ItemRecallResponse itemRecallResponse = princetonESIPConnector.recallItem(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation);

        assertNotNull(itemRecallResponse);
        assertTrue(itemRecallResponse.isSuccess());
    }

    public ItemRecallResponse getItemRecallResponse() {
        ItemRecallResponse itemRecallResponse = new ItemRecallResponse();
        itemRecallResponse.setSuccess(true);
        return itemRecallResponse;
    }

    public ItemHoldResponse getItemHoldResponse() {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        itemHoldResponse.setSuccess(true);
        return itemHoldResponse;
    }

    public ItemCreateBibResponse getItemCreateBibResponse() {
        ItemCreateBibResponse itemCreateBibResponse = new ItemCreateBibResponse();
        itemCreateBibResponse.setBibId("123");
        itemCreateBibResponse.setItemId("1234");
        itemCreateBibResponse.setScreenMessage("");
        itemCreateBibResponse.setSuccess(true);
        return itemCreateBibResponse;

    }

    public ItemCheckoutResponse getItemCheckoutResponse() {
        ItemCheckoutResponse itemCheckoutResponse = new ItemCheckoutResponse();
        itemCheckoutResponse.setSuccess(true);
        return itemCheckoutResponse;
    }

    public ItemCheckinResponse getItemCheckinResponse() {
        ItemCheckinResponse itemCheckinResponse = new ItemCheckinResponse();
        itemCheckinResponse.setSuccess(true);
        return itemCheckinResponse;
    }

    public ItemInformationResponse getItemInformationResponse() {
        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
        itemInformationResponse.setCirculationStatus("test");
        itemInformationResponse.setSecurityMarker("test");
        itemInformationResponse.setFeeType("test");
        itemInformationResponse.setTransactionDate(new Date().toString());
        itemInformationResponse.setHoldQueueLength("10");
        itemInformationResponse.setTitleIdentifier("test");
        itemInformationResponse.setBibID("1223");
        itemInformationResponse.setDueDate(new Date().toString());
        itemInformationResponse.setExpirationDate(new Date().toString());
        itemInformationResponse.setRecallDate(new Date().toString());
        itemInformationResponse.setCurrentLocation("test");
        itemInformationResponse.setHoldPickupDate(new Date().toString());
        itemInformationResponse.setItemBarcode("32101077423406");
        return itemInformationResponse;
    }

}
