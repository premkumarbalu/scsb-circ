package org.recap.ils;

import com.pkrete.jsip2.connection.SIP2SocketConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by chenchulakshmig on 6/10/16.
 */
@Service
public class ColumbiaJSIPConnector extends JSIPConnector {

    @Value("${ils.columbia}")
    private String columbiaILS;

    @Value("${ils.columbia.operator.user.id}")
    private String operatorUserId;

    @Value("${ils.columbia.operator.password}")
    private String operatorPassword;

    @Value("${ils.columbia.operator.location}")
    private String operatorLocation;

    private static SIP2SocketConnection thread;

    /**
     * Instantiates a new Columbia jsip connector.
     */
    public ColumbiaJSIPConnector() {
        ColumbiaJSIPConnector.thread= new SIP2SocketConnection(columbiaILS, 7031);
    }

    @Override
    public String getHost() {
        return columbiaILS;
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
        return operatorLocation;
    }


}
