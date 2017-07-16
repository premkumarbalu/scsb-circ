package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 13/7/17.
 */
public class DeliveryRestrictionEntityUT extends BaseTestCase{

    @Test
    public void testDeliveryRestrictionEntity(){
        DeliveryRestrictionEntity deliveryRestrictionEntity = new DeliveryRestrictionEntity();
        deliveryRestrictionEntity.setDeliveryRestrictionId(1);
        deliveryRestrictionEntity.setDeliveryRestriction("Test");
        deliveryRestrictionEntity.setInstitutionEntity(new InstitutionEntity());
        deliveryRestrictionEntity.setCustomerCodeEntityList(Arrays.asList(new CustomerCodeEntity()));

        assertNotNull(deliveryRestrictionEntity.getCustomerCodeEntityList());
        assertNotNull(deliveryRestrictionEntity.getInstitutionEntity());
        assertNotNull(deliveryRestrictionEntity.getDeliveryRestriction());
        assertNotNull(deliveryRestrictionEntity.getDeliveryRestrictionId());
    }

}