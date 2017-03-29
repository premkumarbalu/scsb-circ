package org.recap.ils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    private static final Logger logger = LoggerFactory.getLogger(CallGFAServicesUT.class);

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
            gfaItemStatus002.setItemBarCode("XXXXXXX");
            gfaItemStatus003.setItemBarCode("32101088094931");

            List<GFAItemStatus> gfaItemStatuses = new ArrayList<>();

            gfaItemStatuses.add(gfaItemStatus001);
            gfaItemStatuses.add(gfaItemStatus002);
            gfaItemStatuses.add(gfaItemStatus003);
            gfaItemStatusCheckRequest.setItemStatus(gfaItemStatuses);

            GFAItemStatusCheckResponse statusResponse = gfaService.itemStatusCheck(gfaItemStatusCheckRequest);
            logger.info(parseToJason(statusResponse));
        } catch (Exception e) {
            logger.error("Exception ",e);
        }
    }

    @Test
    public void testretrieveItem() {
        GFARetrieveItemRequest gfaRetrieveItemRequest = new GFARetrieveItemRequest();
        try {
            TtitemRequest ttitem001 = new TtitemRequest();

            ttitem001.setCustomerCode("PA");
            ttitem001.setItemBarcode("32101088094931");
            ttitem001.setDestination("PA");

            List<TtitemRequest> ttitems = new ArrayList<>();
            ttitems.add(ttitem001);
            RetrieveItemRequest retrieveItem = new RetrieveItemRequest();
            retrieveItem.setTtitem(ttitems);
            gfaRetrieveItemRequest.setRetrieveItem(retrieveItem);
            logger.info(parseToJason(gfaRetrieveItemRequest));
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity(gfaRetrieveItemRequest,getHttpHeaders());
            ResponseEntity<String> responseEntity = restTemplate.exchange(gfaItemRetrival, HttpMethod.POST, requestEntity, String.class);
            logger.info(responseEntity.getStatusCode().toString());
            logger.info(parseToJason(responseEntity.getBody()));
//            if(responseEntity.getBody() != null && responseEntity.getBody().getRetrieveItem() != null && responseEntity.getBody().getRetrieveItem().getTtitem() != null && !responseEntity.getBody().getRetrieveItem().getTtitem().isEmpty()){
//                logger.info(parseToJason(parseToJason(responseEntity.getBody().getRetrieveItem().getTtitem())));
//                List<Ttitem> titemList = responseEntity.getBody().getRetrieveItem().getTtitem();
//                for(Ttitem ttitem:titemList){
//                    logger.info(ttitem.getErrorCode());
//                    logger.info(ttitem.getErrorNote());
//                }
//            }
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
        return headers;
    }

    private String parseToJason(Object obj){
        String jsonString="";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonString=objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
        return jsonString;

    }
}