package org.recap.ils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.gfa.model.*;
import org.recap.request.GFAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by sudhishk on 8/12/16.
 */
public class CallGFAServicesUT extends BaseTestCase {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${gfa.item.status}")
    private String gfaItemStatus;

    @Value("${gfa.item.retrieval.order}")
    private String gfaItemRetrival;

    @Autowired
    private GFAService gfaService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(this).build();
    }

    @Test
    public void testItemStatus() {
        GFAItemStatusCheckRequest gfaItemStatusCheckRequest = new GFAItemStatusCheckRequest();
        try {
            GFAItemStatus gfaItemStatus001 = new GFAItemStatus();
            GFAItemStatus gfaItemStatus002 = new GFAItemStatus();
            GFAItemStatus gfaItemStatus003 = new GFAItemStatus();

            gfaItemStatus001.setItemBarCode("PULTST54337");
            gfaItemStatus002.setItemBarCode("PULTST54321");
            gfaItemStatus003.setItemBarCode("PULTST54322");

            List<GFAItemStatus> gfaItemStatuses = new ArrayList<>();

            gfaItemStatuses.add(gfaItemStatus001);
            gfaItemStatuses.add(gfaItemStatus002);
            gfaItemStatuses.add(gfaItemStatus003);
            gfaItemStatusCheckRequest.setItemStatus(gfaItemStatuses);

            GFAItemStatusCheckResponse statusResponse = gfaService.itemStatusCheck(gfaItemStatusCheckRequest);
        } catch (Exception e) {
            logger.error("Exception "+e);
        }
    }

    @Test
    public void testretrieveItem() {
        GFARetrieveItemRequest gfaRetrieveItemRequest = new GFARetrieveItemRequest();
        try {
            Ttitem ttitem001 = new Ttitem();

            ttitem001.setCustomerCode("PA");
            ttitem001.setItemBarcode("PULTST54322");
            ttitem001.setDestination("PA");
            ttitem001.setDeliveryMethod("PHY");

            List<Ttitem> ttitems = new ArrayList<>();
            ttitems.add(ttitem001);
            RetrieveItem retrieveItem = new RetrieveItem();
            retrieveItem.setTtitem(ttitems);
            gfaRetrieveItemRequest.setRetrieveItem(retrieveItem);
            ObjectMapper objectMapper = new ObjectMapper();
            String json = "";
            json = objectMapper.writeValueAsString(gfaRetrieveItemRequest);
            logger.info(json);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity<>(getHttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(gfaItemStatus)
                    .queryParam(ReCAPConstants.GFA_SERVICE_PARAM, json);
            ResponseEntity<String> responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, String.class);
            logger.info(responseEntity.getStatusCode().toString());
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

            int status = excuteMockURL(gfaRetrieveItemRequest, HttpMethod.GET, restUrl, paramName);
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

            int status = excuteMockURL(gfaRetrieveItemRequest, HttpMethod.GET, restUrl, paramName);
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
            status = 1;
        } catch (RestClientException ex) {
            logger.error("RestClient : " + ex.getMessage());
        }
        return status;
    }

    @Test
    public void testGfaStatus() {
        String gfaStaus;
        gfaStaus = "Out on Ret WO: 424980 09/26/16 To G1";
        gfaStaus = "INC On WO: 430151 01/13/17";
        gfaStaus = "IN";
        gfaStaus = "Out On EDD WO: 12345 01/13/17 To PA";
        gfaStaus = "REACC on WO: 12345";
        gfaStaus = "REFILE on WO: 12345";
        gfaStaus = "Ver On EDD WO: 12345";
        gfaStaus = "Sch On EDD WO: 12345";
        String gfaOnlyStaus = "";

        if (gfaStaus.contains(":")) {
            gfaOnlyStaus = gfaStaus.substring(0, gfaStaus.indexOf(":")+1).toUpperCase();
        } else {
            gfaOnlyStaus = gfaStaus.toUpperCase();
        }
        logger.info(gfaOnlyStaus);
        if (ReCAPConstants.getGFAStatusAvailableList().contains(gfaOnlyStaus)) {
            logger.info("Staus Match");
        }else{
            logger.info("Does Not Match");
        }

    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(ReCAPConstants.API_KEY, ReCAPConstants.RECAP);
        return headers;
    }
}