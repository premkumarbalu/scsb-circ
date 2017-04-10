package org.recap.repository;


import org.recap.model.ItemChangeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by rajeshbabuk on 18/10/16.
 */
public interface ItemChangeLogDetailsRepository extends JpaRepository<ItemChangeLogEntity, Integer> {

     /**
      * Find by record id and operation type item change log entity.
      *
      * @param recordId      the record id
      * @param operationType the operation type
      * @return the item change log entity
      */
     ItemChangeLogEntity findByRecordIdAndOperationType(Integer recordId, String operationType);
}
