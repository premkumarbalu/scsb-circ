package org.recap.repository;

import org.recap.model.CollectionGroupEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by hemalathas on 22/6/16.
 */
public interface CollectionGroupDetailsRepository extends PagingAndSortingRepository<CollectionGroupEntity, Integer> {

    CollectionGroupEntity findByCollectionGroupCode(String collectionGroupCode);
}
