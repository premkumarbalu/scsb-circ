package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.CustomerCodeDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 14/3/17.
 */
public class CustomerCodeEntityUT extends BaseTestCase{

    @Autowired
    CustomerCodeDetailsRepository customerCodeDetailsRepository;

    @Autowired
    InstitutionDetailsRepository institutionDetailRepository;

    @Test
    public void testCustomerCode(){
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionCode("UC");
        institutionEntity.setInstitutionName("University of Chicago");
        InstitutionEntity entity = institutionDetailRepository.save(institutionEntity);
        assertNotNull(entity);

        CustomerCodeEntity customerCodeEntity = new CustomerCodeEntity();
        customerCodeEntity.setCustomerCode("AB");
        customerCodeEntity.setDeliveryRestrictions("AC,BC");
        customerCodeEntity.setDescription("test");
        customerCodeEntity.setOwningInstitutionId(entity.getInstitutionId());
        CustomerCodeEntity savedCustomerCodeEntity = customerCodeDetailsRepository.save(customerCodeEntity);
        assertNotNull(savedCustomerCodeEntity);
        assertNotNull(savedCustomerCodeEntity.getCustomerCodeId());
        assertEquals(savedCustomerCodeEntity.getCustomerCode(),"AB");
        assertEquals(savedCustomerCodeEntity.getDeliveryRestrictions(),"AC,BC");
        assertEquals(savedCustomerCodeEntity.getDescription(),"test");
        assertNotNull(savedCustomerCodeEntity.getOwningInstitutionId());
    }

}