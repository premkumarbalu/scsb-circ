package org.recap.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.recap.ReCAPConstants;
import org.recap.controller.ItemController;
import org.recap.model.BibliographicEntity;
import org.recap.model.ItemEntity;
import org.recap.model.ItemRequestInformation;
import org.recap.model.ItemStatusEntity;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.ItemStatusDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;


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
            return new ResponseEntity(ReCAPConstants.ITEM_BARCODE_IS_REQUIRED, getHttpHeaders(), HttpStatus.OK);
        }
        itemEntityList = itemController.findByBarcodeIn(itemBarcodes);
        if (itemEntityList.size() != 0) {
            if (splitStringAndGetList(itemBarcodes).size() == itemEntityList.size()) {
                ObjectMapper objectMapper = new ObjectMapper();
                ItemEntity itemEntity = objectMapper.convertValue(itemEntityList.get(0), ItemEntity.class);
                availabilityStatus = getItemStatus(itemEntity.getItemAvailabilityStatusId());
                if (availabilityStatus.equalsIgnoreCase(ReCAPConstants.AVAILABLE) && itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.HOLD)) {
                    return new ResponseEntity(ReCAPConstants.HOLD_REQUEST_NOT_FOR_AVAILABLE_ITEM, getHttpHeaders(), HttpStatus.OK);
                } else if (availabilityStatus.equalsIgnoreCase(ReCAPConstants.NOT_AVAILABLE) && itemRequestInformation.getRequestType().equalsIgnoreCase(ReCAPConstants.RETRIEVAL)) {
                    return new ResponseEntity(ReCAPConstants.RETRIEVAL_NOT_FOR_UNAVAILABLE_ITEM, getHttpHeaders(), HttpStatus.OK);
                }
                bibliographicList = objectMapper.convertValue(itemEntity.getBibliographicEntities(), new TypeReference<List<BibliographicEntity>>() {});
                for (BibliographicEntity bibliographicEntityDetails : bibliographicList) {
                    bibliographicIds.add(bibliographicEntityDetails.getBibliographicId());
                }
                if (itemEntityList.size() == 1) {
                    return new ResponseEntity(ReCAPConstants.VALID_REQUEST, getHttpHeaders(), HttpStatus.OK);
                } else {
                    String status = multipleRequestItemValidation(itemEntityList, itemEntity.getCustomerCode(), itemEntity.getItemAvailabilityStatusId(), bibliographicIds);
                    return new ResponseEntity(status, getHttpHeaders(), HttpStatus.OK);
                }
            } else {
                return new ResponseEntity(ReCAPConstants.WRONG_ITEM_BARCODE, getHttpHeaders(), HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(ReCAPConstants.WRONG_ITEM_BARCODE, getHttpHeaders(), HttpStatus.OK);
        }
    }

    private List<String> splitStringAndGetList(String inputString){
        String[] splittedString = inputString.split(",");
        List<String> stringList = Arrays.asList(splittedString);
        return stringList;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ReCAPConstants.RESPONSE_DATE, new Date().toString());
        return responseHeaders;
    }

    public String getItemStatus(Integer itemAvailabilityStatusId){
        String status="";
        ItemStatusEntity itemStatusEntity = new ItemStatusEntity();
        itemStatusEntity = itemStatusDetailsRepository.findByItemStatusId(itemAvailabilityStatusId);
        if(itemStatusEntity != null){
            status = itemStatusEntity.getStatusCode();
        }
        return status;
    }

    public String multipleRequestItemValidation(List<ItemEntity> itemEntityList, String customerCode, Integer itemAvailabilityStatusId, List<Integer> bibliographicIds){
        String status = "";
        List<BibliographicEntity> bibliographicList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        List<ItemEntity> itemEntities = objectMapper.convertValue(itemEntityList,new TypeReference<List<ItemEntity>>(){});
        for(ItemEntity itemEntity :itemEntities){
            if(itemEntity.getItemAvailabilityStatusId() == itemAvailabilityStatusId){
                if(itemEntity.getCustomerCode().equalsIgnoreCase(customerCode)){
                    if(itemEntity.getBibliographicEntities().size() == bibliographicIds.size()){
                        bibliographicList = objectMapper.convertValue(itemEntity.getBibliographicEntities(),new TypeReference<List<BibliographicEntity>>(){});
                        for(BibliographicEntity bibliographicEntity : bibliographicList){
                            Integer bibliographicId = bibliographicEntity.getBibliographicId();
                            if(!bibliographicIds.contains(bibliographicId)){
                                return ReCAPConstants.ITEMBARCODE_WITH_DIFFERENT_BIB;
                            }else{
                                status = ReCAPConstants.VALID_REQUEST;
                            }
                        }
                    }else{
                        return ReCAPConstants.ITEMBARCODE_WITH_DIFFERENT_BIB;
                    }
                }else{
                    return ReCAPConstants.INVALID_CUSTOMER_CODE;
                }
            }else{
                return ReCAPConstants.INVALID_ITEM_BARCODE;
            }
        }
        return status;
    }

}

