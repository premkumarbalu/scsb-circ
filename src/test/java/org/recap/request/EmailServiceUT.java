package org.recap.request;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by sudhishk on 19/1/17.
 */
public class EmailServiceUT extends BaseTestCase {

    @Autowired
    EmailService emailService;

    @Test
    public void testRecalEmail() {
        emailService.sendEmail(ReCAPConstants.NYPL, "NYPLTST67891", "A history of the Burmah Oil Company", "NoPatron", ReCAPConstants.NYPL,"");
        emailService.sendEmail(ReCAPConstants.COLUMBIA, "CULTST42345", "Changing contours of Asian agriculture", "RECAPTST01", ReCAPConstants.COLUMBIA,"");
        emailService.sendEmail(ReCAPConstants.PRINCETON, "PULTST54323", "1863 laws of war", "45678912", ReCAPConstants.PRINCETON,"");
        emailService.sendEmail(ReCAPConstants.PRINCETON, "PULTST54323", "Message", "45678912", ReCAPConstants.GFA,"");
    }
}
