package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 14/3/17.
 */
public class SearchResultRowUT extends BaseTestCase{

    @Test
    public void testSearchResultRow(){
        SearchResultRow searchResultRow = new SearchResultRow();
        searchResultRow.setBarcode("36598741256398");
        searchResultRow.setSearchItemResultRows(Arrays.asList(new SearchItemResultRow()));
        searchResultRow.setItemId(658);
        searchResultRow.setTitle("test");
        searchResultRow.setAuthor("john");
        searchResultRow.setAvailability("Available");
        searchResultRow.setBibId(36598);
        searchResultRow.setCollectionGroupDesignation("Shared");
        searchResultRow.setDeliveryLocation("PB");
        searchResultRow.setOwningInstitution("PUL");
        searchResultRow.setLeaderMaterialType("Monograph");
        searchResultRow.setCustomerCode("NA");
        searchResultRow.setPatronBarcode(45698328);
        searchResultRow.setPublisher("test");
        searchResultRow.setPublisherDate(new Date().toString());
        searchResultRow.setRequestingInstitution("CUL");
        searchResultRow.setRequestNotes("test");
        searchResultRow.setRequestType("Recall");
        searchResultRow.setUseRestriction("Others");
        searchResultRow.setSelected(true);
        searchResultRow.setSelectAllItems(true);
        assertNotNull(searchResultRow);
        assertNotNull(searchResultRow.getBarcode());
        assertNotNull(searchResultRow.getSearchItemResultRows());
        assertNotNull(searchResultRow.getItemId());
        assertNotNull(searchResultRow.getTitle());
        assertNotNull(searchResultRow.getAuthor());
        assertNotNull(searchResultRow.getAvailability());
        assertNotNull(searchResultRow.getBibId());
        assertNotNull(searchResultRow.getCollectionGroupDesignation());
        assertNotNull(searchResultRow.getDeliveryLocation());
        assertNotNull(searchResultRow.getOwningInstitution());
        assertNotNull(searchResultRow.getLeaderMaterialType());
        assertNotNull(searchResultRow.getCustomerCode());
        assertNotNull(searchResultRow.getPatronBarcode());
        assertNotNull(searchResultRow.getPublisher());
        assertNotNull(searchResultRow.getPublisherDate());
        assertNotNull(searchResultRow.getRequestingInstitution());
        assertNotNull(searchResultRow.getRequestNotes());
        assertNotNull(searchResultRow.getRequestType());
        assertNotNull(searchResultRow.getUseRestriction());
        assertTrue(searchResultRow.isSelected());
        assertTrue(searchResultRow.isSelectAllItems());


    }

}