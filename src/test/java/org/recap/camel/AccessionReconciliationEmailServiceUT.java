package org.recap.camel;

import org.junit.Test;
import org.recap.camel.accessionreconciliation.AccessionReconciliationEmailService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

/**
 * Created by akulak on 25/5/17.
 */
public class AccessionReconciliationEmailServiceUT {

    @Test
    public void testEmailIdTo() throws Exception{
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        String emailAddress = "test@mail.com";
        String ccEmailAddress = "testcc@mail.com";
        String institution = "PUL";
        AccessionReconciliationEmailService accessionReconciliationEmailService = new AccessionReconciliationEmailService(institution);
        ReflectionTestUtils.setField(accessionReconciliationEmailService,"pulEmailTo",emailAddress);
        ReflectionTestUtils.setField(accessionReconciliationEmailService,"pulEmailCC",ccEmailAddress);
        accessionReconciliationEmailService.emailIdTo(institution, emailPayLoad);
        assertEquals(emailAddress,emailPayLoad.getTo());
        assertEquals(ccEmailAddress,emailPayLoad.getCc());
    }

}
