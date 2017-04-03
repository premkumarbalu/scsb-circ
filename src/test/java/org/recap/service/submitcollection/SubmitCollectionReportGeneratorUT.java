package org.recap.service.submitcollection;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.ReportDataRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by premkb on 23/3/17.
 */
public class SubmitCollectionReportGeneratorUT extends BaseTestCase{

    @Autowired
    private SubmitCollectionReportGenerator submitCollectionReportGenerator;

    @Test
    public void generateReport(){
        ReportDataRequest reportRequest = new ReportDataRequest();
        reportRequest.setFileName("Submit_Collection_Report");
        reportRequest.setInstitutionCode("PUL");
        reportRequest.setReportType("Submit_Collection_Exception_Report");
        reportRequest.setTransmissionType("FTP");
        String response = submitCollectionReportGenerator.generateReport(reportRequest);
        System.out.println("");
    }
}
