package org.recap;

import java.util.Arrays;
import java.util.List;

/**
 * Created by premkb on 19/8/16.
 */
public final class ReCAPConstants {

    public static final String FAILURE = "Failure";
    public static final String SUCCESS = "Success";

    public static final String COLUMBIA = "CUL";
    public static final String PRINCETON = "PUL";
    public static final String NYPL = "NYPL";
    public static final String GFA = "GFA";

    public static final String RESPONSE_DATE = "Date";

    public static final String REGEX_FOR_EMAIL_ADDRESS = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String INVALID_REQUEST_INSTITUTION = "Please enter valid Institution PUL/CUL/NYPL for requestingInstitution";
    public static final String INVALID_EMAIL_ADDRESS = "Please enter valid emailAddress";
    public static final String START_PAGE_AND_END_PAGE_REQUIRED = "Start page and end page required.";
    public static final String DELIVERY_LOCATION_REQUIRED = "Delivery Location is required for request type Recall/hold/retrieval";
    public static final String INVALID_REQUEST_TYPE = "Please enter the valid request type";
    public static final String RETRIEVAL = "RETRIEVAL";
    public static final String EDD_REQUEST = "EDD";
    public static final String BORROW_DIRECT = "BORROW DIRECT";
    public static final String VALID_REQUEST = "All request parameters are valid.Patron is eligible to raise a request";
    public static final String INVALID_PATRON = "Patron not on file in ILS";
    public static final String AVAILABLE = "Available";
    public static final String NOT_AVAILABLE = "Not Available";
    public static final String ITEMBARCODE_WITH_DIFFERENT_BIB = "All items must be attached to the same bibliographic record, have the same customer code, and the same availability.";
    public static final String INVALID_CUSTOMER_CODE = "Please enter the valid delivery Code";
    public static final String INVALID_DELIVERY_CODE = "Delivery location not valid for this item.";
    public static final String INVALID_ITEM_BARCODE = "Item is not available";
    public static final String ITEM_BARCODE_IS_REQUIRED = "Item Barcode is required";
    public static final String MULTIPLE_ITEMS_NOT_ALLOWED_FOR_EDD = "EDD requests must be done on a single item.";
    public static final String WRONG_ITEM_BARCODE = "Item Barcode(s) not available in database.";
    public static final String RETRIEVAL_NOT_FOR_UNAVAILABLE_ITEM = "Item not available for request.";
    public static final String RECALL_NOT_FOR_AVAILABLE_ITEM = "Recall not available for this item.";
    public static final String RECALL_FOR_ITEM_EXISTS = "Recall for this item already exists.";
    public static final String INITIAL_LOAD_ITEM_EXISTS = "Initial load item(s) cannot be recalled.";
    public static final String EDD_REQUEST_NOT_ALLOWED = "EDD request is not allowed for this customer code";

    public static final String CHAPTER_TITLE_IS_REQUIRED = "Chapter title is required for the request type EDD.";

    // Email
    public static final String REQUEST_RECALL_MAIL_QUEUE = "requestRecallMailSendQueue";
    public static final String REQUEST_LAS_STATUS_MAIL_QUEUE = "requestLASStatusMailSendQueue";
    public static final String REQUEST_ACCESSION_RECONCILATION_MAIL_QUEUE = "AccessionReconcilation";

    public static final String REQUEST_RECALL_EMAIL_TEMPLATE = "request_recall_email_body.vm";
    public static final String REQUEST_LAS_STATUS_EMAIL_TEMPLATE = "request_las_status_email_body.vm";
    public static final String DELETED_RECORDS_EMAIL_TEMPLATE = "deleted_records_email_body.vm";

    // Retrieval,EDD, Hold, Recall, Borrow Direct
    public static final String REQUEST_TYPE_RETRIEVAL = "RETRIEVAL";
    public static final String REQUEST_TYPE_EDD = "EDD";
    public static final String REQUEST_TYPE_RECALL = "RECALL";
    public static final String REQUEST_TYPE_BORROW_DIRECT = "BORROW DIRECT";
    public static final String REQUEST_TYPE_PW_INDIRECT = "PWI";
    public static final String REQUEST_TYPE_PW_DIRECT = "PWD";


    // MQ URI
    public static final String REQUEST_ITEM_QUEUE = "scsbactivemq:queue:RequestItemQ?concurrentConsumers=5";
    public static final String EMAIL_Q = "scsbactivemq:queue:CircEmailQ";
    public static final String SCSB_OUTGOING_QUEUE = "scsbactivemq:queue:scsbOutgoingQ";
    public static final String LAS_OUTGOING_QUEUE = "scsbactivemq:queue:lasOutgoingQ";
    public static final String LAS_INCOMING_QUEUE = "scsbactivemq:queue:lasIncomingQ";

