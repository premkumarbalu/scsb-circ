package org.recap.ils;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 22/3/17.
 */
public class JSIPConnectorFactoryUT extends BaseTestCase{

    @Autowired
    JSIPConnectorFactory jsipConnectorFactory;

    @Test
    public void testPrinstonJSIPConnectorFactory(){
        IJSIPConnector ijsipConnector = jsipConnectorFactory.getJSIPConnector("PUL");
        assertNotNull(ijsipConnector);
        assertNotNull(ijsipConnector.getHost());
        assertNotNull(ijsipConnector.getOperatorLocation());
        assertNotNull(ijsipConnector.getOperatorUserId());
        assertNotNull(ijsipConnector.getOperatorPassword());
    }

    @Test
    public void testColumbiaJSIPConnectorFactory(){
        IJSIPConnector ijsipConnector = jsipConnectorFactory.getJSIPConnector("CUL");
        assertNotNull(ijsipConnector);
        assertNotNull(ijsipConnector.getHost());
        assertNotNull(ijsipConnector.getOperatorLocation());
        assertNotNull(ijsipConnector.getOperatorUserId());
        assertNotNull(ijsipConnector.getOperatorPassword());
    }

    @Test
    public void testNYPLJSIPConnectorFactory(){
        IJSIPConnector ijsipConnector = jsipConnectorFactory.getJSIPConnector("NYPL");
        assertNotNull(ijsipConnector);
        assertNotNull(ijsipConnector.getHost());
        assertNull(ijsipConnector.getOperatorLocation());
        assertNotNull(ijsipConnector.getOperatorUserId());
        assertNotNull(ijsipConnector.getOperatorPassword());
    }

}