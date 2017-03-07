package org.recap.request;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCase;
import org.recap.gfa.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 21/2/17.
 */
public class GFAServiceUT extends BaseTestCase{

    private static final Logger logger = LoggerFactory.getLogger(GFAServiceUT.class);
    @Mock
    GFAService gfaService;

    @Mock
    RestTemplate restTemplate;

    @Value("${gfa.item.status}")
    private String gfaItemStatus;
    @Value("${gfa.item.retrieval.order}")
    private String gfaItemRetrival;

    @Test
    public void testGFAService(){
        GFARetrieveItemRequest gfaRetrieveItemRequest = new GFARetrieveItemRequest();
        RetrieveItemRequest retrieveItemRequest = new RetrieveItemRequest();
        TtitemRequest ttitemRequest = new TtitemRequest();
        ttitemRequest.setCustomerCode("PB");
        ttitemRequest.setDestination("PUL");
        ttitemRequest.setItemBarcode("123");
        ttitemRequest.setItemStatus("Available");
        retrieveItemRequest.setTtitem(Arrays.asList(ttitemRequest));
        gfaRetrieveItemRequest.setRetrieveItem(retrieveItemRequest);


        RetrieveItem retrieveItem = null;
        GFARetrieveItemResponse gfaRetrieveItemResponse = new GFARetrieveItemResponse();
        gfaRetrieveItemResponse.setSuccess(true);
        gfaRetrieveItemResponse.setRetrieveItem(retrieveItem);
        gfaRetrieveItemResponse.setScrenMessage("Success");

        ResponseEntity<GFARetrieveItemResponse> responseEntity = new ResponseEntity(gfaRetrieveItemResponse, HttpStatus.OK);
        HttpEntity requestEntity = new HttpEntity(gfaRetrieveItemRequest, getHttpHeaders());
        Mockito.when(gfaService.getGfaItemRetrival()).thenReturn(gfaItemRetrival);
        Mockito.when(gfaService.getGfaItemStatus()).thenReturn(gfaItemStatus);
        Mockito.when(gfaService.getRestTemplate()).thenReturn(restTemplate);
        Mockito.when(gfaService.getRestTemplate().exchange(gfaService.getGfaItemRetrival(), HttpMethod.POST, requestEntity, GFARetrieveItemResponse.class)).thenReturn(responseEntity);
        Mockito.when(gfaService.itemRetrival(gfaRetrieveItemRequest)).thenCallRealMethod();
        GFARetrieveItemResponse response = gfaService.itemRetrival(gfaRetrieveItemRequest);
        assertNotNull(response);
        assertNull(response.getRetrieveItem());
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}