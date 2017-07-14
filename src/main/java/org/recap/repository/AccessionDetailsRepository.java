package org.recap.repository;

import org.recap.model.AccessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by rajeshbabuk on 8/5/17.
 */
public interface AccessionDetailsRepository extends JpaRepository<AccessionEntity, Integer> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE accessionRequest from recap.accession_t accessionRequest where accessionRequest.ACCESSION_STATUS=?1 AND DATEDIFF(?2,accessionRequest.CREATED_DATE)>=?3", nativeQuery = true)
    int purgeAccessionRequests(@Param("accessionStatus") String accessionStatus, @Param("createdDate") Date createdDate, @Param("dateDifference") Integer dateDifference);
}
