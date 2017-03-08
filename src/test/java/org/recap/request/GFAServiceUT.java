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

    @Value("${gfa.item.permanent.withdrawl.direct}")
    private String gfaItemPermanentWithdrawlDirect;

    @Value("${gfa.item.permanent.withdrawl.indirect}")
    private String gfaItemPermanentWithdrawlInDirect;

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

    @Test
    public void testGfaPWD() {
        GFAPwdRequest gfaPwdRequest = new GFAPwdRequest();
        GFAPwdDsItemRequest gfaPwdDsItemRequest = new GFAPwdDsItemRequest();
        GFAPwdTtItemRequest gfaPwdTtItemRequest = new GFAPwdTtItemRequest();
        gfaPwdTtItemRequest.setCustomerCode("AR");
        gfaPwdTtItemRequest.setItemBarcode("AR00000612");
        gfaPwdTtItemRequest.setDestination("AR");
        gfaPwdTtItemRequest.setRequestor("Dev Tesr");
        gfaPwdDsItemRequest.setTtitem(Arrays.asList(gfaPwdTtItemRequest));
        gfaPwdRequest.setDsitem(gfaPwdDsItemRequest);

        ResponseEntity<GFAPwdResponse> responseEntity = new ResponseEntity(new GFAPwdResponse(), HttpStatus.OK);
        HttpEntity requestEntity = new HttpEntity(gfaPwdRequest, getHttpHeaders());
        Mockito.when(gfaService.getGfaItemPermanentWithdrawlDirect()).thenReturn(gfaItemPermanentWithdrawlDirect);
        Mockito.when(gfaService.getRestTemplate()).thenReturn(restTemplate);
        Mockito.when(gfaService.getRestTemplate().exchange(gfaService.getGfaItemPermanentWithdrawlDirect(), HttpMethod.POST, requestEntity, GFAPwdResponse.class)).thenReturn(responseEntity);
        Mockito.when(gfaService.gfaPermanentWithdrawlDirect(gfaPwdRequest)).thenCallRealMethod();
        GFAPwdResponse response = gfaService.gfaPermanentWithdrawlDirect(gfaPwdRequest);
        assertNotNull(response);
        assertNull(response.getDsitem());
    }

    @Test
    public void testGfaPWI() {
        GFAPwiRequest gfaPwiRequest = new GFAPwiRequest();
        GFAPwiDsItemRequest gfaPwiDsItemRequest = new GFAPwiDsItemRequest();
        GFAPwiTtItemRequest gfaPwiTtItemRequest = new GFAPwiTtItemRequest();
        gfaPwiTtItemRequest.setCustomerCode("AR");
        gfaPwiTtItemRequest.setItemBarcode("AR00051608");
        gfaPwiDsItemRequest.setTtitem(Arrays.asList(gfaPwiTtItemRequest));
        gfaPwiRequest.setDsitem(gfaPwiDsItemRequest);

        ResponseEntity<GFAPwiResponse> responseEntity = new ResponseEntity(new GFAPwiResponse(), HttpStatus.OK);
        HttpEntity requestEntity = new HttpEntity(gfaPwiRequest, getHttpHeaders());
        Mockito.when(gfaService.getGfaItemPermanentWithdrawlInDirect()).thenReturn(gfaItemPermanentWithdrawlInDirect);
        Mockito.when(gfaService.getRestTemplate()).thenReturn(restTemplate);
        Mockito.when(gfaService.getRestTemplate().exchange(gfaService.getGfaItemPermanentWithdrawlInDirect(), HttpMethod.POST, requestEntity, GFAPwiResponse.class)).thenReturn(responseEntity);
        Mockito.when(gfaService.gfaPermanentWithdrawlInDirect(gfaPwiRequest)).thenCallRealMethod();
        GFAPwiResponse response = gfaService.gfaPermanentWithdrawlInDirect(gfaPwiRequest);
        assertNotNull(response);
        assertNull(response.getDsitem());
    }

}