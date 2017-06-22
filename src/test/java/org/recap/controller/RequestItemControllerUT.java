package org.recap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.ils.ColumbiaJSIPConnector;
import org.recap.ils.JSIPConnectorFactory;
import org.recap.ils.NyplApiConnector;
import org.recap.ils.PrincetonJSIPConnector;
import org.recap.ils.model.response.*;
import org.recap.model.ItemRefileRequest;
import org.recap.model.ItemRefileResponse;
import org.recap.model.ItemRequestInformation;
import org.recap.model.ItemResponseInformation;
import org.recap.request.ItemRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by hemalathas on 11/11/16.
 */
public class RequestItemControllerUT extends BaseTestCase {


    private static final Logger logger = LoggerFactory.getLogger(RequestItemControllerUT.class);

    @Mock
    RequestItemController requestItemController;

    @Mock
    JSIPConnectorFactory jsipConectorFactory;

    @Mock
    private ColumbiaJSIPConnector columbiaJSIPConnector;

    @Mock
    private PrincetonJSIPConnector princetonJSIPConnector;

    @Mock
    private NyplApiConnector nyplAPIConnector;

    @Mock
    ItemRequestService itemRequestService;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(requestItemController).build();
    }

    @Test
    public void testCheckoutItemRequest() {
        String callInstitition = "PUL";
        String itemBarcode = "PULTST54325";
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(Arrays.asList(itemBarcode));
        itemRequestInformation.setPatronBarcode("198572368");
        itemRequestInformation.setRequestingInstitution(callInstitition);
        ItemCheckoutResponse itemResponseInformation1 = new ItemCheckoutResponse();
        itemResponseInformation1.setScreenMessage("Checkout successfull");
        itemResponseInformation1.setSuccess(true);
        Mockito.when(requestItemController.getJsipConectorFactory()).thenReturn(jsipConectorFactory);
        Mockito.when(jsipConectorFactory.getPrincetonJSIPConnector()).thenReturn(princetonJSIPConnector);
        Mockito.when(jsipConectorFactory.getColumbiaJSIPConnector()).thenReturn(columbiaJSIPConnector);
        Mockito.when(jsipConectorFactory.getNyplAPIConnector()).thenReturn(nyplAPIConnector);
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition)).thenCallRealMethod();
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition).checkOutItem(itemBarcode, itemRequestInformation.getPatronBarcode())).thenReturn(itemResponseInformation1);
        Mockito.when((ItemCheckoutResponse) requestItemController.checkoutItem(itemRequestInformation,"PUL")).thenCallRealMethod();
        ItemCheckoutResponse itemResponseInformation = (ItemCheckoutResponse) requestItemController.checkoutItem(itemRequestInformation,"PUL");
        assertNotNull(itemResponseInformation);
        logger.info(itemResponseInformation.getTitleIdentifier());
        logger.info(itemResponseInformation.getScreenMessage());
        assertTrue(itemResponseInformation.isSuccess());

    }

    @Test
    public void testCheckinItemRequest(){
        String callInstitition = "PUL";
        String itemBarcode = "PULTST54325";
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(Arrays.asList(itemBarcode));
        itemRequestInformation.setPatronBarcode("198572368");
        itemRequestInformation.setRequestingInstitution(callInstitition);
        ItemCheckinResponse itemResponseInformation1 = new ItemCheckinResponse();
        itemResponseInformation1.setScreenMessage("CheckIn successfull");
        itemResponseInformation1.setSuccess(true);
        Mockito.when(requestItemController.getJsipConectorFactory()).thenReturn(jsipConectorFactory);
        Mockito.when(jsipConectorFactory.getPrincetonJSIPConnector()).thenReturn(princetonJSIPConnector);
        Mockito.when(jsipConectorFactory.getColumbiaJSIPConnector()).thenReturn(columbiaJSIPConnector);
        Mockito.when(jsipConectorFactory.getNyplAPIConnector()).thenReturn(nyplAPIConnector);
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition)).thenCallRealMethod();
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition).checkInItem(itemBarcode, itemRequestInformation.getPatronBarcode())).thenReturn(itemResponseInformation1);
        Mockito.when((ItemCheckoutResponse) requestItemController.checkinItem(itemRequestInformation,"PUL")).thenCallRealMethod();
        AbstractResponseItem abstractResponseItem = (ItemCheckinResponse) requestItemController.checkinItem(itemRequestInformation,"PUL");
        assertNotNull(abstractResponseItem);
        assertTrue(abstractResponseItem.isSuccess());
    }

    @Test
    public void testRefileItem(){
        ItemRefileRequest itemRefileRequest = new ItemRefileRequest();
        itemRefileRequest.setItemBarcodes(Arrays.asList("123"));
        itemRefileRequest.setRequestIds(Arrays.asList(1));
        Mockito.when(requestItemController.getItemRequestService()).thenReturn(itemRequestService);
        Mockito.when(requestItemController.getItemRequestService().reFileItem(itemRefileRequest)).thenReturn(true);
        Mockito.when(requestItemController.refileItem(itemRefileRequest)).thenCallRealMethod();
        ItemRefileResponse refileResponse = requestItemController.refileItem(itemRefileRequest);
        refileResponse.setRequestId(1);
        assertNotNull(itemRefileRequest.getItemBarcodes());
        assertNotNull(itemRefileRequest.getRequestIds());
        assertNotNull(refileResponse);
        assertTrue(refileResponse.isSuccess());
        assertEquals(refileResponse.getScreenMessage(),"Successfully Refiled");
        assertNotNull(refileResponse.getRequestId());
    }

    public ItemHoldResponse getItemHoldResponse(){
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        itemHoldResponse.setSuccess(true);
        return itemHoldResponse;
    }

    private String getPickupLocation(String institution) {
        String pickUpLocation = "";
        if (institution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            pickUpLocation = ReCAPConstants.DEFAULT_PICK_UP_LOCATION_PUL;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            pickUpLocation = ReCAPConstants.DEFAULT_PICK_UP_LOCATION_CUL;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
            pickUpLocation = ReCAPConstants.DEFAULT_PICK_UP_LOCATION_NYPL;
        }
        return pickUpLocation;
    }

    @Test
    public void testCancelHoldItemRequest(){
        String callInstitition = "PUL";
        String itembarcode = "PULTST54325";
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(Arrays.asList(itembarcode));
        itemRequestInformation.setPatronBarcode("198572368");
        itemRequestInformation.setExpirationDate(new Date().toString());
        itemRequestInformation.setBibId("12");
        itemRequestInformation.setTrackingId("235");
        itemRequestInformation.setRequestingInstitution(callInstitition);
        Mockito.when(requestItemController.getJsipConectorFactory()).thenReturn(jsipConectorFactory);
        Mockito.when(jsipConectorFactory.getPrincetonJSIPConnector()).thenReturn(princetonJSIPConnector);
        Mockito.when(jsipConectorFactory.getColumbiaJSIPConnector()).thenReturn(columbiaJSIPConnector);
        Mockito.when(jsipConectorFactory.getNyplAPIConnector()).thenReturn(nyplAPIConnector);
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition)).thenCallRealMethod();
        Mockito.when(requestItemController.getPickupLocation(callInstitition)).thenCallRealMethod();
        Mockito.when((ItemHoldResponse)requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition).cancelHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                getPickupLocation(callInstitition), itemRequestInformation.getTrackingId())).thenReturn(getItemHoldResponse());
        Mockito.when((ItemInformationResponse)requestItemController.cancelHoldItem(itemRequestInformation,callInstitition)).thenCallRealMethod();
        AbstractResponseItem abstractResponseItem = requestItemController.cancelHoldItem(itemRequestInformation,callInstitition);
        assertNotNull(abstractResponseItem);
        assertTrue(abstractResponseItem.isSuccess());
    }

    @Test
    public void testHoldItemRequest(){
        String callInstitition = "PUL";
        String itembarcode = "PULTST54325";
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(Arrays.asList(itembarcode));
        itemRequestInformation.setPatronBarcode("198572368");
        itemRequestInformation.setExpirationDate(new Date().toString());
        itemRequestInformation.setBibId("12");
        itemRequestInformation.setItemOwningInstitution(callInstitition);
        itemRequestInformation.setCallNumber("X");
        itemRequestInformation.setAuthor("John");
        itemRequestInformation.setTitleIdentifier("test");
        itemRequestInformation.setTrackingId("235");
        itemRequestInformation.setRequestingInstitution(callInstitition);
        Mockito.when(requestItemController.getJsipConectorFactory()).thenReturn(jsipConectorFactory);
        Mockito.when(jsipConectorFactory.getPrincetonJSIPConnector()).thenReturn(princetonJSIPConnector);
        Mockito.when(jsipConectorFactory.getColumbiaJSIPConnector()).thenReturn(columbiaJSIPConnector);
        Mockito.when(jsipConectorFactory.getNyplAPIConnector()).thenReturn(nyplAPIConnector);
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition)).thenCallRealMethod();
        Mockito.when(requestItemController.getPickupLocation(callInstitition)).thenCallRealMethod();
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition).placeHold(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getItemOwningInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                getPickupLocation(callInstitition),
                itemRequestInformation.getTrackingId(),
                itemRequestInformation.getTitleIdentifier(),
                itemRequestInformation.getAuthor(),
                itemRequestInformation.getCallNumber())).thenReturn(getItemHoldResponse());
        Mockito.when((ItemInformationResponse)requestItemController.holdItem(itemRequestInformation,callInstitition)).thenCallRealMethod();
        AbstractResponseItem abstractResponseItem = requestItemController.holdItem(itemRequestInformation,callInstitition);
        assertNotNull(abstractResponseItem);
        assertTrue(abstractResponseItem.isSuccess());
    }

    @Test
    public void testItemInformation(){
        String callInstitition = "PUL";
        String itembarcode = "PULTST54325";
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(Arrays.asList(itembarcode));
        itemRequestInformation.setPatronBarcode("198572368");
        itemRequestInformation.setExpirationDate(new Date().toString());
        itemRequestInformation.setBibId("12");
        itemRequestInformation.setTrackingId("235");
        itemRequestInformation.setRequestingInstitution(callInstitition);

        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
        itemInformationResponse.setSuccess(true);

        Mockito.when(requestItemController.getJsipConectorFactory()).thenReturn(jsipConectorFactory);
        Mockito.when(jsipConectorFactory.getPrincetonJSIPConnector()).thenReturn(princetonJSIPConnector);
        Mockito.when(jsipConectorFactory.getColumbiaJSIPConnector()).thenReturn(columbiaJSIPConnector);
        Mockito.when(jsipConectorFactory.getNyplAPIConnector()).thenReturn(nyplAPIConnector);
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition)).thenCallRealMethod();
        Mockito.when((ItemInformationResponse)requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition).lookupItem(itembarcode)).thenReturn(itemInformationResponse);
        Mockito.when((ItemInformationResponse)requestItemController.itemInformation(itemRequestInformation,callInstitition)).thenCallRealMethod();

        AbstractResponseItem abstractResponseItem = (ItemInformationResponse)requestItemController.itemInformation(itemRequestInformation,callInstitition);
        assertNotNull(abstractResponseItem);
        assertTrue(abstractResponseItem.isSuccess());
    }

    @Test
    public void testBibCreation(){
        String callInstitition = "PUL";
        String itembarcode = "PULTST54325";
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(Arrays.asList(itembarcode));
        itemRequestInformation.setPatronBarcode("198572368");
        itemRequestInformation.setExpirationDate(new Date().toString());
        itemRequestInformation.setBibId("12");
        itemRequestInformation.setTrackingId("235");
        itemRequestInformation.setRequestingInstitution(callInstitition);

        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
        itemInformationResponse.setScreenMessage("Item Barcode already Exist");
        itemInformationResponse.setSuccess(true);

        Mockito.when(requestItemController.getJsipConectorFactory()).thenReturn(jsipConectorFactory);
        Mockito.when(jsipConectorFactory.getPrincetonJSIPConnector()).thenReturn(princetonJSIPConnector);
        Mockito.when(jsipConectorFactory.getColumbiaJSIPConnector()).thenReturn(columbiaJSIPConnector);
        Mockito.when(jsipConectorFactory.getNyplAPIConnector()).thenReturn(nyplAPIConnector);
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition)).thenCallRealMethod();
        Mockito.when((ItemInformationResponse)requestItemController.itemInformation(itemRequestInformation,itemRequestInformation.getRequestingInstitution())).thenCallRealMethod();
        Mockito.when((ItemInformationResponse)requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition).lookupItem(itembarcode)).thenReturn(itemInformationResponse);
        Mockito.when(requestItemController.createBibliogrphicItem(itemRequestInformation,callInstitition)).thenCallRealMethod();
        AbstractResponseItem abstractResponseItem = requestItemController.createBibliogrphicItem(itemRequestInformation,callInstitition);
        assertNotNull(abstractResponseItem);
        abstractResponseItem.setEsipDataIn("test");
        abstractResponseItem.setItemOwningInstitution("PUL");
        assertTrue(abstractResponseItem.isSuccess());
        assertEquals(abstractResponseItem.getScreenMessage(),"Item Barcode already Exist");
        assertNotNull(abstractResponseItem.getEsipDataIn());
        assertNotNull(abstractResponseItem.getItemOwningInstitution());
    }

    @Test
    public void testRecallItemRequest(){
        String callInstitition = "PUL";
        String itembarcode = "PULTST54325";
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(Arrays.asList(itembarcode));
        itemRequestInformation.setPatronBarcode("198572368");
        itemRequestInformation.setExpirationDate(new Date().toString());
        itemRequestInformation.setBibId("12");
        itemRequestInformation.setTrackingId("235");
        itemRequestInformation.setRequestingInstitution(callInstitition);

        ItemRecallResponse itemRecallResponse = new ItemRecallResponse();
        itemRecallResponse.setSuccess(true);
        Mockito.when(requestItemController.getJsipConectorFactory()).thenReturn(jsipConectorFactory);
        Mockito.when(jsipConectorFactory.getPrincetonJSIPConnector()).thenReturn(princetonJSIPConnector);
        Mockito.when(jsipConectorFactory.getColumbiaJSIPConnector()).thenReturn(columbiaJSIPConnector);
        Mockito.when(jsipConectorFactory.getNyplAPIConnector()).thenReturn(nyplAPIConnector);
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition)).thenCallRealMethod();
        Mockito.when(requestItemController.getPickupLocation(callInstitition)).thenCallRealMethod();
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition).recallItem(itembarcode, itemRequestInformation.getPatronBarcode(),
                itemRequestInformation.getRequestingInstitution(),
                itemRequestInformation.getExpirationDate(),
                itemRequestInformation.getBibId(),
                getPickupLocation(callInstitition))).thenReturn(itemRecallResponse);
        Mockito.when(requestItemController.recallItem(itemRequestInformation,callInstitition)).thenCallRealMethod();
        AbstractResponseItem abstractResponseItem = requestItemController.recallItem(itemRequestInformation,callInstitition);
        assertNotNull(abstractResponseItem);
        assertTrue(abstractResponseItem.isSuccess());
    }

    @Test
    public void testPatronInformation(){
        String callInstitition = "PUL";
        String itembarcode = "PULTST54325";
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setItemBarcodes(Arrays.asList(itembarcode));
        itemRequestInformation.setPatronBarcode("198572368");
        itemRequestInformation.setExpirationDate(new Date().toString());
        itemRequestInformation.setBibId("12");
        itemRequestInformation.setTrackingId("235");
        itemRequestInformation.setRequestingInstitution(callInstitition);

        PatronInformationResponse patronInformationResponse = new PatronInformationResponse();
        patronInformationResponse.setPatronIdentifier("198572368");

        Mockito.when(requestItemController.getJsipConectorFactory()).thenReturn(jsipConectorFactory);
        Mockito.when(jsipConectorFactory.getPrincetonJSIPConnector()).thenReturn(princetonJSIPConnector);
        Mockito.when(jsipConectorFactory.getColumbiaJSIPConnector()).thenReturn(columbiaJSIPConnector);
        Mockito.when(jsipConectorFactory.getNyplAPIConnector()).thenReturn(nyplAPIConnector);
        Mockito.when(requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition)).thenCallRealMethod();
        Mockito.when((PatronInformationResponse)requestItemController.getJsipConectorFactory().getJSIPConnector(callInstitition).lookupPatron(itemRequestInformation.getPatronBarcode())).thenReturn(patronInformationResponse);
        Mockito.when(requestItemController.patronInformation(itemRequestInformation,callInstitition)).thenCallRealMethod();
        AbstractResponseItem abstractResponseItem = requestItemController.patronInformation(itemRequestInformation,callInstitition);
        assertNotNull(abstractResponseItem);
    }






    @Test
    public void testJsonResponseParse() throws Exception {
//        MvcResult mvcResult = this.mockMvc.perform(get("/requestItem/checkoutItem")
//                .param("", "")
//                .param("", "")
//        ).andReturn();
        String strJson = "{\"patronBarcode\":null,\"itemBarcode\":\"32101095533293\",\"requestType\":null,\"deliveryLocation\":null,\"requestingInstitution\":null,\"bibliographicId\":null,\"expirationDate\":null,\"screenMessage\":\"Checkout Successful.\",\"success\":true,\"emailAddress\":null,\"startPage\":null,\"endPage\":null,\"titleIdentifier\":\"Accommodating Muslims under common law : a comparative analysis / Salim Farrar and Ghena Krayem.\",\"dueDate\":\"20170301    234500\"}";
        ObjectMapper om = new ObjectMapper();

        ItemResponseInformation itemResponseInformation = om.readValue(strJson, ItemResponseInformation.class);

//    List<SearchResultRow> searchResultRowL=new ArrayList<>(Arrays.asList(searchResultRowAr));
        logger.info(itemResponseInformation.getScreenMessage());


    }
}