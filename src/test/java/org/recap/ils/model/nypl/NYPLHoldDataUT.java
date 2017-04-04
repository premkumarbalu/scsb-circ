package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Test;
import org.recap.BaseTestCase;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 3/4/17.
 */
public class NYPLHoldDataUT extends BaseTestCase{

    @Test
    public void testNYPLHoldData(){
        NYPLHoldData nyplHoldData = new NYPLHoldData();
        nyplHoldData.setPatron("test");
        nyplHoldData.setRecordType("test");
        nyplHoldData.setRecord("test");
        nyplHoldData.setNyplSource("test");
        nyplHoldData.setPickupLocation("PB");
        nyplHoldData.setNumberOfCopies(1);
        nyplHoldData.setNeededBy("test");
        nyplHoldData.setJobId("1");
        nyplHoldData.setUpdatedDate(new Date().toString());
        nyplHoldData.setCreatedDate(new Date().toString());
        nyplHoldData.setProcessed(true);
        nyplHoldData.setSuccess(true);

        assertNotNull(nyplHoldData.getPatron());
        assertNotNull(nyplHoldData.getRecordType());
        assertNotNull(nyplHoldData.getRecord());
        assertNotNull(nyplHoldData.getNyplSource());
        assertNotNull(nyplHoldData.getPickupLocation());
        assertNotNull(nyplHoldData.getNumberOfCopies());
        assertNotNull(nyplHoldData.getNeededBy());
        assertNotNull(nyplHoldData.getProcessed());
        assertNotNull(nyplHoldData.getSuccess());
        assertNotNull(nyplHoldData.getUpdatedDate());
        assertNotNull(nyplHoldData.getCreatedDate());
        assertNotNull(nyplHoldData.getJobId());
    }

}