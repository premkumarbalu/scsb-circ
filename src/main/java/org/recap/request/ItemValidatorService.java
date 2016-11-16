
package org.recap.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.model.BibliographicEntity;
import org.recap.model.ItemEntity;
import org.recap.model.ItemRequestInformation;
import org.recap.model.ItemStatusEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.apache.camel.component.jms.JmsMessageType.Map;


/**
 * Created by hemalathas on 11/11/16.
 */

@Component
public class ItemValidatorService {

    @Value("${server.protocol}")
    String serverProtocol;
    @Value("${scsb.solr.client.url}")
    String scsbSolrClientUrl;

    public ResponseEntity itemValidation(ItemRequestInformation itemRequestInformation){
        ResponseEntity responseEntity = null;
        String availabilityStatus="";
        List<Integer> bibliographicIds = new ArrayList<>();
        List<BibliographicEntity> bibliographicList = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        List<ItemEntity> itemEntityList = new ArrayList<>();
        List<String> itemBarcodeList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(itemRequestInformation.getItemBarcodes())){
            String itemBarcodes = itemRequestInformation.getItemBarcodes().toString();
            itemBarcodeList = splitStringAndGetList(itemBarcodes);
        }else{
            return new ResponseEntity(ReCAPConstants.ITEM_BARCODE_IS_REQUIRED,getHttpHeaders(),HttpStatus.OK);
        }
        if(itemBarcodeList.size() != 0){
            try {
                itemEntityList = restTemplate.getForObject(serverProtocol + scsbSolrClientUrl + "item/findByBarcodeIn?barcodes=" + StringUtils.join(itemBarcodeList, ","), List.class);
            }catch(Exception ex){
                responseEntity = new ResponseEntity("Scsb solr client Service is Unavailable.", getHttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE);
                return responseEntity;
            }
            if(itemEntityList.size() != 0){
                if(itemBarcodeList.size() == itemEntityList.size()){
                    ObjectMapper objectMapper = new ObjectMapper();
                    ItemEntity itemEntity = objectMapper.convertValue(itemEntityList.get(0),ItemEntity.class);
                    availabilityStatus = getItemStatus(itemEntity.getItemAvailabilityStatusId());
                    bibliographicList = objectMapper.convertValue(itemEntity.getBibliographicEntities(),new TypeReference<List<BibliographicEntity>>(){});
                    for(BibliographicEntity bibliographicEntityDetails: bibliographicList){
                        bibliographicIds .add(bibliographicEntityDetails.getBibliographicId());
                    }
                    if(availabilityStatus.equalsIgnoreCase(ReCAPConstants.AVAILABLE)){
                        if(itemEntityList.size()>1){
                            String status = requestItemStatus(itemEntityList,itemEntity.getCustomerCode(),itemEntity.getItemAvailabilityStatusId(),bibliographicIds);
                            return new ResponseEntity(status,getHttpHeaders(),HttpStatus.OK);
                        }else{
                            return new ResponseEntity(ReCAPConstants.VALID_REQUEST,getHttpHeaders(),HttpStatus.OK);
                        }
                    }else{
                        responseEntity = new ResponseEntity(ReCAPConstants.INVALID_ITEM_BARCODE, getHttpHeaders(), HttpStatus.OK);
                        return responseEntity;
                    }
                }else{
                    return new ResponseEntity(ReCAPConstants.WRONG_ITEM_BARCODE,getHttpHeaders(),HttpStatus.OK);
                }
            }
        }
        return responseEntity;
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
        RestTemplate restTemplate = new RestTemplate();
        ItemStatusEntity itemStatusEntity = new ItemStatusEntity();
        try{
            itemStatusEntity = restTemplate.getForObject(serverProtocol + scsbSolrClientUrl + "itemStatus/search/findByItemStatusId?itemStatusId="+itemAvailabilityStatusId, ItemStatusEntity.class);
        }catch(Exception ex){
            status = "Scsb solr client Service is Unavailable.";
        }
        if(itemStatusEntity != null){
            status = itemStatusEntity.getStatusCode();
        }
        return status;
    }

    public String requestItemStatus(List<ItemEntity> itemEntityList,String customerCode,Integer itemAvailabilityStatusId,List<Integer> bibliographicIds){
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

