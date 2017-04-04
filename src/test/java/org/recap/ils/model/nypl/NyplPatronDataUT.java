package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Test;
import org.recap.BaseTestCase;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 3/4/17.
 */
public class NyplPatronDataUT extends BaseTestCase{

    @Test
    public void testNyplPatronData(){
        NyplPatronData nyplPatronData = new NyplPatronData();
        nyplPatronData.setId("1");
        nyplPatronData.setUpdatedDate(new Date().toString());
        nyplPatronData.setCreatedDate(new Date().toString());
        nyplPatronData.setDeletedDate(new Date().toString());
        nyplPatronData.setExpirationDate(new Date().toString());
        nyplPatronData.setBirthDate(new Date().toString());
        nyplPatronData.setDeleted(false);
        nyplPatronData.setSuppressed(false);
        nyplPatronData.setNames(Arrays.asList("test"));
        nyplPatronData.setBarCodes(Arrays.asList("3545874547253814556"));
        nyplPatronData.setHomeLibraryCode("test");
        nyplPatronData.setEmails(Arrays.asList("test@gmail.com"));
        nyplPatronData.setFixedFields(new FixedFields());
        nyplPatronData.setVarFields(Arrays.asList(new VarField()));

        assertNotNull(nyplPatronData.getId());
        assertNotNull(nyplPatronData.getUpdatedDate());
        assertNotNull(nyplPatronData.getCreatedDate());
        assertNotNull(nyplPatronData.getDeletedDate());
        assertNotNull(nyplPatronData.getDeleted());
        assertNotNull(nyplPatronData.getSuppressed());
        assertNotNull(nyplPatronData.getNames());
        assertNotNull(nyplPatronData.getBarCodes());
        assertNotNull(nyplPatronData.getExpirationDate());
        assertNotNull(nyplPatronData.getHomeLibraryCode());
        assertNotNull(nyplPatronData.getBirthDate());
        assertNotNull(nyplPatronData.getEmails());
        assertNotNull(nyplPatronData.getFixedFields());
        assertNotNull(nyplPatronData.getVarFields());

    }

}