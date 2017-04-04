package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Test;
import org.recap.BaseTestCase;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 3/4/17.
 */
public class NyplHoldRequestUT extends BaseTestCase{


    @Test
    public void testNyplHoldRequest(){
        NyplHoldRequest nyplHoldRequest = new NyplHoldRequest();
        nyplHoldRequest.setPatron("test");
        nyplHoldRequest.setRecordType("test");
        nyplHoldRequest.setRecord("test");
        nyplHoldRequest.setNyplSource("test");
        nyplHoldRequest.setPickupLocation("PB");
        nyplHoldRequest.setNumberOfCopies(1);
        nyplHoldRequest.setNeededBy("test");

        assertNotNull(nyplHoldRequest.getPatron());
        assertNotNull(nyplHoldRequest.getRecordType());
        assertNotNull(nyplHoldRequest.getRecord());
        assertNotNull(nyplHoldRequest.getNyplSource());
        assertNotNull(nyplHoldRequest.getPickupLocation());
        assertNotNull(nyplHoldRequest.getNumberOfCopies());
        assertNotNull(nyplHoldRequest.getNeededBy());

    }

}