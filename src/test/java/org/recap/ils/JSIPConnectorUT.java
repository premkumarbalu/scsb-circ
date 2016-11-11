package org.recap.ils;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 11/11/16.
 */
public class JSIPConnectorUT extends BaseTestCase{

    @Autowired
    JSIPConnector jsipConnector;

    @Test
    public void testValidPatron(){
        boolean isValid = jsipConnector.patronValidation("PUL", "45678915");
        assertTrue(isValid);
    }

}