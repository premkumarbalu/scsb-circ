package org.recap.ils;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;

/**
 * Created by hemalathas on 11/11/16.
 */
public class JSIPConnectorUT extends BaseTestCase{

    @Autowired
    private PrincetonJSIPConnector princetonJSIPConnector;

    @Test
    public void testValidPatron(){
        boolean isValid = princetonJSIPConnector.patronValidation("PUL", "45678915");
        assertTrue(isValid);
    }

}