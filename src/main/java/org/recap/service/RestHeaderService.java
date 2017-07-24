package org.recap.service;

import org.recap.ReCAPConstants;
import org.recap.spring.SwaggerAPIProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * Created by harikrishnanv on 20/7/17.
 */
@Service
public class RestHeaderService {

    public HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(ReCAPConstants.API_KEY, SwaggerAPIProvider.getInstance().getSwaggerApiKey());
        return headers;
    }
}
