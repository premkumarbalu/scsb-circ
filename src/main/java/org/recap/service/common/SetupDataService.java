package org.recap.service.common;

import org.recap.ReCAPConstants;
import org.recap.model.InstitutionEntity;
import org.recap.model.ItemStatusEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by premkb on 11/6/17.
 */
@Service
public class SetupDataService {

    private static final Logger logger = LoggerFactory.getLogger(SetupDataService.class);

    @Autowired
    private RepositoryService repositoryService;

    private Map<Integer,String> itemStatusIdCodeMap;

    private Map<String,Integer> itemStatusCodeIdMap;

    private Map<Integer,String> institutionEntityMap;

    public Map getItemStatusIdCodeMap() {
        if (null == itemStatusIdCodeMap) {
            itemStatusIdCodeMap = new HashMap();
            try {
                Iterable<ItemStatusEntity> itemStatusEntities = repositoryService.getItemStatusDetailsRepository().findAll();
                for (Iterator iterator = itemStatusEntities.iterator(); iterator.hasNext(); ) {
                    ItemStatusEntity itemStatusEntity = (ItemStatusEntity) iterator.next();
                    itemStatusIdCodeMap.put(itemStatusEntity.getItemStatusId(), itemStatusEntity.getStatusCode());
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
        return itemStatusIdCodeMap;
    }

    public Map getItemStatusCodeIdMap() {
        if (null == itemStatusCodeIdMap) {
            itemStatusCodeIdMap = new HashMap();
            try {
                Iterable<ItemStatusEntity> itemStatusEntities = repositoryService.getItemStatusDetailsRepository().findAll();
                for (Iterator iterator = itemStatusEntities.iterator(); iterator.hasNext(); ) {
                    ItemStatusEntity itemStatusEntity = (ItemStatusEntity) iterator.next();
                    itemStatusCodeIdMap.put(itemStatusEntity.getStatusCode(), itemStatusEntity.getItemStatusId());
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
        return itemStatusCodeIdMap;
    }

    /**
     * Gets institution entity map.
     *
     * @return the institution entity map
     */
    public Map getInstitutionEntityMap() {
        if (null == institutionEntityMap) {
            institutionEntityMap = new HashMap();
            try {
                Iterable<InstitutionEntity> institutionEntities = repositoryService.getInstitutionDetailsRepository().findAll();
                for (Iterator iterator = institutionEntities.iterator(); iterator.hasNext(); ) {
                    InstitutionEntity institutionEntity = (InstitutionEntity) iterator.next();
                    institutionEntityMap.put(institutionEntity.getInstitutionId(), institutionEntity.getInstitutionCode());
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
        return institutionEntityMap;
    }
}
