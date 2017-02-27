package org.recap.ils;

import org.recap.ReCAPConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by hemalathas on 16/11/16.
 */
@Service
public class JSIPConnectorFactory {

    @Autowired
    private ColumbiaJSIPConnector columbiaJSIPConnector;

    @Autowired
    private PrincetonJSIPConnector princetonJSIPConnector;

    @Autowired
    private NyplApiConnector nyplAPIConnector;

    public ColumbiaJSIPConnector getColumbiaJSIPConnector() {
        return columbiaJSIPConnector;
    }

    public PrincetonJSIPConnector getPrincetonJSIPConnector() {
        return princetonJSIPConnector;
    }

    public NyplApiConnector getNyplAPIConnector() {
        return nyplAPIConnector;
    }


    public IJSIPConnector getJSIPConnector(String institutionId) {
        IJSIPConnector ijsipConnector = null;
        if (institutionId.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            ijsipConnector = getPrincetonJSIPConnector();
        } else if (institutionId.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            ijsipConnector = getColumbiaJSIPConnector();
        } else if (institutionId.equalsIgnoreCase(ReCAPConstants.NYPL)) {
            ijsipConnector = getNyplAPIConnector();
        }
        return ijsipConnector;
    }
}
