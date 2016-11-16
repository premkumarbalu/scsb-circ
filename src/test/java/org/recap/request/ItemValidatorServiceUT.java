
package org.recap.request;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.ItemEntity;
import org.recap.model.ItemRequestInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


/**
 * Created by hemalathas on 11/11/16.
 */

public class ItemValidatorServiceUT extends BaseTestCase{

    @Value("${server.protocol}")
    String serverProtocol;
    @Value("${scsb.solr.client.url}")
    String scsbSolrClientUrl;

    protected MockMvc mockMvc;
    @Autowired
    ItemValidatorService itemValidatorService;
    @Mock
    ItemValidatorService mockItemValidatorService;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }



    @Test
    public void testValidItem(){
        ResponseEntity responseEntity = new ResponseEntity("Available",getHttpHeaders(), HttpStatus.OK);
        List<String> itemBarcodes = new ArrayList<>();
        itemBarcodes.add("33433003796665");
        itemBarcodes.add("33433003796673");
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(itemBarcodes);
        Mockito.when(mockItemValidatorService.itemValidation(itemRequestInformation)).thenReturn(responseEntity);
        ResponseEntity responseEntity1 = mockItemValidatorService.itemValidation(itemRequestInformation);
        assertNotNull(responseEntity1);
        assertEquals(responseEntity1.getBody(), "Available");
    }




    @Test
    public void testInValidItem(){
        ResponseEntity responseEntity = new ResponseEntity("Item barcodes should have same bib",getHttpHeaders(), HttpStatus.OK);
        List<String> itemBarcodes = new ArrayList<>();
        itemBarcodes.add("33433005249010");
        itemBarcodes.add("33433003796665");
        itemBarcodes.add("33433005249028");
        itemBarcodes.add("33433005249044");
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(itemBarcodes);
        Mockito.when(mockItemValidatorService.itemValidation(itemRequestInformation)).thenReturn(responseEntity);
        ResponseEntity responseEntity1 = mockItemValidatorService.itemValidation(itemRequestInformation);
        assertNotNull(responseEntity1);
        assertEquals(responseEntity1.getBody(), "Item barcodes should have same bib");
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api_key", "recap");
        return headers;
    }

}