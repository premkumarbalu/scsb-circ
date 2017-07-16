package org.recap.ils;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCase;
import org.recap.ils.model.nypl.*;
import org.recap.ils.model.nypl.request.CancelHoldRequest;
import org.recap.ils.model.nypl.request.CheckinRequest;
import org.recap.ils.model.nypl.request.CheckoutRequest;
import org.recap.ils.model.nypl.request.CreateHoldRequest;
import org.recap.ils.model.nypl.response.*;
import org.recap.ils.model.response.ItemCheckinResponse;
import org.recap.ils.model.response.ItemCheckoutResponse;
import org.recap.ils.model.response.ItemHoldResponse;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.ils.service.NyplApiResponseUtil;
import org.recap.ils.service.NyplOauthTokenApiService;
import org.recap.model.BibliographicEntity;
import org.recap.model.HoldingsEntity;
import org.recap.model.ItemEntity;
import org.recap.processor.NyplJobResponsePollingProcessor;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by rajeshbabuk on 19/12/16.
 */
public class NyplApiServiceConnectorUT extends BaseTestCase {

    private static final Logger logger = LoggerFactory.getLogger(NyplApiServiceConnector.class);

    @Value("${ils.nypl.data.api}")
    public String nyplDataApiUrl;

    @Value("${ils.nypl.operator.user.id}")
    private String operatorUserId;

    @Value("${ils.nypl.operator.password}")
    private String operatorPassword;

    @Mock
    NyplOauthTokenApiService nyplOauthTokenApiService;

    @Mock
    NyplApiServiceConnector nyplApiServiceConnector;

    @Mock
    NyplApiResponseUtil nyplApiResponseUtil;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Mock
    ItemDetailsRepository itemDetailsRepository;

    @Mock
    RestTemplate restTemplate;

    @Mock
    HttpHeaders httpHeaders;

    @Mock
    NyplJobResponsePollingProcessor nyplJobResponsePollingProcessor;

    public HttpEntity getHttpEntity(HttpHeaders headers){
        return new HttpEntity(headers);
    }

    public String getOperatorUserId() {
        return operatorUserId;
    }

    public String getOperatorPassword() {
        return operatorPassword;
    }