    public static final String PUL_REQUEST_TOPIC = "scsbactivemq:topic:PUL.RequestT";
    public static final String PUL_EDD_TOPIC = "scsbactivemq:topic:PUL.EDDT";
    public static final String PUL_RECALL_TOPIC = "scsbactivemq:topic:PUL.RecallT";
    public static final String PUL_BORROW_DIRECT_TOPIC = "scsbactivemq:topic:PUL.BorrowDirectT";

    public static final String CUL_REQUEST_TOPIC = "scsbactivemq:topic:CUL.RequestT";
    public static final String CUL_EDD_TOPIC = "scsbactivemq:topic:CUL.EDDT";
    public static final String CUL_RECALL_TOPIC = "scsbactivemq:topic:CUL.RecallT";
    public static final String CUL_BORROW_DIRECT_TOPIC = "scsbactivemq:topic:CUL.BorrowDirectT";

    public static final String NYPL_REQUEST_TOPIC = "scsbactivemq:topic:NYPL.RequestT";
    public static final String NYPL_EDD_TOPIC = "scsbactivemq:topic:NYPL.EDDT";
    public static final String NYPL_RECALL_TOPIC = "scsbactivemq:topic:NYPL.RecallT";
    public static final String NYPL_BORROW_DIRECT_TOPIC = "scsbactivemq:topic:NYPL.BorrowDirectT";

    // Queue Header
    public static final String REQUEST_TYPE_QUEUE_HEADER = "RequestType";

    //RoutId
    public static final String REQUEST_ITEM_QUEUE_ROUTEID = "RequestItemRouteId";
    public static final String EMAIL_ROUTE_ID = "RequestRecallEmailRouteId";
    public static final String SCSB_OUTGOING_ROUTE_ID = "LasOutgoingRouteId";
    public static final String LAS_INCOMING_ROUTE_ID = "LasIncomingQueueRouteId";

    public static final String PUL_REQUEST_TOPIC_ROUTEID = "PULRequestTopicRouteId";
    public static final String PUL_EDD_TOPIC_ROUTEID = "PULEDDTopicRouteId";
    public static final String PUL_RECALL_TOPIC_ROUTEID = "PULRecallTopicRouteId";
    public static final String PUL_BORROW_DIRECT_TOPIC_ROUTEID = "PULBorrowDirectTopicRouteId";

    public static final String CUL_REQUEST_TOPIC_ROUTEID = "CULRequestTopicRouteId";
    public static final String CUL_EDD_TOPIC_ROUTEID = "CULEDDTopicRouteId";
    public static final String CUL_RECALL_TOPIC_ROUTEID = "CULRecallTopicRouteId";
    public static final String CUL_BORROW_DIRECT_TOPIC_ROUTEID = "CULBorrowDirectTopicRouteId";

    public static final String NYPL_REQUEST_TOPIC_ROUTEID = "NYPLRequestTopicRouteId";
    public static final String NYPL_EDD_TOPIC_ROUTEID = "NYPLEDDTopicRouteId";
    public static final String NYPL_RECALL_TOPIC_ROUTEID = "NYPLRecallTopicRouteId";
    public static final String NYPL_BORROW_DIRECT_TOPIC_ROUTEID = "NYPLBorrowDirectTopicRouteId";

    public static final String REQUEST_ITEM_PUL_REQUEST_TOPIC = "RequestItem-pulRequestTopic";
    public static final String REQUEST_ITEM_PUL_EDD_TOPIC = "RequestItem-pulEDDTopic";
    public static final String REQUEST_ITEM_PUL_RECALL_TOPIC = "RequestItem-pulRecallTopic";
    public static final String REQUEST_ITEM_PUL_BORROW_DIRECT_TOPIC = "RequestItem-pulBorrowDirectTopic";

    public static final String REQUEST_ITEM_CUL_REQUEST_TOPIC = "RequestItem-pulRequestTopic";
    public static final String REQUEST_ITEM_CUL_EDD_TOPIC = "RequestItem-pulEDDTopic";
    public static final String REQUEST_ITEM_CUL_RECALL_TOPIC = "RequestItem-pulRecallTopic";
    public static final String REQUEST_ITEM_CUL_BORROW_DIRECT_TOPIC = "RequestItem-pulBorrowDirectTopic";

    public static final String REQUEST_ITEM_NYPL_REQUEST_TOPIC = "RequestItem-nyplRequestTopic";
    public static final String REQUEST_ITEM_NYPL_EDD_TOPIC = "RequestItem-nyplEDDTopic";
    public static final String REQUEST_ITEM_NYPL_RECALL_TOPIC = "RequestItem-nyplRecallTopic";
    public static final String REQUEST_ITEM_NYPL_BORROW_DIRECT_TOPIC = "RequestItem-nyplBorrowDirectTopic";


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
    public static final String FILE_NAME = "FileName";

