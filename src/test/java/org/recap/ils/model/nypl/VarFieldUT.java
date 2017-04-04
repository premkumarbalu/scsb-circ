package org.recap.ils.model.nypl;

import org.junit.Test;
import org.marc4j.marc.Subfield;
import org.recap.BaseTestCase;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 3/4/17.
 */
public class VarFieldUT extends BaseTestCase{

    @Test
    public void testVarFields(){
        SubField subfield = new SubField();
        subfield.setTag("245");
        subfield.setContent("test");
        VarField varField = new VarField();
        varField.setFieldTag("201");
        varField.setMarcTag("110");
        varField.setInd2("test");
        varField.setInd1("test");
        varField.setSubFields(Arrays.asList(subfield));
        varField.setContent("test");

        assertNotNull(subfield.getContent());
        assertNotNull(subfield.getTag());
        assertNotNull(varField.getFieldTag());
        assertNotNull(varField.getMarcTag());
        assertNotNull(varField.getInd1());
        assertNotNull(varField.getInd2());
        assertNotNull(varField.getContent());
        assertNotNull(varField.getSubFields());
    }


}