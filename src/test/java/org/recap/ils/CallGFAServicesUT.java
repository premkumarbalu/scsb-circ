package org.recap.ils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.controller.RequestItemControllerUT;
import org.recap.gfa.model.GFAItemStatusCheckRequest;
import org.recap.gfa.model.GFARetrieveItemRequest;
import org.recap.model.ItemResponseInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by sudhishk on 8/12/16.
 */
public class CallGFAServicesUT extends BaseTestCase {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(this).build();
    }

    @Test
    public void testItemStatus() {
        GFAItemStatusCheckRequest gfaItemStatusCheckRequest = new GFAItemStatusCheckRequest();
        String restUrl = "";
        String paramName = "";

        try {
            gfaItemStatusCheckRequest.setItemBarcode("");

            int status = excuteMockURL(gfaItemStatusCheckRequest,HttpMethod.GET, restUrl, paramName);
            assertTrue(status == 200);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testretrieveItem() {
        GFARetrieveItemRequest gfaRetrieveItemRequest = new GFARetrieveItemRequest();
        String restUrl = "";
        String paramName = "";
        try {
            gfaRetrieveItemRequest.setItemBarcode("");
            gfaRetrieveItemRequest.setItemOwner("");
            gfaRetrieveItemRequest.setDestination("");
            gfaRetrieveItemRequest.setDeliveryMethod("");
            int status = excuteMockURL(gfaRetrieveItemRequest,HttpMethod.GET, restUrl, paramName);
            assertTrue(status == 200);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testPermanentlyRetrieveItem() {
        GFARetrieveItemRequest gfaRetrieveItemRequest = new GFARetrieveItemRequest();
        String restUrl = "";
        String paramName = "";
        try {
            gfaRetrieveItemRequest.setItemBarcode("");
            gfaRetrieveItemRequest.setItemOwner("");
            gfaRetrieveItemRequest.setDestination("");
            int status = excuteMockURL(gfaRetrieveItemRequest, HttpMethod.GET,restUrl, paramName);
            assertTrue(status == 200);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testPermanentlyRetrieveItemIndirect() {
        GFARetrieveItemRequest gfaRetrieveItemRequest = new GFARetrieveItemRequest();
        String restUrl = "";
        String paramName = "";
        try {
            gfaRetrieveItemRequest.setItemBarcode("");
            gfaRetrieveItemRequest.setItemOwner("");

            int status = excuteMockURL(gfaRetrieveItemRequest,HttpMethod.GET, restUrl, paramName);
            assertTrue(status == 200);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private int excuteMockURL(Object objToConcert, HttpMethod httpMethod, String url, String paramName) throws JsonParseException, Exception {
        String json = "";
        int status = 0;
        String response = "";
        RestTemplate restTemplate = new RestTemplate();

        try {
            List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
            acceptableMediaTypes.add(MediaType.APPLICATION_JSON);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(acceptableMediaTypes);
            HttpEntity<Object> entity = new HttpEntity<Object>(objToConcert, headers);

            response = restTemplate.exchange(url, httpMethod, entity, String.class).getBody();
            logger.info(response);
            status=1;
        }catch(RestClientException ex){
            logger.error("RestClient : "+ ex.getMessage());
        }
    return status;
    }

}