    public static final String FORMAT_MARC = "marc";
    public static final String FORMAT_SCSB = "scsb";

    public static final String SUBMIT_COLLECTION_REPORT = "Submit_Collection_Report";
    public static final String SUBMIT_COLLECTION_REJECTION_REPORT = "Submit_Collection_Rejection_Report";
    public static final String SUBMIT_COLLECTION_EXCEPTION_REPORT = "Submit_Collection_Exception_Report";
    public static final String SUBMIT_COLLECTION_SUCCESS_REPORT = "Submit_Collection_Success_Report";
    public static final String SUBMIT_COLLECTION_ITEM_BARCODE = "ItemBarcode";
    public static final String SUBMIT_COLLECTION_CUSTOMER_CODE = CUSTOMER_CODE;
    public static final String SUBMIT_COLLECTION_EXCEPTION_REPORT_MESSAGE = "Exception report generated";
    public static final String SUBMIT_COLLECTION_REJECTION_REPORT_MESSAGE = "Rejection report generated";
    public static final String SUMBIT_COLLECTION_UPDATE_MESSAGE = "One or more record(s) updated.";
    public static final String SUMBIT_COLLECTION_NOT_UPDATED_MESSAGE = "No record(s) updated.";

    public static final String ITEM_STATUS_AVAILABLE = "Available";
    public static final String INVALID_SCSB_XML_FORMAT_MESSAGE = "Invalid SCSB xml format";
    public static final String INVALID_MARC_XML_FORMAT_MESSAGE = "Invalid Marc xml format";
    public static final String INVALID_MARC_XML_FORMAT_IN_SCSBXML_MESSAGE = "Invalid Marc xml content with in SCSB xml";
    public static final String SCSB_SOLR_CLIENT_SERVICE_UNAVAILABLE = "Scsb Solr Client Service is Unavailable.";
    public static final String SUBMIT_COLLECTION_INTERNAL_ERROR = "Internal error occured during submit collection";
    public static final String SUBMIT_COLLECTION_LIMIT_EXCEED_MESSAGE = "Maximum allowed input record is ";
    public static final String COMPLETE_STATUS = "Complete";
    public static final String INCOMPLETE_STATUS = "Incomplete";
    public static final String BIBRECORD_TAG = "<bibRecords>";
    public static final String SUBMIT_COLLECTION = "submitCollection";
    public static final String SUBMIT_COLLECTION_FOR_NO_FILES = "submitCollectionForNoFiles";
    public static final String MESSAGE = "Message";
    public static final String NOT_AVAILABLE_CGD = "NA";

    public static final String BIB_ID = "BibId";
    public static final String HOLDING_ID = "HoldingId";
    public static final String ITEM_ID = "ItemId";
    public static final String BIBLIOGRAPHIC_ENTITY = "bibliographicEntity";

    public static final String GUEST_USER = "Guest";
    public static final String REQUEST_ITEM_HOLD_FAILURE = "RequestItem - Hold Request Failed";
    public static final String REQUEST_ITEM_AVAILABILITY_STATUS_UPDATE = "RequestItem AvailabilityStatus Change";
    public static final String REQUEST_ITEM_GFA_FAILURE = "RequestItem - LAS Request Failed";
    public static final String REQUEST_ITEM_ITEM_CHANGE_LOG_EXCEPTION = "RequestItem - Exception";
    public static final String REQUEST_ITEM_CANCEL_ITEM_AVAILABILITY_STATUS = "RequestItemCancel AvailabilityStatus Change";
    public static final String REQUEST_ITEM_INSERT = "Request Item Insert";
    public static final String RETRIVAL_ORDER_NOT_REQUIRED_FOR_RECALL = "Retrival order not required for recall";


    public static final String REQUEST_ITEM_AVAILABILITY_STATUS_DATA_UPDATE = "1 - 2";
    public static final String REQUEST_ITEM_AVAILABILITY_STATUS_DATA_ROLLBACK = "2 - 1";
    public static final String REQUEST_ITEM_TITLE_SUFFIX = " [RECAP]";
    public static final String REQUEST_USE_RESTRICTIONS = "No Restrictions";

    public static final String API_KEY = "api_key";
    public static final String RECAP = "recap";
    public static final String UPDATE_ITEM_STATUS_SOLR = "/updateItem/updateItemAvailablityStatus";
    public static final String UPDATE_ITEM_STATUS_SOLR_PARAM_ITEM_ID = "itemBarcode";
    public static final String CIRCULATION_STATUS_CHARGED = "CHARGED";
    public static final String CIRCULATION_STATUS_OTHER = "OTHER";
    public static final String CIRCULATION_STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String CIRCULATION_STATUS_IN_TRANSIT_NYPL = "IN TRANSIT";
    public static final String CIRCULATION_STATUS_ON_HOLDSHELF = "ON HOLDSHELF";

