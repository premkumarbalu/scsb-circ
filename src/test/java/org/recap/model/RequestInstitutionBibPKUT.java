package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 23/3/17.
 */
public class RequestInstitutionBibPKUT extends BaseTestCase{

    @Test
    public void testRequestInstitutionBibPK(){
        RequestInstitutionBibPK requestInstitutionBibPK = new RequestInstitutionBibPK();
        requestInstitutionBibPK.setItemId(1);
        requestInstitutionBibPK.setOwningInstitutionId(1);
        RequestInstitutionBibPK requestInstitutionBibPK1 = new RequestInstitutionBibPK(1,1);
        assertNotNull(requestInstitutionBibPK.getOwningInstitutionId());
        assertNotNull(requestInstitutionBibPK.getItemId());
    }

}