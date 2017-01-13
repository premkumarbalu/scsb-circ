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
    public static final String RETRIEVAL = "RETRIEVAL";
    public static final String RECALL = "RECALL";
    public static final String EDD_REQUEST = "EDD";
    public static final String BORROW_DIRECT = "BORROW DIRECT";
    public static final String PHYSICAL_REQUEST = "Physical";
    public static final String VALID_REQUEST = "All request parameters are valid.Patron is eligible to raise a request";
    public static final String INVALID_PATRON = "Patron is not available";
    public static final String VALID_PATRON = "Patron validated successfully.";
    public static final String AVAILABLE = "Available";
    public static final String NOT_AVAILABLE = "Not Available";
    public static final String ITEMBARCODE_WITH_DIFFERENT_BIB = "Item should belongs to same bib. Given item barcodes are in different bibs";
    public static final String INVALID_CUSTOMER_CODE = "Please enter the valid delivery Code";
    public static final String INVALID_DELIVERY_CODE = "Delivery code is not available for this item";
    public static final String INVALID_ITEM_BARCODE = "Item is not available";
    public static final String ITEM_BARCODE_IS_REQUIRED = "Item Barcode is required";
    public static final String VALID_CUSTOMER_CODE = "Customer code is valid";
    public static final String MULTIPLE_ITEMS_NOT_ALLOWED_FOR_EDD = "Multiple item request not allowed for EDD request type";
    public static final String WRONG_ITEM_BARCODE = "Item Barcode(s) not available in database.";
    public static final String RETRIEVAL_NOT_FOR_UNAVAILABLE_ITEM = "Requested Item is not Available";
    public static final String CHAPTER_TITLE_IS_REQUIRED = "Chapter title is required for the request type EDD.";


    // Retrieval,EDD, Hold, Recall, Borrow Direct
    public static final String REQUEST_TYPE_RETRIEVAL="RETRIEVAL";
    public static final String REQUEST_TYPE_EDD="EDD";
    public static final String REQUEST_TYPE_RECALL="RECALL";
    public static final String REQUEST_TYPE_BORROW_DIRECT="BORROW DIRECT";

    // MQ URI
    public static final String REQUEST_ITEM_QUEUE = "scsbactivemq:queue:RequestItemQ";

    public static final String PUL_REQUEST_TOPIC = "scsbactivemq:topic:PUL.RequestT";
    public static final String PUL_EDD_TOPIC = "scsbactivemq:topic:PUL.EDDT";
    public static final String PUL_RECALL_TOPIC = "scsbactivemq:topic:PUL.RecallT";
    public static final String PUL_BORROW_DIRECT_TOPIC = "scsbactivemq:topic:PUL.BorrowDirectT";

    public static final String NYPL_REQUEST_TOPIC = "scsbactivemq:topic:NYPL.RequestT";
    public static final String NYPL_EDD_TOPIC = "scsbactivemq:topic:NYPL.EDDT";
    public static final String NYPL_RECALL_TOPIC = "scsbactivemq:topic:NYPL.RecallT";
    public static final String NYPL_BORROW_DIRECT_TOPIC = "scsbactivemq:topic:NYPL.BorrowDirectT";

    // Queue Header
    public static final String REQUEST_TYPE_QUEUE_HEADER = "RequestType";

    //RoutId
    public static final String REQUEST_ITEM_QUEUE_ROUTEID = "RequestItemRouteId";

    public static final String PUL_REQUEST_TOPIC_ROUTEID = "PULRequestTopicRouteId";
    public static final String PUL_EDD_TOPIC_ROUTEID = "PULEDDTopicRouteId";
    public static final String PUL_RECALL_TOPIC_ROUTEID = "PULRecallTopicRouteId";
    public static final String PUL_BORROW_DIRECT_TOPIC_ROUTEID = "PULBorrowDirectTopicRouteId";

    public static final String NYPL_REQUEST_TOPIC_ROUTEID = "NYPLRequestTopicRouteId";
    public static final String NYPL_EDD_TOPIC_ROUTEID = "NYPLEDDTopicRouteId";
    public static final String NYPL_RECALL_TOPIC_ROUTEID = "NYPLRecallTopicRouteId";
    public static final String NYPL_BORROW_DIRECT_TOPIC_ROUTEID = "NYPLBorrowDirectTopicRouteId";

    public static final String OWNING_INSTITUTION = "OwningInstitution";
    public static final String OWNING_INSTITUTION_BIB_ID = "OwningInstitutionBibId";
    public static final String TITLE = "Title";
    public static final String OWNING_INSTITUTION_HOLDINGS_ID = "OwningInstitutionHoldingsId";
    public static final String LOCAL_ITEM_ID = "LocalItemId";
    public static final String ITEM_BARCODE = "ItemBarcode";
    public static final String CUSTOMER_CODE = "CustomerCode";
    public static final String CREATE_DATE_ITEM = "CreateDateItem";
    public static final String LAST_UPDATED_DATE_ITEM = "LastUpdatedDateItem";
    public static final String ERROR_DESCRIPTION = "ErrorDescription";
    public static final String COLLECTION_GROUP_DESIGNATION = "CollectionGroupDesignation";

    public static final String FORMAT_MARC = "marc";
    public static final String FORMAT_SCSB = "scsb";

    public static final String SUBMIT_COLLECTION_REPORT = "Submit_Collection_Report";
    public static final String SUBMIT_COLLECTION_REJECTION_REPORT = "Submit_Collection_Rejection_Report";
    public static final String SUBMIT_COLLECTION_EXCEPTION_REPORT = "Submit_Collection_Exception_Report";
    public static final String SUBMIT_COLLECTION_ITEM_BARCODE= "ItemBarcode";
    public static final String SUBMIT_COLLECTION_CUSTOMER_CODE= "CustomerCode";
    public static final String SUBMIT_COLLECTION_EXCEPTION_REPORT_MESSAGE = "Exception report generated";
    public static final String SUBMIT_COLLECTION_REJECTION_REPORT_MESSAGE = "Rejection report generated";
    public static final String SUMBIT_COLLECTION_UPDATE_MESSAGE = "One or more record got updated";
    public static final String SUMBIT_COLLECTION_NOT_UPDATED_MESSAGE = "No record(s) got updated";

    public static final String ITEM_STATUS_AVAILABLE = "Available";
    public static final String INVALID_SCSB_XML_FORMAT_MESSAGE = "Please provide valid SCSB xml format";
    public static final String INVALID_MARC_XML_FORMAT_MESSAGE = "Please provide valid Marc xml format";
    public static final String SUBMIT_COLLECTION_INTERNAL_ERROR = "Internal error occured during submit collection";
    public static final String SUBMIT_COLLECTION_LIMIT_EXCEED_MESSAGE = "Maximum allowed input record is ";
    public static final String COMPLETE_STATUS = "Complete";
    public static final String INCOMPLETE_STATUS = "Incomplete";
    public static final String BIBRECORD_TAG= "<bibRecords>";
    public static final String SUBMIT_COLLECTION = "submitCollection";

    public static final String BIB_ID = "BibId";
    public static final String HOLDING_ID = "HoldingId";
    public static final String ITEM_ID = "ItemId";
    public static final String BIBLIOGRAPHIC_ENTITY = "bibliographicEntity";

    public static final String CHECKOUT_SUCCESS = "Successfully processed checkout item";
    public static final String CHECKIN_SUCCESS = "Successfully processed checkin item";
    public static final String HOLD_SUCCESS = "Successfully processed hold request";
    public static final String CANCEL_HOLD_SUCCESS = "Successfully processed cancel hold request";

    public static final String CHECKOUT_FAILED = "Failed to process checkout item";
    public static final String CHECKIN_FAILED = "Failed to process checkin item";
    public static final String HOLD_FAILED = "Failed to process hold request";
    public static final String CANCEL_HOLD_FAILED = "Failed to process cancel hold request";
    public static final String TRACKING_ID_REQUIRED = "Tracking Id is required";
}