    public static final String SEARCH_RECORDS_SOLR = "/searchService/searchByParam";
    public static final String SEARCH_RECORDS_SOLR_PARAM_FIELD_VALUE = "fieldValue";
    public static final String SEARCH_RECORDS_SOLR_PARAM_FIELD_NAME = "fieldName";
    public static final String SEARCH_RECORDS_SOLR_PARAM_FIELD_NAME_VALUE = "Barcode";

    public static final int ESIPEXPIRATION_DATE_DAY = 1;
    public static final int ESIPEXPIRATION_DATE_MONTH = 6;

    public static final String REQUEST_STATUS_RETRIEVAL_ORDER_PLACED = "RETRIEVAL_ORDER_PLACED";
    public static final String REQUEST_STATUS_RECALLED = "RECALL_ORDER_PLACED";
    public static final String REQUEST_STATUS_EDD = "EDD_ORDER_PLACED";
    public static final String REQUEST_STATUS_REFILED = "REFILED";
    public static final String REQUEST_STATUS_CANCELED = "CANCELED";
    public static final String REQUEST_STATUS_EXCEPTION = "EXCEPTION";
    public static final String REQUEST_STATUS_PENDING = "PENDING";
    public static final String REQUEST_STATUS_PROCESSING = "PROCESSING";
    public static final String REQUEST_STATUS_INITIAL_LOAD = "INITIAL_LOAD";

    public static final String NYPL_HOLD_DATE_FORMAT = "yyyy-MM-dd";
    public static final String NYPL_RECORD_TYPE = "i";

    public static final String DEFAULT_PICK_UP_LOCATION_NYPL = "lb";
    public static final String DEFAULT_PICK_UP_LOCATION_PUL = "rcpcirc";
    public static final String DEFAULT_PICK_UP_LOCATION_CUL = "CIRCrecap";

    public static final String REQUEST_EXCEPTION_REST = "RestClient : ";
    public static final String REQUEST_EXCEPTION = "Exception : ";
    public static final String REQUEST_PARSE_EXCEPTION = "ParseException : ";
    public static final String REQUEST_INVALID_SIP2_RESPONSE = "InvalidSIP2Response ";
    public static final String REQUEST_INVALID_SIP2_RESPONSE_VALUE = "InvalidSIP2ResponseValue ";
    public static final String REQUEST_ILS_EXCEPTION = "ILS Exception : ";
    public static final String REQUEST_LAS_EXCEPTION = "LAS Exception : ";
    public static final String REQUEST_SCSB_EXCEPTION = "SCSB Exception : ";

    public static final String GFA_SERVICE_PARAM = "filter";
    public static final String GFA_STATUS_INCOMING_ON_WORK_ORDER = "INC ON WO:";
    public static final String GFA_STATUS_OUT_ON_EDD_WORK_ORDER = "OUT ON EDD WO:";
    public static final String GFA_STATUS_REACC_ON_WORK_ORDER = "REACC ON WO:";
    public static final String GFA_STATUS_REFILE_ON_WORK_ORDER = "REFILE ON WO:";
    public static final String GFA_STATUS_SCH_ON_WORK_ORDER = "SCH ON EDD WO:";
    public static final String GFA_STATUS_VER_ON_EDD_WORK_ORDER = "VER ON EDD WO:";
    public static final String GFA_STATUS_IN = "IN";

    public static final String GFA_STATUS_NOT_ON_FILE = "NOT ON FILE";
    public static final String GFA_STATUS_OUT_ON_RETRIVAL_WORK_ORDER = "OUT ON RET WO:";
    public static final String GFA_STATUS_PW_INDIRECT_WORK_ORDER = "PWI ON WO:";
    public static final String GFA_STATUS_PW_DIRECT_WORK_ORDER = "PWD ON WO:";
    public static final String GFA_STATUS_SCH_ON_RET_WORK_ORDER = "SCH ON RET WO:";
    public static final String GFA_STATUS_VER_ON_PW_INDIRECT_WORK_ORDER = "VER ON PWI WO:";
    public static final String GFA_STATUS_VER_ON_PW_DIRECT_WORK_ORDER = "VER ON PWD WO:";
    public static final String GFA_STATUS_VER_ON_RET_WORK_ORDER = "VER ON RET WO:";
    public static final String GFA_STATUS_VER_ON_WORK_ORDER = "VER ON WO:";


