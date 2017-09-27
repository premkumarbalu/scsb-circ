package org.recap.util;

import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Created by premkb on 15/9/17.
 */
@Service
public class SecurityUtil {

    @Value("${scsb.encryption.secretkey}")
    private String encryptionSecretKey;

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    public String getEncryptedValue(String inputValue){
        Key aesKey = new SecretKeySpec(encryptionSecretKey.getBytes(), "AES");
        String encryptedString = null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(inputValue.getBytes());
            Base64.Encoder encoder = Base64.getEncoder();
            encryptedString = encoder.encodeToString(encrypted);
        } catch (NoSuchAlgorithmException e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        } catch (NoSuchPaddingException e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        } catch (InvalidKeyException e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        } catch (IllegalBlockSizeException e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        } catch (BadPaddingException e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }
        logger.info("encryptedString--->{}",encryptedString);
        return encryptedString;
    }

    public String getDecryptedValue(String encryptedValue){
        Key aesKey = new SecretKeySpec(encryptionSecretKey.getBytes(), "AES");

        Base64.Decoder decoder = Base64.getDecoder();
        Cipher cipher = null;

        String decrypted = null;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            decrypted = new String(cipher.doFinal(decoder.decode(encryptedValue)));
        } catch (NoSuchAlgorithmException e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        } catch (NoSuchPaddingException e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        } catch (InvalidKeyException e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        } catch (IllegalBlockSizeException e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        } catch (BadPaddingException e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }
        logger.info("decryptedString--->{}",decrypted);
        return decrypted;
    }
}
