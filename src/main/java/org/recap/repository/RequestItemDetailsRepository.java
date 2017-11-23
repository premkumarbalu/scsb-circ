package org.recap.repository;


import org.recap.model.RequestItemEntity;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by rajeshbabuk on 26/10/16.
 */
public interface RequestItemDetailsRepository extends JpaRepository<RequestItemEntity, Integer>, JpaSpecificationExecutor {

    /**
     * Find by request id request item entity.
     *
     * @param requestId the request id
     * @return the request item entity
     */
    RequestItemEntity findByRequestId(@Param("requestId") Integer requestId);

    @Query(value = "select requestItemEntity from RequestItemEntity requestItemEntity inner join requestItemEntity.requestStatusEntity as rse where requestItemEntity.requestId =?1")
    RequestItemEntity findRequestItemByRequestId(@Param("requestId") Integer requestId);

    /**
     * Find by request id in list.
     *
     * @param requestIds the request ids
     * @return the list
     */
    List<RequestItemEntity> findByRequestIdIn(List<Integer> requestIds);

    /**
     * Find by item barcode page.
     *
     * @param pageable    the pageable
     * @param itemBarcode the item barcode
     * @return the page
     */
    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item where item.barcode = :itemBarcode")
    Page<RequestItemEntity> findByItemBarcode(Pageable pageable, @Param("itemBarcode") String itemBarcode);

    @Query(value = "select request from RequestItemEntity request inner join request.requestStatusEntity as rse where rse.requestStatusCode = :requestStatusCode")
    List<RequestItemEntity> findByRequestStatusCode(@Param("requestStatusCode") List<String> requestStatusCode);

    @Query(value = "select request from RequestItemEntity request inner join request.requestStatusEntity as rse where request.requestId in(?1) and rse.requestStatusCode in(?2)")
    List<RequestItemEntity> findByRequestIdsAndStatusCodes(@Param("itemBarcode") List<Integer> requestIds, @Param("requestStatusCode") List<String> requestStatusCodes);

    /**
     * Find by item barcode and request sta code request item entity.
     *
     * @param itemBarcode       the item barcode
     * @param requestStatusCodes the request status code
     * @return the request item entity
     * @throws IncorrectResultSizeDataAccessException the incorrect result size data access exception
     */
    @Query(value = "select requestItemEntity from RequestItemEntity requestItemEntity inner join requestItemEntity.itemEntity ie inner join requestItemEntity.requestStatusEntity as rse  where ie.barcode = :itemBarcode and rse.requestStatusCode= :requestStatusCode ")
    RequestItemEntity findByItemBarcodeAndRequestStaCode(@Param("itemBarcode") String itemBarcode, @Param("requestStatusCode") String requestStatusCodes) throws IncorrectResultSizeDataAccessException;

    /**
     * Find by item barcode list.
     *
     * @param itemBarcode the item barcode
     * @return the list
     */
    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item where item.barcode = :itemBarcode")
    List<RequestItemEntity> findByItemBarcode(@Param("itemBarcode") String itemBarcode);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE recap.request_item_t SET EMAIL_ID ='' where REQUEST_TYPE_ID in (?1) AND DATEDIFF(?2,CREATED_DATE)>=?3", nativeQuery = true)
    int purgeEmailId(@Param("requestTypeIdList") List<Integer> requestTypeIdList, @Param("createdDate") Date createdDate, @Param("dateDifference") Integer dateDifference);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE requestItem from recap.request_item_t requestItem inner join recap.request_item_status_t requestItemStatus on requestItem.REQUEST_STATUS_ID = requestItemStatus.REQUEST_STATUS_ID where requestItemStatus.REQUEST_STATUS_CODE = ?1 AND DATEDIFF(?2,requestItem.CREATED_DATE)>=?3", nativeQuery = true)
    int purgeExceptionRequests(@Param("requestStatusCode") String requestStatusCode, @Param("createdDate") Date createdDate, @Param("dateDifference") Integer dateDifference);

    @Query(value = "select request from RequestItemEntity request inner join request.requestStatusEntity status where request.itemId= :itemId and status.requestStatusCode in (:requestStatusCodes)")
    List<RequestItemEntity> findByitemId(@Param("itemId") Integer itemId, @Param("requestStatusCodes") List<String> requestStatusCodes);

    /**
     * Gets requests based on the given request id range.
     * @param requestIdFrom
     * @param requestIdTo
     * @return
     */
    @Query(value = "SELECT request FROM RequestItemEntity as request WHERE request.requestId BETWEEN :requestIdFrom and :requestIdTo")
    List<RequestItemEntity> getRequestsBasedOnRequestIdRange(@Param("requestIdFrom") Integer requestIdFrom, @Param("requestIdTo")Integer requestIdTo);

    /**
     * Gets requests based on the last updated date range.
     * @param createdDateFrom
     * @param createdDateTo
     * @return
     */
    @Query(value = "SELECT request FROM RequestItemEntity as request WHERE request.createdDate BETWEEN :createdDateFrom and :createdDateTo")
    List<RequestItemEntity> getRequestsBasedOnDateRange(@Param("createdDateFrom") Date createdDateFrom, @Param("createdDateTo") Date createdDateTo);
}
