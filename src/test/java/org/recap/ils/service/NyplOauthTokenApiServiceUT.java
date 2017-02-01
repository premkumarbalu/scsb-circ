package org.recap.ils.service;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ils.service.NyplOauthTokenApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.Assert.assertNotNull;

/**
 * Created by rajeshbabuk on 9/12/16.
 */
public class NyplOauthTokenApiServiceUT extends BaseTestCase {

    @Autowired
    NyplOauthTokenApiService nyplOauthTokenApiService;

    @Value("${ils.nypl.operator.user.id}")
    private String operatorUserId;

    @Value("${ils.nypl.operator.password}")
    private String operatorPassword;

    @Test
    public void testGenerateOAuthToken() throws Exception {
        String accessToken = nyplOauthTokenApiService.generateAccessTokenForNyplApi(operatorUserId, operatorPassword);
        assertNotNull(accessToken);
    }
}
