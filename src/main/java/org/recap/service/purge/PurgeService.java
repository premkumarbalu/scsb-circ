package org.recap.service.purge;

import org.recap.ReCAPConstants;
import org.recap.model.RequestTypeEntity;
import org.recap.repository.RequestItemDetailsRepository;
import org.recap.repository.RequestTypeDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hemalathas on 13/4/17.
 */
@Service
public class PurgeService {

    @Value("${purge.email.address.edd.request.date.limit}")
    private Integer purgeEmailEddRequestDateLimit;

    @Value("${purge.email.address.physical.request.date.limit}")
    private Integer purgeEmailPhysicalRequestDateLimit;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    private RequestTypeDetailsRepository requestTypeDetailsRepository;

    public Map<String,Integer> purgeEmailAddress(){
        List<RequestTypeEntity> requestTypeEntityList = requestTypeDetailsRepository.findAll();
        List<Integer> physicalRequestTypeIdList = new ArrayList<>();
        List<Integer> eddRequestTypeIdList = new ArrayList();
        for(RequestTypeEntity requestTypeEntity : requestTypeEntityList){
            if(requestTypeEntity.getRequestTypeCode().equals(ReCAPConstants.EDD_REQUEST)){
                eddRequestTypeIdList.add(requestTypeEntity.getRequestTypeId());
            }else{
                physicalRequestTypeIdList.add(requestTypeEntity.getRequestTypeId());
            }
        }
        int noOfUpdatedRecordsForEddRequest = requestItemDetailsRepository.purgeEmailId(eddRequestTypeIdList,new Date(), purgeEmailEddRequestDateLimit);
        int noOfUpdatedRecordsForPhysicalRequest = requestItemDetailsRepository.purgeEmailId(physicalRequestTypeIdList,new Date(), purgeEmailPhysicalRequestDateLimit);
        Map<String,Integer> responseMap = new HashMap<>();
        responseMap.put(ReCAPConstants.PURGE_EDD_REQUEST , noOfUpdatedRecordsForEddRequest);
        responseMap.put(ReCAPConstants.PURGE_PHYSICAL_REQUEST , noOfUpdatedRecordsForPhysicalRequest);
        return responseMap;
    }
}
