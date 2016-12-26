package org.recap.ils;

import org.recap.ils.model.AbstractResponseItem;

/**
 * Created by sudhishk on 11/11/16.
 */
public interface IJSIPConnector {

    public abstract String getHost();
    public abstract String getOperatorUserId();
    public abstract String getOperatorPassword();
    public abstract String getOperatorLocation();

    public AbstractResponseItem lookupItem(String itemIdentifier);
    public AbstractResponseItem checkOutItem(String itemIdentifier, String patronIdentifier);
    public AbstractResponseItem checkInItem(String itemIdentifier, String patronIdentifier);
    public AbstractResponseItem placeHold(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation);
    public AbstractResponseItem cancelHold(String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation);
    public Object createBib(String itemIdentifier, String patronIdentifier, String institutionId, String titleIdentifier);
    public boolean patronValidation(String institutionId, String patronIdentifier);
    public Object lookupPatron(String patronIdentifier);
    public Object recallItem(String  itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId,String pickupLocation);

}
