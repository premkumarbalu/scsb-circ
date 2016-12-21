package org.recap.ils;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseException;
import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.messages.SIP2MessageResponse;
import com.pkrete.jsip2.messages.responses.*;
import com.pkrete.jsip2.parser.SIP2CreateBibResponseParser;
import com.pkrete.jsip2.util.MessageUtil;
import org.junit.Test;
import org.recap.BaseTestCase;
import com.pkrete.jsip2.messages.response.SIP2CreateBibResponse;
import com.pkrete.jsip2.messages.response.SIP2RecallResponse;
import org.recap.ils.model.ItemInformationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by saravanakumarp on 28/9/16.
 */
public class ColumbiaJSIPConnectorUT extends BaseTestCase {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ColumbiaJSIPConnector columbiaJSIPConnector;

    private String[] itemIdentifier = {"CU55724132", "CULTST12345","MR68799284","1000534323"," CU65897706","CULTST52345"};


    private String patronIdentifier = "RECAPTST01";
    private String institutionId = "";

    @Test
    public void login() throws Exception {
        boolean sip2LoginRequest = columbiaJSIPConnector.jSIPLogin(null,patronIdentifier);
        assertTrue(sip2LoginRequest);
    }

    @Test
    public void lookupItem() throws Exception {

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
        String patronIdentifier = "RECAPTST01";
        String institutionId = "htccul";
        SIP2PatronStatusResponse patronInformationResponse = columbiaJSIPConnector.lookupUser(institutionId, patronIdentifier);
        assertNotNull(patronInformationResponse);
//        assertTrue(patronInformationResponse.isValid());
    }

    @Test
    public void checkout() throws Exception { // CULTST11345 , CULTST13345 ,
        String itemIdentifier = this.itemIdentifier[4];
        String patronIdentifier = "RECAPPUL01";
        String institutionId = "";
        SIP2CheckoutResponse checkOutResponse = columbiaJSIPConnector.checkOutItem(itemIdentifier, patronIdentifier);
        lookupItem();
        assertNotNull(checkOutResponse);
        assertTrue(checkOutResponse.isOk());
    }

    @Test
    public void checkIn() throws Exception {
        String itemIdentifier = this.itemIdentifier[5];
        String patronIdentifier = "RECAPPUL01";
        String institutionId = "";
        SIP2CheckinResponse checkInResponse = columbiaJSIPConnector.checkInItem(itemIdentifier,patronIdentifier);
        lookupItem();
        assertNotNull(checkInResponse);
        assertTrue(checkInResponse.isOk());
    }

    @Test
    public void check_out_In() throws Exception {
        String itemIdentifier = "";
        String patronIdentifier = "";
        String institutionId = "";
        SIP2CheckoutResponse checkOutResponse = columbiaJSIPConnector.checkOutItem(itemIdentifier, patronIdentifier);
        assertNotNull(checkOutResponse);
        assertTrue(checkOutResponse.isOk());
        SIP2CheckinResponse checkInResponse = columbiaJSIPConnector.checkInItem(itemIdentifier,patronIdentifier);
        assertNotNull(checkInResponse);
        assertTrue(checkInResponse.isOk());
    }

    @Test
    public void cancelHold() throws Exception {
        String itemIdentifier = this.itemIdentifier[1];;
        String patronIdentifier = "RECAPTST01";
        String institutionId = "";
        String expirationDate =MessageUtil.createFutureDate(20,2);
        String bibId="12040033";
        String pickupLocation="CIRCrecap";

        SIP2HoldResponse holdResponse = columbiaJSIPConnector.cancelHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation);
        lookupItem();

        assertNotNull(holdResponse);
        assertTrue(holdResponse.isOk());
    }

    @Test
    public void placeHold() throws Exception {
        String itemIdentifier = this.itemIdentifier[4];
        String patronIdentifier = "RECAPTST01";
        String institutionId = "recaptestreg";
        String expirationDate =MessageUtil.createFutureDate(20,2);
        String bibId="12040033";
        String pickupLocation="CIRCrecap";

        SIP2HoldResponse holdResponse = columbiaJSIPConnector.placeHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation);
        lookupItem();

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

        holdResponse = columbiaJSIPConnector.placeHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation);
        holdResponse = columbiaJSIPConnector.cancelHold(itemIdentifier, patronIdentifier,institutionId ,expirationDate,bibId,pickupLocation);

        assertNotNull(holdResponse);
        assertTrue(holdResponse.isOk());
    }

    @Test
    public void createBib() throws Exception {
        String itemIdentifier = "888888";
        String patronIdentifier = "RECAPTST01";
        String institutionId = "";
        String titleIdentifier ="Recap Testing 8888";
        String bibId ="";
        SIP2CreateBibResponse createBibResponse;

        createBibResponse = columbiaJSIPConnector.createBib(itemIdentifier,patronIdentifier,institutionId ,titleIdentifier);

        assertNotNull(createBibResponse);
        assertTrue(createBibResponse.isOk());
    }

    @Test
    public void testRecall() throws Exception {
        String itemIdentifier = this.itemIdentifier[4];
        String patronIdentifier = "RECAPTST03";
        String institutionId = "recaptestgrd";
        String expirationDate = MessageUtil.createFutureDate(20,2);
        String pickupLocation="CIRCrecap";
        String bibId="12040033";

        SIP2RecallResponse recallResponse = columbiaJSIPConnector.recallItem(itemIdentifier, patronIdentifier,institutionId ,expirationDate,pickupLocation,bibId);

//        assertNotNull(recallResponse);
//        assertTrue(recallResponse.isOk());
    }


    @Test
    public void testCreatebibParser(){
        //44444444444444
        String createBibEsipResponse="821MJ8967832|MA12040035|AF|";
        // 3333333333
        // 821MJ|MA12040036|AF|
        // 6666666
        // 821MJ8967833|MA12040037|AF|
        // 888888
        // 821MJ8967834|MA12040038|AF|

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
}
