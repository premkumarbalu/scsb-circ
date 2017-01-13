package org.recap.ils.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.ils.model.nypl.*;
import org.recap.ils.model.nypl.response.*;
import org.recap.ils.model.response.ItemCheckinResponse;
import org.recap.ils.model.response.ItemCheckoutResponse;
import org.recap.ils.model.response.ItemHoldResponse;
import org.recap.ils.model.response.ItemInformationResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by rajeshbabuk on 20/12/16.
 */

@Service
public class NyplApiResponseUtil {

    public ItemInformationResponse buildItemInformationResponse(ItemResponse itemResponse) {
        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
        ItemData itemData = itemResponse.getItemData();
        itemInformationResponse.setItemBarcode((String) itemData.getBarcode());
        itemInformationResponse.setBibID(itemData.getBibIds().get(0));
        itemInformationResponse.setBibIds(itemData.getBibIds());
        itemInformationResponse.setCallNumber((String) itemData.getCallNumber());
        itemInformationResponse.setItemType((String) itemData.getItemType());
        itemInformationResponse.setSource(itemData.getNyplSource());
        itemInformationResponse.setUpdatedDate(itemData.getUpdatedDate());
        itemInformationResponse.setCreatedDate(itemData.getCreatedDate());
        itemInformationResponse.setDeletedDate((String) itemData.getDeletedDate());
        itemInformationResponse.setDeleted((boolean) itemData.getDeleted());
        if (null != itemData.getStatus()) {
            itemInformationResponse.setDueDate((String) ((LinkedHashMap) itemData.getStatus()).get("dueDate"));
            itemInformationResponse.setCirculationStatus((String) ((LinkedHashMap) itemData.getStatus()).get("display"));
        }
        if (null != itemData.getLocation()) {
            itemInformationResponse.setCurrentLocation((String) ((LinkedHashMap) itemData.getLocation()).get("name"));
        }
        itemInformationResponse.setSuccess(true);
        return itemInformationResponse;
    }

    public ItemCheckoutResponse buildItemCheckoutResponse(CheckoutResponse checkoutResponse) {
        ItemCheckoutResponse itemCheckoutResponse = new ItemCheckoutResponse();
        CheckoutData checkoutData = checkoutResponse.getData();
        itemCheckoutResponse.setItemBarcode(checkoutData.getItemBarcode());
        itemCheckoutResponse.setPatronIdentifier(checkoutData.getPatronBarcode());
        itemCheckoutResponse.setCreatedDate(checkoutData.getCreatedDate());
        itemCheckoutResponse.setUpdatedDate((String) checkoutData.getUpdatedDate());
        itemCheckoutResponse.setDueDate(checkoutData.getDesiredDateDue());
        itemCheckoutResponse.setProcessed(checkoutData.getProcessed());
        itemCheckoutResponse.setJobId(checkoutData.getJobId());
        itemCheckoutResponse.setSuccess(checkoutData.getSuccess());
        return itemCheckoutResponse;
    }

    public ItemCheckinResponse buildItemCheckinResponse(CheckinResponse checkinResponse) {
        ItemCheckinResponse itemCheckinResponse = new ItemCheckinResponse();
        CheckinData checkinData = checkinResponse.getData();
        itemCheckinResponse.setItemBarcode(checkinData.getItemBarcode());
        itemCheckinResponse.setCreatedDate(checkinData.getCreatedDate());
        itemCheckinResponse.setUpdatedDate((String) checkinData.getUpdatedDate());
        itemCheckinResponse.setProcessed(checkinData.getProcessed());
        itemCheckinResponse.setJobId(checkinData.getJobId());
        itemCheckinResponse.setSuccess(checkinData.getSuccess());
        return itemCheckinResponse;
    }

    public ItemHoldResponse buildItemHoldResponse(CreateHoldResponse createHoldResponse) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        CreateHoldData holdData = createHoldResponse.getData();
        itemHoldResponse.setItemOwningInstitution(holdData.getOwningInstitutionId());
        itemHoldResponse.setItemBarcode(holdData.getItemBarcode());
        itemHoldResponse.setPatronIdentifier(holdData.getPatronBarcode());
        itemHoldResponse.setTrackingId(holdData.getTrackingId());
        itemHoldResponse.setCreatedDate(holdData.getCreatedDate());
        itemHoldResponse.setUpdatedDate((String) holdData.getUpdatedDate());
        return itemHoldResponse;
    }

    public ItemHoldResponse buildItemCancelHoldResponse(CancelHoldResponse cancelHoldResponse) {
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        CancelHoldData holdData = cancelHoldResponse.getData();
        itemHoldResponse.setItemOwningInstitution(holdData.getOwningInstitutionId());
        itemHoldResponse.setItemBarcode(holdData.getItemBarcode());
        itemHoldResponse.setPatronIdentifier(holdData.getPatronBarcode());
        itemHoldResponse.setTrackingId(holdData.getTrackingId());
        itemHoldResponse.setCreatedDate(holdData.getCreatedDate());
        itemHoldResponse.setUpdatedDate((String) holdData.getUpdatedDate());
        return itemHoldResponse;
    }

}
