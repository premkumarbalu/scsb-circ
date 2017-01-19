package org.recap.repository;


import org.recap.model.RequestItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by rajeshbabuk on 26/10/16.
 */
public interface RequestItemDetailsRepository extends JpaRepository<RequestItemEntity, Integer>, JpaSpecificationExecutor {

    RequestItemEntity findByRequestId(@Param("requestId") Integer requestId);

    List<RequestItemEntity> findByItemId(@Param("itemId") Integer itemId);

    List<RequestItemEntity> findByItemIdIn(@Param("itemIds") List<Integer> itemIds);

    Page<RequestItemEntity> findByStopCode(Pageable pageable, @Param("stopCode") String stopCode);

    @Query(value = "select request from RequestItemEntity request where request.itemId = (select itemId from ItemEntity item where item.barcode = :itemBarcode)")
    Page<RequestItemEntity> findByItemBarcode(Pageable pageable, @Param("itemBarcode") String itemBarcode);

    @Query(value = "select request from RequestItemEntity request where request.patronId = (select patronId from PatronEntity patron where patron.institutionIdentifier = :patronBarcode)")
    Page<RequestItemEntity> findByPatronBarcode(Pageable pageable, @Param("patronBarcode") String patronBarcode);

    @Query(value = "select request from RequestItemEntity request where request.patronId = (select patronId from PatronEntity patron where patron.institutionIdentifier = :patronBarcode) and request.itemId = (select itemId from ItemEntity item where item.barcode = :itemBarcode) and request.stopCode = :deliveryLocation")
    Page<RequestItemEntity> findByPatronBarcodeAndItemBarcodeAndDeliveryLocation(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("itemBarcode") String itemBarcode, @Param("deliveryLocation") String deliveryLocation);

    @Query(value = "select request from RequestItemEntity request where request.patronId = (select patronId from PatronEntity patron where patron.institutionIdentifier = :patronBarcode) and request.itemId = (select itemId from ItemEntity item where item.barcode = :itemBarcode)")
    Page<RequestItemEntity> findByPatronBarcodeAndItemBarcode(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("itemBarcode") String itemBarcode);

//    @Query(value = "select request from RequestItemEntity request where request.patronId = (select patronId from PatronEntity patron where patron.institutionIdentifier = :patronBarcode) and request.itemId = (select itemId from ItemEntity item where item.barcode = :itemBarcode and item.itemAvailabilityStatusId= :itemAvailabilityStatusId)")
//    Page<RequestItemEntity> findByPatronBarcodeAndItemBarcodeandItemAvailablityStatus(@Param("patronBarcode") String patronBarcode, @Param("itemBarcode") String itemBarcode,@Param("itemAvailabilityStatusId") int itemAvailabilityStatusId);

    @Query(value = "select request from RequestItemEntity request where request.patronId = (select patronId from PatronEntity patron where patron.institutionIdentifier = :patronBarcode) and request.stopCode = :deliveryLocation")
    Page<RequestItemEntity> findByPatronBarcodeAndDeliveryLocation(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("deliveryLocation") String deliveryLocation);

    @Query(value = "select request from RequestItemEntity request where request.itemId = (select itemId from ItemEntity item where item.barcode = :itemBarcode) and request.stopCode = :deliveryLocation")
    Page<RequestItemEntity> findByItemBarcodeAndDeliveryLocation(Pageable pageable, @Param("itemBarcode") String itemBarcode, @Param("deliveryLocation") String deliveryLocation);

    @Transactional
    int deleteByItemIdIn(@Param("itemIds") List<Integer> itemIds);
}
