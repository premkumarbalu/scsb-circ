package org.recap.ils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.ils.model.nypl.*;
import org.recap.ils.model.nypl.request.*;
import org.recap.ils.model.nypl.response.*;
import org.recap.ils.model.response.*;
import org.recap.ils.service.NyplApiResponseUtil;
import org.recap.ils.service.NyplOauthTokenApiService;
import org.recap.model.ItemRefileResponse;
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

    /**
     * Look up item in NYPL for the given item identifier.
     *
     * @param itemIdentifier the item identifier
     * @return
     */
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

    /**
     * Checks out item in NYPL for the given patron.
     *
     * @param itemIdentifier   the item identifier
     * @param patronIdentifier the patron identifier
     * @return
     */
    @Override
    public ItemCheckoutResponse checkOutItem(String itemIdentifier, String patronIdentifier) {
        ItemCheckoutResponse itemCheckoutResponse = new ItemCheckoutResponse();
        try {
            String apiUrl = getNyplDataApiUrl() + ReCAPConstants.NYPL_CHECKOUT_REQUEST_URL;

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

    /**
     * Checks in item in NYPL for the given patron.
     *
     * @param itemIdentifier   the item identifier
     * @param patronIdentifier the patron identifier
     * @return
     */
    @Override
    public ItemCheckinResponse checkInItem(String itemIdentifier, String patronIdentifier) {
        ItemCheckinResponse itemCheckinResponse = new ItemCheckinResponse();
        try {
            String apiUrl = getNyplDataApiUrl() + ReCAPConstants.NYPL_CHECKIN_REQUEST_URL;

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

    /**
     * Creates a hold request in NYPL with the provided information.
     *
     * @param itemIdentifier    the item identifier
     * @param patronIdentifier  the patron identifier
     * @param callInstitutionId the call institution id
     * @param itemInstitutionId the item institution id
     * @param expirationDate    the expiration date
     * @param bibId             the bib id
     * @param deliveryLocation
     * @param trackingId        the tracking id
     * @param title             the title
     * @param author            the author
     * @param callNumber        the call number
     * @return
     */
    @Override
    public AbstractResponseItem placeHold(String itemIdentifier, String patronIdentifier, String callInstitutionId, String itemInstitutionId, String expirationDate, String bibId, String deliveryLocation, String trackingId, String title, String author, String callNumber) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        try {
            String recapHoldApiUrl = getNyplDataApiUrl() + ReCAPConstants.NYPL_RECAP_HOLD_REQUEST_URL;
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

    /**
     * Cancels hold request in NYPL for the given item.
     *
     * @param itemIdentifier   the item identifier
     * @param patronIdentifier the patron identifier
     * @param institutionId    the institution id
     * @param expirationDate   the expiration date
     * @param bibId            the bib id
     * @param pickupLocation   the pickup location
     * @param trackingId       the tracking id
     * @return
     */
    @Override
    public AbstractResponseItem cancelHold(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation, String trackingId) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        try {
            String apiUrl = getNyplDataApiUrl() + ReCAPConstants.NYPL_RECAP_CANCEL_HOLD_REQUEST_URL;

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
     * Query for job response by job id.
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

    /**
     * Gets the NYPL hold response information by the tracking id.
     *
     * @param trackingId
     * @return
     * @throws Exception
     */
    private NYPLHoldResponse queryForNyplHoldResponseByTrackingId(String trackingId) throws Exception {
        String apiUrl = getNyplDataApiUrl() + "/hold-requests/" + trackingId;
        HttpHeaders headers = getHttpHeaders();
        HttpEntity requestEntity = getHttpEntity(headers);
        ResponseEntity<NYPLHoldResponse> jobResponseEntity = getRestTemplate().exchange(apiUrl, HttpMethod.GET, requestEntity, NYPLHoldResponse.class);
        return jobResponseEntity.getBody();
    }

    /**
     * Build Http headers to access NYPL API.
     *
     * @return
     * @throws Exception
     */
    private HttpHeaders getHttpHeaders() throws Exception {
        String authorization = "Bearer " + getNyplOauthTokenApiService().generateAccessTokenForNyplApi(getOperatorUserId(), getOperatorPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authorization);
        return headers;
    }

    /**
     * This method initiates the hold request on NYPL end to get the tracking id and use it in recap hold request subsequently.
     *
     * @param itemIdentifier
     * @param patronIdentifier
     * @param itemInstitutionId
     * @param deliveryLocation
     * @return
     * @throws Exception
     */
    private String initiateNyplHoldRequest(String itemIdentifier, String patronIdentifier, String itemInstitutionId, String deliveryLocation) throws Exception {
        String trackingId = null;
        String nyplHoldApiUrl = nyplDataApiUrl + ReCAPConstants.NYPL_HOLD_REQUEST_URL;
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

    /**
     * Gets patron id by the given patron barcode from NYPL.
     *
     * @param patronBarcode
     * @return
     * @throws Exception
     */
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

    /**
     * Get patron response information based on the patron id from NYPL.
     *
     * @param patronIdentifier
     * @return
     * @throws Exception
     */
    private NyplPatronResponse queryForPatronResponse(String patronIdentifier) throws Exception {
        String apiUrl = nyplDataApiUrl + ReCAPConstants.NYPL_PATRON_BY_BARCODE_URL + patronIdentifier;
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

    /**
     * Creates a recall request for the item in NYPL.
     *
     * @param itemIdentifier   the item identifier
     * @param patronIdentifier the patron identifier
     * @param institutionId    the institution id
     * @param expirationDate   the expiration date
     * @param bibId            the bib id
     * @param pickupLocation   the pickup location
     * @return
     */
    @Override
    public AbstractResponseItem recallItem(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation) {
        ItemRecallResponse itemRecallResponse = new ItemRecallResponse();
        try {
            String apiUrl = getNyplDataApiUrl() + ReCAPConstants.NYPL_RECAP_RECALL_REQUEST_URL;

            RecallRequest recallRequest = new RecallRequest();
            recallRequest.setOwningInstitutionId(nyplApiResponseUtil.getItemOwningInstitutionByItemBarcode(itemIdentifier));
            recallRequest.setItemBarcode(itemIdentifier);

            HttpEntity<RecallRequest> requestEntity = new HttpEntity(recallRequest, getHttpHeaders());
            ResponseEntity<RecallResponse> responseEntity = getRestTemplate().exchange(apiUrl, HttpMethod.POST, requestEntity, RecallResponse.class);
            RecallResponse recallResponse = responseEntity.getBody();
            itemRecallResponse = getNyplApiResponseUtil().buildItemRecallResponse(recallResponse);
            RecallData recallData = recallResponse.getData();
            if (null != recallData) {
                String jobId = recallData.getJobId();
                itemRecallResponse.setJobId(jobId);
                getLogger().info("Initiated recall request on NYPL");
                getLogger().info("Nypl recall request job id -> {}" , jobId);
                JobResponse jobResponse = getNyplJobResponsePollingProcessor().pollNyplRequestItemJobResponse(itemRecallResponse.getJobId());
                String statusMessage = jobResponse.getStatusMessage();
                itemRecallResponse.setScreenMessage(statusMessage);
                JobData jobData = jobResponse.getData();
                if (null != jobData) {
                    itemRecallResponse.setSuccess(jobData.getSuccess());
                    getLogger().info("Recall request Finished -> " + jobData.getFinished());
                    getLogger().info("Recall request Success -> " + jobData.getSuccess());
                    getLogger().info(statusMessage);
                } else {
                    itemRecallResponse.setSuccess(false);
                    getLogger().info("Recall request Finished -> " + false);
                    getLogger().info("Recall request Success -> " + false);
                    getLogger().info(statusMessage);
                }
            }
        } catch (HttpClientErrorException httpException) {
            getLogger().error(ReCAPConstants.LOG_ERROR,httpException);
            itemRecallResponse.setSuccess(false);
            itemRecallResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            getLogger().error(ReCAPConstants.LOG_ERROR,e);
            itemRecallResponse.setSuccess(false);
            itemRecallResponse.setScreenMessage(e.getMessage());
        }
        return itemRecallResponse;
    }

    /**
     * Creates a refile request for the item in NYPL.
     *
     * @param itemIdentifier the item identifier
     * @return ItemRefileResponse
     */
    @Override
    public ItemRefileResponse refileItem(String itemIdentifier) {
        ItemRefileResponse itemRefileResponse = new ItemRefileResponse();
        try {
            String apiUrl = getNyplDataApiUrl() + ReCAPConstants.NYPL_RECAP_REFILE_REQUEST_URL;

            RefileRequest refileRequest = new RefileRequest();
            refileRequest.setItemBarcode(itemIdentifier);

            HttpEntity<RefileRequest> requestEntity = new HttpEntity(refileRequest, getHttpHeaders());
            ResponseEntity<RefileResponse> responseEntity = getRestTemplate().exchange(apiUrl, HttpMethod.POST, requestEntity, RefileResponse.class);
            RefileResponse refileResponse = responseEntity.getBody();
            itemRefileResponse = getNyplApiResponseUtil().buildItemRefileResponse(refileResponse);
            RefileData refileData = refileResponse.getData();
            if (null != refileData) {
                String jobId = refileData.getJobId();
                itemRefileResponse.setJobId(jobId);
                getLogger().info("Initiated refile request on NYPL");
                getLogger().info("Nypl refile request job id -> {}" , jobId);
                JobResponse jobResponse = getNyplJobResponsePollingProcessor().pollNyplRequestItemJobResponse(itemRefileResponse.getJobId());
                String statusMessage = jobResponse.getStatusMessage();
                itemRefileResponse.setScreenMessage(statusMessage);
                JobData jobData = jobResponse.getData();
                if (null != jobData) {
                    itemRefileResponse.setSuccess(jobData.getSuccess());
                    getLogger().info("Refile request Finished -> " + jobData.getFinished());
                    getLogger().info("Refile request Success -> " + jobData.getSuccess());
                    getLogger().info(statusMessage);
                } else {
                    itemRefileResponse.setSuccess(false);
                    getLogger().info("Refile request Finished -> " + false);
                    getLogger().info("Refile request Success -> " + false);
                    getLogger().info(statusMessage);
                }
            }
        } catch (HttpClientErrorException httpException) {
            getLogger().error(ReCAPConstants.LOG_ERROR,httpException);
            itemRefileResponse.setSuccess(false);
            itemRefileResponse.setScreenMessage(httpException.getStatusText());
        } catch (Exception e) {
            getLogger().error(ReCAPConstants.LOG_ERROR,e);
            itemRefileResponse.setSuccess(false);
            itemRefileResponse.setScreenMessage(e.getMessage());
        }
        return itemRefileResponse;
    }
}
