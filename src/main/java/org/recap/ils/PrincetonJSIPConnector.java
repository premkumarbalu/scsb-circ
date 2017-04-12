package org.recap.ils;

import com.pkrete.jsip2.connection.SIP2SocketConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by chenchulakshmig on 6/10/16.
 */
@Service
public class PrincetonJSIPConnector extends JSIPConnector {

    @Value("${ils.princeton}")
    private String princetonILS;

    @Value("${ils.princeton.operator.user.id}")
    private String operatorUserId;

    @Value("${ils.princeton.operator.password}")
    private String operatorPassword;

    @Value("${ils.princeton.operator.location}")
    private String operatorLocation;

    private static SIP2SocketConnection thread;

    /**
     * Instantiates a new Princeton jsip connector.
     */
    public PrincetonJSIPConnector() {
        PrincetonJSIPConnector.thread= new SIP2SocketConnection(princetonILS, 7031);
    }

    @Override
    public String getHost() {
        return princetonILS;
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
