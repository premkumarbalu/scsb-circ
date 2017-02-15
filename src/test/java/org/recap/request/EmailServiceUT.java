package org.recap.request;

import org.apache.camel.ProducerTemplate;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.camel.EmailPayLoad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by sudhishk on 19/1/17.
 */
public class EmailServiceUT extends BaseTestCase{

    @Autowired
    EmailService emailService;

    @Test
    public void testRecalEmail(){
        emailService.recallEmail(ReCAPConstants.NYPL,"NYPLTST67891","A history of the Burmah Oil Company","NoPatron");
        emailService.recallEmail(ReCAPConstants.COLUMBIA,"CULTST42345","Changing contours of Asian agriculture","RECAPTST01");
        emailService.recallEmail(ReCAPConstants.PRINCETON,"PULTST54323","1863 laws of war","45678912");
    }
}
