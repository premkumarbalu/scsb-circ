package org.recap.repository;


import org.recap.model.ItemChangeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by rajeshbabuk on 18/10/16.
 */
public interface ItemChangeLogDetailsRepository extends JpaRepository<ItemChangeLogEntity, Integer> {

     ItemChangeLogEntity findByRecordIdAndOperationType(Integer recordId, String operationType);
}
