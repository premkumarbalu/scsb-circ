package org.recap.ils;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseException;
import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.messages.SIP2MessageResponse;
import com.pkrete.jsip2.parser.SIP2CreateBibResponseParser;
import com.pkrete.jsip2.util.MessageUtil;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCase;
import org.recap.ils.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by saravanakumarp on 28/9/16.
 */
public class ColumbiaJSIPConnectorUT extends BaseTestCase {

    private static final Logger logger = LoggerFactory.getLogger(ColumbiaJSIPConnectorUT.class);

    @Mock
    private ColumbiaJSIPConnector columbiaJSIPConnector;
    private String[] itemIdentifier = {"CU55724132", "CULTST12345","MR68799284","1000534323"," CU65897706","CULTST52345","CULTST11345","CULTST13345"};
    private String[] patrons = {"RECAPTST01","RECAPTST02","RECAPTST02","RECAPPUL01"};


    private String patronIdentifier = "RECAPTST01";
    private String institutionId = "";

    @Test
    public void login() throws Exception {
        Mockito.when(columbiaJSIPConnector.jSIPLogin(null,patronIdentifier)).thenReturn(true);
        boolean sip2LoginRequest = columbiaJSIPConnector.jSIPLogin(null,patronIdentifier);
        assertTrue(sip2LoginRequest);
    }

    @Test
    public void lookupItem() throws Exception {
        Mockito.when((ItemInformationResponse)columbiaJSIPConnector.lookupItem(itemIdentifier[4])).thenReturn(getItemInformationResponse());
        ItemInformationResponse itemInformationResponse = (ItemInformationResponse)columbiaJSIPConnector.lookupItem(itemIdentifier[4]);
        logger.info("");
        logger.info("Circulation Status     :" + itemInformationResponse.getCirculationStatus());
        logger.info("Security Marker        :" + itemInformationResponse.getSecurityMarker());
        logger.info("Fee Type               :" + itemInformationResponse.getFeeType());
        logger.info("Transaction Date       :" + itemInformationResponse.getTransactionDate());
        logger.info("Hold Queue Length (CF) :" + itemInformationResponse.getHoldQueueLength());
        logger.info("Title                  :" + itemInformationResponse.getTitleIdentifier());
        logger.info("BibId                  :" + itemInformationResponse.getBibID());
        logger.info("DueDate                :" + itemInformationResponse.getDueDate());
        logger.info("Expiration Date        :" + itemInformationResponse.getExpirationDate());
        logger.info("Recall Date            :" + itemInformationResponse.getRecallDate());
        logger.info("Current Location       :" + itemInformationResponse.getCurrentLocation());
        logger.info("Hold Pickup Date       :" + itemInformationResponse.getHoldPickupDate());

    }

    @Test
    public void lookupUser() throws Exception {
        String patronIdentifier = "RECAPTST03";
        Mockito.when((PatronInformationResponse) columbiaJSIPConnector.lookupPatron(patronIdentifier)).thenReturn(getPatronInformationResponse());
        PatronInformationResponse patronInformationResponse =  (PatronInformationResponse) columbiaJSIPConnector.lookupPatron(patronIdentifier);
        logger.info("Patron Info      :" + patronInformationResponse.getScreenMessage());
        assertNotNull(patronInformationResponse);
    }

    @Test
    public void checkout() throws Exception {
        String itemIdentifier = this.itemIdentifier[4];
        String patronIdentifier = "RECAPPUL01";
        String institutionId = "";
        Mockito.when((ItemCheckoutResponse)columbiaJSIPConnector.checkOutItem(itemIdentifier, patronIdentifier)).thenReturn(getItemCheckoutResponse());
        ItemCheckoutResponse itemCheckoutResponse = (ItemCheckoutResponse)columbiaJSIPConnector.checkOutItem(itemIdentifier, patronIdentifier);
        lookupItem();
        assertNotNull(itemCheckoutResponse);
        assertTrue(itemCheckoutResponse.isSuccess());
    }

    @Test
    public void checkIn() throws Exception {
        String itemIdentifier = this.itemIdentifier[4];
        String patronIdentifier = "RECAPPUL01";
        String institutionId = "";
        Mockito.when((ItemCheckinResponse)columbiaJSIPConnector.checkInItem(itemIdentifier, patronIdentifier)).thenReturn(getItemCheckinResponse());
        ItemCheckinResponse itemCheckinResponse = (ItemCheckinResponse)columbiaJSIPConnector.checkInItem(itemIdentifier, patronIdentifier);
        lookupItem();
        assertNotNull(itemCheckinResponse);
        assertTrue(itemCheckinResponse.isSuccess());
    }

