package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.ItemStatusEntity;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 17/11/16.
 */
public class ItemStatusDetailsRepositoryUT extends BaseTestCase{

    @Autowired
    ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Test
    public void testItemStatus(){
        ItemStatusEntity itemStatusEntity = itemStatusDetailsRepository.findByItemStatusId(1);
        assertNotNull(itemStatusEntity);
        assertEquals(itemStatusEntity.getStatusCode(),"Available");
    }



}