package org.recap.request;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.ItemEntity;
import org.recap.model.ItemRequestInformation;
import org.recap.model.SearchResultRow;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.RequestTypeDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Created by sudhishk on 1/12/16.
 */
@Component
public class ItemEDDRequestService {

    private static final Logger logger = LoggerFactory.getLogger(ItemEDDRequestService.class);

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private RequestTypeDetailsRepository requestTypeDetailsRepository;

    @Autowired
    private ItemRequestService itemRequestService;

    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    public RequestTypeDetailsRepository getRequestTypeDetailsRepository() {
        return requestTypeDetailsRepository;
    }

    public ItemRequestService getItemRequestService() {
        return itemRequestService;
    }

    public ItemInformationResponse getItemInformationResponse() {
        return new ItemInformationResponse();
    }

    public ItemInformationResponse eddRequestItem(ItemRequestInformation itemRequestInfo, Exchange exchange) {

        List<ItemEntity> itemEntities;
        ItemEntity itemEntity;
        ItemInformationResponse itemResponseInformation = getItemInformationResponse();
        Integer requestId;
        String userNotes = "";
        try {
            itemEntities = getItemDetailsRepository().findByBarcodeIn(itemRequestInfo.getItemBarcodes());

            if (itemEntities != null && !itemEntities.isEmpty()) {
                logger.info("Item Exists in SCSB Database");
                itemEntity = itemEntities.get(0);
                if (itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId().trim().length() <= 0) {
                    itemRequestInfo.setBibId(itemEntity.getBibliographicEntities().get(0).getOwningInstitutionBibId());
                }
                SearchResultRow searchResultRow = getItemRequestService().searchRecords(itemEntity);

                itemRequestInfo.setItemOwningInstitution(itemEntity.getInstitutionEntity().getInstitutionCode());
                itemRequestInfo.setTitleIdentifier(searchResultRow.getTitle().replaceAll("[^\\x00-\\x7F]", "?"));
                itemRequestInfo.setItemAuthor(searchResultRow.getAuthor());
                itemRequestInfo.setCustomerCode(itemEntity.getCustomerCode());
                userNotes = itemRequestInfo.getRequestNotes();
                itemRequestInfo.setRequestNotes(getNotes(itemRequestInfo));
                itemResponseInformation.setItemId(itemEntity.getItemId());
                itemResponseInformation.setPatronBarcode(itemRequestInfo.getPatronBarcode());

                if (getItemRequestService().getGfaService().isUseQueueLasCall()) {
                    requestId = getItemRequestService().updateRecapRequestItem(itemRequestInfo, itemEntity, ReCAPConstants.REQUEST_STATUS_PENDING);
                } else {
                    requestId = getItemRequestService().updateRecapRequestItem(itemRequestInfo, itemEntity, ReCAPConstants.REQUEST_STATUS_EDD);
                }
                itemResponseInformation.setRequestId(requestId);
                itemRequestInfo.setRequestNotes(userNotes);
                itemResponseInformation = getItemRequestService().updateGFA(itemRequestInfo, itemResponseInformation);
                itemRequestInfo.setRequestNotes(getNotes(itemRequestInfo));
            } else {
                itemResponseInformation.setScreenMessage(ReCAPConstants.WRONG_ITEM_BARCODE);
                itemResponseInformation.setSuccess(false);
            }
            logger.info("Finish Processing");
            itemResponseInformation.setItemOwningInstitution(itemRequestInfo.getItemOwningInstitution());
            itemResponseInformation.setDueDate(itemRequestInfo.getExpirationDate());
            itemResponseInformation.setRequestingInstitution(itemRequestInfo.getRequestingInstitution());
            itemResponseInformation.setTitleIdentifier(itemRequestInfo.getTitleIdentifier());
            itemResponseInformation.setBibID(itemRequestInfo.getBibId());
            itemResponseInformation.setItemBarcode(itemRequestInfo.getItemBarcodes().get(0));
            itemResponseInformation.setRequestType(itemRequestInfo.getRequestType());
            itemResponseInformation.setEmailAddress(itemRequestInfo.getEmailAddress());
            itemResponseInformation.setDeliveryLocation(itemRequestInfo.getDeliveryLocation());
            itemResponseInformation.setRequestNotes(getItemRequestService().getNotes(itemResponseInformation.isSuccess(), itemResponseInformation.getScreenMessage(), itemRequestInfo.getRequestNotes()));
            itemResponseInformation.setUsername(itemRequestInfo.getUsername());
            // Update Topics
            getItemRequestService().sendMessageToTopic(itemRequestInfo.getRequestingInstitution(), itemRequestInfo.getRequestType(), itemResponseInformation, exchange);
        } catch (RestClientException ex) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION_REST, ex);
        } catch (Exception ex) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, ex);
        }
        return itemResponseInformation;
    }

    private String getNotes(ItemRequestInformation itemRequestInfo) {
        String notes = "";
        if (!StringUtils.isBlank(itemRequestInfo.getRequestNotes())) {
            notes = String.format("User: %s", itemRequestInfo.getRequestNotes());
        }
        notes += String.format("\n\nStart Page: %s \nEnd Page: %s \nVolume Number: %s \nIssue: %s \nArticle Author: %s \nArticle/Chapter Title: %s ", itemRequestInfo.getStartPage(), itemRequestInfo.getEndPage(), itemRequestInfo.getVolume(), itemRequestInfo.getIssue(), itemRequestInfo.getAuthor(), itemRequestInfo.getChapterTitle());
        return notes;
    }
}
