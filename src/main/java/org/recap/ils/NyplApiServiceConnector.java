package org.recap.ils;

import org.recap.ils.model.*;
import org.recap.ils.model.request.CancelHoldRequest;
import org.recap.ils.model.request.CheckinRequest;
import org.recap.ils.model.request.CheckoutRequest;
import org.recap.ils.model.request.CreateHoldRequest;
import org.recap.ils.model.response.*;
import org.recap.ils.service.NyplApiResponseUtil;
import org.recap.ils.service.NyplOauthTokenApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Created by rajeshbabuk on 19/12/16.
 */
@Component
public abstract class NyplApiServiceConnector implements IJSIPConnector {

    @Value("${ils.nypl.data.api}")
    public String nyplDataApiUrl;

    @Autowired
    NyplOauthTokenApiService nyplOauthTokenApiService;

    @Autowired
    NyplApiResponseUtil nyplApiResponseUtil;

    public abstract String getHost();

    public abstract String getOperatorUserId();

    public abstract String getOperatorPassword();

    public abstract String getOperatorLocation();

    @Override
    public ItemInformationResponse lookupItem(String itemIdentifier, String source) {
        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
        try {
            String apiUrl = nyplDataApiUrl + "/items/" + source + "/" + itemIdentifier;
            String authorization = "Bearer " + nyplOauthTokenApiService.generateAccessTokenForNyplApi(getOperatorUserId(), getOperatorPassword());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", authorization);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity(headers);
            ResponseEntity<ItemResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, ItemResponse.class);
            ItemResponse itemResponse = responseEntity.getBody();
            itemInformationResponse = nyplApiResponseUtil.buildItemInformationResponse(itemResponse);
        } catch (HttpClientErrorException httpException) {
            httpException.printStackTrace();
            itemInformationResponse.setSuccess(false);
            itemInformationResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
            itemInformationResponse.setSuccess(false);
            itemInformationResponse.setScreenMessage(e.getMessage());
        }
        return itemInformationResponse;
    }

    @Override
    public ItemCheckoutResponse checkOutItem(String itemIdentifier, String patronIdentifier) {
        ItemCheckoutResponse itemCheckoutResponse = new ItemCheckoutResponse();
        try {
            String apiUrl = nyplDataApiUrl + "/checkout-requests";
            String authorization = "Bearer " + nyplOauthTokenApiService.generateAccessTokenForNyplApi(getOperatorUserId(), getOperatorPassword());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", authorization);

            CheckoutRequest checkoutRequest = new CheckoutRequest();
            checkoutRequest.setPatronBarcode(patronIdentifier);
            checkoutRequest.setItemBarcode(itemIdentifier);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<CheckoutRequest> requestEntity = new HttpEntity(checkoutRequest, headers);
            ResponseEntity<CheckoutResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CheckoutResponse.class);
            CheckoutResponse checkoutResponse = responseEntity.getBody();
            itemCheckoutResponse = nyplApiResponseUtil.buildItemCheckoutResponse(checkoutResponse);
        } catch (HttpClientErrorException httpException) {
            httpException.printStackTrace();
            itemCheckoutResponse.setSuccess(false);
            itemCheckoutResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
            itemCheckoutResponse.setSuccess(false);
            itemCheckoutResponse.setScreenMessage(e.getMessage());
        }
        return itemCheckoutResponse;
    }

    @Override
    public ItemCheckinResponse checkInItem(String itemIdentifier, String patronIdentifier) {
        ItemCheckinResponse itemCheckinResponse = new ItemCheckinResponse();
        try {
            String apiUrl = nyplDataApiUrl + "/checkin-requests";
            String authorization = "Bearer " + nyplOauthTokenApiService.generateAccessTokenForNyplApi(getOperatorUserId(), getOperatorPassword());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", authorization);

            CheckinRequest checkinRequest = new CheckinRequest();
            checkinRequest.setItemBarcode(itemIdentifier);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<CheckinRequest> requestEntity = new HttpEntity(checkinRequest, headers);
            ResponseEntity<CheckinResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CheckinResponse.class);
            CheckinResponse checkinResponse = responseEntity.getBody();
            itemCheckinResponse = nyplApiResponseUtil.buildItemCheckinResponse(checkinResponse);
        } catch (HttpClientErrorException httpException) {
            httpException.printStackTrace();
            itemCheckinResponse.setSuccess(false);
            itemCheckinResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
            itemCheckinResponse.setSuccess(false);
            itemCheckinResponse.setScreenMessage(e.getMessage());
        }
        return itemCheckinResponse;
    }

    @Override
    public ItemHoldResponse placeHold(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation, String trackingId, String title, String author, String callNumber) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        try {
            String apiUrl = nyplDataApiUrl + "/recap/hold-requests";
            String authorization = "Bearer " + nyplOauthTokenApiService.generateAccessTokenForNyplApi(getOperatorUserId(), getOperatorPassword());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", authorization);

            CreateHoldRequest createHoldRequest = new CreateHoldRequest();
            createHoldRequest.setTrackingId(trackingId);
            createHoldRequest.setOwningInstitutionId(institutionId);
            createHoldRequest.setItemBarcode(itemIdentifier);
            createHoldRequest.setPatronBarcode(patronIdentifier);
            Description description = new Description();
            description.setTitle(title);
            description.setAuthor(author);
            description.setCallNumber(callNumber);
            createHoldRequest.setDescription(description);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<CreateHoldRequest> requestEntity = new HttpEntity(createHoldRequest, headers);
            ResponseEntity<CreateHoldResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CreateHoldResponse.class);
            CreateHoldResponse createHoldResponse = responseEntity.getBody();
            itemHoldResponse = nyplApiResponseUtil.buildItemHoldResponse(createHoldResponse);
        } catch (HttpClientErrorException httpException) {
            httpException.printStackTrace();
            itemHoldResponse.setSuccess(false);
            itemHoldResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
            itemHoldResponse.setSuccess(false);
            itemHoldResponse.setScreenMessage(e.getMessage());
        }
        return itemHoldResponse;
    }

    @Override
    public ItemHoldResponse cancelHold(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation, String trackingId) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        try {
            String apiUrl = nyplDataApiUrl + "/recap/cancel-hold-requests";
            String authorization = "Bearer " + nyplOauthTokenApiService.generateAccessTokenForNyplApi(getOperatorUserId(), getOperatorPassword());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", authorization);

            CancelHoldRequest cancelHoldRequest = new CancelHoldRequest();
            cancelHoldRequest.setTrackingId(trackingId);
            cancelHoldRequest.setOwningInstitutionId(institutionId);
            cancelHoldRequest.setItemBarcode(itemIdentifier);
            cancelHoldRequest.setPatronBarcode(patronIdentifier);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<CancelHoldRequest> requestEntity = new HttpEntity(cancelHoldRequest, headers);
            ResponseEntity<CancelHoldResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CancelHoldResponse.class);
            CancelHoldResponse cancelHoldResponse = responseEntity.getBody();
            itemHoldResponse = nyplApiResponseUtil.buildItemCancelHoldResponse(cancelHoldResponse);
        } catch (HttpClientErrorException httpException) {
            httpException.printStackTrace();
            itemHoldResponse.setSuccess(false);
            itemHoldResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
            itemHoldResponse.setSuccess(false);
            itemHoldResponse.setScreenMessage(e.getMessage());
        }
        return itemHoldResponse;
    }

    @Override
    public Object createBib(String itemIdentifier, String patronIdentifier, String institutionId, String titleIdentifier) {
        return null;
    }

    @Override
    public boolean patronValidation(String institutionId, String patronIdentifier) {
        return false;
    }

    @Override
    public Object lookupPatron(String patronIdentifier) {
        return null;
    }

    @Override
    public Object recallItem(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation) {
        return null;
    }
}
