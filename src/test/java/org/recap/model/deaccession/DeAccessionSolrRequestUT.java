package org.recap.model.deaccession;

import org.junit.Test;
import org.recap.BaseTestCase;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 17/3/17.
 */
public class DeAccessionSolrRequestUT extends BaseTestCase{


    @Test
    public void testDeAccessionSolrRequest(){
        DeAccessionSolrRequest deAccessionSolrRequest = new DeAccessionSolrRequest();
        deAccessionSolrRequest.setBibIds(Arrays.asList(123));
        deAccessionSolrRequest.setHoldingsIds(Arrays.asList(369));
        deAccessionSolrRequest.setItemIds(Arrays.asList(14752));
        assertNotNull(deAccessionSolrRequest.getBibIds());
        assertNotNull(deAccessionSolrRequest.getHoldingsIds());
        assertNotNull(deAccessionSolrRequest.getItemIds());
    }

}