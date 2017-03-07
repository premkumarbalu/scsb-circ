package org.recap.ils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.recap.processor.NyplJobResponsePollingProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(NyplApiServiceConnector.class);

    @Value("${ils.nypl.data.api}")
    public String nyplDataApiUrl;

    @Autowired
    NyplOauthTokenApiService nyplOauthTokenApiService;

    @Autowired
    NyplApiResponseUtil nyplApiResponseUtil;

    @Autowired
    NyplJobResponsePollingProcessor nyplJobResponsePollingProcessor;

    @Override
    public abstract String getHost();

    @Override
    public abstract String getOperatorUserId();

    @Override
    public abstract String getOperatorPassword();

    @Override
    public abstract String getOperatorLocation();

    @Override
    public ItemInformationResponse lookupItem(String itemIdentifier) {
        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
        try {
            String institutionId = nyplApiResponseUtil.getItemOwningInstitutionByItemBarcode(itemIdentifier);
            String source = nyplApiResponseUtil.getNyplSource(institutionId);
            itemIdentifier = nyplApiResponseUtil.getNormalizedItemIdForNypl(itemIdentifier);
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
            logger.error(ReCAPConstants.LOG_ERROR,httpException);
            itemInformationResponse.setSuccess(false);
            itemInformationResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
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
            checkoutRequest.setDesiredDateDue(nyplApiResponseUtil.getExpirationDateForNypl());

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<CheckoutRequest> requestEntity = new HttpEntity(checkoutRequest, getHttpHeaders());
            ResponseEntity<CheckoutResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, CheckoutResponse.class);
            CheckoutResponse checkoutResponse = responseEntity.getBody();
            itemCheckoutResponse = nyplApiResponseUtil.buildItemCheckoutResponse(checkoutResponse);
            CheckoutData checkoutData = checkoutResponse.getData();
            if (null != checkoutData) {
                String jobId = checkoutData.getJobId();
                itemCheckoutResponse.setJobId(jobId);
                logger.info("Initiated checkout on NYPL");
                logger.info("Nypl checkout job id -> {} " , jobId);
                JobResponse jobResponse = nyplJobResponsePollingProcessor.pollNyplRequestItemJobResponse(itemCheckoutResponse.getJobId());
                String statusMessage = jobResponse.getStatusMessage();
                itemCheckoutResponse.setScreenMessage(statusMessage);
                JobData jobData = jobResponse.getData();
                if (null != jobData) {
                    itemCheckoutResponse.setSuccess(jobData.getSuccess());
                    logger.info("Checkout Finished ->  {} " , jobData.getFinished());
                    logger.info("Checkout Success -> {}", jobData.getSuccess());
                    logger.info(statusMessage);
                } else {
                    itemCheckoutResponse.setSuccess(false);
                    logger.info("Checkout Finished -> {}" , false);
                    logger.info("Checkout Success -> {}", false);
                    logger.info(statusMessage);
                }
            }
        } catch (HttpClientErrorException httpException) {
            logger.error(ReCAPConstants.LOG_ERROR,httpException);
            itemCheckoutResponse.setSuccess(false);
            itemCheckoutResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
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
                logger.info("Initiated checkin on NYPL");
                logger.info("Nypl checkin job id -> {} " , jobId);
                JobResponse jobResponse = nyplJobResponsePollingProcessor.pollNyplRequestItemJobResponse(itemCheckinResponse.getJobId());
                String statusMessage = jobResponse.getStatusMessage();
                itemCheckinResponse.setScreenMessage(statusMessage);
                JobData jobData = jobResponse.getData();
                if (null != jobData) {
                    itemCheckinResponse.setSuccess(jobData.getSuccess());
                    logger.info("Checkin Finished -> {}" , jobData.getFinished());
                    logger.info("Checkin Success -> {}" , jobData.getSuccess());
                    logger.info(statusMessage);
                } else {
                    itemCheckinResponse.setSuccess(false);
                    logger.info("Checkin Finished -> " + false);
                    logger.info("Checkin Success -> " + false);
                    logger.info(statusMessage);
                }
            }
        } catch (HttpClientErrorException httpException) {
            logger.error(ReCAPConstants.LOG_ERROR,httpException);
            itemCheckinResponse.setSuccess(false);
            itemCheckinResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
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
                trackingId = initiateNyplHoldRequest(itemIdentifier, patronIdentifier, itemInstitutionId, pickupLocation);
            }
            CreateHoldRequest createHoldRequest = new CreateHoldRequest();
            createHoldRequest.setTrackingId(trackingId);
            createHoldRequest.setOwningInstitutionId(itemInstitutionId);
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
                    logger.info("Nypl Hold request job id -> {} " , jobId);
                    JobResponse jobResponse = nyplJobResponsePollingProcessor.pollNyplRequestItemJobResponse(itemHoldResponse.getJobId());
                    String statusMessage = jobResponse.getStatusMessage();
                    itemHoldResponse.setScreenMessage(statusMessage);
                    JobData jobData = jobResponse.getData();
                    if (null != jobData) {
                        itemHoldResponse.setSuccess(jobData.getSuccess());
                        logger.info("Hold Finished -> " + jobData.getFinished());
                        logger.info("Hold Success -> " + jobData.getSuccess());
                        logger.info(statusMessage);
                    } else {
                        itemHoldResponse.setSuccess(false);
                        logger.info("Hold Finished -> " + false);
                        logger.info("Hold Success -> " + false);
                        logger.info(statusMessage);
                    }
                }
            }
        } catch (HttpClientErrorException httpException) {
            logger.error(ReCAPConstants.LOG_ERROR,httpException);
            itemHoldResponse.setSuccess(false);
            itemHoldResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
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
            cancelHoldRequest.setOwningInstitutionId(nyplApiResponseUtil.getItemOwningInstitutionByItemBarcode(itemIdentifier));
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
                logger.info("Initiated cancel hold request on NYPL");
                logger.info("Nypl cancel hold request job id -> {}" , jobId);
                JobResponse jobResponse = nyplJobResponsePollingProcessor.pollNyplRequestItemJobResponse(itemHoldResponse.getJobId());
                String statusMessage = jobResponse.getStatusMessage();
                itemHoldResponse.setScreenMessage(statusMessage);
                JobData jobData = jobResponse.getData();
                if (null != jobData) {
                    itemHoldResponse.setSuccess(jobData.getSuccess());
                    logger.info("Cancel hold Finished -> " + jobData.getFinished());
                    logger.info("Cancel hold Success -> " + jobData.getSuccess());
                    logger.info(statusMessage);
                } else {
                    itemHoldResponse.setSuccess(false);
                    logger.info("Cancel hold Finished -> " + false);
                    logger.info("Cancel hold Success -> " + false);
                    logger.info(statusMessage);
                }
            }
        } catch (HttpClientErrorException httpException) {
            logger.error(ReCAPConstants.LOG_ERROR,httpException);
            itemHoldResponse.setSuccess(false);
            itemHoldResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
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
        return jobResponseEntity.getBody();
    }

    private NYPLHoldResponse queryForNyplHoldResponseByTrackingId(String trackingId) throws Exception {
        String apiUrl = nyplDataApiUrl + "/hold-requests/" + trackingId;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity requestEntity = new HttpEntity(getHttpHeaders());
        ResponseEntity<NYPLHoldResponse> jobResponseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, NYPLHoldResponse.class);
        return jobResponseEntity.getBody();
    }

    private HttpHeaders getHttpHeaders() throws Exception {
        String authorization = "Bearer " + nyplOauthTokenApiService.generateAccessTokenForNyplApi(getOperatorUserId(), getOperatorPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);
        return headers;
    }

    private String initiateNyplHoldRequest(String itemIdentifier, String patronIdentifier, String itemInstitutionId, String pickupLocation) throws Exception {
        String trackingId = null;
        String nyplHoldApiUrl = nyplDataApiUrl + "/hold-requests";
        String nyplSource = nyplApiResponseUtil.getNyplSource(itemInstitutionId);
        NyplHoldRequest nyplHoldRequest = new NyplHoldRequest();
        nyplHoldRequest.setRecord(nyplApiResponseUtil.getNormalizedItemIdForNypl(itemIdentifier));
        nyplHoldRequest.setPatron(getPatronIdByPatronBarcode(patronIdentifier));
        nyplHoldRequest.setNyplSource(nyplSource);
        nyplHoldRequest.setRecordType(ReCAPConstants.NYPL_RECORD_TYPE);
        nyplHoldRequest.setPickupLocation(pickupLocation);
        nyplHoldRequest.setNumberOfCopies(1);
        nyplHoldRequest.setNeededBy(nyplApiResponseUtil.getExpirationDateForNypl());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<NyplHoldRequest> requestEntity = new HttpEntity(nyplHoldRequest, getHttpHeaders());
        ResponseEntity<NYPLHoldResponse> responseEntity = restTemplate.exchange(nyplHoldApiUrl, HttpMethod.POST, requestEntity, NYPLHoldResponse.class);
        NYPLHoldResponse nyplHoldResponse = responseEntity.getBody();
        NYPLHoldData nyplHoldData = nyplHoldResponse.getData();
        if (null != nyplHoldData) {
            trackingId = String.valueOf(nyplHoldData.getId());
        }
        return trackingId;
    }

    private String getPatronIdByPatronBarcode(String patronBarcode) throws Exception {
        String patronId = null;
        NyplPatronResponse nyplPatronResponse = queryForPatronResponse(patronBarcode);
        List<NyplPatronData> nyplPatronDatas = nyplPatronResponse.getData();
        if (CollectionUtils.isNotEmpty(nyplPatronDatas)) {
            NyplPatronData nyplPatronData = nyplPatronDatas.get(0);
            patronId = nyplPatronData.getId();
        }
        return patronId;
    }

    private NyplPatronResponse queryForPatronResponse(String patronIdentifier) throws Exception {
        String apiUrl = nyplDataApiUrl + "/patrons?barcode=" + patronIdentifier;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity requestEntity = new HttpEntity(getHttpHeaders());
        ResponseEntity<NyplPatronResponse> jobResponseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, NyplPatronResponse.class);
        return jobResponseEntity.getBody();
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