    protected static final List<String> GFA_STATUS_AVAILABLE_LIST = Arrays.asList(GFA_STATUS_INCOMING_ON_WORK_ORDER, GFA_STATUS_OUT_ON_EDD_WORK_ORDER, GFA_STATUS_REACC_ON_WORK_ORDER, GFA_STATUS_REFILE_ON_WORK_ORDER, GFA_STATUS_SCH_ON_WORK_ORDER, GFA_STATUS_VER_ON_EDD_WORK_ORDER, GFA_STATUS_IN);
    protected static final List<String> GFA_STATUS_NOT_AVAILABLE_LIST = Arrays.asList(GFA_STATUS_NOT_ON_FILE, GFA_STATUS_OUT_ON_RETRIVAL_WORK_ORDER, GFA_STATUS_PW_INDIRECT_WORK_ORDER, GFA_STATUS_PW_DIRECT_WORK_ORDER,
            GFA_STATUS_SCH_ON_RET_WORK_ORDER, GFA_STATUS_VER_ON_PW_INDIRECT_WORK_ORDER, GFA_STATUS_VER_ON_PW_DIRECT_WORK_ORDER, GFA_STATUS_VER_ON_RET_WORK_ORDER, GFA_STATUS_VER_ON_WORK_ORDER);


    protected static final List<String> REQUEST_TYPE_LIST = Arrays.asList(ReCAPConstants.RETRIEVAL, ReCAPConstants.REQUEST_TYPE_EDD, ReCAPConstants.BORROW_DIRECT, ReCAPConstants.REQUEST_TYPE_RECALL);

    public static final String SUCCESSFULLY_PROCESSED_REQUEST_ITEM = "Successfully Processed Request Item";
    public static final String REQUEST_ITEM_BARCODE_NOT_FOUND = "ITEM BARCODE NOT FOUND.";
    public static final String REQUEST_CANCELLATION_SUCCCESS = "Request cancellation succcessfully processed";
    public static final String RECALL_CANCELLATION_SUCCCESS = "Recall request cancellation succcessfully processed";
    public static final String REQUEST_CANCELLATION_EDD_SUCCCESS = "EDD request cancellation successfully processed.";
    public static final String REQUEST_CANCELLATION_NOT_ON_HOLD_IN_ILS = "This Request cannot be canceled, this item is not on hold in ILS";
    public static final String REQUEST_CANCELLATION_NOT_ACTIVE = "RequestId is not active status to be canceled";
    public static final String REQUEST_CANCELLATION_DOES_NOT_EXIST = "RequestId does not exist";

    public static final String GFA_RETRIVAL_ORDER_SUCCESSFUL = "Retrieval order successfully created in LAS.";
    public static final String GFA_RETRIVAL_ITEM_NOT_AVAILABLE = "Item not available in LAS";
    public static final String GFA_ITEM_STATUS_CHECK_FAILED = "SCSB: Item status check failed to return valid response.";
    public static final String REQUEST_CANCELLED_NO_REFILED = "canceled";
    public static final String REQUEST_CANCELLED_SUBJECT = "Cancel Request - ";

    public static final String REQUEST_RECALL_TO_BORRWER = "recalled";
    public static final String REQUEST_RECALL_SUBJECT = "Recall - ";

    public static final String REQUEST_REFILE_BODY = "The following item has been requested and was found to be under Refile in LAS.";
    public static final String REQUEST_REFILE_SUBJECT = "Request for Item on Refile WO - ";


    public static final String SUBMIT_COLLECTION_FAILURE_REPORT = "SubmitCollection_Failure_Report";


    //Deaccession
    public static final String REQUESTED_ITEM_DEACCESSIONED = "The requested item has already been deaccessioned.";
    public static final String ITEM_BARCDE_DOESNOT_EXIST = "Item barcode doesn't exist in SCSB database.";
    public static final String DEACCESSION_REPORT = "DeAccession_Report";
    public static final String DEACCESSION_SUMMARY_REPORT = "DeAccession_Summary_Report";
    public static final String DATE_OF_DEACCESSION = "DateOfDeAccession";
    public static final String BARCODE = "Barcode";
    public static final String OWNING_INST_BIB_ID = "OwningInstitutionBibId";
    public static final String COLLECTION_GROUP_CODE = "CollectionGroupCode";
    public static final String REASON_FOR_FAILURE = "ReasonForFailure";
    public static final String STATUS = "Status";
    public static final String DEACCESSION_IN_SOLR_URL = "deaccessionInSolrService/deaccessionInSolr";
    public static final String DEACCESSION_NO_BARCODE_ERROR = "Provide one or more barcodes to deaccession";
    public static final String DEACCESSION_NO_BARCODE_PROVIDED_ERROR = "No barcode provided for deaccession";
    public static final String REQUEST_ITEM_CANCEL_DEACCESSION_ITEM = "RequestItemCancel DeaccessionItem";
    public static final String REQUEST_ITEM_CANCELED_FOR_DEACCESSION = "The request for this item has been canceled because the item has been deaccessioned.";
    public static final String REASON_CANCEL_REQUEST_FAILED = "Canceling hold for the requested item failed for the reason";
    public static final String DISCOVERY = "Discovery";


