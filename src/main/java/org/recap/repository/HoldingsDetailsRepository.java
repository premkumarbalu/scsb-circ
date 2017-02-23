package org.recap.repository;

import org.recap.model.HoldingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * Created by chenchulakshmig on 6/13/16.
 */
public interface HoldingsDetailsRepository extends JpaRepository<HoldingsEntity, String> {

    HoldingsEntity findByHoldingsId(Integer holdingsId);

    Long countByOwningInstitutionId(Integer owningInstitutionId);

    @Query(value = "SELECT COUNT(*) FROM ITEM_T, ITEM_HOLDINGS_T WHERE ITEM_HOLDINGS_T.ITEM_INST_ID = ITEM_T.OWNING_INST_ID AND " +
            "ITEM_HOLDINGS_T.OWNING_INST_ITEM_ID = ITEM_T.OWNING_INST_ITEM_ID AND ITEM_T.IS_DELETED = 0 AND " +
            "ITEM_HOLDINGS_T.OWNING_INST_HOLDINGS_ID = :owningInstitutionHoldingsId AND ITEM_HOLDINGS_T.HOLDINGS_INST_ID = :owningInstitutionId", nativeQuery = true)
    Long getNonDeletedItemsCount(@Param("owningInstitutionId") Integer owningInstitutionId, @Param("owningInstitutionHoldingsId") String owningInstitutionHoldingsId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE HoldingsEntity holdings SET holdings.isDeleted = true, holdings.lastUpdatedBy = :lastUpdatedBy, holdings.lastUpdatedDate = :lastUpdatedDate WHERE holdings.holdingsId IN :holdingIds")
    int markHoldingsAsDeleted(@Param("holdingIds") List<Integer> holdingIds, @Param("lastUpdatedBy") String lastUpdatedBy, @Param("lastUpdatedDate") Date lastUpdatedDate);

}
