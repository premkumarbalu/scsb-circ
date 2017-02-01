package org.recap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.ils.model.response.ItemCheckoutResponse;
import org.recap.model.ItemRequestInformation;
import org.recap.model.ItemResponseInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by hemalathas on 11/11/16.
 */
public class RequestItemControllerUT extends BaseTestCase {


    private Logger logger = LoggerFactory.getLogger(RequestItemControllerUT.class);

    @Autowired
    RequestItemController requestItemController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(requestItemController).build();
    }

    @Test
    public void testCheckoutItemRequest() {
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(Arrays.asList("423423423423"));
        itemRequestInformation.setPatronBarcode("198572368");
        itemRequestInformation.setRequestingInstitution("PUL");

        ItemCheckoutResponse itemResponseInformation = (ItemCheckoutResponse) requestItemController.checkoutItem(itemRequestInformation,"PUL");
        assertNotNull(itemResponseInformation);
        logger.info(itemResponseInformation.getTitleIdentifier());
        logger.info(itemResponseInformation.getScreenMessage());
        assertTrue(itemResponseInformation.isSuccess());

    }

    @Test
    public void testJsonResponseParse() throws Exception {
//        MvcResult mvcResult = this.mockMvc.perform(get("/requestItem/checkoutItem")
//                .param("", "")
//                .param("", "")
//        ).andReturn();
        String strJson = "{\"patronBarcode\":null,\"itemBarcode\":\"32101095533293\",\"requestType\":null,\"deliveryLocation\":null,\"requestingInstitution\":null,\"bibliographicId\":null,\"expirationDate\":null,\"itemId\":null,\"screenMessage\":\"Checkout Successful.\",\"success\":true,\"emailAddress\":null,\"startPage\":null,\"endPage\":null,\"titleIdentifier\":\"Accommodating Muslims under common law : a comparative analysis / Salim Farrar and Ghena Krayem.\",\"dueDate\":\"20170301    234500\"}";
        ObjectMapper om = new ObjectMapper();

        ItemResponseInformation itemResponseInformation = om.readValue(strJson, ItemResponseInformation.class);

//    List<SearchResultRow> searchResultRowL=new ArrayList<>(Arrays.asList(searchResultRowAr));
        logger.info(itemResponseInformation.getScreenMessage());


    }
}