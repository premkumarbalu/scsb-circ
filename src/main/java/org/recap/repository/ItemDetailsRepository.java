package org.recap.repository;

import org.recap.model.ItemEntity;
import org.recap.model.ItemPK;
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
 * Created by chenchulakshmig on 21/6/16.
 */
public interface ItemDetailsRepository extends JpaRepository<ItemEntity, ItemPK> {

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
     * Find list of ItemEntity based on the give list of barcodes and owning institution id.
     *
     * @param barcodes            the barcodes
     * @param owningInstitutionId the owning institution id
     * @return the list
     */
    List<ItemEntity> findByBarcodeInAndOwningInstitutionId(List<String> barcodes, Integer owningInstitutionId);

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

    /**
     * Mark item as not deleted int.
     *
     * @param itemIds         the item ids
     * @param lastUpdatedBy   the last updated by
     * @param lastUpdatedDate the last updated date
     * @return the int
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ItemEntity item SET item.isDeleted = false, item.lastUpdatedBy = :lastUpdatedBy, item.lastUpdatedDate = :lastUpdatedDate WHERE item.itemId IN :itemIds")
    int markItemsAsNotDeleted(@Param("itemIds") List<Integer> itemIds, @Param("lastUpdatedBy") String lastUpdatedBy, @Param("lastUpdatedDate") Date lastUpdatedDate);

    /**
     * Gets item entities for the given barcode and item availability status id.
     *
     * @param barcode                  the barcode
     * @param itemAvailabilityStatusId the item availability status id
     * @return the list
     */
    @Query("select item from ItemEntity item where item.barcode = :barcode and item.itemAvailabilityStatusId = :itemAvailabilityStatusId")
    List<ItemEntity> findByBarcodeAndNotAvailable(@Param("barcode") String barcode,@Param("itemAvailabilityStatusId") Integer itemAvailabilityStatusId);

    /**
     * Gets item count for the given date difference, status id, cataloging status and isDeleted.
     *
     * @param dateDifference           the date difference
     * @return the not available items count
     */
    @Query(value = "select count(*) from recap.request_item_t rit " +
            "inner join recap.item_t it on rit.ITEM_ID = it.ITEM_ID " +
            "where rit.REQUEST_STATUS_ID in (:requestStatusCodes) " +
            "and date(rit.LAST_UPDATED_DATE) < DATE_SUB(date(curdate()), INTERVAL :dateDifference DAY) " +
            "and it.IS_DELETED = 0 " +
            "and it.ITEM_AVAIL_STATUS_ID = :notAvailableId", nativeQuery = true)
    Long getNotAvailableItemsCount(@Param("dateDifference") Integer dateDifference,
                                   @Param("requestStatusCodes") List<Integer> requestStatusCodes,
                                   @Param("notAvailableId") Integer notAvailableId);

    /**
     * Gets item entities for the given date difference,status id, cataloging status and isDeleted.
     *
     * @param dateDifference           the date difference
     * @param getFrom                  the get from
     * @param batchSize                the batch size
     * @return the not available items
     */
    @Query(value = "select it.* from recap.request_item_t rit " +
            "inner join recap.item_t it on rit.ITEM_ID = it.ITEM_ID " +
            "where rit.REQUEST_STATUS_ID in (:requestStatusCodes) " +
            "and date(rit.LAST_UPDATED_DATE) < DATE_SUB(date(curdate()), INTERVAL :dateDifference DAY) " +
            "and it.IS_DELETED = 0 " +
            "and it.ITEM_AVAIL_STATUS_ID = :notAvailableId " +
            "order by rit.LAST_UPDATED_DATE desc limit :getFrom , :batchSize", nativeQuery = true)
    List<ItemEntity> getNotAvailableItems(@Param("dateDifference") Integer dateDifference,
                                          @Param("requestStatusCodes") List<Integer> requestStatusCodes,
                                          @Param("getFrom") long getFrom ,
                                          @Param("batchSize") long batchSize,
                                          @Param("notAvailableId") Integer notAvailableId);

    /**
     * Updates the item availability status ,last updated date and last updated by for the given barcodes.
     *
     * @param itemAvailabilityStatusId the item availability status id
     * @param lastUpdatedBy            the last updated by
     * @param barcode                  the barcode
     * @return the int
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ItemEntity item SET item.itemAvailabilityStatusId = :itemAvailabilityStatusId , item.lastUpdatedBy = :lastUpdatedBy where item.barcode IN (:barcode)")
    int updateAvailabilityStatus(@Param("itemAvailabilityStatusId") Integer itemAvailabilityStatusId,@Param("lastUpdatedBy") String lastUpdatedBy,@Param("barcode") String barcode);

    /**
     * Gets item entities for the given barcode and status code.
     *
     * @param barcode    the barcode
     * @param statusCode the status code
     * @return the list
     */
    List<ItemEntity> findByBarcodeAndItemStatusEntity_StatusCode(@Param("barcode") String barcode,@Param("statusCode") String statusCode);


    /**
     * Gets list of itementity based on list of owning institution item id and owning institution id.
     *
     * @param owningInstitutionItemIdList the owning institution item id list
     * @param owningInstitutionId         the owning institution id
     * @return the list
     */
    List<ItemEntity> findByOwningInstitutionItemIdInAndOwningInstitutionId(List<String> owningInstitutionItemIdList, Integer owningInstitutionId);
}