package org.recap.ils;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ils.model.Description;
import org.recap.ils.model.request.CancelHoldRequest;
import org.recap.ils.model.request.CheckinRequest;
import org.recap.ils.model.request.CheckoutRequest;
import org.recap.ils.model.request.CreateHoldRequest;
import org.recap.ils.model.response.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by rajeshbabuk on 2/12/16.
 */
public class NyplDataApiServiceUT extends BaseTestCase {

    @Value("${ils.nypl.data.api}")
    public String nyplDataApiUrl;

    @Value("${ils.nypl.oauth.token.api}")
    public String nyplOauthTokenApiUrl;

    @Value("${ils.nypl.operator.user.id}")
    private String operatorUserId;

    @Value("${ils.nypl.operator.password}")
    private String operatorPassword;

    @Test
    public void testGenerateOAuthToken() throws Exception {
        String accessToken = generateAccessTokenForNyplApi();
        assertNotNull(accessToken);
    }

    private String generateAccessTokenForNyplApi() throws Exception {
        String authorization = "Basic " + new String(Base64Utils.encode((operatorUserId + ":" + operatorPassword).getBytes()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authorization);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> requestEntity = new HttpEntity("grant_type=client_credentials", headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(nyplOauthTokenApiUrl, HttpMethod.POST, requestEntity, String.class);
        JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        return (String) jsonObject.get("access_token");
    }

    @Test
    public void createRecapHoldRequest() throws Exception {
        String apiUrl = nyplDataApiUrl + "/recap/hold-requests";
        String authorization = "Bearer " + generateAccessTokenForNyplApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);

        CreateHoldRequest createHoldRequest = new CreateHoldRequest();
        createHoldRequest.setTrackingId("");
        createHoldRequest.setOwningInstitutionId("");
        createHoldRequest.setItemBarcode("");
        createHoldRequest.setPatronBarcode("");
        Description description = new Description();
        description.setTitle("");
        description.setAuthor("");
        description.setCallNumber("");
        createHoldRequest.setDescription(description);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<CreateHoldRequest> requestEntity = new HttpEntity(createHoldRequest, headers);
        ResponseEntity<CreateHoldResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CreateHoldResponse.class);
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        CreateHoldResponse createHoldResponse = responseEntity.getBody();
        assertNotNull(createHoldResponse);
    }

    @Test
    public void cancelRecapHoldRequest() throws Exception {
        String apiUrl = nyplDataApiUrl + "/v0.1/recap/cancel-hold-requests";
        String authorization = "Bearer " + generateAccessTokenForNyplApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);