    //Logger
    public static final String LOG_ERROR="error-->";
    public static final String GFA_ITEM_STATUS_MISMATCH = "There is a mismatch in item status between SCSB and LAS. Please contact ReCAP staff (<a href=\"mailto:{0}\">{1}</a>) for further assistance.";
    public static final String GFA_SERVER_DOWN = "LAS item status check failed to give valid response or LAS is down. Please contact ReCAP staff (<a href=\"mailto:{0}\">{1}</a>) for further assistance.";
    public static final String LAS_DEACCESSION_REJECT_ERROR = "LAS has rejected the {0} request with Error code : \"{1}\" and Error note : \"{2}\".";
    public static final String LAS_REJECTED = "LAS has rejected";
    public static final String CREATING_A_BIB_RECORD_FAILED_IN_ILS = "Creating a Bib record failed in ILS";
    public static final String INVALID_NO_RESPONSE_FROM_ILS = "Invalid/No Response from ILS";
    public static final String LOGIN_FAILED = "Login Failed";
    public static final String PATRON_VALIDATION_FAILED = "Patron Validation Failed";
    public static final String ITEM_STATUS_REQUEST_FAILED = "Item Status Request Failed";
    public static final String RECALL_FAILED_NO_MESSAGE_RETURNED = "Recall failed, no message returned";
    public static final String RECALL_CANNOT_BE_PROCESSED_THE_ITEM_IS_NOT_CHECKED_OUT_IN_ILS = "Recall Cannot be processed, the item is not checked out in ILS";
    public static final String FINISH_PROCESSING = "Finish Processing";

    public static final String EMAIL_BODY_FOR = "emailBodyFor";
    public static final String SUBMIT_COLLECTION_SUCCESS_LIST = "submitCollectionSuccessList";
    public static final String SUBMIT_COLLECTION_FAILURE_LIST = "submitCollectionFailureList";
    public static final String SUBMIT_COLLECTION_REJECTION_LIST = "submitCollectionRejectionList";
    public static final String SUBMIT_COLLECTION_EXCEPTION_LIST = "submitCollectionExceptionList";
    public static final String SUBMIT_COLLECTION_EXCEPTION_RECORD = "Exception record - Item is unavailable in scsb to update";
    public static final String SUBMIT_COLLECTION_REJECTION_RECORD = "Rejection record - Only use restriction and cgd not updated because the item is in use";
    public static final String SUBMIT_COLLECTION_SUCCESS_RECORD = "Success record";
    public static final String SUBMIT_COLLECTION_FAILED_RECORD = "Failed record";
    public static final String ALL_INST = "ALL";
    public static final String FTP = "FTP";
    public static final String SUBMIT_COLLECTION_SUMMARY = "SubmitCollectionSummary";
    public static final String REST = "rest-api";
    public static final String SUBMIT_COLLECTION_EMAIL_BODY_VM = "submit_collection_email_body.vm";
    public static final String SUBMIT_COLLECTION_EMAIL_BODY_FOR_EMPTY_DIRECTORY_VM = "submit_collection_email_body_for_emptyDirectory.vm";
    public static final String SFTP = "sftp://";
    public static final String AT = "@";
    public static final String PRIVATE_KEY_FILE = "?privateKeyFile=";
    public static final String KNOWN_HOST_FILE = "&knownHostsFile=";
    public static final String PROCESS_INPUT = "processInput";
    public static final String SUBMIT_COLLECTION_SFTP_OPTIONS = "&move=.done&delay=10m&localWorkDirectory=";
    public static final String SUBMIT_COLLECTION_COMPLETE_RECORD_UPDATE = "Complete item record info updated through submit collection";
    public static final String SUBMIT_COLLECTION_DUMMY_RECORD_UPDATE = "Dummy item record removed and actual record added through submit collection";

    public static final String PURGE_EDD_REQUEST = "noOfUpdatedRecordsForEddRequest";
    public static final String PURGE_PHYSICAL_REQUEST = "noOfUpdatedRecordsForPhysicalRequest";

    public static final String DEACCESSION_ROLLBACK = "Deaccession Rollback";
    public static final String DEACCESSION_ROLLBACK_NOTES = " Hence, the transaction of deaccessioning item is rolled back.";

    public static final String COUNT_OF_PURGED_EXCEPTION_REQUESTS = "countOfPurgedExceptionRequests";

