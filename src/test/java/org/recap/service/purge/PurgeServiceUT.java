package org.recap.service.purge;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 13/4/17.
 */
public class PurgeServiceUT extends BaseTestCase{

    @Autowired
    PurgeService purgeService;

    @Test
    public void testPurgeEmailAddress(){
        Map<String,Integer> responseMap = purgeService.purgeEmailAddress();
        assertNotNull(responseMap);
        assertNotNull(responseMap.get(ReCAPConstants.PURGE_EDD_REQUEST));
        assertNotNull(responseMap.get(ReCAPConstants.PURGE_PHYSICAL_REQUEST));
    }

    @Test
    public void testPurgeExceptionRequests(){
        String status = purgeService.purgeExceptionRequests();
        assertNotNull(status);
        assertEquals(status, ReCAPConstants.SUCCESS);
    }

}