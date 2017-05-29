package org.recap.repository;

import org.recap.model.BibliographicEntity;
import org.recap.model.BibliographicPK;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by pvsubrah on 6/10/16.
 */
public interface BibliographicDetailsRepository extends JpaRepository<BibliographicEntity, BibliographicPK> {

    /**
     * Find by owning institution bib id list.
     *
     * @param owningInstitutionBibId the owning institution bib id
     * @return the list
     */
    List<BibliographicEntity> findByOwningInstitutionBibId(String owningInstitutionBibId);

    /**
     * Gets non deleted items count.
     *
     * @param owningInstitutionId    the owning institution id
     * @param owningInstitutionBibId the owning institution bib id
     * @return the non deleted items count
     */
    @Query(value = "SELECT COUNT(*) FROM ITEM_T, BIBLIOGRAPHIC_ITEM_T WHERE BIBLIOGRAPHIC_ITEM_T.ITEM_INST_ID = ITEM_T.OWNING_INST_ID " +
            "AND BIBLIOGRAPHIC_ITEM_T.OWNING_INST_ITEM_ID = ITEM_T.OWNING_INST_ITEM_ID AND ITEM_T.IS_DELETED = 0 AND " +
            "BIBLIOGRAPHIC_ITEM_T.OWNING_INST_BIB_ID = :owningInstitutionBibId AND BIBLIOGRAPHIC_ITEM_T.BIB_INST_ID = :owningInstitutionId", nativeQuery = true)
    Long getNonDeletedItemsCount(@Param("owningInstitutionId") Integer owningInstitutionId, @Param("owningInstitutionBibId") String owningInstitutionBibId);

    /**
     * Mark bibs as deleted int.
     *
     * @param bibliographicIds the bibliographic ids
     * @param lastUpdatedBy    the last updated by
     * @param lastUpdatedDate  the last updated date
     * @return the int
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE BibliographicEntity bib SET bib.isDeleted = true, bib.lastUpdatedBy = :lastUpdatedBy, bib.lastUpdatedDate = :lastUpdatedDate WHERE bib.bibliographicId IN :bibliographicIds")
    int markBibsAsDeleted(@Param("bibliographicIds") List<Integer> bibliographicIds, @Param("lastUpdatedBy") String lastUpdatedBy, @Param("lastUpdatedDate") Date lastUpdatedDate);

    /**
     * Mark bibs as not deleted int.
     *
     * @param bibliographicIds the bibliographic ids
     * @param lastUpdatedBy    the last updated by
     * @param lastUpdatedDate  the last updated date
     * @return the int
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE BibliographicEntity bib SET bib.isDeleted = false, bib.lastUpdatedBy = :lastUpdatedBy, bib.lastUpdatedDate = :lastUpdatedDate WHERE bib.bibliographicId IN :bibliographicIds")
    int markBibsAsNotDeleted(@Param("bibliographicIds") List<Integer> bibliographicIds, @Param("lastUpdatedBy") String lastUpdatedBy, @Param("lastUpdatedDate") Date lastUpdatedDate);

    /**
     * Find bibliographic entity by using owning institution id and owning institution bib id.
     *
     * @param owningInstitutionId    the owning institution id
     * @param owningInstitutionBibId the owning institution bib id
     * @return the bibliographic entity
     */
    BibliographicEntity findByOwningInstitutionIdAndOwningInstitutionBibId(@Param("owningInstitutionId") Integer owningInstitutionId, @Param("owningInstitutionBibId") String owningInstitutionBibId);


}
