package org.recap.service.requestdataload;

import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.camel.requestinitialdataload.RequestDataLoadCSVRecord;
import org.recap.model.ItemEntity;
import org.recap.model.RequestItemEntity;
import org.recap.model.RequestTypeEntity;
import org.recap.repository.ItemDetailsRepository;
import org.recap.repository.RequestItemDetailsRepository;
import org.recap.repository.RequestTypeDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hemalathas on 4/5/17.
 */
@Service
public class RequestDataLoadService {

    private static final Logger logger = LoggerFactory.getLogger(RequestDataLoadService.class);

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private RequestTypeDetailsRepository requestTypeDetailsRepository;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    /**
     * To save the given requestDataLoadCSVRecords in scsb.
     *
     * @param requestDataLoadCSVRecords the request data load csv records
     * @param barcodeSet                the barcode set
     * @return the set
     * @throws ParseException the parse exception
     */
    public Set<String> process(List<RequestDataLoadCSVRecord> requestDataLoadCSVRecords, Set<String> barcodeSet) throws ParseException {
        List<RequestItemEntity> requestItemEntityList = new ArrayList<>();
        List<String> duplicateBarcodes = new ArrayList<>();
        Set<String> barcodesNotInScsb = new HashSet<>();
        RequestItemEntity requestItemEntity = null;
        for(RequestDataLoadCSVRecord requestDataLoadCSVRecord : requestDataLoadCSVRecords){
            Integer itemId = 0;
            Integer requestingInstitutionId = 0 ;
            requestItemEntity = new RequestItemEntity();
            if(!barcodeSet.add(requestDataLoadCSVRecord.getBarcode())){
                duplicateBarcodes.add(requestDataLoadCSVRecord.getBarcode());
                logger.info("Barcodes duplicated in the incoming record {}",requestDataLoadCSVRecord.getBarcode());
                continue;
            }
            Map<String,Integer> itemInfo = getItemInfo(requestDataLoadCSVRecord.getBarcode());
            if(itemInfo.get(ReCAPConstants.REQUEST_DATA_LOAD_ITEM_ID) != null){
                itemId = itemInfo.get(ReCAPConstants.REQUEST_DATA_LOAD_ITEM_ID);
            }
            if(itemInfo.get(ReCAPConstants.REQUEST_DATA_LOAD_REQUESTING_INST_ID) != null){
                requestingInstitutionId = itemInfo.get(ReCAPConstants.REQUEST_DATA_LOAD_REQUESTING_INST_ID);
            }
            if(itemId == 0 || requestingInstitutionId == 0){
                barcodesNotInScsb.add(requestDataLoadCSVRecord.getBarcode());
            }else{
                prepareRequestItemEntities(requestItemEntityList, requestItemEntity, requestDataLoadCSVRecord, itemId, requestingInstitutionId);
            }
        }
        savingRequestItemEntities(requestItemEntityList);
        logger.info("Total request item count not in db {}" ,barcodesNotInScsb.size());
        logger.info("Total duplicate barcodes from las report{}", duplicateBarcodes.size());
        return barcodesNotInScsb;
    }

    private void prepareRequestItemEntities(List<RequestItemEntity> requestItemEntityList, RequestItemEntity requestItemEntity, RequestDataLoadCSVRecord requestDataLoadCSVRecord, Integer itemId, Integer requestingInstitutionId) throws ParseException {
        List<RequestItemEntity> requestAlreadyPlacedList = requestItemDetailsRepository.findByitemId(itemId,Arrays.asList(ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED,ReCAPConstants.REQUEST_STATUS_RECALLED,ReCAPConstants.REQUEST_STATUS_EDD,ReCAPConstants.REQUEST_STATUS_INITIAL_LOAD));
        if (CollectionUtils.isEmpty(requestAlreadyPlacedList)) {
            requestItemEntity.setItemId(itemId);
            requestItemEntity.setRequestingInstitutionId(requestingInstitutionId);
            SimpleDateFormat formatter = new SimpleDateFormat(ReCAPConstants.REQUEST_DATA_LOAD_DATE_FORMAT);
            requestItemEntity.setCreatedBy(ReCAPConstants.REQUEST_DATA_LOAD_CREATED_BY);
            setValuesFromOutReportToRequestItemEntity(requestItemEntity, requestDataLoadCSVRecord, formatter);
            requestItemEntity.setRequestStatusId(9);
            requestItemEntity.setPatronId(ReCAPConstants.REQUEST_DATA_LOAD_PATRON_ID);
            requestItemEntityList.add(requestItemEntity);
        }
    }