        CancelHoldRequest cancelHoldRequest = new CancelHoldRequest();
        cancelHoldRequest.setTrackingId("");
        cancelHoldRequest.setOwningInstitutionId("");
        cancelHoldRequest.setItemBarcode("");
        cancelHoldRequest.setPatronBarcode("");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<CancelHoldRequest> requestEntity = new HttpEntity(cancelHoldRequest, headers);
        ResponseEntity<CancelHoldResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CancelHoldResponse.class);
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        CancelHoldResponse cancelHoldResponse = responseEntity.getBody();
        assertNotNull(cancelHoldResponse);
    }

    @Test
    public void nyplBibs() throws Exception {
        String apiUrl = nyplDataApiUrl + "/recap/nypl-bibs?barcode={barcode}&customercode={customercode}";
        String authorization = "Bearer " + generateAccessTokenForNyplApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
        headers.set("Authorization", authorization);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity requestEntity = new HttpEntity(headers);
        Map<String, String> params  = new HashMap<>();
        params.put("barcode", "33433001941651");
        params.put("customercode", "NA");
        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, String.class, params);
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        JAXBContext jaxbContext = JAXBContext.newInstance(BibRecords.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        String bibRecordsXml = (String) responseEntity.getBody();
        BibRecords bibRecords = (BibRecords) unmarshaller.unmarshal(new StringReader(bibRecordsXml));
        assertNotNull(bibRecords);
        assertNotNull(bibRecords.getBibRecords());
        assertTrue(bibRecords.getBibRecords().size() > 0);
        assertNotNull(bibRecords.getBibRecords().get(0));
        assertNotNull(bibRecords.getBibRecords().get(0).getBib());
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings());
        assertTrue(bibRecords.getBibRecords().get(0).getHoldings().size() > 0);
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0));
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding());
        assertTrue(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().size() > 0);
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0));
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems());
        assertTrue(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().size() > 0);
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0));
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent());
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection());
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord());
        assertTrue(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().size() > 0);
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0));
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield());
        assertTrue(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield().size() > 0);
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield().get(0));
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield().get(0).getTag());
        assertEquals(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield().get(0).getTag(), "876");
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield().get(0).getSubfield());
        assertTrue(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield().get(0).getSubfield().size() > 0);
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield().get(0).getSubfield().get(0));
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield().get(0).getSubfield().get(0).getCode());
        assertEquals(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield().get(0).getSubfield().get(0).getCode(), "p");
        assertNotNull(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield().get(0).getSubfield().get(0).getValue());
        assertEquals(bibRecords.getBibRecords().get(0).getHoldings().get(0).getHolding().get(0).getItems().get(0).getContent().getCollection().getRecord().get(0).getDatafield().get(0).getSubfield().get(0).getValue(), "33433001941651");
    }

    @Test
    public void checkin() throws Exception {
        String apiUrl = nyplDataApiUrl + "/checkin-requests";
        String authorization = "Bearer " + generateAccessTokenForNyplApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);

        CheckinRequest checkinRequest = new CheckinRequest();
        checkinRequest.setItemBarcode("33433001888415");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<CheckinRequest> requestEntity = new HttpEntity(checkinRequest, headers);
        ResponseEntity<CheckinResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CheckinResponse.class);
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        CheckinResponse checkinResponse = responseEntity.getBody();
        assertNotNull(checkinResponse);
    }

    @Test
    public void checkout() throws Exception {
        String apiUrl = nyplDataApiUrl + "/checkout-requests";
        String authorization = "Bearer " + generateAccessTokenForNyplApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);

        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPatronBarcode("");
        checkoutRequest.setItemBarcode("");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<CheckoutRequest> requestEntity = new HttpEntity(checkoutRequest, headers);
        ResponseEntity<CheckoutResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CheckoutResponse.class);
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        CheckoutResponse checkoutResponse = responseEntity.getBody();
        assertNotNull(checkoutResponse);
    }

    @Test
    public void getCheckoutRequestById() throws Exception {
        String checkoutId = "36";
        String apiUrl = nyplDataApiUrl + "/checkout-requests/" + checkoutId;
        String authorization = "Bearer " + generateAccessTokenForNyplApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity requestEntity = new HttpEntity(headers);
        ResponseEntity<CheckoutResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, CheckoutResponse.class);
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        CheckoutResponse checkoutResponse = responseEntity.getBody();
        assertNotNull(checkoutResponse);
    }

    @Test
    public void getCheckinRequestById() throws Exception {
        String checkinId = "20";
        String apiUrl = nyplDataApiUrl + "/checkin-requests/" + checkinId;
        String authorization = "Bearer " + generateAccessTokenForNyplApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity requestEntity = new HttpEntity(headers);
        ResponseEntity<CheckinResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, CheckinResponse.class);
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        CheckinResponse checkinResponse = responseEntity.getBody();
        assertNotNull(checkinResponse);
    }

    @Test
    public void getJobById() throws Exception {
        String jobId = "51658611ff25b28b";
        String apiUrl = nyplDataApiUrl + "/jobs/" + jobId;
        String authorization = "Bearer " + generateAccessTokenForNyplApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity requestEntity = new HttpEntity(headers);
        ResponseEntity<JobResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, JobResponse.class);
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        JobResponse jobResponse = responseEntity.getBody();
        assertNotNull(jobResponse);
        assertNotNull(jobResponse.getData().getStarted());
        assertNotNull(jobResponse.getData().getFinished());
        assertNotNull(jobResponse.getData().getSuccess());
    }

    @Test
    public void getItemByNyplSourceAndId() throws Exception {
        String nyplSource = "recap-PUL";
        String id = "1787347";
        String apiUrl = nyplDataApiUrl + "/items/" + nyplSource + "/" + id;
        String authorization = "Bearer " + generateAccessTokenForNyplApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity requestEntity = new HttpEntity(headers);
        ResponseEntity<ItemResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, ItemResponse.class);
        assertNotNull(responseEntity);
        ItemResponse itemResponse = responseEntity.getBody();
        assertEquals(id, itemResponse.getItemData().getId());
        assertNotNull(itemResponse);
    }

    @Test
    public void getItems() throws Exception {
        String apiUrl = nyplDataApiUrl + "/items?limit={limit}&nyplSource={nyplSource}";
        String authorization = "Bearer " + generateAccessTokenForNyplApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity requestEntity = new HttpEntity(headers);
        Map<String, String> params  = new HashMap<>();
        params.put("id", "");
        params.put("offset", "");
        params.put("limit", "0");
        params.put("barcode", "");
        params.put("nyplSource", "nypl-sierra");
        ResponseEntity<ItemsResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, ItemsResponse.class, params);
        assertNotNull(responseEntity);
        ItemsResponse itemsResponse = responseEntity.getBody();
        assertNotNull(itemsResponse);
    }
}