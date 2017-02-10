package org.recap.request;

import org.jboss.logging.Logger;
import org.recap.ReCAPConstants;
import org.recap.controller.ItemController;
import org.recap.model.ItemRequestInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hemalathas on 3/11/16.
 */
@Component
public class RequestParamaterValidatorService {

    private Logger logger = Logger.getLogger(RequestParamaterValidatorService.class);

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.solr.client.url}")
    String scsbSolrClientUrl;

    @Autowired
    ItemController itemController;

    public ResponseEntity validateItemRequestParameters(ItemRequestInformation itemRequestInformation) {
        ResponseEntity responseEntity = null;
        Map<Integer, String> errorMessageMap = new HashMap<>();
        Integer errorCount = 1;

        if (StringUtils.isEmpty(itemRequestInformation.getRequestingInstitution()) || !itemRequestInformation.getRequestingInstitution().equalsIgnoreCase(ReCAPConstants.PRINCETON) && !itemRequestInformation.getRequestingInstitution().equalsIgnoreCase(ReCAPConstants.COLUMBIA)
                && !itemRequestInformation.getRequestingInstitution().equalsIgnoreCase(ReCAPConstants.NYPL)) {
            errorMessageMap.put(errorCount, ReCAPConstants.INVALID_REQUEST_INSTITUTION);
            errorCount++;
        }
        if (!validateEmailAddress(itemRequestInformation.getEmailAddress())) {
            errorMessageMap.put(errorCount, ReCAPConstants.INVALID_EMAIL_ADDRESS);
            errorCount++;
        }
        if (itemRequestInformation.getRequestType() == null || itemRequestInformation.getRequestType().trim().length() <= 0
                && (!itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RETRIEVAL)
                || !itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)
                || !itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.BORROW_DIRECT)
                || !itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL))
                ){
            errorMessageMap.put(errorCount, ReCAPConstants.INVALID_REQUEST_TYPE);
            errorCount++;
        }else{
            if (itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.EDD_REQUEST)) {
                if (!CollectionUtils.isEmpty(itemRequestInformation.getItemBarcodes())) {
                    if (itemController.splitStringAndGetList(itemRequestInformation.getItemBarcodes().toString()).size() > 1) {
                        errorMessageMap.put(errorCount, ReCAPConstants.MULTIPLE_ITEMS_NOT_ALLOWED_FOR_EDD);
                        errorCount++;
                    }
                } else {
                    errorMessageMap.put(errorCount, ReCAPConstants.ITEM_BARCODE_IS_REQUIRED);
                    errorCount++;
                }
                if (StringUtils.isEmpty(itemRequestInformation.getChapterTitle())) {
                    errorMessageMap.put(errorCount, ReCAPConstants.CHAPTER_TITLE_IS_REQUIRED);
                    errorCount++;
                }
                if (itemRequestInformation.getStartPage() == null || itemRequestInformation.getEndPage() == null) {
                    errorMessageMap.put(errorCount, ReCAPConstants.START_PAGE_AND_END_PAGE_REQUIRED);
                    errorCount++;
                } else if (itemRequestInformation.getStartPage() == 0 || itemRequestInformation.getEndPage() == 0) {
                    errorMessageMap.put(errorCount, ReCAPConstants.INVALID_PAGE_NUMBER);
                    errorCount++;
                } else {
                    if (itemRequestInformation.getEndPage() < itemRequestInformation.getStartPage()) {
                        errorMessageMap.put(errorCount, ReCAPConstants.INVALID_END_PAGE);
                        errorCount++;
                    }
                }
            } else if (itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL) || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RETRIEVAL)) {
                if (StringUtils.isEmpty(itemRequestInformation.getDeliveryLocation())) {
                    errorMessageMap.put(errorCount, ReCAPConstants.DELIVERY_LOCATION_REQUIRED);
                    errorCount++;
                }
//                else{
//                    String customerCodeStatus = customerCodeValidation(itemRequestInformation.getDeliveryLocation());
//                    if(customerCodeStatus.equalsIgnoreCase(ReCAPConstants.INVALID_CUSTOMER_CODE)){
//                        errorMessageMap.put(errorCount, ReCAPConstants.INVALID_CUSTOMER_CODE);
//                        errorCount++;
//                    }
//                }
            }
        }

        if (errorMessageMap.size() > 0) {
            return new ResponseEntity(buildErrorMessage(errorMessageMap), getHttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        return responseEntity;
    }

    private boolean validateEmailAddress(String toEmailAddress) {
        boolean bSuccess =false;
        try {
            if(!StringUtils.isEmpty(toEmailAddress)) {
                String regex = ReCAPConstants.REGEX_FOR_EMAIL_ADDRESS;
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(toEmailAddress);
                bSuccess = matcher.matches();
            }else{
                bSuccess = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return bSuccess ;
    }

    public HttpHeaders getHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ReCAPConstants.RESPONSE_DATE, new Date().toString());
        return responseHeaders;
    }

    private String buildErrorMessage(Map<Integer, String> erroMessageMap) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        erroMessageMap.entrySet().forEach(entry -> {
            errorMessageBuilder.append(entry.getValue()).append("\n");
        });
        return errorMessageBuilder.toString();
    }
}
