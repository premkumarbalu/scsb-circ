package org.recap.controller;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 12/4/17.
 */
public class PurgeControllerUT extends BaseTestCase {

    @Autowired
    PurgeController purgeEmailAddressController;

    @Test
    public void testPurgeEmailAddress() {
        ResponseEntity responseEntity = purgeEmailAddressController.purgeEmailAddress();
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void testPurgeExceptionRequests() {
        ResponseEntity responseEntity = purgeEmailAddressController.purgeExceptionRequests();
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void testPurgeAccessionRequests() {
        ResponseEntity responseEntity = purgeEmailAddressController.purgeAccessionRequests();
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
    }

}