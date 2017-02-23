package org.recap.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.recap.ReCAPConstants;
import org.recap.gfa.model.*;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.ItemRequestInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${gfa.item.status}")
    private String gfaItemStatus;
    @Value("${gfa.item.retrieval.order}")
    private String gfaItemRetrival;

    public GFAItemStatusCheckResponse itemStatusCheck(GFAItemStatusCheckRequest gfaItemStatusCheckRequest) {

        ObjectMapper objectMapper = new ObjectMapper();
        String filterParamValue = "";
        GFAItemStatusCheckResponse gfaItemStatusCheckResponse = null;
        try {
            filterParamValue = objectMapper.writeValueAsString(gfaItemStatusCheckRequest);
            logger.info(filterParamValue);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity<>(new HttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(gfaItemStatus).queryParam(ReCAPConstants.GFA_SERVICE_PARAM, filterParamValue);
            ResponseEntity<GFAItemStatusCheckResponse> responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, GFAItemStatusCheckResponse.class);
            gfaItemStatusCheckResponse = responseEntity.getBody();

            logger.info("",responseEntity.getStatusCode());
        } catch (JsonProcessingException e) {
            logger.info(ReCAPConstants.REQUEST_PARSE_EXCEPTION,e);
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION,e);
        }
        return gfaItemStatusCheckResponse;
    }

    public GFARetrieveItemResponse itemRetrival(GFARetrieveItemRequest gfaRetrieveItemRequest) {
        GFARetrieveItemResponse gfaRetrieveItemResponse = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity requestEntity = new HttpEntity(gfaRetrieveItemRequest, getHttpHeaders());
            ResponseEntity<GFARetrieveItemResponse> responseEntity = restTemplate.exchange(gfaItemRetrival, HttpMethod.POST, requestEntity, GFARetrieveItemResponse.class);
            logger.info(responseEntity.getStatusCode().toString());
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                gfaRetrieveItemResponse = responseEntity.getBody();
                if (gfaRetrieveItemResponse != null && gfaRetrieveItemResponse.getRetrieveItem() != null && gfaRetrieveItemResponse.getRetrieveItem().getTtitem() != null && !gfaRetrieveItemResponse.getRetrieveItem().getTtitem().isEmpty()) {
                    List<Ttitem> titemList = gfaRetrieveItemResponse.getRetrieveItem().getTtitem();
                    for (Ttitem ttitem : titemList) {
                        logger.info(ttitem.getErrorCode());
                        logger.info(ttitem.getErrorNote());
                        gfaRetrieveItemResponse.setSuccess(false);
                        gfaRetrieveItemResponse.setScrenMessage(ttitem.getErrorNote());
                    }
                } else {
                    gfaRetrieveItemResponse.setSuccess(true);
                }
            } else {
                gfaRetrieveItemResponse.setSuccess(false);
            }
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION,e);
        }
        return gfaRetrieveItemResponse;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public ItemInformationResponse executeRetriveOrder(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {
        GFAItemStatusCheckRequest gfaItemStatusCheckRequest = new GFAItemStatusCheckRequest();
        GFARetrieveItemRequest gfaRetrieveItemRequest = new GFARetrieveItemRequest();
        GFAItemStatusCheckResponse gfaItemStatusCheckResponse = null;
        String itemStatus = "";
        String gfaOnlyStaus = "";

        try {
            GFAItemStatus gfaItemStatus001 = new GFAItemStatus();
            gfaItemStatus001.setItemBarCode(itemRequestInfo.getItemBarcodes().get(0));
            List<GFAItemStatus> gfaItemStatuses = new ArrayList<>();
            gfaItemStatuses.add(gfaItemStatus001);
            gfaItemStatusCheckRequest.setItemStatus(gfaItemStatuses);
            gfaItemStatusCheckResponse = itemStatusCheck(gfaItemStatusCheckRequest);
            if (gfaItemStatusCheckResponse != null
                    && gfaItemStatusCheckResponse.getDsitem() != null
                    && gfaItemStatusCheckResponse.getDsitem().getTtitem() != null && !gfaItemStatusCheckResponse.getDsitem().getTtitem().isEmpty()) {

                itemStatus = gfaItemStatusCheckResponse.getDsitem().getTtitem().get(0).getItemStatus();
                if (itemStatus.contains(":")) {
                    gfaOnlyStaus = itemStatus.substring(0, itemStatus.indexOf(':') + 1).toUpperCase();
                } else {
                    gfaOnlyStaus = itemStatus.toUpperCase();
                }
                logger.info(gfaOnlyStaus);

                if (ReCAPConstants.getGFAStatusAvailableList().contains(gfaOnlyStaus)) {
                    TtitemRequest ttitem001 = new TtitemRequest();
                    ttitem001.setCustomerCode(itemRequestInfo.getCustomerCode());
                    ttitem001.setItemBarcode(itemRequestInfo.getItemBarcodes().get(0));
                    ttitem001.setDestination(itemRequestInfo.getDeliveryLocation());
                    if (itemRequestInfo.getRequestType().equalsIgnoreCase(ReCAPConstants.REQUEST_TYPE_EDD)) {
                        ttitem001.setDeliveryMethod(ReCAPConstants.REQUEST_DELIVERY_METHOD_EDD);
                    } else {
                        ttitem001.setDeliveryMethod(ReCAPConstants.REQUEST_DELIVERY_METHOD_PHY);
                    }

                    List<TtitemRequest> ttitems = new ArrayList<>();
                    ttitems.add(ttitem001);
                    RetrieveItemRequest retrieveItem = new RetrieveItemRequest();
                    retrieveItem.setTtitem(ttitems);
                    gfaRetrieveItemRequest.setRetrieveItem(retrieveItem);
                    GFARetrieveItemResponse gfaRetrieveItemResponse = itemRetrival(gfaRetrieveItemRequest);
                    if (gfaRetrieveItemResponse.isSuccess()) {
                        itemResponseInformation.setSuccess(true);
                        itemResponseInformation.setScreenMessage(ReCAPConstants.GFA_RETRIVAL_ORDER_SUCCESSFUL);
                    } else {
                        itemResponseInformation.setSuccess(false);
                        itemResponseInformation.setScreenMessage(ReCAPConstants.GFA_RETRIVAL_ORDER_ERROR);
                    }
                } else {
                    itemResponseInformation.setSuccess(false);
                    itemResponseInformation.setScreenMessage(ReCAPConstants.GFA_RETRIVAL_ITEM_NOT_AVAILABLE);
                }
            } else {
                itemResponseInformation.setSuccess(false);
                itemResponseInformation.setScreenMessage(ReCAPConstants.GFA_ITEM_STATUS_CHECK_FAILED);
            }
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return itemResponseInformation;
    }
}