    private void setValuesFromOutReportToRequestItemEntity(RequestItemEntity requestItemEntity, RequestDataLoadCSVRecord requestDataLoadCSVRecord, SimpleDateFormat formatter) throws ParseException {
        requestItemEntity.setRequestTypeId(getRequestTypeId(requestDataLoadCSVRecord.getDeliveryMethod()));
        Date createdDate = getDateFormat(requestDataLoadCSVRecord.getCreatedDate());
        requestItemEntity.setCreatedDate(formatter.parse(formatter.format(createdDate)));
        Date updatedDate = getDateFormat(requestDataLoadCSVRecord.getLastUpdatedDate());
        requestItemEntity.setLastUpdatedDate(formatter.parse(formatter.format(updatedDate)));
        String stopCode=requestDataLoadCSVRecord.getStopCode() != null ? requestDataLoadCSVRecord.getStopCode() : "Stop Code Not Found";
        requestItemEntity.setStopCode(stopCode);
    }

    private void savingRequestItemEntities(List<RequestItemEntity> requestItemEntityList) {
        if (!CollectionUtils.isEmpty(requestItemEntityList)){
            List<RequestItemEntity> savedRequestItemEntities = requestItemDetailsRepository.save(requestItemEntityList);
            requestItemDetailsRepository.flush();
            logger.info("Total request item count saved in db {}", savedRequestItemEntities.size());
        }
    }

    private Date getDateFormat(String date) throws ParseException {
        SimpleDateFormat formatter=new SimpleDateFormat(ReCAPConstants.REQUEST_DATA_LOAD_DATE_FORMAT);
        if (StringUtils.isNotBlank(date)){
            return formatter.parse(date);
        }
        else {
            String currentDate = formatter.format(new Date());
            return formatter.parse(currentDate);
        }
    }

    private Map<String,Integer> getItemInfo(String barcode){
        Integer itemId = 0;
        Integer owningInstitutionId = 0;
        Map<String,Integer> itemInfo = new HashMap<>();
        List<ItemEntity> itemEntityList = itemDetailsRepository.findByBarcodeAndItemStatusEntity_StatusCode(barcode,ReCAPConstants.NOT_AVAILABLE);
        if(org.apache.commons.collections.CollectionUtils.isNotEmpty(itemEntityList)){
            Integer itemInstitutionId = itemEntityList.get(0).getOwningInstitutionId();
            for(ItemEntity itemEntity : itemEntityList){
                if(itemEntity.getOwningInstitutionId() == itemInstitutionId){
                    itemId = itemEntityList.get(0).getItemId();
                    owningInstitutionId = itemEntityList.get(0).getOwningInstitutionId();
                }else{
                    logger.info("Barcodes duplicated in database with different institution {}",barcode);
                    return itemInfo;
                }
            }
            itemInfo.put(ReCAPConstants.REQUEST_DATA_LOAD_ITEM_ID , itemId);
            itemInfo.put(ReCAPConstants.REQUEST_DATA_LOAD_REQUESTING_INST_ID , owningInstitutionId);
        }
        return itemInfo;
    }

    private Integer getRequestTypeId(String deliveyMethod){
        Integer requestTypeId = 0;
        if(deliveyMethod.equalsIgnoreCase(ReCAPConstants.REQUEST_DATA_LOAD_REQUEST_TYPE)){
            RequestTypeEntity requestTypeEntity = requestTypeDetailsRepository.findByrequestTypeCode(ReCAPConstants.RETRIEVAL);
            requestTypeId = requestTypeEntity.getRequestTypeId();
        }
        return requestTypeId;
    }

}
