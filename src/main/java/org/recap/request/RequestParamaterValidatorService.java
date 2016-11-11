package org.recap.request;

import org.recap.ReCAPConstants;
import org.recap.model.ItemRequestInformation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hemalathas on 3/11/16.
 */
@Component
public class RequestParamaterValidatorService {


    public ResponseEntity validateItemRequestParameters(ItemRequestInformation itemRequestInformation){
        ResponseEntity responseEntity = null;
        Map<Integer,String> errorMessageMap = new HashMap<>();
        Integer errorCount = 1;

        if(StringUtils.isEmpty(itemRequestInformation.getRequestingInstitution()) || !itemRequestInformation.getRequestingInstitution().equalsIgnoreCase(ReCAPConstants.PRINCETON) && !itemRequestInformation.getRequestingInstitution().equalsIgnoreCase(ReCAPConstants.COLUMBIA)
                && !itemRequestInformation.getRequestingInstitution().equalsIgnoreCase(ReCAPConstants.NYPL)){
            errorMessageMap.put(errorCount, ReCAPConstants.INVALID_REQUEST_INSTITUTION);
            errorCount++;
        }
        if(StringUtils.isEmpty(itemRequestInformation.getEmailAddress()) || !validateEmailAddress(itemRequestInformation.getEmailAddress())){
            errorMessageMap.put(errorCount,ReCAPConstants.INVALID_EMAIL_ADDRESS);
            errorCount++;
        }
        if(StringUtils.isEmpty(itemRequestInformation.getRequestType())){
            errorMessageMap.put(errorCount,ReCAPConstants.INVALID_REQUEST_TYPE);
            errorCount++;
        }else{
            if(itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.EDD_REQUEST)){
                if(itemRequestInformation.getStartPage() == null || itemRequestInformation.getEndPage() == null){
                    errorMessageMap.put(errorCount,ReCAPConstants.START_PAGE_AND_END_PAGE_REQUIRED);
                    errorCount++;
                }else if(itemRequestInformation.getStartPage() == 0 || itemRequestInformation.getEndPage() == 0){
                    errorMessageMap.put(errorCount,ReCAPConstants.INVALID_PAGE_NUMBER);
                    errorCount++;
                }else {
                    if (itemRequestInformation.getEndPage() < itemRequestInformation.getStartPage()) {
                        errorMessageMap.put(errorCount, ReCAPConstants.INVALID_END_PAGE);
                        errorCount++;
                    }
                }
            }else if(itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RECALL) || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.HOLD) || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RETRIEVAL)){
                if(StringUtils.isEmpty(itemRequestInformation.getDeliveryLocation())){
                    errorMessageMap.put(errorCount,ReCAPConstants.DELIVERY_LOCATION_REQUIRED);
                    errorCount++;
                }
            }
        }

        if(errorMessageMap.size()>0){
            responseEntity = new ResponseEntity(buildErrorMessage(errorMessageMap),getHttpHeaders(), HttpStatus.OK);
        }

        return responseEntity;
    }

    private boolean validateEmailAddress(String toEmailAddress){
        String regex = ReCAPConstants.REGEX_FOR_EMAIL_ADDRESS;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(toEmailAddress);
        return matcher.matches();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ReCAPConstants.RESPONSE_DATE, new Date().toString());
        return responseHeaders;
    }

    private String buildErrorMessage(Map<Integer,String> erroMessageMap){
        StringBuilder errorMessageBuilder = new StringBuilder();
        erroMessageMap.entrySet().forEach(entry -> {
            errorMessageBuilder.append(entry.getValue()).append("\n");
        });
        return errorMessageBuilder.toString();
    }
    private List<String> splitStringAndGetList(String inputString){
        String[] splittedString = inputString.split(",");
        List<String> stringList = Arrays.asList(splittedString);
        return stringList;
    }
}
