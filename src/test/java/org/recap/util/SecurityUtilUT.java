package org.recap.util;

import junit.framework.Assert;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by premkb on 15/9/17.
 */

public class SecurityUtilUT extends BaseTestCase{

    @Autowired
    private SecurityUtil securityUtil;

    @Test
    public void getEncryptedValue(){
        String value = "test@mail.com";
        String encryptedValue = securityUtil.getEncryptedValue(value);
        assertNotNull(encryptedValue);
        String decryptedValue = securityUtil.getDecryptedValue(encryptedValue);
        assertNotNull(decryptedValue);
        assertEquals(value,decryptedValue);
    }

    @Test
    public void getDecryptedValue(){
        String decryptedValue = securityUtil.getDecryptedValue("lPH5sNf/t/IAVAooi6loSw==");
        assertNotNull(decryptedValue);
        String encryptedValue = securityUtil.getEncryptedValue(decryptedValue);
        assertNotNull(encryptedValue);
        assertEquals("",decryptedValue);

    }
}
