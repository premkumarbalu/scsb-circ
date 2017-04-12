package org.recap.repository;

import org.recap.model.RequestTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by rajeshbabuk on 28/10/16.
 */
public interface RequestTypeDetailsRepository extends JpaRepository<RequestTypeEntity, Integer> {
    /**
     * Find byrequest type code request type entity.
     *
     * @param requestTypeCode the request type code
     * @return the request type entity
     */
    RequestTypeEntity findByrequestTypeCode(String requestTypeCode);

}
