package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.CustomerCodeDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

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
        customerCodeEntity.setCustomerCodeId(1);
        customerCodeEntity.setCustomerCode("AB");
        customerCodeEntity.setDeliveryRestrictions("AC,BC");
        customerCodeEntity.setRecapDeliveryRestrictions("No Restriction");
        customerCodeEntity.setPwdDeliveryRestrictions("Others");
        customerCodeEntity.setDescription("test");
        customerCodeEntity.setOwningInstitutionId(entity.getInstitutionId());
        customerCodeEntity.setInstitutionEntity(entity);
        customerCodeEntity.setPickupLocation("Discovery");
        customerCodeEntity.setDeliveryRestrictionEntityList(Arrays.asList(new DeliveryRestrictionEntity()));

        assertNotNull(customerCodeEntity.getCustomerCodeId());
        assertEquals(customerCodeEntity.getCustomerCode(),"AB");
        assertEquals(customerCodeEntity.getDeliveryRestrictions(),"AC,BC");
        assertEquals(customerCodeEntity.getDescription(),"test");
        assertEquals(customerCodeEntity.getPickupLocation(),"Discovery");
        assertNotNull(customerCodeEntity.getOwningInstitutionId());
        assertNotNull(customerCodeEntity.getInstitutionEntity());
        assertNotNull(customerCodeEntity.getRecapDeliveryRestrictions());
        assertNotNull(customerCodeEntity.getPwdDeliveryRestrictions());
        assertNotNull(customerCodeEntity.getDeliveryRestrictionEntityList());
    }

}