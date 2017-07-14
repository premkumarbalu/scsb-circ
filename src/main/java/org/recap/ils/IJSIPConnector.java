package org.recap.ils;

import org.recap.ils.model.response.AbstractResponseItem;

/**
 * Created by sudhishk on 11/11/16.
 */
public interface IJSIPConnector {

    /**
     * Gets host.
     *
     * @return the host
     */
    public abstract String getHost();

    /**
     * Gets operator user id.
     *
     * @return the operator user id
     */
    public abstract String getOperatorUserId();

    /**
     * Gets operator password.
     *
     * @return the operator password
     */
    public abstract String getOperatorPassword();

    /**
     * Gets operator location.
     *
     * @return the operator location
     */
    public abstract String getOperatorLocation();

    /**
     * Lookup item abstract response item.
     *
     * @param itemIdentifier the item identifier
     * @return the abstract response item
     */
    public AbstractResponseItem lookupItem(String itemIdentifier);

    /**
     * Check out item object.
     *
     * @param itemIdentifier   the item identifier
     * @param patronIdentifier the patron identifier
     * @return the object
     */
    public Object checkOutItem(String itemIdentifier, String patronIdentifier);

    /**
     * Check in item object.
     *
     * @param itemIdentifier   the item identifier
     * @param patronIdentifier the patron identifier
     * @return the object
     */
    public Object checkInItem(String itemIdentifier, String patronIdentifier);

    /**
     * Place hold object.
     *
     * @param itemIdentifier    the item identifier
     * @param patronIdentifier  the patron identifier
     * @param callInstitutionId the call institution id
     * @param itemInstitutionId the item institution id
     * @param expirationDate    the expiration date
     * @param bibId             the bib id
     * @param pickupLocation    the pickup location
     * @param trackingId        the tracking id
     * @param title             the title
     * @param author            the author
     * @param callNumber        the call number
     * @return the object
     */
    public Object placeHold(String itemIdentifier, String patronIdentifier, String callInstitutionId, String itemInstitutionId, String expirationDate, String bibId, String pickupLocation, String trackingId, String title, String author, String callNumber);

    /**
     * Cancel hold object.
     *
     * @param itemIdentifier   the item identifier
     * @param patronIdentifier the patron identifier
     * @param institutionId    the institution id
     * @param expirationDate   the expiration date
     * @param bibId            the bib id
     * @param pickupLocation   the pickup location
     * @param trackingId       the tracking id
     * @return the object
     */
    public Object cancelHold(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation, String trackingId);

    /**
     * Create bib object.
     *
     * @param itemIdentifier   the item identifier
     * @param patronIdentifier the patron identifier
     * @param institutionId    the institution id
     * @param titleIdentifier  the title identifier
     * @return the object
     */
    public Object createBib(String itemIdentifier, String patronIdentifier, String institutionId, String titleIdentifier);

    /**
     * Patron validation boolean.
     *
     * @param institutionId    the institution id
     * @param patronIdentifier the patron identifier
     * @return the boolean
     */
    public boolean patronValidation(String institutionId, String patronIdentifier);

    /**
     * Lookup patron abstract response item.
     *
     * @param patronIdentifier the patron identifier
     * @return the abstract response item
     */
    public AbstractResponseItem lookupPatron(String patronIdentifier);

    /**
     * Recall item object.
     *
     * @param itemIdentifier   the item identifier
     * @param patronIdentifier the patron identifier
     * @param institutionId    the institution id
     * @param expirationDate   the expiration date
     * @param bibId            the bib id
     * @param pickupLocation   the pickup location
     * @return the object
     */
    public Object recallItem(String  itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId,String pickupLocation);

    /**
     * Refile Item object.
     *
     * @param itemIdentifier
     * @return the object
     */
    public Object refileItem(String itemIdentifier);


}