    @Test
    public void lookupItem() throws Exception {
        String itemIdentifier = getBibEntity().getItemEntities().get(0).getBarcode();
        String itemBarcode = getBibEntity().getItemEntities().get(0).getBarcode();
        String institutionId = "PUL";
        String source = "recap-PUL";
        Mockito.when(nyplApiServiceConnector.getRestTemplate()).thenReturn(restTemplate);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil()).thenReturn(nyplApiResponseUtil);
        Mockito.when(nyplApiServiceConnector.getNyplDataApiUrl()).thenReturn(nyplDataApiUrl);
        Mockito.when(nyplApiServiceConnector.getNyplOauthTokenApiService()).thenReturn(nyplOauthTokenApiService);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().getItemDetailsRepository()).thenReturn(itemDetailsRepository);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().getItemDetailsRepository().findByBarcode(itemBarcode)).thenReturn(getBibEntity().getItemEntities());
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().getItemOwningInstitutionByItemBarcode(itemIdentifier)).thenCallRealMethod();
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().getNyplSource(institutionId)).thenCallRealMethod();
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().getNormalizedItemIdForNypl(itemIdentifier)).thenCallRealMethod();
        ResponseEntity<ItemResponse> responseEntity = new ResponseEntity<ItemResponse>(getItemResponse(), HttpStatus.OK);
        ItemResponse itemResponse = responseEntity.getBody();
        HttpHeaders headers = new HttpHeaders();
        Mockito.when(nyplApiServiceConnector.getHttpHeader()).thenReturn(headers);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer null");
        HttpEntity requestEntity = new HttpEntity(headers);
        Mockito.when(nyplApiServiceConnector.getHttpEntity(headers)).thenReturn(requestEntity);
        String apiUrl = nyplApiServiceConnector.getNyplDataApiUrl() + "/items/" + source + "/" + itemIdentifier;
        Mockito.when(nyplApiServiceConnector.getApiUrl(Mockito.any(),Mockito.any())).thenReturn(apiUrl);
        Mockito.when(restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, ItemResponse.class)).thenReturn(responseEntity);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().buildItemInformationResponse(itemResponse)).thenCallRealMethod();
        Mockito.when((ItemInformationResponse) nyplApiServiceConnector.lookupItem(itemIdentifier)).thenCallRealMethod();
        ItemInformationResponse itemInformationResponse = (ItemInformationResponse) nyplApiServiceConnector.lookupItem(itemIdentifier);
        assertNotNull(itemInformationResponse);
        assertNotNull(itemInformationResponse.getItemBarcode());
        assertNotNull(itemInformationResponse.getBibID());
        assertTrue(itemInformationResponse.isSuccess());
    }

    @Test
    public void checkoutItem() throws Exception {
        String itemBarcode = "33433001888415";
        String patronBarcode = "23333095887111";
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        ResponseEntity<CheckoutResponse> responseEntity = new ResponseEntity<CheckoutResponse>(getCheckOutItemResponse(),HttpStatus.OK);
        CheckoutResponse checkoutResponse = responseEntity.getBody();
        String apiUrl = nyplDataApiUrl + "/checkout-requests";
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil()).thenReturn(nyplApiResponseUtil);
        Mockito.when(nyplApiServiceConnector.getNyplDataApiUrl()).thenReturn(nyplDataApiUrl);
        Mockito.when(nyplApiServiceConnector.getNyplOauthTokenApiService()).thenReturn(nyplOauthTokenApiService);
        Mockito.when(nyplApiServiceConnector.getNyplJobResponsePollingProcessor()).thenReturn(nyplJobResponsePollingProcessor);
        Mockito.when(nyplApiServiceConnector.getLogger()).thenReturn(logger);
        Mockito.when(nyplApiServiceConnector.getCheckOutRequest()).thenReturn(checkoutRequest);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().getExpirationDateForNypl()).thenCallRealMethod();
        HttpEntity<CheckoutRequest> requestEntity = new HttpEntity(checkoutRequest, getHttpHeaders());
        Mockito.when(nyplApiServiceConnector.getRestTemplate()).thenReturn(restTemplate);
        Mockito.when(nyplApiServiceConnector.getRestTemplate().exchange(apiUrl, HttpMethod.POST, requestEntity, CheckoutResponse.class)).thenReturn(responseEntity);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().buildItemCheckoutResponse(checkoutResponse)).thenCallRealMethod();
        Mockito.when(nyplApiServiceConnector.getNyplJobResponsePollingProcessor().pollNyplRequestItemJobResponse(Mockito.any())).thenCallRealMethod();
        Mockito.when((ItemCheckoutResponse) nyplApiServiceConnector.checkOutItem(itemBarcode, patronBarcode)).thenCallRealMethod();
        ItemCheckoutResponse itemCheckoutResponse = (ItemCheckoutResponse) nyplApiServiceConnector.checkOutItem(itemBarcode, patronBarcode);
        assertNotNull(itemCheckoutResponse);

    }

    @Test
    public void checkinItem() throws Exception {
        String itemBarcode = "33433001888415";
        String patronBarcode = "23333095887111";
        CheckinRequest checkinRequest = new CheckinRequest();
        ResponseEntity<CheckinResponse> responseEntity = new ResponseEntity<CheckinResponse>(getCheckinResponse(),HttpStatus.OK);
        CheckinResponse checkinResponse = responseEntity.getBody();
        String apiUrl = nyplDataApiUrl + "/checkin-requests";
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil()).thenReturn(nyplApiResponseUtil);
        Mockito.when(nyplApiServiceConnector.getNyplDataApiUrl()).thenReturn(nyplDataApiUrl);
        Mockito.when(nyplApiServiceConnector.getNyplOauthTokenApiService()).thenReturn(nyplOauthTokenApiService);
        Mockito.when(nyplApiServiceConnector.getNyplJobResponsePollingProcessor()).thenReturn(nyplJobResponsePollingProcessor);
        Mockito.when(nyplApiServiceConnector.getLogger()).thenReturn(logger);
        Mockito.when(nyplApiServiceConnector.getCheckInRequest()).thenReturn(checkinRequest);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().getExpirationDateForNypl()).thenCallRealMethod();
        HttpEntity<CheckoutRequest> requestEntity = new HttpEntity(checkinRequest, getHttpHeaders());
        Mockito.when(nyplApiServiceConnector.getRestTemplate()).thenReturn(restTemplate);
        Mockito.when(nyplApiServiceConnector.getRestTemplate().exchange(apiUrl, HttpMethod.POST, requestEntity, CheckinResponse.class)).thenReturn(responseEntity);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().buildItemCheckinResponse(checkinResponse)).thenCallRealMethod();
        Mockito.when(nyplApiServiceConnector.getNyplJobResponsePollingProcessor().pollNyplRequestItemJobResponse(Mockito.any())).thenCallRealMethod();
        Mockito.when((ItemCheckinResponse) nyplApiServiceConnector.checkInItem(itemBarcode, patronBarcode)).thenCallRealMethod();
        ItemCheckinResponse itemCheckinResponse = (ItemCheckinResponse) nyplApiServiceConnector.checkInItem(itemBarcode, patronBarcode);
        assertNotNull(itemCheckinResponse);
    }

    @Test
    public void placeHold() throws Exception {
        String itemBarcode = "33433001888415";
        String patronBarcode = "23333095887111";
        String callInstitutionId = "NYPL";
        String itemInstitutionId = "NYPL";
        String expirationDate = new Date().toString();
        String bibId = "";
        String pickupLocation = "";
        String trackingId = "1231";
        String title = "";
        String author = "";
        String callNumber = "";
        String recapHoldApiUrl = nyplDataApiUrl + "/recap/hold-requests";
        String apiUrl = nyplDataApiUrl + "/hold-requests/" + trackingId;
        CreateHoldRequest createHoldRequest = new CreateHoldRequest();
        HttpHeaders headers = getHttpHeaders();
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil()).thenReturn(nyplApiResponseUtil);
        Mockito.when(nyplApiServiceConnector.getNyplDataApiUrl()).thenReturn(nyplDataApiUrl);
        Mockito.when(nyplApiServiceConnector.getNyplOauthTokenApiService()).thenReturn(nyplOauthTokenApiService);
        Mockito.when(nyplApiServiceConnector.getNyplJobResponsePollingProcessor()).thenReturn(nyplJobResponsePollingProcessor);
        Mockito.when(nyplApiServiceConnector.getLogger()).thenReturn(logger);
        Mockito.when(nyplApiServiceConnector.getRestTemplate()).thenReturn(restTemplate);
        Mockito.when(nyplApiServiceConnector.getHttpHeader()).thenReturn(headers);
        Mockito.when(nyplApiServiceConnector.getCreateHoldRequest()).thenReturn(createHoldRequest);
        ResponseEntity<CreateHoldResponse> responseEntity = new ResponseEntity<CreateHoldResponse>(getCreateHoldResponse(),HttpStatus.OK);
        ResponseEntity<NYPLHoldResponse> responseEntity1 = new ResponseEntity<NYPLHoldResponse>(getNyplHoldResponse(),HttpStatus.OK);
        CreateHoldResponse createHoldResponse = responseEntity.getBody();
        HttpEntity<CreateHoldRequest> requestEntity = new HttpEntity(createHoldRequest, getHttpHeaders());
        Mockito.when(restTemplate.exchange(recapHoldApiUrl, HttpMethod.POST, requestEntity, CreateHoldResponse.class)).thenReturn(responseEntity);
        requestEntity = new HttpEntity(headers);
        Mockito.when(nyplApiServiceConnector.getHttpEntity(headers)).thenReturn(requestEntity);
        Mockito.when(restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, NYPLHoldResponse.class)).thenReturn(responseEntity1);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().buildItemHoldResponse(createHoldResponse)).thenCallRealMethod();
        Mockito.when(nyplApiServiceConnector.getNyplJobResponsePollingProcessor().pollNyplRequestItemJobResponse(Mockito.any())).thenCallRealMethod();
        ItemHoldResponse itemHoldResponse1 = new ItemHoldResponse();
        itemHoldResponse1.setSuccess(true);
        Mockito.when((ItemHoldResponse)nyplApiServiceConnector.placeHold(itemBarcode, patronBarcode, callInstitutionId, itemInstitutionId, expirationDate, bibId, pickupLocation, trackingId, title, author, callNumber)).thenCallRealMethod();
        ItemHoldResponse itemHoldResponse = (ItemHoldResponse)nyplApiServiceConnector.placeHold(itemBarcode, patronBarcode, callInstitutionId, itemInstitutionId, expirationDate, bibId, pickupLocation, trackingId, title, author, callNumber);
        assertNotNull(itemHoldResponse);
    }

    @Test
    public void cancelHold() throws Exception {
        String itemBarcode = getBibEntity().getItemEntities().get(0).getBarcode();
        String itemIdentifier = getBibEntity().getItemEntities().get(0).getBarcode();
        String patronBarcode = "23333095887111";
        String institutionId = "NYPL";
        String expirationDate = "";
        String bibId = "";
        String pickupLocation = "";
        String trackingId = "1231";

        String apiUrl = nyplDataApiUrl + "/recap/cancel-hold-requests";
        CancelHoldRequest cancelHoldRequest = new CancelHoldRequest();
        HttpEntity<CancelHoldRequest> requestEntity = new HttpEntity(cancelHoldRequest, getHttpHeaders());
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil()).thenReturn(nyplApiResponseUtil);
        Mockito.when(nyplApiServiceConnector.getNyplDataApiUrl()).thenReturn(nyplDataApiUrl);
        Mockito.when(nyplApiServiceConnector.getNyplOauthTokenApiService()).thenReturn(nyplOauthTokenApiService);
        Mockito.when(nyplApiServiceConnector.getNyplJobResponsePollingProcessor()).thenReturn(nyplJobResponsePollingProcessor);
        Mockito.when(nyplApiServiceConnector.getLogger()).thenReturn(logger);
        Mockito.when(nyplApiServiceConnector.getRestTemplate()).thenReturn(restTemplate);
        Mockito.when(nyplApiServiceConnector.getCancelHoldRequest()).thenReturn(cancelHoldRequest);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().getItemOwningInstitutionByItemBarcode(itemIdentifier)).thenCallRealMethod();
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().getItemDetailsRepository()).thenReturn(itemDetailsRepository);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().getItemDetailsRepository().findByBarcode(itemBarcode)).thenReturn(getBibEntity().getItemEntities());
        ResponseEntity<CancelHoldResponse> responseEntity = new ResponseEntity<CancelHoldResponse>(getCancelHoldResponse(),HttpStatus.OK);
        CancelHoldResponse cancelHoldResponse = responseEntity.getBody();
        Mockito.when(restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CancelHoldResponse.class)).thenReturn(responseEntity);
        Mockito.when(nyplApiServiceConnector.getNyplApiResponseUtil().buildItemCancelHoldResponse(cancelHoldResponse)).thenCallRealMethod();
        Mockito.when(nyplApiServiceConnector.getNyplJobResponsePollingProcessor().pollNyplRequestItemJobResponse(Mockito.any())).thenCallRealMethod();
        ItemHoldResponse itemHoldResponse1 = new ItemHoldResponse();
        itemHoldResponse1.setScreenMessage("Request cancelled.");
        Mockito.when((ItemHoldResponse)nyplApiServiceConnector.cancelHold(itemBarcode, patronBarcode, institutionId, expirationDate, bibId, pickupLocation, trackingId)).thenCallRealMethod();
        ItemHoldResponse itemHoldResponse = (ItemHoldResponse)nyplApiServiceConnector.cancelHold(itemBarcode, patronBarcode, institutionId, expirationDate, bibId, pickupLocation, trackingId);
        assertNotNull(itemHoldResponse);

    }

    public CheckoutResponse getCheckOutItemResponse(){
        CheckoutResponse checkoutResponse = new CheckoutResponse();
        CheckoutData checkoutData = new CheckoutData();
        checkoutData.setId(123);
        checkoutData.setJobId("14214");
        checkoutData.setProcessed(true);
        checkoutData.setSuccess(true);
        checkoutData.setUpdatedDate("2017-03-30");
        checkoutData.setCreatedDate("2017-03-30");
        checkoutData.setPatronBarcode("23333095887111");
        checkoutData.setItemBarcode("33433001888415");
        checkoutData.setDesiredDateDue("2017-03-30");
        checkoutResponse.setData(checkoutData);
        checkoutResponse.setCount(1);
        checkoutResponse.setStatusCode(1);
        checkoutResponse.setDebugInfo(Arrays.asList(new DebugInfo()));
        return checkoutResponse;
    }

    public CheckinResponse getCheckinResponse(){
        CheckinResponse checkinResponse = new CheckinResponse();
        CheckinData checkinData = new CheckinData();
        checkinData.setId(1);
        checkinData.setJobId("123");
        checkinData.setProcessed(true);
        checkinData.setSuccess(true);
        checkinData.setUpdatedDate("2017-03-30");
        checkinData.setCreatedDate("2017-03-30");
        checkinData.setItemBarcode("33433001888415");
        checkinResponse.setData(checkinData);
        checkinResponse.setCount(1);
        checkinResponse.setStatusCode(1);
        checkinResponse.setDebugInfo(Arrays.asList(new DebugInfo()));
        return checkinResponse;
    }

    public ItemResponse getItemResponse(){
        ItemResponse itemResponse = new ItemResponse();
        ItemData itemData = new ItemData();
        VarField varField = new VarField();
        SubField subField = new SubField();
        subField.setContent("test");
        subField.setTag("test");
        varField.setContent("test");
        varField.setFieldTag("test");
        varField.setInd1("test");
        varField.setInd2("test");
        varField.setFieldTag("test");
        varField.setSubFields(Arrays.asList(subField));

        itemData.setNyplSource("test");
        itemData.setBibIds(Arrays.asList("123"));
        itemData.setId("468");
        itemData.setNyplType("test");
        itemData.setUpdatedDate(new Date().toString());
        itemData.setDeletedDate(new Date().toString());
        itemData.setDeleted(true);
        itemData.setBarcode("33236547125452");
        itemData.setCallNumber("12");
        itemData.setItemType("test");
        itemData.setFixedFields("test");
        itemData.setVarFields(Arrays.asList(varField));

        itemResponse.setItemData(itemData);
        itemResponse.setCount(1);
        itemResponse.setDebugInfo(Arrays.asList(new DebugInfo()));
        itemResponse.setStatusCode(1);
        return itemResponse;
    }

    public ItemInformationResponse getItemInformationResponse(){
        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
        itemInformationResponse.setCirculationStatus("test");
        itemInformationResponse.setSecurityMarker("test");
        itemInformationResponse.setFeeType("test");
        itemInformationResponse.setTransactionDate(new Date().toString());
        itemInformationResponse.setHoldQueueLength("10");
        itemInformationResponse.setTitleIdentifier("test");
        itemInformationResponse.setBibID("1223");
        itemInformationResponse.setDueDate(new Date().toString());
        itemInformationResponse.setExpirationDate(new Date().toString());
        itemInformationResponse.setRecallDate(new Date().toString());
        itemInformationResponse.setCurrentLocation("test");
        itemInformationResponse.setHoldPickupDate(new Date().toString());
        itemInformationResponse.setItemBarcode("123");
        itemInformationResponse.setSuccess(true);
        return itemInformationResponse;
    }


    public BibliographicEntity getBibEntity() throws Exception {

        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setCreatedBy("tst");
        bibliographicEntity.setLastUpdatedBy("tst");
        bibliographicEntity.setOwningInstitutionId(2);
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));
        bibliographicEntity.setCatalogingStatus("Complete");
        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setCreatedBy("tst");
        holdingsEntity.setLastUpdatedBy("tst");
        holdingsEntity.setOwningInstitutionId(2);
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));
        holdingsEntity.setBibliographicEntities(Arrays.asList(bibliographicEntity));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity.setOwningInstitutionId(2);
        itemEntity.setBarcode("0002");
        itemEntity.setCallNumber("x.12321");
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCallNumberType("1");
        itemEntity.setCustomerCode("123");
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("tst");
        itemEntity.setLastUpdatedBy("tst");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        return bibliographicEntity;
    }

    private HttpHeaders getHttpHeaders() throws Exception {
        String authorization = "Bearer " + nyplOauthTokenApiService.generateAccessTokenForNyplApi(getOperatorUserId(), getOperatorPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);
        return headers;
    }

    public CreateHoldResponse getCreateHoldResponse(){
        CreateHoldResponse createHoldResponse = new CreateHoldResponse();
        CreateHoldData createHoldData = new CreateHoldData();
        Description description = new Description();
        description.setAuthor("John");
        description.setCallNumber("PB");
        description.setTitle("test");
        createHoldData.setCreatedDate("2017-03-30");
        createHoldData.setDescription(description);
        createHoldData.setId(1);
        createHoldData.setItemBarcode("33433001888415");
        createHoldData.setPatronBarcode("23333095887111");
        createHoldData.setOwningInstitutionId("NYPL");
        createHoldData.setTrackingId("1231");
        createHoldData.setUpdatedDate("2017-03-30");
        createHoldResponse.setCount(1);
        createHoldResponse.setData(createHoldData);
        createHoldResponse.setDebugInfo(Arrays.asList(new DebugInfo()));
        createHoldResponse.setStatusCode(1);
        return createHoldResponse;
    }

    public NYPLHoldResponse getNyplHoldResponse(){
        NYPLHoldResponse nyplHoldResponse = new NYPLHoldResponse();
        NYPLHoldData nyplHoldData = new NYPLHoldData();
        nyplHoldData.setId(1);
        nyplHoldData.setPatron("45632892514");
        nyplHoldData.setJobId("1231");
        nyplHoldData.setProcessed(true);
        nyplHoldData.setSuccess(true);
        nyplHoldData.setUpdatedDate("2017-03-30");
        nyplHoldData.setCreatedDate("2017-03-30");
        nyplHoldData.setRecordType("test");
        nyplHoldData.setRecord("test");
        nyplHoldData.setNyplSource("test");
        nyplHoldData.setPickupLocation("test");
        nyplHoldData.setNeededBy("test");
        nyplHoldData.setNumberOfCopies(1);
        nyplHoldResponse.setStatusCode(1);
        nyplHoldResponse.setDebugInfo(Arrays.asList(new DebugInfo()));
        nyplHoldResponse.setCount(1);
        nyplHoldResponse.setData(nyplHoldData);
        return nyplHoldResponse;
    }

    public CancelHoldResponse getCancelHoldResponse(){
        CancelHoldResponse cancelHoldResponse = new CancelHoldResponse();
        CancelHoldData cancelHoldData = new CancelHoldData();
        cancelHoldData.setCreatedDate("2017-03-30");
        cancelHoldData.setId(1);
        cancelHoldData.setItemBarcode("33433001888415");
        cancelHoldData.setPatronBarcode("23333095887111");
        cancelHoldData.setOwningInstitutionId("NYPL");
        cancelHoldData.setTrackingId("1231");
        cancelHoldData.setUpdatedDate("2017-03-30");
        cancelHoldData.setJobId("1");
        cancelHoldResponse.setData(cancelHoldData);
        cancelHoldResponse.setStatusCode(1);
        cancelHoldResponse.setCount(1);
        cancelHoldResponse.setDebugInfo(Arrays.asList(new DebugInfo()));
        return cancelHoldResponse;
    }

}
