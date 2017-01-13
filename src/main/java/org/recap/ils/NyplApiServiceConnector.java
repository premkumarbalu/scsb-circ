package org.recap.ils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.recap.ReCAPConstants;
import org.recap.ils.model.nypl.*;
import org.recap.ils.model.nypl.request.CancelHoldRequest;
import org.recap.ils.model.nypl.request.CheckinRequest;
import org.recap.ils.model.nypl.request.CheckoutRequest;
import org.recap.ils.model.nypl.request.CreateHoldRequest;
import org.recap.ils.model.nypl.response.*;
import org.recap.ils.model.response.*;
import org.recap.ils.service.NyplApiResponseUtil;
import org.recap.ils.service.NyplOauthTokenApiService;
import org.recap.model.ItemEntity;
import org.recap.processor.NyplJobResponsePollingProcessor;
import org.recap.repository.ItemDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rajeshbabuk on 19/12/16.
 */
@Component
public abstract class NyplApiServiceConnector implements IJSIPConnector {

    private Logger logger = Logger.getLogger(NyplApiServiceConnector.class);

    @Value("${ils.nypl.data.api}")
    public String nyplDataApiUrl;

    @Autowired
    NyplOauthTokenApiService nyplOauthTokenApiService;

    @Autowired
    NyplApiResponseUtil nyplApiResponseUtil;

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    @Autowired
    NyplJobResponsePollingProcessor nyplJobResponsePollingProcessor;

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

