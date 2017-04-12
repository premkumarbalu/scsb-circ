package org.recap.repository;


import org.recap.model.RequestItemEntity;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query(value = "select request from RequestItemEntity request where request.itemId = (select item.itemId from ItemEntity item where item.barcode = :itemBarcode)")
    Page<RequestItemEntity> findByItemBarcode(Pageable pageable, @Param("itemBarcode") String itemBarcode);

    /**
     * Find by item barcode and request sta code request item entity.
     *
     * @param itemBarcode       the item barcode
     * @param requestStatusCode the request status code
     * @return the request item entity
     * @throws IncorrectResultSizeDataAccessException the incorrect result size data access exception
     */
    @Query(value = "select requestItemEntity from RequestItemEntity requestItemEntity inner join requestItemEntity.itemEntity ie inner join requestItemEntity.requestStatusEntity as rse  where ie.barcode = :itemBarcode and rse.requestStatusCode= :requestStatusCode ")
    RequestItemEntity findByItemBarcodeAndRequestStaCode(@Param("itemBarcode") String itemBarcode, @Param("requestStatusCode") String requestStatusCode) throws IncorrectResultSizeDataAccessException;

    /**
     * Find by item barcode list.
     *
     * @param itemBarcode the item barcode
     * @return the list
     */
    @Query(value = "select request from RequestItemEntity request where request.itemId = (select item.itemId from ItemEntity item where item.barcode = :itemBarcode)")
    List<RequestItemEntity> findByItemBarcode(@Param("itemBarcode") String itemBarcode);
}
