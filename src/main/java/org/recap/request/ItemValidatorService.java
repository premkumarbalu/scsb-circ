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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by hemalathas on 11/11/16.
 */
@Component
public class ItemValidatorService {

    /**
     * The Scsb solr client url.
     */
    @Value("${scsb.solr.client.url}")
    private String scsbSolrClientUrl;

    /**
     * The Item status details repository.
     */
    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    /**
     * The Item details repository.
     */
    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    /**
     * The Item controller.
     */
    @Autowired
    private ItemController itemController;

    /**
     * The Customer code details repository.
     */
    @Autowired
    private CustomerCodeDetailsRepository customerCodeDetailsRepository;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    /**
     * Item validation response entity.
     *
     * @param itemRequestInformation the item request information
     * @return the response entity
     */
    public ResponseEntity itemValidation(ItemRequestInformation itemRequestInformation) {
        List<ItemEntity> itemEntityList = getItemEntities(itemRequestInformation.getItemBarcodes());
        RequestItemEntity requestItemList;

        if (itemRequestInformation.getItemBarcodes().size() == 1) {
            if (itemEntityList != null && !itemEntityList.isEmpty()) {
                if (!itemEntityList.isEmpty()) {
                    for (ItemEntity itemEntity1 : itemEntityList) {
                        if (!checkRequestItemStatus(itemEntity1.getBarcode(), ReCAPConstants.REQUEST_STATUS_INITIAL_LOAD)) {
                            return new ResponseEntity(ReCAPConstants.INITIAL_LOAD_ITEM_EXISTS, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                        }
                        String availabilityStatus = getItemStatus(itemEntity1.getItemAvailabilityStatusId());
                        if (availabilityStatus.equalsIgnoreCase(ReCAPConstants.NOT_AVAILABLE) && (itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RETRIEVAL)
                                || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)
                                || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.BORROW_DIRECT))) {
                            return new ResponseEntity(ReCAPConstants.RETRIEVAL_NOT_FOR_UNAVAILABLE_ITEM, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                        } else if (availabilityStatus.equalsIgnoreCase(ReCAPConstants.AVAILABLE) && itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)) {
                            return new ResponseEntity(ReCAPConstants.RECALL_NOT_FOR_AVAILABLE_ITEM, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                        }

                        if(itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)) {
                            if (!checkRequestItemStatus(itemEntity1.getBarcode(), ReCAPConstants.REQUEST_STATUS_EDD)) {
                                return new ResponseEntity(ReCAPConstants.RECALL_FOR_EDD_ITEM, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                            }else if (!checkRequestItemStatus(itemEntity1.getBarcode(), ReCAPConstants.REQUEST_STATUS_CANCELED)) {
                                return new ResponseEntity(ReCAPConstants.RECALL_FOR_CANCELLED_ITEM, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                            }
                        }

                        if (!checkRequestItemStatus(itemEntity1.getBarcode(), ReCAPConstants.REQUEST_STATUS_RECALLED)) {
                            return new ResponseEntity(ReCAPConstants.RECALL_FOR_ITEM_EXISTS, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                        }

                        if (itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
                            CustomerCodeEntity customerCodeEntity = customerCodeDetailsRepository.findByCustomerCodeAndRecapDeliveryRestrictionLikeEDD(itemEntity1.getCustomerCode());
                            if (customerCodeEntity == null) {
                                return new ResponseEntity(ReCAPConstants.EDD_REQUEST_NOT_ALLOWED, getHttpHeaders(), HttpStatus.BAD_REQUEST);
                            }
                        }
                    }
                }
                ItemEntity itemEntity = itemEntityList.get(0);
                ResponseEntity responseEntity1 = null;
                if (itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RETRIEVAL) || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)) {
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
                return responseEntity1;
            } else {
                return new ResponseEntity(ReCAPConstants.WRONG_ITEM_BARCODE, getHttpHeaders(), HttpStatus.BAD_REQUEST);
            }
        } else if (itemRequestInformation.getItemBarcodes().size() > 1) {
            Set<Integer> bibliographicIds = new HashSet<>();
            for (ItemEntity itemEntity : itemEntityList) {
                List<BibliographicEntity> bibliographicList = itemEntity.getBibliographicEntities();
                for (BibliographicEntity bibliographicEntityDetails : bibliographicList) {
                    bibliographicIds.add(bibliographicEntityDetails.getBibliographicId());
                }
            }
            return multipleRequestItemValidation(itemEntityList, bibliographicIds, itemRequestInformation);
        }
        return new ResponseEntity(ReCAPConstants.VALID_REQUEST, getHttpHeaders(), HttpStatus.OK);
    }

    private List<ItemEntity> getItemEntities(List<String> itemBarcodes) {
        List<ItemEntity> itemEntityList = null;
        if (CollectionUtils.isNotEmpty(itemBarcodes)) {
            itemEntityList = itemController.findByBarcodeIn(itemBarcodes.toString());
        }
        return itemEntityList;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ReCAPConstants.RESPONSE_DATE, new Date().toString());
        return responseHeaders;
    }

    /**
     * Gets item status.
     *
     * @param itemAvailabilityStatusId the item availability status id
     * @return the item status
     */
    public String getItemStatus(Integer itemAvailabilityStatusId) {
        String status = "";
        ItemStatusEntity itemStatusEntity;
        itemStatusEntity = itemStatusDetailsRepository.findByItemStatusId(itemAvailabilityStatusId);
        if (itemStatusEntity != null) {
            status = itemStatusEntity.getStatusCode();
        }
        return status;
    }

    private ResponseEntity multipleRequestItemValidation(List<ItemEntity> itemEntityList, Set<Integer> bibliographicIds, ItemRequestInformation itemRequestInformation) {
        String status = "";
        List<BibliographicEntity> bibliographicList;

        for (ItemEntity itemEntity : itemEntityList) {
            if (!checkRequestItemStatus(itemEntity.getBarcode(), ReCAPConstants.REQUEST_STATUS_INITIAL_LOAD)) {
                return new ResponseEntity(ReCAPConstants.INITIAL_LOAD_ITEM_EXISTS, getHttpHeaders(), HttpStatus.BAD_REQUEST);
            }
            if (itemEntity.getItemAvailabilityStatusId() == 2 && (itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RETRIEVAL)
                    || itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD))) {
                return new ResponseEntity(ReCAPConstants.INVALID_ITEM_BARCODE, getHttpHeaders(), HttpStatus.BAD_REQUEST);
            } else if (itemEntity.getItemAvailabilityStatusId() == 1 && itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_RECALL)) {
                return new ResponseEntity(ReCAPConstants.RECALL_NOT_FOR_AVAILABLE_ITEM, getHttpHeaders(), HttpStatus.BAD_REQUEST);
            }
            if (!checkRequestItemStatus(itemEntity.getBarcode(), ReCAPConstants.REQUEST_STATUS_RECALLED)) {
                return new ResponseEntity(ReCAPConstants.RECALL_FOR_ITEM_EXISTS, getHttpHeaders(), HttpStatus.BAD_REQUEST);
            }

            if (!(itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD))) {
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

    /**
     * Check delivery location int.
     *
     * @param customerCode           the customer code
     * @param itemRequestInformation the item request information
     * @return the int
     */
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
        }
        return bSuccess;
    }

    private boolean checkRequestItemStatus(String barcode, String requestItemStatus) {
        RequestItemEntity requestItemList = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode(barcode, requestItemStatus);
        if (requestItemList != null && requestItemList.getRequestId() > 0) {
            return false;
        } else {
            return true;
        }
    }
}