            CheckoutRequest checkoutRequest = new CheckoutRequest();
            checkoutRequest.setPatronBarcode(patronIdentifier);
            checkoutRequest.setItemBarcode(itemIdentifier);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<CheckoutRequest> requestEntity = new HttpEntity(checkoutRequest, getHttpHeaders());
            ResponseEntity<CheckoutResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CheckoutResponse.class);
            CheckoutResponse checkoutResponse = responseEntity.getBody();
            itemCheckoutResponse = nyplApiResponseUtil.buildItemCheckoutResponse(checkoutResponse);
            CheckoutData checkoutData = checkoutResponse.getData();
            if (null != checkoutData) {
                String jobId = checkoutData.getJobId();
                itemCheckoutResponse.setJobId(jobId);
                Boolean success = nyplJobResponsePollingProcessor.pollNyplRequestItemJobResponse(itemCheckoutResponse.getJobId());
                if (success) {
                    itemCheckoutResponse.setScreenMessage(ReCAPConstants.CHECKOUT_SUCCESS);
                    itemCheckoutResponse.setSuccess(success);
                } else {
                    itemCheckoutResponse.setScreenMessage(ReCAPConstants.CHECKOUT_FAILED);
                    itemCheckoutResponse.setSuccess(false);
                }
            }
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

            CheckinRequest checkinRequest = new CheckinRequest();
            checkinRequest.setItemBarcode(itemIdentifier);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<CheckinRequest> requestEntity = new HttpEntity(checkinRequest, getHttpHeaders());
            ResponseEntity<CheckinResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CheckinResponse.class);
            CheckinResponse checkinResponse = responseEntity.getBody();
            itemCheckinResponse = nyplApiResponseUtil.buildItemCheckinResponse(checkinResponse);
            CheckinData checkinData = checkinResponse.getData();
            if (null != checkinData) {
                String jobId = checkinData.getJobId();
                itemCheckinResponse.setJobId(jobId);
                Boolean success = nyplJobResponsePollingProcessor.pollNyplRequestItemJobResponse(itemCheckinResponse.getJobId());
                if (success) {
                    itemCheckinResponse.setScreenMessage(ReCAPConstants.CHECKIN_SUCCESS);
                    itemCheckinResponse.setSuccess(success);
                } else {
                    itemCheckinResponse.setScreenMessage(ReCAPConstants.CHECKIN_FAILED);
                    itemCheckinResponse.setSuccess(false);
                }
            }
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
    public AbstractResponseItem placeHold(String itemIdentifier, String patronIdentifier, String callInstitutionId, String itemInstitutionId, String expirationDate, String bibId, String pickupLocation, String trackingId, String title, String author, String callNumber) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        try {
            String recapHoldApiUrl = nyplDataApiUrl + "/recap/hold-requests";
            if (StringUtils.isBlank(trackingId)) {
                // TODO : Initiate nypl hold request to get tracking Id.
                logger.info("Tracking Id is required");
                itemHoldResponse.setScreenMessage(ReCAPConstants.TRACKING_ID_REQUIRED);
                itemHoldResponse.setSuccess(false);
                return itemHoldResponse;
            }
            CreateHoldRequest createHoldRequest = new CreateHoldRequest();
            createHoldRequest.setTrackingId(trackingId);
            createHoldRequest.setOwningInstitutionId(callInstitutionId);
            createHoldRequest.setItemBarcode(itemIdentifier);
            createHoldRequest.setPatronBarcode(patronIdentifier);
            Description description = new Description();
            description.setTitle(title);
            description.setAuthor(author);
            description.setCallNumber(callNumber);
            createHoldRequest.setDescription(description);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<CreateHoldRequest> requestEntity = new HttpEntity(createHoldRequest, getHttpHeaders());
            ResponseEntity<CreateHoldResponse> responseEntity = restTemplate.exchange(recapHoldApiUrl, HttpMethod.POST, requestEntity, CreateHoldResponse.class);
            CreateHoldResponse createHoldResponse = responseEntity.getBody();
            itemHoldResponse = nyplApiResponseUtil.buildItemHoldResponse(createHoldResponse);
            CreateHoldData createHoldData = createHoldResponse.getData();
            if (null != createHoldData) {
                String responseTrackingId = createHoldData.getTrackingId();
                NYPLHoldResponse nyplHoldResponse = queryForNyplHoldResponseByTrackingId(responseTrackingId);
                NYPLHoldData nyplHoldData = nyplHoldResponse.getData();
                if (null != nyplHoldData) {
                    String jobId = nyplHoldData.getJobId();
                    itemHoldResponse.setJobId(jobId);
                    logger.info("Initiated recap hold request on NYPL");
                    logger.info("Nypl Hold request job id -> " + jobId);
                    Boolean success = nyplJobResponsePollingProcessor.pollNyplRequestItemJobResponse(itemHoldResponse.getJobId());
                    if (success) {
                        itemHoldResponse.setScreenMessage(ReCAPConstants.HOLD_SUCCESS);
                        itemHoldResponse.setSuccess(success);
                        logger.info("Success -> " + success);
                        logger.info(ReCAPConstants.HOLD_SUCCESS);
                    } else {
                        itemHoldResponse.setScreenMessage(ReCAPConstants.HOLD_FAILED);
                        itemHoldResponse.setSuccess(false);
                        logger.info("Success -> " + false);
                        logger.info(ReCAPConstants.HOLD_FAILED);
                    }
                }
            }
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
    public AbstractResponseItem cancelHold(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation, String trackingId) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        try {
            String apiUrl = nyplDataApiUrl + "/recap/cancel-hold-requests";

            CancelHoldRequest cancelHoldRequest = new CancelHoldRequest();
            cancelHoldRequest.setTrackingId(trackingId);
            cancelHoldRequest.setOwningInstitutionId(institutionId);
            cancelHoldRequest.setItemBarcode(itemIdentifier);
            cancelHoldRequest.setPatronBarcode(patronIdentifier);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<CancelHoldRequest> requestEntity = new HttpEntity(cancelHoldRequest, getHttpHeaders());
            ResponseEntity<CancelHoldResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CancelHoldResponse.class);
            CancelHoldResponse cancelHoldResponse = responseEntity.getBody();
            itemHoldResponse = nyplApiResponseUtil.buildItemCancelHoldResponse(cancelHoldResponse);
            CancelHoldData cancelHoldData = cancelHoldResponse.getData();
            if (null != cancelHoldData) {
                String jobId = cancelHoldData.getJobId();
                itemHoldResponse.setJobId(jobId);
                Boolean success = nyplJobResponsePollingProcessor.pollNyplRequestItemJobResponse(itemHoldResponse.getJobId());
                if (success) {
                    itemHoldResponse.setScreenMessage(ReCAPConstants.CANCEL_HOLD_SUCCESS);
                    itemHoldResponse.setSuccess(success);
                } else {
                    itemHoldResponse.setScreenMessage(ReCAPConstants.CANCEL_HOLD_FAILED);
                    itemHoldResponse.setSuccess(false);
                }
            }
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

    public JobResponse queryForJob(String jobId) throws Exception {
        String apiUrl = nyplDataApiUrl + "/jobs/" + jobId;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity requestEntity = new HttpEntity(getHttpHeaders());
        ResponseEntity<JobResponse> jobResponseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, JobResponse.class);
        JobResponse jobResponse = jobResponseEntity.getBody();
        return jobResponse;
    }

    private NYPLHoldResponse queryForNyplHoldResponseByTrackingId(String trackingId) throws Exception {
        String apiUrl = nyplDataApiUrl + "/hold-requests/" + trackingId;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity requestEntity = new HttpEntity(getHttpHeaders());
        ResponseEntity<NYPLHoldResponse> jobResponseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, NYPLHoldResponse.class);
        NYPLHoldResponse nyplHoldResponse = jobResponseEntity.getBody();
        return nyplHoldResponse;
    }

    private HttpHeaders getHttpHeaders() throws Exception {
        String authorization = "Bearer " + nyplOauthTokenApiService.generateAccessTokenForNyplApi(getOperatorUserId(), getOperatorPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);
        return headers;
    }

    @Override
    public Object createBib(String itemIdentifier, String patronIdentifier, String institutionId, String titleIdentifier) {
        return null;
    }

    @Override
    public boolean patronValidation(String institutionId, String patronIdentifier) {
        return true;
    }

    @Override
    public AbstractResponseItem lookupPatron(String patronIdentifier) {
        return null;
    }

    @Override
    public Object recallItem(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation) {
        return null;
    }
}
