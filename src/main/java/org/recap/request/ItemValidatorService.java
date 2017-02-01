package org.recap.request;

import org.apache.commons.collections.CollectionUtils;
import org.recap.ReCAPConstants;
import org.recap.controller.ItemController;
import org.recap.model.*;
import org.recap.repository.CustomerCodeDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.ItemStatusDetailsRepository;
import org.recap.repository.RequestItemDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Created by hemalathas on 11/11/16.
 */

@Component
public class ItemValidatorService {

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.solr.client.url}")
    String scsbSolrClientUrl;

    @Autowired
    ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    @Autowired
    ItemController itemController;

    @Autowired
    CustomerCodeDetailsRepository customerCodeDetailsRepository;

    public ResponseEntity itemValidation(ItemRequestInformation itemRequestInformation) {
        ResponseEntity responseEntity = null;
        String availabilityStatus = "";
        List<Integer> bibliographicIds = new ArrayList<>();
        List<BibliographicEntity> bibliographicList = new ArrayList<>();
        List<ItemEntity> itemEntityList = new ArrayList<>();
        String itemBarcodes = "";
        if (CollectionUtils.isNotEmpty(itemRequestInformation.getItemBarcodes())) {
            itemBarcodes = itemRequestInformation.getItemBarcodes().toString();
        } else {
            return new ResponseEntity(ReCAPConstants.ITEM_BARCODE_IS_REQUIRED, getHttpHeaders(), HttpStatus.BAD_REQUEST);
        }
        itemEntityList = itemController.findByBarcodeIn(itemBarcodes);
        if (itemEntityList != null && itemEntityList.size() != 0) {
            if (splitStringAndGetList(itemBarcodes).size() == itemEntityList.size()) { // check if the no. of barcode from input and database is same.
                ItemEntity itemEntity = itemEntityList.get(0);
                // Item availability Status from SCSB Item table
                availabilityStatus = getItemStatus(itemEntity.getItemAvailabilityStatusId());
                if (availabilityStatus.equalsIgnoreCase(ReCAPConstants.NOT_AVAILABLE)
                        && (itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RETRIEVAL)
                        || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)
                        || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.BORROW_DIRECT))) {
                    return new ResponseEntity(ReCAPConstants.RETRIEVAL_NOT_FOR_UNAVAILABLE_ITEM, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                } else if (availabilityStatus.equalsIgnoreCase(ReCAPConstants.AVAILABLE) && itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RECALL)) {
                    return new ResponseEntity(ReCAPConstants.RECALL_NOT_FOR_AVAILABLE_ITEM, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                } else {
                    ResponseEntity responseEntity1 = null;
                    if (itemEntityList.size() == 1) {
                        if (!(itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD) || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.BORROW_DIRECT))) {
                            int validateCustomerCode = checkDeliveryLocation(itemEntity.getCustomerCode(), itemRequestInformation);
                            if (validateCustomerCode == 1) {
                                responseEntity1 = new ResponseEntity(ReCAPConstants.VALID_REQUEST, getHttpHeaders(), HttpStatus.OK);
                            } else if (validateCustomerCode == 0) {
                                responseEntity1 = new ResponseEntity(ReCAPConstants.INVALID_CUSTOMER_CODE, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                            } else if (validateCustomerCode == -1) {
                                responseEntity1 = new ResponseEntity(ReCAPConstants.INVALID_DELIVERY_CODE, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                            }
                        } else {
                            responseEntity1 = new ResponseEntity(ReCAPConstants.VALID_REQUEST, getHttpHeaders(), HttpStatus.OK);
                        }
                    } else if (itemEntityList.size() > 1) {
                        bibliographicList = itemEntity.getBibliographicEntities();
                        for (BibliographicEntity bibliographicEntityDetails : bibliographicList) {
                            bibliographicIds.add(bibliographicEntityDetails.getBibliographicId());
                        }
                        responseEntity1 = multipleRequestItemValidation(itemEntityList, itemEntity.getItemAvailabilityStatusId(), bibliographicIds, itemRequestInformation);
                    }
                    return responseEntity1;
                }
            } else {
                return new ResponseEntity(ReCAPConstants.WRONG_ITEM_BARCODE, getHttpHeaders(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity(ReCAPConstants.WRONG_ITEM_BARCODE, getHttpHeaders(), HttpStatus.BAD_REQUEST);
        }
    }

    private List<String> splitStringAndGetList(String inputString) {
        String[] splittedString = inputString.split(",");
        List<String> stringList = Arrays.asList(splittedString);
        return stringList;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ReCAPConstants.RESPONSE_DATE, new Date().toString());
        return responseHeaders;
    }

    public String getItemStatus(Integer itemAvailabilityStatusId) {
        String status = "";
        ItemStatusEntity itemStatusEntity = new ItemStatusEntity();
        itemStatusEntity = itemStatusDetailsRepository.findByItemStatusId(itemAvailabilityStatusId);
        if (itemStatusEntity != null) {
            status = itemStatusEntity.getStatusCode();
        }
        return status;
    }

    public ResponseEntity multipleRequestItemValidation(List<ItemEntity> itemEntityList, Integer itemAvailabilityStatusId, List<Integer> bibliographicIds, ItemRequestInformation itemRequestInformation) {
        String status = "";
        List<BibliographicEntity> bibliographicList = null;

        for (ItemEntity itemEntity : itemEntityList) {
            if (itemEntity.getItemAvailabilityStatusId() == 1 && (itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RETRIEVAL)
                    || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)
                    || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.BORROW_DIRECT))) {

            } else if (itemEntity.getItemAvailabilityStatusId() == 2 && itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RECALL)) {
                // Validate Patron
            } else if (itemEntity.getItemAvailabilityStatusId() == 1 && itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RECALL)) {
                return new ResponseEntity(ReCAPConstants.RECALL_NOT_FOR_AVAILABLE_ITEM, getHttpHeaders(), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity(ReCAPConstants.INVALID_ITEM_BARCODE, getHttpHeaders(), HttpStatus.BAD_REQUEST);
            }
            if (!(itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD) || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.BORROW_DIRECT))) {
                int validateCustomerCode = checkDeliveryLocation(itemEntity.getCustomerCode(), itemRequestInformation);
                if (validateCustomerCode == 1) {
                    if (itemEntity.getBibliographicEntities().size() == bibliographicIds.size()) {
                        bibliographicList = itemEntity.getBibliographicEntities();
                        for (BibliographicEntity bibliographicEntity : bibliographicList) {
                            Integer bibliographicId = bibliographicEntity.getBibliographicId();
                            if (!bibliographicIds.contains(bibliographicId)) {
                                return new ResponseEntity(ReCAPConstants.ITEMBARCODE_WITH_DIFFERENT_BIB, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                            } else {
                                status = ReCAPConstants.VALID_REQUEST;
                            }
                        }
                    } else {
                        return new ResponseEntity(ReCAPConstants.ITEMBARCODE_WITH_DIFFERENT_BIB, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                    }
                } else {
                    if (validateCustomerCode == 0) {
                        return new ResponseEntity(ReCAPConstants.INVALID_CUSTOMER_CODE, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                    } else if (validateCustomerCode == -1) {
                        return new ResponseEntity(ReCAPConstants.INVALID_DELIVERY_CODE, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                    }
                }
            }
        }
        return new ResponseEntity(status, getHttpHeaders(), HttpStatus.OK);
    }

    public int checkDeliveryLocation(String customerCode, ItemRequestInformation itemRequestInformation) {
        int bSuccess = 0;
        CustomerCodeEntity customerCodeEntity = customerCodeDetailsRepository.findByCustomerCode(itemRequestInformation.getDeliveryLocation());
        if (customerCodeEntity != null && customerCodeEntity.getCustomerCode().equalsIgnoreCase(itemRequestInformation.getDeliveryLocation())) {
            if (itemRequestInformation.getItemOwningInstitution().equalsIgnoreCase(itemRequestInformation.getRequestingInstitution())) {
                customerCodeEntity = customerCodeDetailsRepository.findByCustomerCode(customerCode);
                String deliveryRestrictions = customerCodeEntity.getDeliveryRestrictions();
                if (deliveryRestrictions != null && deliveryRestrictions.trim().length() > 0) {
                    if (deliveryRestrictions.contains(itemRequestInformation.getDeliveryLocation())) {
                        bSuccess = 1;
                    } else {
                        bSuccess = -1;
                    }
                } else {
                    bSuccess = -1;
                }
            } else {
                bSuccess = 1;
            }
        } else {
            bSuccess = 0;
        }
        return bSuccess;
    }
}



