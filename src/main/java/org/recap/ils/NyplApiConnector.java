package org.recap.ils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by rajeshbabuk on 19/12/16.
 */
@Service
public class NyplApiConnector extends NyplApiServiceConnector {

    @Value("${ils.newyork}")
    private String newyorkILS;

    @Value("${ils.nypl.operator.user.id}")
    private String operatorUserId;

    @Value("${ils.nypl.operator.password}")
    private String operatorPassword;

    @Override
    public String getHost() {
        return newyorkILS;
    }

    @Override
    public String getOperatorUserId() {
        return operatorUserId;
    }

    @Override
    public String getOperatorPassword() {
        return operatorPassword;
    }

    @Override
    public String getOperatorLocation() {
        return null;
    }
}
