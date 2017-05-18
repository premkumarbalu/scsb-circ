package org.recap.service.requestdataload;
import org.recap.ReCAPConstants;
import org.recap.camel.requestinitialdataload.RequestDataLoadCSVRecord;
import org.recap.camel.requestinitialdataload.RequestDataLoadErrorCSVRecord;
import org.recap.camel.requestinitialdataload.processor.RequestInitialDataLoadProcessor;
import org.recap.model.*;
import org.recap.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
    private CustomerCodeDetailsRepository customerCodeDetailsRepository;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    public List<RequestDataLoadErrorCSVRecord> process(List<RequestDataLoadCSVRecord> requestDataLoadCSVRecords, Set<String> barcodeSet) throws ParseException {
        List<RequestItemEntity> requestItemEntityList = new ArrayList<>();
        List<RequestDataLoadErrorCSVRecord> requestDataLoadErrorCSVRecords = new ArrayList<>();
        RequestItemEntity requestItemEntity = null;
        for(RequestDataLoadCSVRecord requestDataLoadCSVRecord : requestDataLoadCSVRecords){
            Integer itemId = 0;
            Integer requestingInstitutionId = 0 ;
            RequestDataLoadErrorCSVRecord requestDataLoadErrorCSVRecord = new RequestDataLoadErrorCSVRecord();
            requestItemEntity = new RequestItemEntity();
            if(!barcodeSet.add(requestDataLoadCSVRecord.getBarcode())){
                requestDataLoadErrorCSVRecord.setBarcodes(requestDataLoadCSVRecord.getBarcode());
                requestDataLoadErrorCSVRecords.add(requestDataLoadErrorCSVRecord);
                continue;
            }
            Map<String,Integer> itemInfo = getItemInfo(requestDataLoadCSVRecord.getBarcode());
            if(itemInfo.get(ReCAPConstants.REQUEST_DATA_LOAD_ITEM_ID) != null){
                itemId = itemInfo.get(ReCAPConstants.REQUEST_DATA_LOAD_ITEM_ID);
            }
            if(itemInfo.get(ReCAPConstants.REQUEST_DATA_LOAD_REQUESTING_INST_ID) != null){
                requestingInstitutionId = itemInfo.get(ReCAPConstants.REQUEST_DATA_LOAD_REQUESTING_INST_ID);
            }
            Integer requestingInstitution = getRequestingInstitution(requestDataLoadCSVRecord.getStopCode(),requestingInstitutionId);
            if(itemId == 0 || requestingInstitution == 0){
                if(requestingInstitution == 0){
                    requestDataLoadErrorCSVRecord.setStopCodes(requestDataLoadCSVRecord.getStopCode());
                }
                if (itemId == 0) {
                    requestDataLoadErrorCSVRecord.setBarcodes(requestDataLoadCSVRecord.getBarcode());
                }
                requestDataLoadErrorCSVRecords.add(requestDataLoadErrorCSVRecord);
            }else{
                requestItemEntity.setItemId(itemId);
                requestItemEntity.setRequestTypeId(getRequestTypeId(requestDataLoadCSVRecord.getDeliveryMethod()));
                requestItemEntity.setRequestingInstitutionId(requestingInstitution);
                SimpleDateFormat formatter=new SimpleDateFormat(ReCAPConstants.REQUEST_DATA_LOAD_DATE_FORMAT);
                if(requestDataLoadCSVRecord.getExpiryDate() != null){
                    requestItemEntity.setRequestExpirationDate(formatter.parse(requestDataLoadCSVRecord.getExpiryDate()));
                }
                requestItemEntity.setCreatedBy(ReCAPConstants.REQUEST_DATA_LOAD_CREATED_BY);
                requestItemEntity.setCreatedDate(formatter.parse(requestDataLoadCSVRecord.getCreatedDate()));
                requestItemEntity.setLastUpdatedDate(formatter.parse(requestDataLoadCSVRecord.getLastUpdatedDate()));
                requestItemEntity.setStopCode(requestDataLoadCSVRecord.getStopCode());
                requestItemEntity.setRequestStatusId(9);
                requestItemEntity.setPatronId(ReCAPConstants.REQUEST_DATA_LOAD_PATRON_ID);
                requestItemEntityList.add(requestItemEntity);
            }
        }
        List<RequestItemEntity> savedRequestItemEntities = requestItemDetailsRepository.save(requestItemEntityList);
        logger.info("Total request item count saved in db "+ savedRequestItemEntities.size());
        return requestDataLoadErrorCSVRecords;
    }

    private Map<String,Integer> getItemInfo(String barcode){
        Integer itemAvailabilityStatusId = 0;
        Map<String,Integer> itemInfo = new HashMap<>();
        ItemStatusEntity itemStatusEntity = itemStatusDetailsRepository.findByStatusCode(ReCAPConstants.NOT_AVAILABLE);
        if(itemStatusEntity != null){
            itemAvailabilityStatusId = itemStatusEntity.getItemStatusId();
        }
        ItemEntity itemEntity = itemDetailsRepository.findByBarcodeAndNotAvailable(barcode,itemAvailabilityStatusId);
        if(itemEntity != null){
            itemInfo.put(ReCAPConstants.REQUEST_DATA_LOAD_ITEM_ID , itemEntity.getItemId());
            itemInfo.put(ReCAPConstants.REQUEST_DATA_LOAD_REQUESTING_INST_ID , itemEntity.getOwningInstitutionId());
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

    private Integer getRequestingInstitution(String stopCode,Integer requestingInstitution){
        Integer requestingInstitutionId = 0;
        if(!StringUtils.isEmpty(stopCode)){
            CustomerCodeEntity customerCodeEntity = customerCodeDetailsRepository.findByCustomerCode(stopCode);
            if(customerCodeEntity != null){
                if(customerCodeEntity.getOwningInstitutionId() == null){
                    return requestingInstitution;
                }
                requestingInstitutionId = customerCodeEntity.getOwningInstitutionId();
            }
        }
        return requestingInstitutionId;
    }
}
