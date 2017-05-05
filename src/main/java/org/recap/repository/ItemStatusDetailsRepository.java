package org.recap.repository;

import org.recap.model.ItemStatusEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by hemalathas on 22/6/16.
 */
public interface ItemStatusDetailsRepository extends PagingAndSortingRepository<ItemStatusEntity, Integer> {

    /**
     * Find by status code item status entity.
     *
     * @param statusCode the status code
     * @return the item status entity
     */
    ItemStatusEntity findByStatusCode(String statusCode);

    /**
     * Find by item status id item status entity.
     *
     * @param itemStatusId the item status id
     * @return the item status entity
     */
    @Cacheable("Reference")
    ItemStatusEntity findByItemStatusId(Integer itemStatusId);
}
