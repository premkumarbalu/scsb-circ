package org.recap.camel;

import org.junit.Test;
import org.recap.camel.accessionreconcilation.AccessionReconcialtionEmailService;
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
        AccessionReconcialtionEmailService accessionReconcialtionEmailService = new AccessionReconcialtionEmailService(institution);
        ReflectionTestUtils.setField(accessionReconcialtionEmailService,"pulEmailTo",emailAddress);
        String email = accessionReconcialtionEmailService.emailIdTo(institution);
        assertEquals(emailAddress,email);
    }

    @Test
    public void testReportLocation() throws Exception{
        String reportLocation = "accession-reconcilation/processed/local/pul";
        String institution = "PUL";
        AccessionReconcialtionEmailService accessionReconcialtionEmailService = new AccessionReconcialtionEmailService(institution);
        ReflectionTestUtils.setField(accessionReconcialtionEmailService,"pulReportLocation",reportLocation);
        String location = accessionReconcialtionEmailService.reportLocation(institution);
        assertEquals(reportLocation,location);
    }
}
