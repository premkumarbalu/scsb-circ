package org.recap.repository;

import org.recap.model.DeletedRecordsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by rajeshbabuk on 8/5/17.
 */
public interface DeletedRecordsRepository extends JpaRepository<DeletedRecordsEntity, Integer> {

    List<DeletedRecordsEntity> findByDeletedReportedStatus(@Param("deletedReportedStatus") String customerCode);
    Long countByDeletedReportedStatus(@Param("deletedReportedStatus") String customerCode);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE DELETED_RECORDS_T SET DELETED_REPORTED_STATUS =:updateDeletedReportedStatus where DELETED_REPORTED_STATUS = :deletedReportedStatus ", nativeQuery = true)
    int updateDeletedReportedStatus(@Param("updateDeletedReportedStatus") String updateDeletedReportedStatus, @Param("deletedReportedStatus") String deletedReportedStatus);
}
