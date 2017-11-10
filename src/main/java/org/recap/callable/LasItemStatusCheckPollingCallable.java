package org.recap.callable;

import org.recap.gfa.model.GFAItemStatus;
import org.recap.gfa.model.GFAItemStatusCheckRequest;
import org.recap.gfa.model.GFAItemStatusCheckResponse;
import org.recap.request.GFAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


public class LasItemStatusCheckPollingCallable implements Callable {

    private static final Logger logger = LoggerFactory.getLogger(LasItemStatusCheckPollingCallable.class);

    private static String barcode;
    private GFAService gfaService;
    private Integer pollingTimeInterval;


    public LasItemStatusCheckPollingCallable(Integer pollingTimeInterval, GFAService gfaService) {
        this.gfaService = gfaService;
        this.pollingTimeInterval = pollingTimeInterval;
    }


    @Override
    public GFAItemStatusCheckResponse call() throws Exception {
        return poll();
    }

    private GFAItemStatusCheckResponse poll() throws Exception {
        Boolean statusFlag = false;
        GFAItemStatusCheckResponse gfaItemStatusCheckResponse=null;
        GFAItemStatusCheckRequest gfaItemStatusCheckRequest = new GFAItemStatusCheckRequest();
        //Todo Pol ItemRequest Rest Service
        GFAItemStatus gfaItemStatus001 = new GFAItemStatus();
        gfaItemStatus001.setItemBarCode(barcode);
        List<GFAItemStatus> gfaItemStatuses = new ArrayList<>();
        gfaItemStatuses.add(gfaItemStatus001);
        gfaItemStatusCheckRequest.setItemStatus(gfaItemStatuses);
        try {
            gfaItemStatusCheckResponse = gfaService.itemStatusCheck(gfaItemStatusCheckRequest);
            if (gfaItemStatusCheckResponse != null
                    && gfaItemStatusCheckResponse.getDsitem() != null
                    && gfaItemStatusCheckResponse.getDsitem().getTtitem() != null && !gfaItemStatusCheckResponse.getDsitem().getTtitem().isEmpty()) {
                Thread.sleep(pollingTimeInterval);
            }
        } catch (Exception e) {
            logger.error("",e);
        }
        return gfaItemStatusCheckResponse;
    }
    public static String getBarcode() {
        return barcode;
    }

    public static void setBarcode(String barcode) {
        LasItemStatusCheckPollingCallable.barcode = barcode;
    }
}
