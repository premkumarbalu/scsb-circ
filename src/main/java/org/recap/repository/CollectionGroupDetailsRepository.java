package org.recap.repository;

import org.recap.model.CollectionGroupEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by hemalathas on 22/6/16.
 */
public interface CollectionGroupDetailsRepository extends PagingAndSortingRepository<CollectionGroupEntity, Integer> {

    /**
     * Find by collection group code collection group entity.
     *
     * @param collectionGroupCode the collection group code
     * @return the collection group entity
     */
    CollectionGroupEntity findByCollectionGroupCode(String collectionGroupCode);
}