    public static final String REQUEST_DATA_LOAD_CREATED_BY = "LAS";
    public static final String REQUEST_DATA_LOAD_REQUEST_TYPE = "PHY";
    public static final String REQUEST_DATA_LOAD_PATRON_ID = "0000000";
    public static final String REQUEST_DATA_LOAD_ITEM_ID = "itemId";
    public static final String REQUEST_DATA_LOAD_REQUESTING_INST_ID = "requestingInstitutionId";
    public static final String USE_RESTRICTION_UNAVAILABLE = "use restriction is unavailable in the input xml";
    public static final String CGD_NA = "cgd is still in NA status, provide cdg in input xml";
    public static final String RECORD_INCOMPLETE = "Record continue to be incomplete because ";
    public static final String DELETED_RECORDS_SUCCESS_MSG = "Deleted records completed successfully";
    public static final String DELETED_RECORDS_FAILURE_MSG = "Deleted records failed due to unexpected error";

    public static final String DELETED_STATUS_NOT_REPORTED = "Not Reported";
    public static final String DELETED_STATUS_REPORTED = "Reported";
    public static final String DELETED_MAIl_TO = "DELETED_MAIl_TO";
    public static final String EMAIL_SUBJECT_DELETED_RECORDS = "List of Deleted Records";
    public static final String EMAIL_DELETED_RECORDS_DISPLAY_MESSAGE = "Total No. of Records Deleted : ";
    public static final String DAILY_RECONCILIATION = "DailyReconciliation";

    private ReCAPConstants() {
    }

    public static final List getGFAStatusAvailableList() {
        return GFA_STATUS_AVAILABLE_LIST;
    }

    public static final List getGFAStatusNotAvailableList() {
        return GFA_STATUS_NOT_AVAILABLE_LIST;
    }

    public static final List getRequestTypeList() {
        return REQUEST_TYPE_LIST;
    }

    //Daily Reconcilation
    public static final String DAILY_RR_FTP_ROUTE_ID = "DailyReconcilationFtpRoute";
    public static final String DAILY_RR_FS_ROUTE_ID = "DailyReconcilationFsRoute";
    public static final String DAILY_RR_FTP_OPTIONS = "&move=.done&delay=2s&localWorkDirectory=";
    public static final String DAILY_RR_FS_OPTIONS = "?delete=true";
    public static final String DAILY_RR_FS_FILE = "file:";
    public static final String DAILY_RR_LAS = "LAS";
    public static final String DAILY_RR_SCSB = "SCSB";
    public static final String DAILY_RR_COMPARISON = "Comparison";
    public static final String DAILY_RR_FILE_DATE_FORMAT = "yyyyMMdd";
    public static final String DAILY_RR = "DailyRR";
    public static final String DATE_CELL_STYLE_FORMAT = "MM/dd/yyyy HH:mm:ss.S";
    public static final String DAILY_RR_REQUEST_ID = "RequestId";
    public static final String DAILY_RR_BARCODE = BARCODE;
    public static final String DAILY_RR_CUSTOMER_CODE = "CustomerCode";
    public static final String DAILY_RR_STOP_CODE = "StopCode";
    public static final String DAILY_RR_PATRON_ID = "PatronId";
    public static final String DAILY_RR_CREATED_DATE = "CreatedDate";
    public static final String DAILY_RR_LAST_UPDATED_DATE = "LastUpdatedDate";
    public static final String DAILY_RR_REQUESTING_INST = "RequestingInstitution";
    public static final String DAILY_RR_OWNING_INSTITUTION = "OwningInstitution";
    public static final String DAILY_RR_DELIVERY_METHOD = "DeliveryMethod";
    public static final String DAILY_RR_STATUS = "Status";
    public static final String DAILY_RR_EMAIL = "Email";
    public static final String DAILY_RR_MATCHED = "Matched";
    public static final String DAILY_RR_MISMATCH = "Mismatch";
    public static final String DAILY_RR_LAS_NOT_GIVEN_STATUS = "LASNotGivenStatus";
    public static final String DAILY_RR_SCSB_NOT_GIVEN_STATUS = "StatusNotFoundInScsb";
    public static final String DAILY_RR_NOT_IN_SCSB = "NotInScsb";

    //status Reconciliation

    public static final String STATUS_RECONCILIATION_REPORT = "scsbactivemq:queue:statusReconciliationReportQ";
    public static final String STATUS_RECONCILIATION_REPORT_ID = "statusReconciliationReportRoute";
    public static final String REPORT_FILE_NAME = "fileName";

    public static final String COMPLETED = "Completed";

