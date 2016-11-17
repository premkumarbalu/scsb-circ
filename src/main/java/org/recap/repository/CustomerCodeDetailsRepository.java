package org.recap.repository;


import org.recap.model.CustomerCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;


import java.util.List;


public interface CustomerCodeDetailsRepository extends JpaRepository<CustomerCodeEntity, Integer> {

    CustomerCodeEntity findByCustomerCode(@Param("customerCode") String customerCode);

    List<CustomerCodeEntity> findByCustomerCodeIn(List<String> customerCodes);
}
