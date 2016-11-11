package org.recap.ils;

import com.ceridwen.circulation.SIP.server.MessageHandlerDummyImpl;
import com.ceridwen.circulation.SIP.server.SocketDaemon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by chenchulakshmig on 6/10/16.
 */
@Service
public class PrincetonESIPConnector extends ESIPConnector {

    @Value("${ils.princeton}")
    private String princetonILS;

    @Value("${ils.princeton.operator.user.id}")
    private String operatorUserId;

    @Value("${ils.princeton.operator.password}")
    private String operatorPassword;

    @Value("${ils.princeton.operator.location}")
    private String operatorLocation;

    static SocketDaemon thread;

    public PrincetonESIPConnector() {
        try {
            PrincetonESIPConnector.thread = new SocketDaemon(princetonILS, 7031, new MessageHandlerDummyImpl());
            PrincetonESIPConnector.thread.setStrictChecksumChecking(true);
            PrincetonESIPConnector.thread.start();
        } catch (Exception e) {

        }
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