    @Test
    public void check_out_In() throws Exception {
        String itemIdentifier = "CULTST12345";
        String patronIdentifier = "RECAPTST01";
        String institutionId = "recaptestreg";
        Mockito.when((ItemCheckoutResponse)columbiaJSIPConnector.checkOutItem(itemIdentifier, patronIdentifier)).thenReturn(getItemCheckoutResponse());
        ItemCheckoutResponse itemCheckoutResponse = (ItemCheckoutResponse)columbiaJSIPConnector.checkOutItem(itemIdentifier, patronIdentifier);
        assertNotNull(itemCheckoutResponse);
        assertTrue(itemCheckoutResponse.isSuccess());
        Mockito.when((ItemCheckinResponse)columbiaJSIPConnector.checkInItem(itemIdentifier, patronIdentifier)).thenReturn(getItemCheckinResponse());
        ItemCheckinResponse itemCheckinResponse = (ItemCheckinResponse)columbiaJSIPConnector.checkInItem(itemIdentifier, patronIdentifier);
        lookupItem();
        assertNotNull(itemCheckinResponse);
        assertTrue(itemCheckinResponse.isSuccess());
    }

    @Test
    public void cancelHold() throws Exception {
        String itemIdentifier = this.itemIdentifier[1];;
        String patronIdentifier = "RECAPTST01";
        String callInstitutionId = "";
        String itemInstitutionId = "";
        String expirationDate =MessageUtil.createFutureDate(20,2);
        String bibId="12040033";
        String pickupLocation="CIRCrecap";
        Mockito.when((ItemHoldResponse) columbiaJSIPConnector.placeHold(itemIdentifier, patronIdentifier, callInstitutionId, itemInstitutionId, expirationDate,bibId,pickupLocation, null, null, null, null)).thenReturn(getItemHoldResponse());
        ItemHoldResponse holdResponse = (ItemHoldResponse) columbiaJSIPConnector.placeHold(itemIdentifier, patronIdentifier, callInstitutionId, itemInstitutionId, expirationDate,bibId,pickupLocation, null, null, null, null);
        lookupItem();

        assertNotNull(holdResponse);
        assertTrue(holdResponse.isSuccess());
    }

