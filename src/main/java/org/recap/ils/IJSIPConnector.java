package org.recap.ils;

/**
 * Created by sudhishk on 11/11/16.
 */
public interface IJSIPConnector {

    public abstract String getHost();
    public abstract String getOperatorUserId();
    public abstract String getOperatorPassword();
    public abstract String getOperatorLocation();

    public Object lookupItem(String itemIdentifier);
    public Object checkOutItem(String itemIdentifier, String patronIdentifier);
    public Object checkInItem(String itemIdentifier, String patronIdentifier);
    public Object placeHold(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation);
    public Object cancelHold(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation);
    public Object createBib(String itemIdentifier, String patronIdentifier, String institutionId, String titleIdentifier);
    public boolean patronValidation(String institutionId, String patronIdentifier);
    public Object lookupPatron(String patronIdentifier);
    public Object recallItem(String  itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId,String pickupLocation);

}
