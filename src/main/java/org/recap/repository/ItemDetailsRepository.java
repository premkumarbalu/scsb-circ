package org.recap.repository;

import org.recap.model.ItemEntity;
import org.recap.model.ItemPK;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by chenchulakshmig on 21/6/16.
 */
public interface ItemDetailsRepository extends PagingAndSortingRepository<ItemEntity, ItemPK> {

    /**
     * Count by is deleted false long.
     *
     * @return the long
     */
    Long countByIsDeletedFalse();

    /**
     * Find all by is deleted false page.
     *
     * @param pageable the pageable
     * @return the page
     */
    Page<ItemEntity> findAllByIsDeletedFalse(Pageable pageable);

    /**
     * Find by item id item entity.
     *
     * @param itemId the item id
     * @return the item entity
     */
    ItemEntity findByItemId(Integer itemId);

    /**
     * Count by owning institution id and is deleted false long.
     *
     * @param institutionId the institution id
     * @return the long
     */
    Long countByOwningInstitutionIdAndIsDeletedFalse(Integer institutionId);

    /**
     * Find by owning institution id and is deleted false page.
     *
     * @param pageable      the pageable
     * @param institutionId the institution id
     * @return the page
     */
    Page<ItemEntity> findByOwningInstitutionIdAndIsDeletedFalse(Pageable pageable, Integer institutionId);

    /**
     * Find by owning institution id list.
     *
     * @param owningInstitutionId the owning institution id
     * @return the list
     */
    List<ItemEntity> findByOwningInstitutionId(Integer owningInstitutionId);

    /**
     * Find by owning institution item id item entity.
     *
     * @param owningInstitutionItemId the owning institution item id
     * @return the item entity
     */
    ItemEntity findByOwningInstitutionItemId(@Param("owningInstitutionItemId") String owningInstitutionItemId);

    /**
     * Find by barcode in list.
     *
     * @param barcodes the barcodes
     * @return the list
     */
    List<ItemEntity> findByBarcodeIn(List<String> barcodes);

    /**
     * Find by barcode in and complete list.
     *
     * @param barcodes the barcodes
     * @return the list
     */
    @Query("select item from ItemEntity item where item.barcode in (:barcodes) and item.isDeleted = 0 and item.catalogingStatus='Complete'")
    List<ItemEntity> findByBarcodeInAndComplete(@Param("barcodes") List<String> barcodes);

    /**
     * Find by barcode list.
     *
     * @param barcode the barcode
     * @return the list
     */
    List<ItemEntity> findByBarcode(String barcode);

    /**
     * Mark item as deleted int.
     *
     * @param itemId          the item id
     * @param lastUpdatedBy   the last updated by
     * @param lastUpdatedDate the last updated date
     * @return the int
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ItemEntity item SET item.isDeleted = true, item.lastUpdatedBy = :lastUpdatedBy, item.lastUpdatedDate = :lastUpdatedDate WHERE item.itemId = :itemId")
    int markItemAsDeleted(@Param("itemId") Integer itemId, @Param("lastUpdatedBy") String lastUpdatedBy, @Param("lastUpdatedDate") Date lastUpdatedDate);

}