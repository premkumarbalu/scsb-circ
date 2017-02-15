package org.recap.request;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.recap.ReCAPConstants;
import org.recap.ils.model.response.ItemInformationResponse;
import org.recap.model.*;
import org.recap.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by sudhishk on 1/12/16.
 */
@Component
public class ItemRequestDBService {

    private Logger logger = Logger.getLogger(ItemRequestDBService.class);

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private PatronDetailsRepository patronDetailsRepository;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    @Autowired
    private RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Autowired
    private RequestInstitutionBibDetailsRepository requestInstitutionBibDetailsRepository;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private RequestTypeDetailsRepository requestTypeDetailsRepository;

    public Integer updateRecapRequestItem(ItemRequestInformation itemRequestInformation, ItemEntity itemEntity, RequestTypeEntity requestTypeEntity, String requestStatusCode) {

        RequestItemEntity requestItemEntity = new RequestItemEntity();
        PatronEntity patronEntity;
        PatronEntity savedPatronEntity;
        RequestItemEntity savedItemRequest;
        Integer requestId = 0;
        try {
            // Patron Information
            patronEntity = patronDetailsRepository.findByInstitutionIdentifier(itemRequestInformation.getPatronBarcode());
            RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(requestStatusCode);
            InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(itemRequestInformation.getRequestingInstitution());

            if (patronEntity == null) {
                patronEntity = new PatronEntity();
                patronEntity.setInstitutionIdentifier(itemRequestInformation.getPatronBarcode());
                patronEntity.setInstitutionId(itemEntity.getInstitutionEntity().getInstitutionId());
                patronEntity.setEmailId(itemRequestInformation.getEmailAddress());
                savedPatronEntity = patronDetailsRepository.save(patronEntity);
            } else {
                if (itemRequestInformation.getEmailAddress() != null && itemRequestInformation.getEmailAddress().trim().length() > 0) {
                    patronEntity.setEmailId(itemRequestInformation.getEmailAddress());
                }
                savedPatronEntity = patronDetailsRepository.save(patronEntity);
            }

            //Request Item
            requestItemEntity.setItemId(itemEntity.getItemId());
            requestItemEntity.setRequestingInstitutionId(institutionEntity.getInstitutionId());
            requestItemEntity.setRequestTypeId(requestTypeEntity.getRequestTypeId());
            requestItemEntity.setRequestExpirationDate(getExpirationDate(itemRequestInformation.getExpirationDate(), itemRequestInformation.getRequestingInstitution()));
            requestItemEntity.setCreatedBy(getUser(itemRequestInformation.getUsername()));
            requestItemEntity.setCreatedDate(new Date());
            requestItemEntity.setLastUpdatedDate(new Date());
            requestItemEntity.setPatronId(savedPatronEntity.getPatronId());
            requestItemEntity.setStopCode(itemRequestInformation.getDeliveryLocation());
            requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
            requestItemEntity.setNotes(itemRequestInformation.getRequestNotes());


            savedItemRequest = requestItemDetailsRepository.save(requestItemEntity);
            if (savedItemRequest != null) {
                requestId = savedItemRequest.getRequestId();


                saveItemChangeLogEntity(savedItemRequest.getRequestId(), getUser(itemRequestInformation.getUsername()), "Request Item Insert", savedItemRequest.getItemId() + " - " + savedItemRequest.getPatronId());
            }
            logger.info("SCSB DB Update Successful");
        } catch (ParseException e) {
            logger.error(ReCAPConstants.REQUEST_PARSE_EXCEPTION, e);
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return requestId;
    }

    public ItemInformationResponse updateRecapRequestItem(ItemInformationResponse itemInformationResponse) {

        RequestItemEntity requestItemEntity = new RequestItemEntity();
        PatronEntity patronEntity;
        PatronEntity savedPatronEntity;
        RequestItemEntity savedItemRequest;
        Integer requestId = 0;
        try {
            // Patron Information
            patronEntity = patronDetailsRepository.findByInstitutionIdentifier(itemInformationResponse.getPatronBarcode());
            RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusCode(ReCAPConstants.REQUEST_STATUS_EXCEPTION);
            RequestTypeEntity requestTypeEntity = requestTypeDetailsRepository.findByrequestTypeCode(itemInformationResponse.getRequestType());
            InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(itemInformationResponse.getRequestingInstitution());

            if (patronEntity == null) {
                patronEntity = new PatronEntity();
                patronEntity.setInstitutionIdentifier(itemInformationResponse.getPatronBarcode());
                patronEntity.setInstitutionId(institutionEntity.getInstitutionId());
                patronEntity.setEmailId(itemInformationResponse.getEmailAddress());
                savedPatronEntity = patronDetailsRepository.save(patronEntity);
            } else {
                if (itemInformationResponse.getEmailAddress() != null && itemInformationResponse.getEmailAddress().trim().length() > 0) {
                    patronEntity.setEmailId(itemInformationResponse.getEmailAddress());
                }
                savedPatronEntity = patronDetailsRepository.save(patronEntity);
            }

            //Request Item
            requestItemEntity.setItemId(itemInformationResponse.getItemId());
            requestItemEntity.setRequestingInstitutionId(institutionEntity.getInstitutionId());
            requestItemEntity.setRequestTypeId(requestTypeEntity.getRequestTypeId());
            requestItemEntity.setRequestExpirationDate(getExpirationDate(itemInformationResponse.getExpirationDate(), itemInformationResponse.getRequestingInstitution()));
            requestItemEntity.setCreatedBy(getUser(itemInformationResponse.getUsername()));
            requestItemEntity.setCreatedDate(new Date());
            requestItemEntity.setLastUpdatedDate(new Date());
            requestItemEntity.setPatronId(savedPatronEntity.getPatronId());
            requestItemEntity.setStopCode(itemInformationResponse.getDeliveryLocation());
            requestItemEntity.setRequestStatusId(requestStatusEntity.getRequestStatusId());
            requestItemEntity.setNotes(itemInformationResponse.getRequestNotes());

            savedItemRequest = requestItemDetailsRepository.save(requestItemEntity);
            if (savedItemRequest != null) {
                requestId = savedItemRequest.getRequestId();
                saveItemChangeLogEntity(savedItemRequest.getRequestId(), getUser(itemInformationResponse.getUsername()), ReCAPConstants.REQUEST_ITEM_INSERT, savedItemRequest.getItemId() + " - " + savedItemRequest.getPatronId());
            }
            itemInformationResponse.setRequestId(requestId);
            logger.info("SCSB DB Update Successful");
        } catch (ParseException e) {
            logger.error(ReCAPConstants.REQUEST_PARSE_EXCEPTION, e);
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return itemInformationResponse;
    }

    public void updateItemAvailabilutyStatus(List<ItemEntity> itemEntities,String userName) {
        for (ItemEntity itemEntity : itemEntities) {
            itemEntity.setItemAvailabilityStatusId(2); // Not Available
            itemEntity.setLastUpdatedBy(getUser(userName));
            itemEntity.setLastUpdatedDate(new Date());

            saveItemChangeLogEntity(itemEntity.getItemId(), getUser(userName), ReCAPConstants.REQUEST_ITEM_AVAILABILITY_STATUS_UPDATE, ReCAPConstants.REQUEST_ITEM_AVAILABILITY_STATUS_DATA_UPDATE);
        }
        // Not Available
        itemDetailsRepository.save(itemEntities);

    }

    public void rollbackUpdateItemAvailabilutyStatus(ItemEntity itemEntity, String userName) {
        itemEntity.setItemAvailabilityStatusId(1); // Available
        itemEntity.setLastUpdatedBy(getUser(userName));
        itemEntity.setLastUpdatedDate(new Date());
        itemDetailsRepository.save(itemEntity);
        saveItemChangeLogEntity(itemEntity.getItemId(), getUser(userName), ReCAPConstants.REQUEST_ITEM_AVAILABILITY_STATUS_UPDATE, ReCAPConstants.REQUEST_ITEM_AVAILABILITY_STATUS_DATA_ROLLBACK);
    }

    public void saveItemChangeLogEntity(Integer recordId, String userName, String operationType, String notes) {
        ItemChangeLogEntity itemChangeLogEntity = new ItemChangeLogEntity();
        itemChangeLogEntity.setUpdatedBy(userName);
        itemChangeLogEntity.setUpdatedDate(new Date());
        itemChangeLogEntity.setOperationType(operationType);
        itemChangeLogEntity.setRecordId(recordId);
        itemChangeLogEntity.setNotes(notes);
        itemChangeLogDetailsRepository.save(itemChangeLogEntity);
    }

    public void getTempBibId(ItemRequestInformation itemRequestInfo, ItemEntity itemEntity) {
        InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(itemRequestInfo.getRequestingInstitution());
        RequestInstitutionBibEntity requestInstitutionBibEntity = requestInstitutionBibDetailsRepository.findByItemIdAndOwningInstitutionId(itemEntity.getItemId(), institutionEntity.getInstitutionId());
        if (requestInstitutionBibEntity != null) {
            itemRequestInfo.setBibId(requestInstitutionBibEntity.getOwningInstitutionBibId());
        } else {
            itemRequestInfo.setBibId("");
        }
    }

    public void createTempBibId(ItemRequestInformation itemRequestInfo, ItemEntity itemEntity) {
        InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(itemRequestInfo.getRequestingInstitution());
        RequestInstitutionBibEntity requestInstitutionBibEntityIns = new RequestInstitutionBibEntity();
        requestInstitutionBibEntityIns.setItemId(itemEntity.getItemId());
        requestInstitutionBibEntityIns.setOwningInstitutionId(institutionEntity.getInstitutionId());
        requestInstitutionBibEntityIns.setOwningInstitutionBibId(itemRequestInfo.getBibId());
        requestInstitutionBibDetailsRepository.save(requestInstitutionBibEntityIns);
    }

    public String getUser(String userId) {
        if (StringUtils.isBlank(userId)) {
            return "Discovery";
        } else {
            return userId;
        }
    }

    private Date getExpirationDate(String expirationDate, String requestingInstitutionId) throws ParseException {
        if (StringUtils.isNotBlank(expirationDate)) {
            if (ReCAPConstants.NYPL.equalsIgnoreCase(requestingInstitutionId)) {
                DateFormat dateFormatter = new SimpleDateFormat(ReCAPConstants.NYPL_HOLD_DATE_FORMAT);
                return dateFormatter.parse(expirationDate);
            } else {
                return simpleDateFormat.parse(expirationDate);
            }
        }
        return null;
    }

}
