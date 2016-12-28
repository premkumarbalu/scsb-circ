package org.recap.repository;

import org.recap.model.RequestTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by rajeshbabuk on 28/10/16.
 */
public interface RequestTypeDetailsRepository extends JpaRepository<RequestTypeEntity, Integer> {
    RequestTypeEntity findByrequestTypeCode(String requestTypeCode);

}
