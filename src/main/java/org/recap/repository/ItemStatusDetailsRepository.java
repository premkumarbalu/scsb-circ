package org.recap.repository;

import org.recap.model.ItemStatusEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by hemalathas on 22/6/16.
 */
public interface ItemStatusDetailsRepository extends PagingAndSortingRepository<ItemStatusEntity, Integer> {

    ItemStatusEntity findByStatusCode(String statusCode);

    ItemStatusEntity findByItemStatusId(Integer itemStatusId);
}