    @Test
    public void placeHold() throws Exception {
        String itemIdentifier = this.itemIdentifier[4];
        String patronIdentifier = "RECAPTST01";
        String institutionId = "recaptestreg";
        String expirationDate = MessageUtil.createFutureDate(20,2);
        String bibId= "12040033";
        String pickupLocation="CIRCrecap";
        Mockito.when((ItemHoldResponse) columbiaJSIPConnector.cancelHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation, null)).thenReturn(getItemHoldResponse());
        ItemHoldResponse holdResponse = (ItemHoldResponse) columbiaJSIPConnector.cancelHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation, null);
        lookupItem();

        assertNotNull(holdResponse);
        assertTrue(holdResponse.isSuccess());
    }

    @Test
    public void bothHold() throws Exception {
        String itemIdentifier = "32101095533293";
        String patronIdentifier = "198572368";
        String callInstitutionId = "htccul";
        String itemInstitutionId = "";
        String expirationDate =MessageUtil.getSipDateTime(); // Date Format YYYYMMDDZZZZHHMMSS
        String bibId="100001";
        String pickupLocation="htcsc";
        ItemHoldResponse holdResponse;
        Mockito.when((ItemHoldResponse) columbiaJSIPConnector.placeHold(itemIdentifier, patronIdentifier, callInstitutionId, itemInstitutionId, expirationDate,bibId,pickupLocation, null, null, null, null)).thenReturn(getItemHoldResponse());
        Mockito.when((ItemHoldResponse) columbiaJSIPConnector.cancelHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation, null)).thenReturn(getItemHoldResponse());
        holdResponse = (ItemHoldResponse) columbiaJSIPConnector.placeHold(itemIdentifier, patronIdentifier, callInstitutionId, itemInstitutionId, expirationDate,bibId,pickupLocation, null, null, null, null);
        holdResponse = (ItemHoldResponse) columbiaJSIPConnector.cancelHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation, null);

        assertNotNull(holdResponse);
        assertTrue(holdResponse.isSuccess());
    }

    @Test
    public void createBib() throws Exception {
        String itemIdentifier = "888888";
        String patronIdentifier = "RECAPTST01";
        String institutionId = "";
        String titleIdentifier ="Recap Testing 8888";
        String bibId ="";
        ItemCreateBibResponse itemCreateBibResponse;
        Mockito.when(columbiaJSIPConnector.createBib(itemIdentifier,patronIdentifier,institutionId ,titleIdentifier)).thenReturn(getItemCreateBibResponse());
        itemCreateBibResponse= columbiaJSIPConnector.createBib(itemIdentifier,patronIdentifier,institutionId ,titleIdentifier);

        logger.info(itemCreateBibResponse.getBibId());
        logger.info(itemCreateBibResponse.getItemId());
        logger.info(itemCreateBibResponse.getScreenMessage());

        assertNotNull(itemCreateBibResponse);
        assertTrue(itemCreateBibResponse.isSuccess());
    }

    @Test
    public void testRecall() throws Exception {
        String itemIdentifier = this.itemIdentifier[4];
        String patronIdentifier = "RECAPTST03";
        String institutionId = "recaptestgrd";
        String expirationDate = MessageUtil.createFutureDate(20,2);
        String pickupLocation="CIRCrecap";
        String bibId="12040033";
        Mockito.when(columbiaJSIPConnector.recallItem(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation)).thenReturn(getItemRecallResponse());
        ItemRecallResponse itemRecallResponse= columbiaJSIPConnector.recallItem(itemIdentifier, patronIdentifier, institutionId, expirationDate, bibId, pickupLocation);
        assertNotNull(itemRecallResponse);
        assertTrue(itemRecallResponse.isSuccess());
    }


    @Test
    public void testCreatebibParser(){
        String createBibEsipResponse="821MJ8967832|MA12040035|AF|";
        SIP2CreateBibResponseParser sip2CreateBibResponseParser = new SIP2CreateBibResponseParser();
        try {
            SIP2MessageResponse sIP2MessageResponse =sip2CreateBibResponseParser.parse(createBibEsipResponse);
            if(sIP2MessageResponse.getScreenMessage().size()>0) {
                logger.info("" + sIP2MessageResponse.getScreenMessage().get(0));
            }
            logger.info(sIP2MessageResponse.getItemIdentifier());
            logger.info(sIP2MessageResponse.getBibId());
        } catch (InvalidSIP2ResponseValueException e) {
            e.printStackTrace();
        } catch (InvalidSIP2ResponseException e) {
            e.printStackTrace();
        }
    }

    public ItemCheckoutResponse getItemCheckoutResponse(){
        ItemCheckoutResponse itemCheckoutResponse = new ItemCheckoutResponse();
        itemCheckoutResponse.setSuccess(true);
        return itemCheckoutResponse;
    }

    public ItemCheckinResponse getItemCheckinResponse(){
        ItemCheckinResponse itemCheckinResponse = new ItemCheckinResponse();
        itemCheckinResponse.setSuccess(true);
        return itemCheckinResponse;
    }

    public ItemInformationResponse getItemInformationResponse(){
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
        return itemInformationResponse;
    }

    public ItemHoldResponse getItemHoldResponse(){
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        itemHoldResponse.setSuccess(true);
        return itemHoldResponse;
    }

    public ItemCreateBibResponse getItemCreateBibResponse(){
        ItemCreateBibResponse itemCreateBibResponse = new ItemCreateBibResponse();
        itemCreateBibResponse.setBibId("123");
        itemCreateBibResponse.setItemId("1234");
        itemCreateBibResponse.setScreenMessage("");
        itemCreateBibResponse.setSuccess(true);
        return itemCreateBibResponse;

    }

    public ItemRecallResponse getItemRecallResponse(){
        ItemRecallResponse itemRecallResponse = new ItemRecallResponse();
        itemRecallResponse.setSuccess(true);
        return itemRecallResponse;
    }

    public PatronInformationResponse getPatronInformationResponse(){
        PatronInformationResponse patronInformationResponse = new PatronInformationResponse();
        patronInformationResponse.setScreenMessage("Patron validated successfully.");
        return patronInformationResponse;
    }


}
