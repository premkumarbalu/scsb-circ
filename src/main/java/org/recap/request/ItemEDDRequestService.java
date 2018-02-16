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
import org.recap.util.ItemRequestServiceUtil;
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

    @Autowired
    private ItemRequestServiceUtil itemRequestServiceUtil;

    /**
     * Gets item details repository.
     *
     * @return the item details repository
     */
    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    /**
     * Gets request type details repository.
     *
     * @return the request type details repository
     */
    public RequestTypeDetailsRepository getRequestTypeDetailsRepository() {
        return requestTypeDetailsRepository;
    }

    /**
     * Gets item request service.
     *
     * @return the item request service
     */
    public ItemRequestService getItemRequestService() {
        return itemRequestService;
    }

    /**
     * Gets item information response.
     *
     * @return the item information response
     */
    public ItemInformationResponse getItemInformationResponse() {
        return new ItemInformationResponse();
    }

    /**
     * Edd request item item information response.
     *
     * @param itemRequestInfo the item request info
     * @param exchange        the exchange
     * @return the item information response
     */
    public ItemInformationResponse eddRequestItem(ItemRequestInformation itemRequestInfo, Exchange exchange) {

        List<ItemEntity> itemEntities;
        ItemEntity itemEntity=null;
        ItemInformationResponse itemResponseInformation = getItemInformationResponse();
        Integer requestId = 0;
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
                itemRequestInfo.setTitleIdentifier(getItemRequestService().removeDiacritical(searchResultRow.getTitle().replaceAll("[^\\x00-\\x7F]", "?")));
                itemRequestInfo.setItemAuthor(getItemRequestService().removeDiacritical(searchResultRow.getAuthor()));
                itemRequestInfo.setCustomerCode(itemEntity.getCustomerCode());
                // Save user Notes to be sent to LAS
                userNotes = itemRequestInfo.getRequestNotes();
                // Add EDD Information to notes to be saved in database
                itemRequestInfo.setRequestNotes(getNotes(itemRequestInfo));
                boolean isItemStatusAvailable;
                synchronized (this) {
                    // Change Item Availablity
                    isItemStatusAvailable = getItemRequestService().updateItemAvailabilutyStatus(itemEntities, itemRequestInfo.getUsername());
                }

                requestId = getItemRequestService().updateRecapRequestItem(itemRequestInfo, itemEntity, ReCAPConstants.REQUEST_STATUS_PROCESSING);
                itemRequestInfo.setRequestId(requestId);
                itemResponseInformation.setRequestId(requestId);

                if (requestId == 0) {
                    itemResponseInformation.setScreenMessage(ReCAPConstants.INTERNAL_ERROR_DURING_REQUEST);
                    itemResponseInformation.setSuccess(false);
                } else if (!isItemStatusAvailable) {
                    itemResponseInformation.setScreenMessage(ReCAPConstants.RETRIEVAL_NOT_FOR_UNAVAILABLE_ITEM);
                    itemResponseInformation.setSuccess(false);
                } else {
                    // Process
                    if (getItemRequestService().getGfaService().isUseQueueLasCall()) {
                        getItemRequestService().updateRecapRequestItem(itemRequestInfo, itemEntity, ReCAPConstants.REQUEST_STATUS_PENDING);
                    }
                    itemRequestInfo.setRequestNotes(userNotes);
                    itemResponseInformation.setItemId(itemEntity.getItemId());
                    itemResponseInformation.setPatronBarcode(itemRequestInfo.getPatronBarcode());
                    itemResponseInformation = getItemRequestService().updateGFA(itemRequestInfo, itemResponseInformation);
                    itemRequestInfo.setRequestNotes(getNotes(itemRequestInfo));
                }
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
            itemResponseInformation.setUsername(itemRequestInfo.getUsername());
            if (!itemResponseInformation.isSuccess()) {
                itemResponseInformation.setRequestNotes(itemRequestInfo.getRequestNotes() + "\n" + ReCAPConstants.REQUEST_SCSB_EXCEPTION + itemResponseInformation.getScreenMessage());
                getItemRequestService().updateChangesToDb(itemResponseInformation, ReCAPConstants.REQUEST_TYPE_EDD + "-" + itemResponseInformation.getRequestingInstitution());
                getItemRequestService().rollbackUpdateItemAvailabilutyStatus(itemEntity,ReCAPConstants.GUEST_USER);
            } else {
                itemResponseInformation.setRequestNotes(itemRequestInfo.getRequestNotes());
                if(itemEntity != null) {
                    itemRequestServiceUtil.updateSolrIndex(itemEntity);
                }
            }
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
