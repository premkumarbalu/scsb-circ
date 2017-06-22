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

    /**
     * The Nypl data api url.
     */
    @Value("${ils.nypl.data.api}")
    public String nyplDataApiUrl;

    /**
     * The Nypl oauth token api service.
     */
    @Autowired
    NyplOauthTokenApiService nyplOauthTokenApiService;

    /**
     * The Nypl api response util.
     */
    @Autowired
    NyplApiResponseUtil nyplApiResponseUtil;

    /**
     * The Nypl job response polling processor.
     */
    @Autowired
    NyplJobResponsePollingProcessor nyplJobResponsePollingProcessor;

    /**
     * Gets nypl api response util.
     *
     * @return the nypl api response util
     */
    public NyplApiResponseUtil getNyplApiResponseUtil() {
        return nyplApiResponseUtil;
    }

    /**
     * Gets nypl oauth token api service.
     *
     * @return the nypl oauth token api service
     */
    public NyplOauthTokenApiService getNyplOauthTokenApiService() {
        return nyplOauthTokenApiService;
    }

    /**
     * Gets nypl job response polling processor.
     *
     * @return the nypl job response polling processor
     */
    public NyplJobResponsePollingProcessor getNyplJobResponsePollingProcessor() {
        return nyplJobResponsePollingProcessor;
    }

    /**
     * Gets nypl data api url.
     *
     * @return the nypl data api url
     */
    public String getNyplDataApiUrl() {
        return nyplDataApiUrl;
    }

    /**
     * Get rest template rest template.
     *
     * @return the rest template
     */
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    /**
     * Get http header http headers.
     *
     * @return the http headers
     */
    public HttpHeaders getHttpHeader(){
        return new HttpHeaders();
    }

    /**
     * Get http entity http entity.
     *
     * @param headers the headers
     * @return the http entity
     */
    public HttpEntity getHttpEntity(HttpHeaders headers){
        return new HttpEntity(headers);
    }

    /**
     * Get api url string.
     *
     * @param source         the source
     * @param itemIdentifier the item identifier
     * @return the string
     */
    public String getApiUrl(String source,String itemIdentifier){
        return getNyplDataApiUrl() + "/items/" + source + "/" + itemIdentifier;
    }

    /**
     * Get check out request checkout request.
     *
     * @return the checkout request
     */
    public CheckoutRequest getCheckOutRequest(){
        return new CheckoutRequest();
    }

    /**
     * Gets logger.
     *
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Get check in request checkin request.
     *
     * @return the checkin request
     */
    public CheckinRequest getCheckInRequest(){
        return new CheckinRequest();
    }

    /**
     * Get create hold request create hold request.
     *
     * @return the create hold request
     */
    public CreateHoldRequest getCreateHoldRequest(){
        return new CreateHoldRequest();
    }

    /**
     * Get cancel hold request cancel hold request.
     *
     * @return the cancel hold request
     */
    public CancelHoldRequest getCancelHoldRequest(){
        return new CancelHoldRequest();
    }

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
            String institutionId = getNyplApiResponseUtil().getItemOwningInstitutionByItemBarcode(itemIdentifier);
            String source = getNyplApiResponseUtil().getNyplSource(institutionId);
            itemIdentifier = getNyplApiResponseUtil().getNormalizedItemIdForNypl(itemIdentifier);
            String apiUrl = getApiUrl(source,itemIdentifier);
            String authorization = "Bearer " + getNyplOauthTokenApiService().generateAccessTokenForNyplApi(getOperatorUserId(), getOperatorPassword());

            HttpHeaders headers = getHttpHeader();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", authorization);

            HttpEntity requestEntity = getHttpEntity(headers);
            ResponseEntity<ItemResponse> responseEntity = getRestTemplate().exchange(apiUrl, HttpMethod.GET, requestEntity, ItemResponse.class);
            ItemResponse itemResponse = responseEntity.getBody();
            itemInformationResponse = getNyplApiResponseUtil().buildItemInformationResponse(itemResponse);
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
            String apiUrl = getNyplDataApiUrl() + "/checkout-requests";

            CheckoutRequest checkoutRequest = getCheckOutRequest();
            checkoutRequest.setPatronBarcode(patronIdentifier);
            checkoutRequest.setItemBarcode(itemIdentifier);
            checkoutRequest.setDesiredDateDue(getNyplApiResponseUtil().getExpirationDateForNypl());


            HttpEntity<CheckoutRequest> requestEntity = new HttpEntity(checkoutRequest, getHttpHeaders());
            ResponseEntity<CheckoutResponse> responseEntity = getRestTemplate().exchange(apiUrl, HttpMethod.POST, requestEntity, CheckoutResponse.class);
            CheckoutResponse checkoutResponse = responseEntity.getBody();
            itemCheckoutResponse = getNyplApiResponseUtil().buildItemCheckoutResponse(checkoutResponse);
            CheckoutData checkoutData = checkoutResponse.getData();
            if (null != checkoutData) {
                String jobId = checkoutData.getJobId();
                itemCheckoutResponse.setJobId(jobId);
                getLogger().info("Initiated checkout on NYPL");
                getLogger().info("Nypl checkout job id -> {} " , jobId);
                JobResponse jobResponse = getNyplJobResponsePollingProcessor().pollNyplRequestItemJobResponse(itemCheckoutResponse.getJobId());
                String statusMessage = jobResponse.getStatusMessage();
                itemCheckoutResponse.setScreenMessage(statusMessage);
                JobData jobData = jobResponse.getData();
                if (null != jobData) {
                    itemCheckoutResponse.setSuccess(jobData.getSuccess());
                    getLogger().info("Checkout Finished ->  {} " , jobData.getFinished());
                    getLogger().info("Checkout Success -> {}", jobData.getSuccess());
                    getLogger().info(statusMessage);
                } else {
                    itemCheckoutResponse.setSuccess(false);
                    getLogger().info("Checkout Finished -> {}" , false);
                    getLogger().info("Checkout Success -> {}", false);
                    getLogger().info(statusMessage);
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
            String apiUrl = getNyplDataApiUrl() + "/checkin-requests";

            CheckinRequest checkinRequest = getCheckInRequest();
            checkinRequest.setItemBarcode(itemIdentifier);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<CheckinRequest> requestEntity = new HttpEntity(checkinRequest, getHttpHeaders());
            ResponseEntity<CheckinResponse> responseEntity = getRestTemplate().exchange(apiUrl, HttpMethod.POST, requestEntity, CheckinResponse.class);
            CheckinResponse checkinResponse = responseEntity.getBody();
            itemCheckinResponse = getNyplApiResponseUtil().buildItemCheckinResponse(checkinResponse);
            CheckinData checkinData = checkinResponse.getData();
            if (null != checkinData) {
                String jobId = checkinData.getJobId();
                itemCheckinResponse.setJobId(jobId);
                getLogger().info("Initiated checkin on NYPL");
                getLogger().info("Nypl checkin job id -> {} " , jobId);
                JobResponse jobResponse = getNyplJobResponsePollingProcessor().pollNyplRequestItemJobResponse(itemCheckinResponse.getJobId());
                String statusMessage = jobResponse.getStatusMessage();
                itemCheckinResponse.setScreenMessage(statusMessage);
                JobData jobData = jobResponse.getData();
                if (null != jobData) {
                    itemCheckinResponse.setSuccess(jobData.getSuccess());
                    getLogger().info("Checkin Finished -> {}" , jobData.getFinished());
                    getLogger().info("Checkin Success -> {}" , jobData.getSuccess());
                    getLogger().info(statusMessage);
                } else {
                    itemCheckinResponse.setSuccess(false);
                    getLogger().info("Checkin Finished -> " + false);
                    getLogger().info("Checkin Success -> " + false);
                    getLogger().info(statusMessage);
                }
            }
        } catch (HttpClientErrorException httpException) {
            getLogger().error(ReCAPConstants.LOG_ERROR,httpException);
            itemCheckinResponse.setSuccess(false);
            itemCheckinResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            getLogger().error(ReCAPConstants.LOG_ERROR,e);
            itemCheckinResponse.setSuccess(false);
            itemCheckinResponse.setScreenMessage(e.getMessage());
        }
        return itemCheckinResponse;
    }

    @Override
    public AbstractResponseItem placeHold(String itemIdentifier, String patronIdentifier, String callInstitutionId, String itemInstitutionId, String expirationDate, String bibId, String deliveryLocation, String trackingId, String title, String author, String callNumber) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        try {
            String recapHoldApiUrl = getNyplDataApiUrl() + "/recap/hold-requests";
            if (StringUtils.isBlank(trackingId)) {
                trackingId = initiateNyplHoldRequest(itemIdentifier, patronIdentifier, itemInstitutionId, deliveryLocation);
            }
            CreateHoldRequest createHoldRequest = getCreateHoldRequest();
            createHoldRequest.setTrackingId(trackingId);
            createHoldRequest.setOwningInstitutionId(itemInstitutionId);
            createHoldRequest.setItemBarcode(itemIdentifier);
            createHoldRequest.setPatronBarcode(patronIdentifier);
            Description description = new Description();
            description.setTitle(title);
            description.setAuthor(author);
            description.setCallNumber(callNumber);
            createHoldRequest.setDescription(description);

            HttpEntity<CreateHoldRequest> requestEntity = new HttpEntity(createHoldRequest, getHttpHeaders());
            ResponseEntity<CreateHoldResponse> responseEntity = getRestTemplate().exchange(recapHoldApiUrl, HttpMethod.POST, requestEntity, CreateHoldResponse.class);
            CreateHoldResponse createHoldResponse = responseEntity.getBody();
            itemHoldResponse = getNyplApiResponseUtil().buildItemHoldResponse(createHoldResponse);
            CreateHoldData createHoldData = createHoldResponse.getData();
            if (null != createHoldData) {
                String responseTrackingId = createHoldData.getTrackingId();
                NYPLHoldResponse nyplHoldResponse = queryForNyplHoldResponseByTrackingId(responseTrackingId);
                NYPLHoldData nyplHoldData = nyplHoldResponse.getData();
                if (null != nyplHoldData) {
                    String jobId = nyplHoldData.getJobId();
                    itemHoldResponse.setJobId(jobId);
                    getLogger().info("Initiated recap hold request on NYPL");
                    getLogger().info("Nypl Hold request job id -> {} " , jobId);
                    JobResponse jobResponse = getNyplJobResponsePollingProcessor().pollNyplRequestItemJobResponse(itemHoldResponse.getJobId());
                    String statusMessage = jobResponse.getStatusMessage();
                    itemHoldResponse.setScreenMessage(statusMessage);
                    JobData jobData = jobResponse.getData();
                    if (null != jobData) {
                        itemHoldResponse.setSuccess(jobData.getSuccess());
                        getLogger().info("Hold Finished -> " + jobData.getFinished());
                        getLogger().info("Hold Success -> " + jobData.getSuccess());
                        getLogger().info(statusMessage);
                    } else {
                        itemHoldResponse.setSuccess(false);
                        getLogger().info("Hold Finished -> " + false);
                        getLogger().info("Hold Success -> " + false);
                        getLogger().info(statusMessage);
                    }
                }
            }
        } catch (HttpClientErrorException httpException) {
            getLogger().error(ReCAPConstants.LOG_ERROR,httpException);
            itemHoldResponse.setSuccess(false);
            itemHoldResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            getLogger().error(ReCAPConstants.LOG_ERROR,e);
            itemHoldResponse.setSuccess(false);
            itemHoldResponse.setScreenMessage(e.getMessage());
        }
        return itemHoldResponse;
    }

    @Override
    public AbstractResponseItem cancelHold(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation, String trackingId) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        try {
            String apiUrl = getNyplDataApiUrl() + "/recap/cancel-hold-requests";

            CancelHoldRequest cancelHoldRequest = getCancelHoldRequest();
            cancelHoldRequest.setTrackingId(trackingId);
            cancelHoldRequest.setOwningInstitutionId(getNyplApiResponseUtil().getItemOwningInstitutionByItemBarcode(itemIdentifier));
            cancelHoldRequest.setItemBarcode(itemIdentifier);
            cancelHoldRequest.setPatronBarcode(patronIdentifier);

            HttpEntity<CancelHoldRequest> requestEntity = new HttpEntity(cancelHoldRequest, getHttpHeaders());
            ResponseEntity<CancelHoldResponse> responseEntity = getRestTemplate().exchange(apiUrl, HttpMethod.POST, requestEntity, CancelHoldResponse.class);
            CancelHoldResponse cancelHoldResponse = responseEntity.getBody();
            itemHoldResponse = getNyplApiResponseUtil().buildItemCancelHoldResponse(cancelHoldResponse);
            CancelHoldData cancelHoldData = cancelHoldResponse.getData();
            if (null != cancelHoldData) {
                String jobId = cancelHoldData.getJobId();
                itemHoldResponse.setJobId(jobId);
                getLogger().info("Initiated cancel hold request on NYPL");
                getLogger().info("Nypl cancel hold request job id -> {}" , jobId);
                JobResponse jobResponse = getNyplJobResponsePollingProcessor().pollNyplRequestItemJobResponse(itemHoldResponse.getJobId());
                String statusMessage = jobResponse.getStatusMessage();
                itemHoldResponse.setScreenMessage(statusMessage);
                JobData jobData = jobResponse.getData();
                if (null != jobData) {
                    itemHoldResponse.setSuccess(jobData.getSuccess());
                    getLogger().info("Cancel hold Finished -> " + jobData.getFinished());
                    getLogger().info("Cancel hold Success -> " + jobData.getSuccess());
                    getLogger().info(statusMessage);
                } else {
                    itemHoldResponse.setSuccess(false);
                    getLogger().info("Cancel hold Finished -> " + false);
                    getLogger().info("Cancel hold Success -> " + false);
                    getLogger().info(statusMessage);
                }
            }
        } catch (HttpClientErrorException httpException) {
            getLogger().error(ReCAPConstants.LOG_ERROR,httpException);
            itemHoldResponse.setSuccess(false);
            itemHoldResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            getLogger().error(ReCAPConstants.LOG_ERROR,e);
            itemHoldResponse.setSuccess(false);
            itemHoldResponse.setScreenMessage(e.getMessage());
        }
        return itemHoldResponse;
    }

    /**
     * Query for job job response.
     *
     * @param jobId the job id
     * @return the job response
     * @throws Exception the exception
     */
    public JobResponse queryForJob(String jobId) throws Exception {
        String apiUrl = nyplDataApiUrl + "/jobs/" + jobId;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity requestEntity = new HttpEntity(getHttpHeaders());
        ResponseEntity<JobResponse> jobResponseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, JobResponse.class);
        return jobResponseEntity.getBody();
    }

    private NYPLHoldResponse queryForNyplHoldResponseByTrackingId(String trackingId) throws Exception {
        String apiUrl = getNyplDataApiUrl() + "/hold-requests/" + trackingId;
        HttpHeaders headers = getHttpHeaders();
        HttpEntity requestEntity = getHttpEntity(headers);
        ResponseEntity<NYPLHoldResponse> jobResponseEntity = getRestTemplate().exchange(apiUrl, HttpMethod.GET, requestEntity, NYPLHoldResponse.class);
        return jobResponseEntity.getBody();
    }

    private HttpHeaders getHttpHeaders() throws Exception {
        String authorization = "Bearer " + getNyplOauthTokenApiService().generateAccessTokenForNyplApi(getOperatorUserId(), getOperatorPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);
        return headers;
    }

    private String initiateNyplHoldRequest(String itemIdentifier, String patronIdentifier, String itemInstitutionId, String deliveryLocation) throws Exception {
        String trackingId = null;
        String nyplHoldApiUrl = nyplDataApiUrl + "/hold-requests";
        String nyplSource = nyplApiResponseUtil.getNyplSource(itemInstitutionId);
        NyplHoldRequest nyplHoldRequest = new NyplHoldRequest();
        nyplHoldRequest.setRecord(nyplApiResponseUtil.getNormalizedItemIdForNypl(itemIdentifier));
        nyplHoldRequest.setPatron(getPatronIdByPatronBarcode(patronIdentifier));
        nyplHoldRequest.setNyplSource(nyplSource);
        nyplHoldRequest.setRecordType(ReCAPConstants.NYPL_RECORD_TYPE);
        nyplHoldRequest.setPickupLocation("");
        nyplHoldRequest.setDeliveryLocation(deliveryLocation);
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
        ItemRecallResponse itemRecallResponse = new ItemRecallResponse();
        itemRecallResponse.setSuccess(true);
        return itemRecallResponse;
    }
}
