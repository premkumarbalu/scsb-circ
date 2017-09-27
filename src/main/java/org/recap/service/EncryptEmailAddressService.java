package org.recap.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.recap.ReCAPConstants;
import org.recap.model.RequestItemEntity;
import org.recap.repository.RequestItemDetailsRepository;
import org.recap.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by akulak on 20/9/17.
 */
@Service
public class EncryptEmailAddressService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptEmailAddressService.class);

    public static final String REQUEST_ID = "requestId";

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    public String encryptEmailAddress() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int count = 0;
        long totalRequestItems = requestItemDetailsRepository.count();
        int totalPageCounts =(int) Math.ceil((double)totalRequestItems / (double)1000);
        logger.info("total page counts for updating email address : {}",totalPageCounts);
        for (int i =0;i < totalPageCounts;i++){
            List<RequestItemEntity> requestItemEntityListToSave = new ArrayList<>();
            try {
                Pageable pageable = new PageRequest(i,1000, Sort.Direction.ASC,REQUEST_ID);
                Page<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findAll(pageable);
                List<RequestItemEntity> requestItemEntityList = requestItemEntities.getContent();
                for (RequestItemEntity requestItemEntity : requestItemEntityList) {
                    if (StringUtils.isNotBlank(requestItemEntity.getEmailId())){
                        String encryptedEmailId = securityUtil.getEncryptedValue(requestItemEntity.getEmailId());
                        logger.info("Going to update email address for request id: {} , old email address: {} and encrypted email address: {}",requestItemEntity.getRequestId(),requestItemEntity.getEmailId(),encryptedEmailId);
                        requestItemEntity.setEmailId(encryptedEmailId);
                        requestItemEntityListToSave.add(requestItemEntity);
                        ++count;
                    }
                }
                if (CollectionUtils.isNotEmpty(requestItemEntityListToSave)){
                    requestItemDetailsRepository.save(requestItemEntityListToSave);
                    logger.info("Total number of request item entities saved in db : {}",requestItemEntityListToSave.size());
                }
            }catch (Exception e){
                logger.error(ReCAPConstants.LOG_ERROR,e);
                return "Error occurred"+e.getMessage();
            }
        }
        logger.info("Total time taken to encrypted all email address in ms {}",stopWatch.getTime());
        stopWatch.stop();
        return "Total encrypted email Address - "+count+"and total time taken in ms - "+stopWatch.getTime();
    }
}
