package org.recap.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.recap.gfa.model.*;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.ItemRequestInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sudhishk on 27/1/17.
 */
@Service
public class GFAService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String GFA_ITEM_STATUS = "http://recapgfa.princeton.edu:9092/lasapi/rest/lasapiSvc/itemStatus";
    private static final String GFA_ITEM_RETRIVAL = "http://recapgfa.princeton.edu:9092/lasapi/rest/lasapiSvc/retrieveItem";
    private static final String GFA_SERVICE_PARAM = "filter";
    private static final String GFA_STATUS_INCOMING_ON_WORK_ORDER = "INC On WO:";
    private static final String DELIVERY_METHOD= "PHY";

    public GFAItemStatusCheckResponse itemStatusCheck(GFAItemStatusCheckRequest gfaItemStatusCheckRequest) {

        ObjectMapper objectMapper = new ObjectMapper();
        String filterParamValue = "";
        GFAItemStatusCheckResponse gfaItemStatusCheckResponse = null;
        try {
            filterParamValue = objectMapper.writeValueAsString(gfaItemStatusCheckRequest);
            logger.info(filterParamValue);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity<>(new HttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GFA_ITEM_STATUS).queryParam(GFA_SERVICE_PARAM, filterParamValue);
            ResponseEntity<GFAItemStatusCheckResponse> responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, GFAItemStatusCheckResponse.class);
            gfaItemStatusCheckResponse = responseEntity.getBody();

            logger.info(responseEntity.getStatusCode().toString());
        } catch (JsonProcessingException e) {
            logger.info("Exception: ", e);
        }
        return gfaItemStatusCheckResponse;
    }

    public boolean itemRetrival(GFARetrieveItemRequest gfaRetrieveItemRequest) {
        boolean bSuccess = false;
        ObjectMapper objectMapper = new ObjectMapper();
        String filterParamValue = "";
        try {
            filterParamValue = objectMapper.writeValueAsString(gfaRetrieveItemRequest);
            logger.info(filterParamValue);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity<>(new HttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GFA_ITEM_RETRIVAL)
                    .queryParam(GFA_SERVICE_PARAM, filterParamValue);
            ResponseEntity<String> responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, String.class);
            logger.info(responseEntity.getStatusCode().toString());
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                bSuccess = true;
            }
        } catch (JsonProcessingException e) {
            logger.info("Exception: ", e);
        }
        return bSuccess;
    }

    public ItemInformationResponse executeRetriveOrder(ItemRequestInformation itemRequestInfo,ItemInformationResponse itemResponseInformation) {
        GFAItemStatusCheckRequest gfaItemStatusCheckRequest = new GFAItemStatusCheckRequest();
        GFARetrieveItemRequest gfaRetrieveItemRequest = new GFARetrieveItemRequest();
        GFAItemStatusCheckResponse gfaItemStatusCheckResponse = null;
        String itemStatus = "";
        try {
            GFAItemStatus gfaItemStatus001 = new GFAItemStatus();
            gfaItemStatus001.setItemBarCode("PULTST54337");
            List<GFAItemStatus> gfaItemStatuses = new ArrayList<>();
            gfaItemStatuses.add(gfaItemStatus001);
            gfaItemStatusCheckRequest.setItemStatus(gfaItemStatuses);
            gfaItemStatusCheckResponse = itemStatusCheck(gfaItemStatusCheckRequest);
            if (gfaItemStatusCheckResponse != null
                    && gfaItemStatusCheckResponse.getDsitem() != null
                    && gfaItemStatusCheckResponse.getDsitem().getTtitem() != null && gfaItemStatusCheckResponse.getDsitem().getTtitem().size() > 0) {
                itemStatus = gfaItemStatusCheckResponse.getDsitem().getTtitem().get(0).getItemStatus();
                if(itemStatus.startsWith(GFA_STATUS_INCOMING_ON_WORK_ORDER)){
                    Ttitem ttitem001 = new Ttitem();
                    ttitem001.setCustomerCode(itemRequestInfo.getCustomerCode());
                    ttitem001.setItemBarcode(itemRequestInfo.getItemBarcodes().get(0));
                    ttitem001.setDestination(itemRequestInfo.getDeliveryLocation());
                    ttitem001.setDeliveryMethod(DELIVERY_METHOD);
                    List<Ttitem> ttitems = new ArrayList<>();
                    ttitems.add(ttitem001);
                    RetrieveItem retrieveItem = new RetrieveItem();
                    retrieveItem.setTtitem(ttitems);

                    boolean bSuccsess = itemRetrival(gfaRetrieveItemRequest);
                    if(bSuccsess){
                        itemResponseInformation.setSuccess(true);
                        itemResponseInformation.setScreenMessage("Successful created retrival order in GFA");
                    }else{
                        itemResponseInformation.setSuccess(false);
                        itemResponseInformation.setScreenMessage("GFA returned error while creating retrival order");
                    }
                }else{
                    itemResponseInformation.setSuccess(false);
                    itemResponseInformation.setScreenMessage("Item in GFA is not available");
                }
            } else {
                itemResponseInformation.setSuccess(false);
                itemResponseInformation.setScreenMessage("Item does not exist in GFA, or GFA system is down");
            }
        } catch (Exception e) {
            logger.error("Exception ", e);
        }
        return itemResponseInformation;
    }
}
