package org.recap.repository;


import org.recap.model.CustomerCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


/**
 * The interface Customer code details repository.
 */
public interface CustomerCodeDetailsRepository extends JpaRepository<CustomerCodeEntity, Integer> {

    /**
     * Find by customer code customer code entity.
     *
     * @param customerCode the customer code
     * @return the customer code entity
     */
    CustomerCodeEntity findByCustomerCode(@Param("customerCode") String customerCode);

    /**
     * Find by customer code in list.
     *
     * @param customerCodes the customer codes
     * @return the list
     */
    List<CustomerCodeEntity> findByCustomerCodeIn(List<String> customerCodes);

    @Query(value="select customerCode from CustomerCodeEntity customerCode where customerCode.customerCode =:customerCode and customerCode.recapDeliveryRestrictions LIKE ('%EDD%')")
    CustomerCodeEntity findByCustomerCodeAndRecapDeliveryRestrictionLikeEDD(@Param("customerCode") String customerCode);
}
