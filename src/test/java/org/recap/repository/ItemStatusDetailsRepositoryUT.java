package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.ItemStatusEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hemalathas on 17/11/16.
 */
public class ItemStatusDetailsRepositoryUT extends BaseTestCase{

    private static final Logger logger = LoggerFactory.getLogger(ItemStatusDetailsRepositoryUT.class);
    @Autowired
    ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Test
    public void testItemStatus(){
        ItemStatusEntity itemStatusEntity = itemStatusDetailsRepository.findByItemStatusId(1);
        logger.info(itemStatusEntity.getStatusCode());
        assertNotNull(itemStatusEntity);
        assertEquals(itemStatusEntity.getStatusCode(),"Available");
    }



}