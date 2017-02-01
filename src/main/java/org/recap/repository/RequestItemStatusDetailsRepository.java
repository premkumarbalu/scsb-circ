package org.recap.repository;


import org.recap.model.RequestStatusEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by hemalathas on 22/6/16.
 */
public interface RequestItemStatusDetailsRepository extends PagingAndSortingRepository<RequestStatusEntity, Integer> {

    RequestStatusEntity findByRequestStatusCode(String requestStatusCode);

    RequestStatusEntity  findByRequestStatusId(Integer requestStatusId);
}
