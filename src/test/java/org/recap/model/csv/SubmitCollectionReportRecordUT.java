package org.recap.model.csv;

import org.junit.Test;
import org.recap.BaseTestCase;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 13/7/17.
 */
public class SubmitCollectionReportRecordUT extends BaseTestCase{

    @Test
    public void testSubmitCollectionReportRecord(){
        SubmitCollectionReportRecord submitCollectionReportRecord = new SubmitCollectionReportRecord();
        submitCollectionReportRecord.setOwningInstitution("PUL");
        submitCollectionReportRecord.setCustomerCode("PB");
        submitCollectionReportRecord.setItemBarcode("33256845687546764");
        submitCollectionReportRecord.setMessage("Success");

        assertNotNull(submitCollectionReportRecord.getMessage());
        assertNotNull(submitCollectionReportRecord.getCustomerCode());
        assertNotNull(submitCollectionReportRecord.getItemBarcode());
        assertNotNull(submitCollectionReportRecord.getOwningInstitution());
    }

}