    public static final String ACCESSION_RR_FTP_OPTIONS = "&move=.done&delay=2s&localWorkDirectory=";
    public static final String ACCESSION_RECONCILATION_FTP_PUL_ROUTE = "accessionReconcilationFtpPulRoute";
    public static final String ACCESSION_RECONCILATION_FTP_CUL_ROUTE = "accessionReconcilationFtpCulRoute";
    public static final String ACCESSION_RECONCILATION_FTP_NYPL_ROUTE = "accessionReconcilationFtpNyplRoute";
    public static final String ACCESSION_RECONCILATION_FS_PUL_ROUTE = "accessionReconcilationFsPulRoute";
    public static final String ACCESSION_RECONCILATION_FS_CUL_ROUTE = "accessionReconcilationFsCulRoute";
    public static final String ACCESSION_RECONCILATION_FS_NYPL_ROUTE = "accessionReconcilationFsNyplRoute";
    public static final String ACCESSION_RECONCILATION_FILE_NAME = "AccessionReconcilation";
    public static final String ACCESSION_RECONCILATION_SOLR_CLIENT_URL = "accessionReconcilationService/startAccessionReconcilation";
    public static final String STARTING = "Starting";

    public static final String SUBMIT_COLLECTION_FTP_PUL_ROUTE = "pulSubmitCollectionFTPRoute";
    public static final String SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE = "pulSubmitCollectionFTPCgdProtectedRoute";
    public static final String SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE = "pulSubmitCollectionFTPCgdNotProtectedRoute";
    public static final String SUBMIT_COLLECTION_FTP_CUL_ROUTE = "culSubmitCollectionFTPRoute";
    public static final String SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE = "culSubmitCollectionFTPCgdProtectedRoute";
    public static final String SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE = "culSubmitCollectionFTPCgdNotProtectedRoute";
    public static final String SUBMIT_COLLECTION_FTP_NYPL_ROUTE = "nyplSubmitCollectionFTPRoute";
    public static final String SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE = "nyplSubmitCollectionFTPCgdProtectedRoute";
    public static final String SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_NYPL_ROUTE = "nyplSubmitCollectionFTPCgdNotProtectedRoute";

    public static final String DELETED_MAIL_QUEUE = "deletedRecordsMailSendQueue";
    public static final String COUNT_OF_PURGED_ACCESSION_REQUESTS = "countOfPurgedAccessionRequests";

    public static final String STATUS_RECONCILIATION_CHANGE_LOG_OPERATION_TYPE = "StatusReconciliation-ItemAvailablityStatusChange";

    public static final String REQUEST_DATA_LOAD_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public static final String REQUEST_INITIAL_LOAD_FS_FILE = "file:";
    public static final String REQUEST_INITIAL_LOAD_PUL_FS_ROUTE = "requestInitialLoadPulFsRoute";
    public static final String REQUEST_INITIAL_LOAD_CUL_FS_ROUTE = "requestInitialLoadCulFsRoute";
    public static final String REQUEST_INITIAL_LOAD_NYPL_FS_ROUTE = "requestInitialLoadNyplFsRoute";
    public static final String REQUEST_INITIAL_LOAD_PUL_FTP_ROUTE = "requestInitialLoadPulFtpRoute";
    public static final String REQUEST_INITIAL_LOAD_CUL_FTP_ROUTE = "requestInitialLoadCulFtpRoute";
    public static final String REQUEST_INITIAL_LOAD_NYPL_FTP_ROUTE = "requestInitialLoadNyplFtpRoute";
    public static final String REQUEST_INITIAL_FILE_NAME = "RequestInvalidItem_";

    public static final String FOR = "for";
    public static final String STATUS_RECONCILIATION = "StatusReconciliation";
    public static final String STATUS_RECONCILIATION_FAILURE = "StatusReconciliationFailure";

    public static final String REQUEST_INITIAL_LOAD_PUL = "pul";
    public static final String REQUEST_INITIAL_LOAD_CUL = "cul";
    public static final String REQUEST_INITIAL_LOAD_NYPL = "nypl";

    // NYPL API URL
    public static final String NYPL_CHECKOUT_REQUEST_URL = "/checkout-requests";
    public static final String NYPL_CHECKIN_REQUEST_URL = "/checkin-requests";
    public static final String NYPL_RECAP_HOLD_REQUEST_URL = "/recap/hold-requests";
    public static final String NYPL_RECAP_CANCEL_HOLD_REQUEST_URL = "/recap/cancel-hold-requests";
    public static final String NYPL_RECAP_RECALL_REQUEST_URL = "/recap/recall-requests";
    public static final String NYPL_HOLD_REQUEST_URL = "/hold-requests";
    public static final String NYPL_PATRON_BY_BARCODE_URL = "/patrons?barcode=";

    public static final String INPUT_RECORDS = "inputRecords";
    public static final String INSTITUTION = "institution";
    public static final String IS_CGD_PROTECTED = "isCGDProtected";
}
