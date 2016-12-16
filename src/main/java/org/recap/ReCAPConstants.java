package org.recap;

/**
 * Created by premkb on 19/8/16.
 */
public class ReCAPConstants {

    public static final String FAILURE = "Failure";
    public static final String SUCCESS = "Success";

    public static final String COLUMBIA = "CUL";
    public static final String PRINCETON = "PUL";
    public static final String NYPL = "NYPL";

    public static final String RESPONSE_DATE = "Date";

    public static final String REGEX_FOR_EMAIL_ADDRESS = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String INVALID_REQUEST_INSTITUTION = "Please enter valid Institution PUL/CUL/NYPL for requestingInstitution";
    public static final String INVALID_EMAIL_ADDRESS = "Please enter valid emailAddress";
    public static final String START_PAGE_AND_END_PAGE_REQUIRED = "Startpage and endpage information is required for the request type EDD";
    public static final String INVALID_PAGE_NUMBER = "Page number should not be 0. Page number starts with 1";
    public static final String INVALID_END_PAGE = "End page should not be 0 and less than start page";
    public static final String DELIVERY_LOCATION_REQUIRED = "Delivery Location is required for request type Recall/hold/retrieval";
    public static final String EMPTY_PATRON_BARCODE = "Patron barcode should not be null or empty.Please enter the valid patron barcode";
    public static final String INVALID_REQUEST_TYPE = "Please enter the valid request type";
    public static final String RETRIEVAL = "Retrieval";
    public static final String HOLD = "Hold";
    public static final String RECALL = "Recall";
    public static final String EDD_REQUEST = "EDD";
    public static final String BORROW_DIRECT = "Borrow Direct";
    public static final String PHYSICAL_REQUEST = "Physical";
    public static final String VALID_REQUEST = "All request parameters are valid.Patron is eligible to raise a request";
    public static final String INVALID_PATRON = "Patron is not available";
    public static final String VALID_PATRON = "Patron validated successfully.";
    public static final String AVAILABLE = "Available";
    public static final String NOT_AVAILABLE = "Not Available";
    public static final String ITEMBARCODE_WITH_DIFFERENT_BIB = "Item should belongs to same bib.Given item barcodes are in different bibs";
    public static final String INVALID_CUSTOMER_CODE = "Please enter the valid Customer Code";
    public static final String INVALID_ITEM_BARCODE = "Item is not available";
    public static final String ITEM_BARCODE_IS_REQUIRED = "Item Barcode is required";
    public static final String VALID_CUSTOMER_CODE = "Customer code is valid";
    public static final String MULTIPLE_ITEMS_NOT_ALLOWED_FOR_EDD = "Multiple item request not allowed for EDD request type";
    public static final String WRONG_ITEM_BARCODE = "Item Barcode(s) not available in database.";
    public static final String HOLD_REQUEST_NOT_FOR_AVAILABLE_ITEM = "Request type cannot be hold if the item status is available";
    public static final String RETRIEVAL_NOT_FOR_UNAVAILABLE_ITEM = "Request type cannot be retrieval if the item status is unAvailable";
    public static final String CHAPTER_TITLE_IS_REQUIRED = "Chapter title is required for the request type EDD.";

    // Retrieval,EDD, Hold, Recall, Borrow Direct
    public static final String REQUEST_TYPE_RETRIEVAL="Retrieval";
    public static final String REQUEST_TYPE_EDD="EDD";
    public static final String REQUEST_TYPE_HOLD="Hold";
    public static final String REQUEST_TYPE_RECALL="Recall";
    public static final String REQUEST_TYPE_BORROW_DIRECT="Borrow Direct";

    // MQ URI
    public static final String REQUEST_ITEM_QUEUE = "scsbactivemq:queue:RequestItemQ";

    public static final String PUL_REQUEST_TOPIC = "scsbactivemq:topic:PUL.RequestT";
    public static final String PUL_EDD_TOPIC = "scsbactivemq:topic:PUL.EDDT";
    public static final String PUL_HOLD_TOPIC = "scsbactivemq:topic:PUL.HoldT";
    public static final String PUL_RECALL_TOPIC = "scsbactivemq:topic:PUL.RecallT";
    public static final String PUL_BORROW_DIRECT_TOPIC = "scsbactivemq:topic:PUL.BorrowDirectT";

    // Queue Header
    public static final String REQUEST_TYPE_QUEUE_HEADER = "RequestType";

    //RoutId
    public static final String REQUEST_ITEM_QUEUE_ROUTEID = "RequestItemRouteId";

    public static final String PUL_REQUEST_TOPIC_ROUTEID = "PULRequestTopicRouteId";
    public static final String PUL_EDD_TOPIC_ROUTEID = "PULEDDTopicRouteId";
    public static final String PUL_HOLD_TOPIC_ROUTEID = "PULHoldTopicRouteId";
    public static final String PUL_RECALL_TOPIC_ROUTEID = "PULRecallTopicRouteId";
    public static final String PUL_BORROW_DIRECT_TOPIC_ROUTEID = "PULBorrowDirectTopicRouteId";

}
