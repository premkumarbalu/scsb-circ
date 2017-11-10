package org.recap.processor;

import org.apache.camel.CamelContext;
import org.recap.ReCAPConstants;
import org.recap.callable.LasItemStatusCheckPollingCallable;
import org.recap.camel.route.StartRouteProcessor;
import org.recap.gfa.model.GFAItemStatusCheckResponse;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.ItemRequestInformation;
import org.recap.model.RequestItemEntity;
import org.recap.model.RequestStatusEntity;
import org.recap.repository.RequestItemDetailsRepository;
import org.recap.repository.RequestItemStatusDetailsRepository;
import org.recap.request.GFAService;
import org.recap.request.ItemRequestService;
import org.recap.util.ItemRequestServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Component
public class LasItemStatusCheckPollingProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LasItemStatusCheckPollingProcessor.class);

    @Value("${las.polling.time.interval}")
    private Integer pollingTimeInterval;

    @Autowired
    private GFAService gfaService;

    @Autowired
    RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Autowired
    ItemRequestServiceUtil itemRequestServiceUtil;

    @Autowired
    private CamelContext camelContext;


    public GFAItemStatusCheckResponse pollLasItemStatusJobResponse() {
        GFAItemStatusCheckResponse gfaItemStatusCheckResponse = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<GFAItemStatusCheckResponse> future = executor.submit(new LasItemStatusCheckPollingCallable(pollingTimeInterval, gfaService));
            gfaItemStatusCheckResponse = future.get();
            if (gfaItemStatusCheckResponse != null
                    && gfaItemStatusCheckResponse.getDsitem() != null
                    && gfaItemStatusCheckResponse.getDsitem().getTtitem() != null && !gfaItemStatusCheckResponse.getDsitem().getTtitem().isEmpty()) {
                camelContext.startRoute(ReCAPConstants.REQUEST_ITEM_LAS_STATUS_CHECK_QUEUE_ROUTEID);
            }
            executor.shutdown();
        } catch (InterruptedException e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        } catch (ExecutionException e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return gfaItemStatusCheckResponse;
    }

    private ItemInformationResponse lasRetrivalOrder(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation) {
        try {
            itemResponseInformation = gfaService.executeRetriveOrder(itemRequestInfo, itemResponseInformation);
        } catch (Exception e) {
            itemResponseInformation.setSuccess(false);
            itemResponseInformation.setScreenMessage(ReCAPConstants.REQUEST_SCSB_EXCEPTION + e.getMessage());
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return itemResponseInformation;
    }

    private void retrivalInforamtion(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, RequestItemEntity requestItemEntity) {
        itemRequestInfo = new ItemRequestInformation();
        itemRequestInfo.setItemBarcodes(Arrays.asList(requestItemEntity.getItemEntity().getBarcode()));
        itemRequestInfo.setRequestType(requestItemEntity.getRequestTypeEntity().getRequestTypeCode());
        itemRequestInfo.setCustomerCode(requestItemEntity.getItemEntity().getCustomerCode());
        itemRequestInfo.setDeliveryLocation(requestItemEntity.getStopCode());
        itemRequestInfo.setPatronBarcode(requestItemEntity.getPatronId());
        itemRequestInfo.setRequestId(requestItemEntity.getRequestId());

        itemResponseInformation = new ItemInformationResponse();
        itemResponseInformation.setRequestId(requestItemEntity.getRequestId());
    }

    private void eddInforamtion(ItemRequestInformation itemRequestInfo, ItemInformationResponse itemResponseInformation, RequestItemEntity requestItemEntity) {
        itemRequestInfo = new ItemRequestInformation();
        itemRequestInfo.setItemBarcodes(Arrays.asList(requestItemEntity.getItemEntity().getBarcode()));
        itemRequestInfo.setRequestType(requestItemEntity.getRequestTypeEntity().getRequestTypeCode());
        itemRequestInfo.setCustomerCode(requestItemEntity.getItemEntity().getCustomerCode());
        itemRequestInfo.setDeliveryLocation(requestItemEntity.getStopCode());
        itemRequestInfo.setPatronBarcode(requestItemEntity.getPatronId());
        itemRequestInfo.setRequestId(requestItemEntity.getRequestId());
        itemRequestInfo.setEmailAddress(requestItemEntity.getEmailId());

        itemRequestInfo.setStartPage(itemRequestInfo.getStartPage());
//        itemRequestInfo.setEndPage();
//
//        itemRequestInfo.setChapterTitle();
//        itemRequestInfo.setAuthor();
//        itemRequestInfo.setVolume();
//        itemRequestInfo.setIssue();
//        itemRequestInfo.setRequestNotes();
//
//        itemRequestInfo.setTitleIdentifier();
//        itemRequestInfo.setItemAuthor();
//        itemRequestInfo.setItemVolume();
//        itemRequestInfo.setCallNumber();

        itemResponseInformation = new ItemInformationResponse();
        itemResponseInformation.setRequestId(requestItemEntity.getRequestId());
    }


}
