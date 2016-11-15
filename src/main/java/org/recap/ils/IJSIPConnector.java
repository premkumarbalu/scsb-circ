package org.recap.ils;

import com.pkrete.jsip2.messages.responses.SIP2CheckinResponse;
import com.pkrete.jsip2.messages.responses.SIP2CheckoutResponse;
import com.pkrete.jsip2.messages.responses.SIP2HoldResponse;
import com.pkrete.jsip2.messages.responses.SIP2ItemInformationResponse;
import org.recap.ils.jsipmessages.SIP2CreateBibResponse;

/**
 * Created by sudhishk on 11/11/16.
 */
public interface IJSIPConnector {

    public abstract String getHost();
    public abstract String getOperatorUserId();
    public abstract String getOperatorPassword();
    public abstract String getOperatorLocation();

    public SIP2ItemInformationResponse  lookupItem  (String itemIdentifier, String institutionId, String patronIdentifier);
    public SIP2CheckoutResponse         checkOutItem(String itemIdentifier, String institutionId, String patronIdentifier);
    public SIP2CheckinResponse          checkInItem (String itemIdentifier, String institutionId, String patronIdentifier);
    public SIP2HoldResponse             placeHold   (String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation);
    public SIP2HoldResponse             cancelHold  (String itemIdentifier, String patronIdentifier, String institutionId, String expirationDate, String bibId, String pickupLocation);
    public SIP2CreateBibResponse        createBib   (String itemIdentifier, String patronIdentifier, String institutionId, String titleIdentifier, String bibId);

}
