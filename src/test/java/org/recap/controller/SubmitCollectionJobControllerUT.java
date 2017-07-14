package org.recap.controller;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 14/7/17.
 */
public class SubmitCollectionJobControllerUT extends BaseTestCase{

    @Autowired
    SubmitCollectionJobController submitCollectionJobController;

    @Test
    public void testSubmitCollectionJobController() throws Exception {
        String response = submitCollectionJobController.startSubmitCollection();
        assertNotNull(response);
        assertEquals(response,"Success");
    }

}