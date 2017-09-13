package org.recap.ils;

import com.pkrete.jsip2.connection.SIP2SocketConnection;
import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseException;
import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.messages.requests.*;
import com.pkrete.jsip2.messages.responses.*;
import com.pkrete.jsip2.util.MessageUtil;
import com.pkrete.jsip2.variables.HoldMode;
import org.recap.ReCAPConstants;
import org.recap.ils.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by sudhishk on 9/11/16.
 * Common Connector for SIP2 protocol, using jsip2 library
 */
public abstract class JSIPConnector implements IJSIPConnector {
    private static final Logger logger = LoggerFactory.getLogger(JSIPConnector.class);


    private SIP2SocketConnection getSocketConnection() {
        SIP2SocketConnection connection = new SIP2SocketConnection(getHost(), 7031);
        try {
            connection.connect();
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR, e);
        }
        return connection;
    }

    /**
     * J sip login boolean.
     *
     * @param connection       the connection
     * @param patronIdentifier the patron identifier
     * @return the boolean
     */
    public boolean jSIPLogin(SIP2SocketConnection connection, String patronIdentifier) {
        SIP2LoginRequest login;
        boolean loginPatronStatus = false;
        try {
            if (connection == null) {
                connection = getSocketConnection();
            }
            if (connection.connect()) {
                login = new SIP2LoginRequest(getOperatorUserId(), getOperatorPassword(), getOperatorLocation());
                SIP2LoginResponse loginResponse = (SIP2LoginResponse) connection.send(login);
                SIP2PatronInformationRequest request = new SIP2PatronInformationRequest(patronIdentifier);
                SIP2PatronInformationResponse response = (SIP2PatronInformationResponse) connection.send(request);
                loginPatronStatus = loginResponse.isOk() && response.isValidPatron() && response.isValidPatronPassword();
            }
        } catch (InvalidSIP2ResponseException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE, e);
        } catch (InvalidSIP2ResponseValueException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE_VALUE, e);
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR, e);
        }
        return loginPatronStatus;
    }

    @Override
    public boolean patronValidation(String institutionId, String patronIdentifier) {
        boolean loginPatronStatus = false;
        SIP2SocketConnection connection = getSocketConnection();
        try {
            SIP2LoginRequest login = new SIP2LoginRequest(getOperatorUserId(), getOperatorPassword(), getOperatorLocation());
            SIP2LoginResponse loginResponse = (SIP2LoginResponse) connection.send(login);
            SIP2PatronInformationRequest request = new SIP2PatronInformationRequest(institutionId, patronIdentifier, getOperatorPassword());
            SIP2PatronInformationResponse response = (SIP2PatronInformationResponse) connection.send(request);
            loginPatronStatus = loginResponse.isOk() && response.isValidPatron() && response.isValidPatronPassword();
        } catch (Exception ex) {
            logger.error(ReCAPConstants.LOG_ERROR, ex);
        } finally {
            connection.close();
        }
        return loginPatronStatus;
    }

    @Override
    public AbstractResponseItem lookupItem(String itemIdentifier) {
        SIP2LoginRequest login;
        SIP2SocketConnection connection = getSocketConnection();
        SIP2ItemInformationResponse sip2ItemInformationResponse;
        ItemInformationResponse itemInformationResponse = new ItemInformationResponse();
        try {
            login = new SIP2LoginRequest(getOperatorUserId(), getOperatorPassword(), getOperatorLocation());
            logger.info(login.getData());
            SIP2LoginResponse loginResponse = (SIP2LoginResponse) connection.send(login);
            if (loginResponse.isOk()) {
                sendAcsStatus(connection);
                SIP2ItemInformationRequest itemRequest = new SIP2ItemInformationRequest(itemIdentifier);
                itemInformationResponse.setEsipDataIn(itemRequest.getData());
                sip2ItemInformationResponse = (SIP2ItemInformationResponse) connection.send(itemRequest);
                itemInformationResponse.setEsipDataOut(sip2ItemInformationResponse.getData());
                itemInformationResponse.setItemBarcode(sip2ItemInformationResponse.getItemIdentifier());
                itemInformationResponse.setScreenMessage(getScreenMessage(sip2ItemInformationResponse.getScreenMessage()));
                itemInformationResponse.setSuccess(sip2ItemInformationResponse.isOk());
                itemInformationResponse.setTitleIdentifier(sip2ItemInformationResponse.getTitleIdentifier());

                itemInformationResponse.setDueDate(formatFromSipDate(sip2ItemInformationResponse.getDueDate()));
                itemInformationResponse.setRecallDate(formatFromSipDate(sip2ItemInformationResponse.getRecallDate()));
                itemInformationResponse.setHoldPickupDate(formatFromSipDate(sip2ItemInformationResponse.getHoldPickupDate()));
                itemInformationResponse.setTransactionDate(formatFromSipDate(sip2ItemInformationResponse.getTransactionDate()));
                itemInformationResponse.setExpirationDate(formatFromSipDate(sip2ItemInformationResponse.getExpirationDate()));

                itemInformationResponse.setCirculationStatus(sip2ItemInformationResponse.getCirculationStatus().name());
                itemInformationResponse.setCurrentLocation(sip2ItemInformationResponse.getCurrentLocation());
                itemInformationResponse.setPermanentLocation(sip2ItemInformationResponse.getPermanentLocation());
                if (sip2ItemInformationResponse.getFeeType() != null && sip2ItemInformationResponse.getFeeType().name() != null) {
                    itemInformationResponse.setFeeType(sip2ItemInformationResponse.getFeeType().name());
                }
                itemInformationResponse.setHoldQueueLength(sip2ItemInformationResponse.getHoldQueueLength());
                itemInformationResponse.setOwner(sip2ItemInformationResponse.getOwner());
                if (sip2ItemInformationResponse.getSecurityMarker() != null && sip2ItemInformationResponse.getSecurityMarker().name() != null) {
                    itemInformationResponse.setSecurityMarker(sip2ItemInformationResponse.getSecurityMarker().name());
                }
                itemInformationResponse.setCurrencyType((sip2ItemInformationResponse.getCurrencyType() != null) ? sip2ItemInformationResponse.getCurrencyType().name() : "");
                itemInformationResponse.setBibID(sip2ItemInformationResponse.getBibId());
            } else {
                itemInformationResponse.setScreenMessage(ReCAPConstants.ILS_LOGIN_FAILED);
                itemInformationResponse.setSuccess(false);
                logger.info(itemInformationResponse.getScreenMessage());
            }
        } catch (InvalidSIP2ResponseException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE, e);
            itemInformationResponse.setSuccess(false);
            itemInformationResponse.setScreenMessage(ReCAPConstants.INVALID_NO_RESPONSE_FROM_ILS);
        } catch (InvalidSIP2ResponseValueException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE_VALUE, e);
            itemInformationResponse.setSuccess(false);
            itemInformationResponse.setScreenMessage(ReCAPConstants.SCREEN_MESSAGE_ITEM_BARCODE_NOT_FOUND);
            itemInformationResponse.setCirculationStatus(ReCAPConstants.ITEM_BARCODE_NOT_FOUND);
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR, e);
            itemInformationResponse.setSuccess(false);
            itemInformationResponse.setScreenMessage("SCSB Exception: ILS connection failed");
        } finally {
            connection.close();
        }
        return itemInformationResponse;
    }

    private void sendAcsStatus(SIP2SocketConnection connection) throws InvalidSIP2ResponseException, InvalidSIP2ResponseValueException {
        SIP2SCStatusRequest sip2SCStatusRequest = new SIP2SCStatusRequest();
        logger.info(sip2SCStatusRequest.getData());
        connection.send(sip2SCStatusRequest);
    }

    /**
     * Lookup user sip 2 patron status response.
     *
     * @param institutionId    the institution id
     * @param patronIdentifier the patron identifier
     * @return the sip 2 patron status response
     */
    public SIP2PatronStatusResponse lookupUser(String institutionId, String patronIdentifier) {
        SIP2SocketConnection connection = getSocketConnection();
        SIP2PatronStatusResponse patronStatusResponse = null;
        try {
            if (connection.connect()) {
                SIP2PatronStatusRequest patronStatusRequest = new SIP2PatronStatusRequest(institutionId, patronIdentifier);
                logger.info(patronStatusRequest.getData());
                patronStatusResponse = (SIP2PatronStatusResponse) connection.send(patronStatusRequest);
            } else {
                logger.info(ReCAPConstants.ITEM_REQUEST_FAILED);
            }
        } catch (InvalidSIP2ResponseException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE, e);
        } catch (InvalidSIP2ResponseValueException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE_VALUE, e);
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR, e);
        } finally {
            connection.close();
        }
        return patronStatusResponse;
    }

    @Override
    public AbstractResponseItem checkOutItem(String itemIdentifier, String patronIdentifier) {
        SIP2SocketConnection connection = getSocketConnection();
        SIP2CheckoutResponse checkoutResponse = null;
        ItemCheckoutResponse itemCheckoutResponse = new ItemCheckoutResponse();
        try {
            if (connection.connect()) {
                if (jSIPLogin(connection, patronIdentifier)) {
                    SIP2SCStatusRequest status = new SIP2SCStatusRequest();
                    SIP2ACSStatusResponse statusResponse = (SIP2ACSStatusResponse) connection.send(status);
                    if (statusResponse.getSupportedMessages().isCheckout()) {
                        sendAcsStatus(connection);

                        SIP2CheckoutRequest checkoutRequest = new SIP2CheckoutRequest(patronIdentifier, itemIdentifier);
                        checkoutRequest.setCurrentLocation("");

                        itemCheckoutResponse.setEsipDataIn(checkoutRequest.getData());
                        checkoutResponse = (SIP2CheckoutResponse) connection.send(checkoutRequest);
                        itemCheckoutResponse.setEsipDataOut(itemCheckoutResponse.getEsipDataOut());

                        itemCheckoutResponse.setItemBarcode(checkoutResponse.getItemIdentifier());
                        itemCheckoutResponse.setPatronIdentifier(checkoutResponse.getPatronIdentifier());
                        itemCheckoutResponse.setTitleIdentifier(checkoutResponse.getTitleIdentifier());
                        itemCheckoutResponse.setDesensitize(checkoutResponse.isDesensitizeSupported());
                        itemCheckoutResponse.setRenewal(checkoutResponse.isRenewalOk());
                        itemCheckoutResponse.setMagneticMedia(checkoutResponse.isMagneticMedia());
                        itemCheckoutResponse.setDueDate(formatFromSipDate(checkoutResponse.getDueDate()));
                        itemCheckoutResponse.setTransactionDate(formatFromSipDate(checkoutResponse.getTransactionDate()));
                        itemCheckoutResponse.setInstitutionID(checkoutResponse.getInstitutionId());
                        itemCheckoutResponse.setItemOwningInstitution(checkoutResponse.getInstitutionId());
                        itemCheckoutResponse.setPatronIdentifier(checkoutResponse.getPatronIdentifier());
                        itemCheckoutResponse.setMediaType((checkoutResponse.getMediaType() != null) ? checkoutResponse.getMediaType().name() : "");
                        itemCheckoutResponse.setBibId(checkoutResponse.getBibId());
                        itemCheckoutResponse.setScreenMessage((!checkoutResponse.getScreenMessage().isEmpty()) ? checkoutResponse.getScreenMessage().get(0) : "");
                        itemCheckoutResponse.setSuccess(checkoutResponse.isOk());

                    }
                } else {
                    itemCheckoutResponse.setScreenMessage(ReCAPConstants.ILS_LOGIN_FAILED);
                    itemCheckoutResponse.setSuccess(false);
                    logger.info(itemCheckoutResponse.getScreenMessage());
                }
            }
        } catch (InvalidSIP2ResponseException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE, e);
            itemCheckoutResponse.setScreenMessage(e.getMessage());
            itemCheckoutResponse.setSuccess(false);
        } catch (InvalidSIP2ResponseValueException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE_VALUE, e);
            itemCheckoutResponse.setScreenMessage(e.getMessage());
            itemCheckoutResponse.setSuccess(false);
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR, e);
            itemCheckoutResponse.setScreenMessage(e.getMessage());
            itemCheckoutResponse.setSuccess(false);
        } finally {
            connection.close();
        }
        return itemCheckoutResponse;
    }

    @Override
    public AbstractResponseItem checkInItem(String itemIdentifier, String patronIdentifier) {
        SIP2SocketConnection connection = getSocketConnection();
        SIP2CheckinResponse checkinResponse = null;
        ItemCheckinResponse itemCheckinResponse = new ItemCheckinResponse();
        try {
            if (connection.connect()) { // Connect to the SIP Server - Princton, Voyager, ILS
                SIP2LoginRequest login = new SIP2LoginRequest(getOperatorUserId(), getOperatorPassword(), getOperatorLocation());
                SIP2LoginResponse loginResponse = (SIP2LoginResponse) connection.send(login);
                if (loginResponse.isOk()) {
                    SIP2SCStatusRequest status = new SIP2SCStatusRequest();
                    SIP2ACSStatusResponse statusResponse = (SIP2ACSStatusResponse) connection.send(status);
                    if (statusResponse.getSupportedMessages().isCheckin()) {
                        sendAcsStatus(connection);
                        SIP2CheckinRequest checkinRequest = new SIP2CheckinRequest(itemIdentifier);

                        itemCheckinResponse.setEsipDataIn(checkinRequest.getData());
                        checkinResponse = (SIP2CheckinResponse) connection.send(checkinRequest);
                        itemCheckinResponse.setEsipDataOut(checkinResponse.getData());

                        if (checkinResponse.isOk()) {
                            logger.info(ReCAPConstants.CHECK_IN_REQUEST_SUCCESSFUL);
                            itemCheckinResponse.setItemBarcode(checkinResponse.getItemIdentifier());
                            itemCheckinResponse.setTitleIdentifier(checkinResponse.getTitleIdentifier());
                            itemCheckinResponse.setDueDate(formatFromSipDate(checkinResponse.getDueDate()));
                            itemCheckinResponse.setResensitize(checkinResponse.isResensitize());
                            itemCheckinResponse.setAlert(checkinResponse.isAlert());
                            itemCheckinResponse.setMagneticMedia(checkinResponse.isMagneticMedia());
                            itemCheckinResponse.setTransactionDate(formatFromSipDate(checkinResponse.getTransactionDate()));
                            itemCheckinResponse.setInstitutionID(checkinResponse.getInstitutionId());
                            itemCheckinResponse.setItemOwningInstitution(checkinResponse.getInstitutionId());
                            itemCheckinResponse.setPatronIdentifier(checkinResponse.getPatronIdentifier());
                            itemCheckinResponse.setMediaType((checkinResponse.getMediaType() != null) ? checkinResponse.getMediaType().name() : "");
                            itemCheckinResponse.setBibId(checkinResponse.getBibId());
                            itemCheckinResponse.setPermanentLocation(checkinResponse.getPermanentLocation());
                            itemCheckinResponse.setCollectionCode(checkinResponse.getCollectionCode());
                            itemCheckinResponse.setSortBin(checkinResponse.getSortBin());
                            itemCheckinResponse.setCallNumber(checkinResponse.getCallNumber());
                            itemCheckinResponse.setDestinationLocation(checkinResponse.getDestinationLocation());
                            itemCheckinResponse.setAlertType((checkinResponse.getAlertType() != null) ? checkinResponse.getAlertType().name() : "");
                            itemCheckinResponse.setHoldPatronId(checkinResponse.getHoldPatronId());
                            itemCheckinResponse.setHoldPatronName(checkinResponse.getHoldPatronName());
                        } else {
                            logger.info(ReCAPConstants.CHECK_IN_REQUEST_FAILED);
                            logger.info("Response -> " + checkinResponse.getData());
                        }
                        itemCheckinResponse.setScreenMessage((!checkinResponse.getScreenMessage().isEmpty()) ? checkinResponse.getScreenMessage().get(0) : "");
                        itemCheckinResponse.setSuccess(checkinResponse.isOk());
                    }
                } else {
                    logger.info(ReCAPConstants.ILS_LOGIN_FAILED);
                }
            }
        } catch (InvalidSIP2ResponseException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE, e);
        } catch (InvalidSIP2ResponseValueException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE_VALUE, e);
        } finally {
            connection.close();
        }
        return itemCheckinResponse;
    }

    @Override
    public Object placeHold(String itemIdentifier, String patronIdentifier, String callInstitutionId, String itemInstitutionId, String expirationDate, String bibId, String pickupLocation, String trackingId, String title, String author, String callNumber) {
        return hold(HoldMode.ADD, itemIdentifier, patronIdentifier, callInstitutionId, bibId, pickupLocation);
    }

    @Override
    public Object cancelHold(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation, String trackingId) {
        return hold(HoldMode.DELETE, itemIdentifier, patronIdentifier, institutionId, bibId, pickupLocation);
    }

    private AbstractResponseItem hold(HoldMode holdMode, String itemIdentifier, String patronIdentifier, String institutionId, String bibId, String pickupLocation) {
        SIP2SocketConnection connection = getSocketConnection();
        SIP2HoldResponse holdResponse = null;
        ItemHoldResponse itemHoldResponse = new ItemHoldResponse();
        try {
            if (connection.connect()) { // Connect to the SIP Server - Princton, Voyager, ILS
                SIP2LoginRequest login = new SIP2LoginRequest(getOperatorUserId(), getOperatorPassword(), getOperatorLocation());
                SIP2LoginResponse loginResponse = (SIP2LoginResponse) connection.send(login);

                /* Check the response*/
                if (loginResponse.isOk()) {
                    /* The patron must be validated before placing a hold */
                    SIP2PatronInformationRequest request = new SIP2PatronInformationRequest(institutionId, patronIdentifier, getOperatorPassword());
                    SIP2PatronInformationResponse response = (SIP2PatronInformationResponse) connection.send(request);

                    /* Check if the patron and patron password are valid */
                    if (response.isValidPatron() && response.isValidPatronPassword()) {
                        logger.info(bibId);
                        SIP2HoldRequest holdRequest = new SIP2HoldRequest(patronIdentifier, itemIdentifier);
                        holdRequest.setHoldMode(holdMode);
                        holdRequest.setExpirationDate(MessageUtil.createFutureDate(ReCAPConstants.ESIPEXPIRATION_DATE_DAY, ReCAPConstants.ESIPEXPIRATION_DATE_MONTH));
                        holdRequest.setBibId(bibId);
                        holdRequest.setPickupLocation(pickupLocation);

                        logger.info("Request Hold -> " + holdRequest.getData());
                        itemHoldResponse.setEsipDataIn(holdRequest.getData());
                        holdResponse = (SIP2HoldResponse) connection.send(holdRequest);
                        itemHoldResponse.setEsipDataOut(holdResponse.getData());

                        itemHoldResponse.setItemBarcode(holdResponse.getItemIdentifier());
                        itemHoldResponse.setScreenMessage(getScreenMessage(holdResponse.getScreenMessage()));
                        itemHoldResponse.setSuccess(holdResponse.isOk());
                        itemHoldResponse.setTitleIdentifier(holdResponse.getTitleIdentifier());
                        itemHoldResponse.setExpirationDate(formatFromSipDate(holdResponse.getExpirationDate()));
                        itemHoldResponse.setTransactionDate(formatFromSipDate(holdResponse.getTransactionDate()));
                        itemHoldResponse.setInstitutionID(holdResponse.getInstitutionId());
                        itemHoldResponse.setPatronIdentifier(holdResponse.getPatronIdentifier());
                        itemHoldResponse.setBibId(holdResponse.getBibId());
                        itemHoldResponse.setQueuePosition(holdResponse.getQueuePosition());
                        itemHoldResponse.setLCCN(holdResponse.getLccn());
                        itemHoldResponse.setISBN(holdResponse.getIsbn());
                        itemHoldResponse.setAvailable(holdResponse.isAvailable());
                    } else {
                        itemHoldResponse.setSuccess(false);
                        itemHoldResponse.setScreenMessage(ReCAPConstants.PATRON_VALIDATION_FAILED + ((!response.getScreenMessage().isEmpty()) ? response.getScreenMessage().get(0) : ""));
                    }
                } else {
                    itemHoldResponse.setSuccess(false);
                    itemHoldResponse.setScreenMessage(ReCAPConstants.ILS_LOGIN_FAILED);
                    logger.info(itemHoldResponse.getScreenMessage());
                }
            }
        } catch (InvalidSIP2ResponseException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE, e);
            holdResponse = new SIP2HoldResponse("");
            holdResponse.setScreenMessage(java.util.Arrays.asList(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE));
        } catch (InvalidSIP2ResponseValueException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE_VALUE, e);
            holdResponse = new SIP2HoldResponse("");
            holdResponse.setScreenMessage(java.util.Arrays.asList(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE_VALUE));
        } finally {
            connection.close();
        }
        return itemHoldResponse;
    }

    @Override
    public ItemCreateBibResponse createBib(String itemIdentifier, String patronIdentifier, String institutionId, String titleIdentifier) {
        SIP2SocketConnection connection = getSocketConnection();
        SIP2CreateBibResponse createBibResponse = null;
        ItemCreateBibResponse itemCreateBibResponse = new ItemCreateBibResponse();
        try {
            if (connection.connect()) {
                SIP2LoginRequest login = new SIP2LoginRequest(getOperatorUserId(), getOperatorPassword(), getOperatorLocation());
                SIP2LoginResponse loginResponse = (SIP2LoginResponse) connection.send(login);
                if (loginResponse.isOk()) {
                    SIP2SCStatusRequest status = new SIP2SCStatusRequest();
                    connection.send(status);
                    SIP2PatronInformationRequest request = new SIP2PatronInformationRequest(institutionId, patronIdentifier, getOperatorPassword());
                    SIP2PatronInformationResponse response = (SIP2PatronInformationResponse) connection.send(request);
                    if (response.isValidPatron() && response.isValidPatronPassword()) {
                        SIP2CreateBibRequest createBibRequest = new SIP2CreateBibRequest(patronIdentifier, titleIdentifier, itemIdentifier);
                        logger.info("Request Create -> " + createBibRequest.getData());
                        itemCreateBibResponse.setEsipDataIn(createBibRequest.getData());
                        createBibResponse = (SIP2CreateBibResponse) connection.send(createBibRequest);
                        itemCreateBibResponse.setEsipDataOut(createBibResponse.getData());

                        itemCreateBibResponse.setItemBarcode(createBibResponse.getItemIdentifier());
                        itemCreateBibResponse.setScreenMessage((!createBibResponse.getScreenMessage().isEmpty()) ? createBibResponse.getScreenMessage().get(0) : "");
                        itemCreateBibResponse.setSuccess(createBibResponse.isOk());
                        itemCreateBibResponse.setBibId(createBibResponse.getBibId());
                        itemCreateBibResponse.setItemId(createBibResponse.getItemIdentifier());
                    } else {
                        itemCreateBibResponse.setSuccess(false);
                        itemCreateBibResponse.setScreenMessage(ReCAPConstants.PATRON_VALIDATION_FAILED + ((!response.getScreenMessage().isEmpty()) ? response.getScreenMessage().get(0) : ""));
                    }
                } else {
                    logger.info(ReCAPConstants.ILS_LOGIN_FAILED);
                    itemCreateBibResponse.setSuccess(false);
                    itemCreateBibResponse.setScreenMessage(ReCAPConstants.ILS_LOGIN_FAILED);
                }
            }
        } catch (InvalidSIP2ResponseException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE, e);
            itemCreateBibResponse.setSuccess(false);
            itemCreateBibResponse.setScreenMessage(e.getMessage());
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE_VALUE, e);
            itemCreateBibResponse.setSuccess(false);
            itemCreateBibResponse.setScreenMessage(e.getMessage());
        } finally {
            connection.close();
        }
        return itemCreateBibResponse;

    }

    @Override
    public AbstractResponseItem lookupPatron(String patronIdentifier) {
        SIP2SocketConnection connection = getSocketConnection();
        SIP2PatronInformationRequest sip2PatronInformationRequest;
        SIP2PatronInformationResponse sip2PatronInformationResponse;
        PatronInformationResponse patronInformationResponse = new PatronInformationResponse();
        try {
            if (connection.connect()) {
                SIP2LoginRequest login = new SIP2LoginRequest(getOperatorUserId(), getOperatorPassword(), getOperatorLocation());
                SIP2LoginResponse loginResponse = (SIP2LoginResponse) connection.send(login);
                if (loginResponse.isOk()) {
                    sip2PatronInformationRequest = new SIP2PatronInformationRequest(patronIdentifier);
                    patronInformationResponse.setEsipDataIn(sip2PatronInformationRequest.getData());
                    sip2PatronInformationResponse = (SIP2PatronInformationResponse) connection.send(sip2PatronInformationRequest);
                    patronInformationResponse.setEsipDataOut(sip2PatronInformationResponse.getData());
                    patronInformationResponse.setSuccess(true);

                    patronInformationResponse.setScreenMessage(sip2PatronInformationResponse.getStatus().isChargePrivilegesDenied()
                            + "" + sip2PatronInformationResponse.getStatus().isRenewalPrivilegesDenied()
                            + "" + sip2PatronInformationResponse.getStatus().isRecallPrivilegesDenied()
                            + "" + sip2PatronInformationResponse.getStatus().isHoldPrivilegesDenied()
                            + " - "
                            + ((sip2PatronInformationResponse.getScreenMessage() != null) ? sip2PatronInformationResponse.getScreenMessage().get(0) : ""));
                    patronInformationResponse.setPatronName(sip2PatronInformationResponse.getPersonalName());
                    patronInformationResponse.setPatronIdentifier(sip2PatronInformationResponse.getPatronIdentifier());
                    patronInformationResponse.setEmail(sip2PatronInformationResponse.getEmail());
                    patronInformationResponse.setBirthDate(sip2PatronInformationResponse.getBirthDate());
                    patronInformationResponse.setPhone(sip2PatronInformationResponse.getPhone());
                    patronInformationResponse.setPermanentLocation(sip2PatronInformationResponse.getPermanentLocation());
                    patronInformationResponse.setPickupLocation(sip2PatronInformationResponse.getPickupLocation());

                    patronInformationResponse.setChargedItemsCount(sip2PatronInformationResponse.getChargedItemsCount());
                    patronInformationResponse.setChargedItemsLimit(sip2PatronInformationResponse.getChargedItemsLimit());

                    patronInformationResponse.setFeeLimit(sip2PatronInformationResponse.getFeeLimit());
                    patronInformationResponse.setFeeType((sip2PatronInformationResponse.getFeeType() != null) ? sip2PatronInformationResponse.getFeeType().name() : "");

                    patronInformationResponse.setHoldItemsCount(sip2PatronInformationResponse.getHoldItemsCount());
                    patronInformationResponse.setHoldItemsLimit(sip2PatronInformationResponse.getHoldItemsLimit());
                    patronInformationResponse.setUnavailableHoldsCount(sip2PatronInformationResponse.getUnavailableHoldsCount());

                    patronInformationResponse.setFineItemsCount(sip2PatronInformationResponse.getFineItemsCount());
                    patronInformationResponse.setFeeAmount(sip2PatronInformationResponse.getFeeAmount());
                    patronInformationResponse.setHomeAddress(sip2PatronInformationResponse.getHomeAddress());
                    patronInformationResponse.setItems(sip2PatronInformationResponse.getItems());
                    patronInformationResponse.setItemType((sip2PatronInformationResponse.getItemType() != null) ? sip2PatronInformationResponse.getItemType().name() : "");

                    patronInformationResponse.setOverdueItemsCount(sip2PatronInformationResponse.getOverdueItemsCount());
                    patronInformationResponse.setOverdueItemsLimit(sip2PatronInformationResponse.getOverdueItemsLimit());
                    patronInformationResponse.setPacAccessType(sip2PatronInformationResponse.getPacAccessType());
                    patronInformationResponse.setPatronGroup(sip2PatronInformationResponse.getPatronGroup());
                    patronInformationResponse.setPatronType(sip2PatronInformationResponse.getPatronType());
                    patronInformationResponse.setDueDate(sip2PatronInformationResponse.getDueDate());
                    patronInformationResponse.setExpirationDate(sip2PatronInformationResponse.getExpirationDate());
                    patronInformationResponse.setStatus(sip2PatronInformationResponse.getStatus().toString());
                } else {
                    patronInformationResponse.setSuccess(true);
                    patronInformationResponse.setScreenMessage(loginResponse.getScreenMessage().get(0));
                }
            } else {
                patronInformationResponse.setSuccess(true);
                patronInformationResponse.setScreenMessage(ReCAPConstants.ILS_CONNECTION_FAILED);
            }
        } catch (Exception ex) {
            logger.error("", ex);
        } finally {
            connection.close();
        }
        return patronInformationResponse;
    }

    @Override
    public ItemRecallResponse recallItem(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation) {
        SIP2RecallResponse sip2RecallResponse = null;
        SIP2SocketConnection connection = getSocketConnection();
        ItemRecallResponse itemRecallResponse = new ItemRecallResponse();
        try {
            if (connection.connect()) { // Connect to the SIP Server - Princton, Voyager, ILS
                /* Login to the ILS */
                /* Create a login request */
                SIP2LoginRequest login = new SIP2LoginRequest(getOperatorUserId(), getOperatorPassword(), getOperatorLocation());
                /* Send the request */
                SIP2LoginResponse loginResponse = (SIP2LoginResponse) connection.send(login);

                /* Check the response*/
                if (loginResponse.isOk()) {
                    /* Send SCStatusRequest */
                    SIP2SCStatusRequest status = new SIP2SCStatusRequest();
                    connection.send(status);

                    /* The patron must be validated before placing a hold */
                    SIP2PatronInformationRequest request = new SIP2PatronInformationRequest(institutionId, patronIdentifier, getOperatorPassword());
                    SIP2PatronInformationResponse response = (SIP2PatronInformationResponse) connection.send(request);

                    /* Check if the patron and patron password are valid */
                    if (response.isValidPatron() && response.isValidPatronPassword()) {
                        SIP2RecallRequest recallRequest = new SIP2RecallRequest(patronIdentifier, itemIdentifier);
                        recallRequest.setHoldMode(HoldMode.ADD);
                        recallRequest.setInstitutionId(institutionId);
                        recallRequest.setExpirationDate(MessageUtil.createFutureDate(ReCAPConstants.ESIPEXPIRATION_DATE_DAY, ReCAPConstants.ESIPEXPIRATION_DATE_MONTH));
                        recallRequest.setBibId(bibId);
                        recallRequest.setPickupLocation(pickupLocation);

                        logger.info("Request Recall -> " + recallRequest.getData());
                        itemRecallResponse.setEsipDataIn(recallRequest.getData());
                        sip2RecallResponse = (SIP2RecallResponse) connection.send(recallRequest);
                        itemRecallResponse.setEsipDataOut(sip2RecallResponse.getData());

                        itemRecallResponse.setItemBarcode(sip2RecallResponse.getItemIdentifier());
                        itemRecallResponse.setScreenMessage((!sip2RecallResponse.getScreenMessage().isEmpty()) ? sip2RecallResponse.getScreenMessage().get(0) : "");
                        itemRecallResponse.setSuccess(sip2RecallResponse.isOk());
                        itemRecallResponse.setTitleIdentifier(sip2RecallResponse.getTitleIdentifier());
                        itemRecallResponse.setTransactionDate(formatFromSipDate(sip2RecallResponse.getDueDate()));
                        itemRecallResponse.setExpirationDate(formatFromSipDate(sip2RecallResponse.getExpirationDate()));
                        itemRecallResponse.setInstitutionID(sip2RecallResponse.getInstitutionId());
                        itemRecallResponse.setPickupLocation(sip2RecallResponse.getPickupLocation());
                        itemRecallResponse.setPatronIdentifier(sip2RecallResponse.getPatronIdentifier());
                    } else {
                        itemRecallResponse.setScreenMessage(ReCAPConstants.PATRON_VALIDATION_FAILED + ((!response.getScreenMessage().isEmpty()) ? response.getScreenMessage().get(0) : ""));
                        itemRecallResponse.setSuccess(false);
                    }
                } else {
                    logger.info(ReCAPConstants.ILS_LOGIN_FAILED);
                    itemRecallResponse.setScreenMessage(ReCAPConstants.ILS_LOGIN_FAILED);
                    itemRecallResponse.setSuccess(true);
                }
            }
        } catch (InvalidSIP2ResponseException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE, e);
        } catch (InvalidSIP2ResponseValueException e) {
            logger.error(ReCAPConstants.REQUEST_INVALID_SIP2_RESPONSE_VALUE, e);
        } finally {
            connection.close();
        }
        return itemRecallResponse;
    }

    private String formatFromSipDate(String sipDate) {
        SimpleDateFormat sipFormat = new SimpleDateFormat("yyyyMMdd    HHmmss");
        SimpleDateFormat requiredFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        String reformattedStr = "";
        try {
            if (sipDate != null && sipDate.trim().length() > 0) {
                reformattedStr = requiredFormat.format(sipFormat.parse(sipDate));
            }
        } catch (ParseException e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION, e);
        }
        return reformattedStr;
    }

    private String getScreenMessage(List<String> screenMessage) {
        String retMessage;
        for (int i = 0; !screenMessage.isEmpty() && i < screenMessage.size(); i++) {
            String strScreenMessage = screenMessage.get(i);
            logger.info(strScreenMessage);
        }
        retMessage = (!screenMessage.isEmpty()) ? screenMessage.get(0) : "";
        return retMessage;
    }

    public Object refileItem(String itemIdentifier) {
        return null;
    }
}
