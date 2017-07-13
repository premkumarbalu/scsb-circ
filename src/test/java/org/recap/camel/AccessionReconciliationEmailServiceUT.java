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
        String emailAddress = "test@mail.com";
        String institution = "PUL";
        AccessionReconciliationEmailService accessionReconciliationEmailService = new AccessionReconciliationEmailService(institution);
        ReflectionTestUtils.setField(accessionReconciliationEmailService,"pulEmailTo",emailAddress);
        String email = accessionReconciliationEmailService.emailIdTo(institution);
        assertEquals(emailAddress,email);
    }

    @Test
    public void testReportLocation() throws Exception{
        String reportLocation = "accession-reconcilation/processed/local/pul";
        String institution = "PUL";
        AccessionReconciliationEmailService accessionReconciliationEmailService = new AccessionReconciliationEmailService(institution);
        ReflectionTestUtils.setField(accessionReconciliationEmailService,"pulReportLocation",reportLocation);
        String location = accessionReconciliationEmailService.reportLocation(institution);
        assertEquals(reportLocation,location);
    }
}
