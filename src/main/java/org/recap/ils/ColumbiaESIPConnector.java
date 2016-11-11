package org.recap.ils;

import com.ceridwen.circulation.SIP.server.MessageHandlerDummyImpl;
import com.ceridwen.circulation.SIP.server.SocketDaemon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by chenchulakshmig on 6/10/16.
 */
@Service
public class ColumbiaESIPConnector extends ESIPConnector {

    @Value("${ils.columbia}")
    private String columbiaILS;

    @Value("${ils.columbia.operator.user.id}")
    private String operatorUserId;

    @Value("${ils.columbia.operator.password}")
    private String operatorPassword;

    @Value("${ils.columbia.operator.location}")
    private String operatorLocation;

    static SocketDaemon thread;

    public ColumbiaESIPConnector() {
        try {
            ColumbiaESIPConnector.thread = new SocketDaemon(columbiaILS, 7032, new MessageHandlerDummyImpl());
            ColumbiaESIPConnector.thread.setStrictChecksumChecking(true);
            ColumbiaESIPConnector.thread.start();
        } catch (Exception e) {

        }
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
