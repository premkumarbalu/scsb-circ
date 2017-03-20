package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 13/3/17.
 */
public class CollectionGroupEntityUT extends BaseTestCase{

    @Autowired
    CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Test
    public void testCollectionGroupEntity(){
        CollectionGroupEntity collectionGroupEntity = new CollectionGroupEntity();
        collectionGroupEntity.setCreatedDate(new Date());
        collectionGroupEntity.setCollectionGroupCode("others");
        collectionGroupEntity.setCollectionGroupDescription("others");
        collectionGroupEntity.setLastUpdatedDate(new Date());
        CollectionGroupEntity savedCollectionGroupEntity = collectionGroupDetailsRepository.save(collectionGroupEntity);
        assertNotNull(savedCollectionGroupEntity);
        assertNotNull(savedCollectionGroupEntity.getCollectionGroupId());
        assertNotNull(savedCollectionGroupEntity.getCollectionGroupCode());
        assertNotNull(savedCollectionGroupEntity.getCollectionGroupDescription());
        assertNotNull(savedCollectionGroupEntity.getCreatedDate());
        assertNotNull(savedCollectionGroupEntity.getLastUpdatedDate());
    }

}