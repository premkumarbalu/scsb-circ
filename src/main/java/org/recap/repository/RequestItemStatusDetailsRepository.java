package org.recap.repository;


import org.recap.model.RequestStatusEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by hemalathas on 22/6/16.
 */
public interface RequestItemStatusDetailsRepository extends PagingAndSortingRepository<RequestStatusEntity, Integer> {

    /**
     * Find by request status code request status entity.
     *
     * @param requestStatusCode the request status code
     * @return the request status entity
     */
    RequestStatusEntity findByRequestStatusCode(String requestStatusCode);

    /**
     * Find by request status id request status entity.
     *
     * @param requestStatusId the request status id
     * @return the request status entity
     */
    RequestStatusEntity  findByRequestStatusId(Integer requestStatusId);


    List<RequestStatusEntity> findByRequestStatusCodeIn(List<String> requestStatusCode);
}
