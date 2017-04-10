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

    /**
     * Gets columbia jsip connector.
     *
     * @return the columbia jsip connector
     */
    public ColumbiaJSIPConnector getColumbiaJSIPConnector() {
        return columbiaJSIPConnector;
    }

    /**
     * Gets princeton jsip connector.
     *
     * @return the princeton jsip connector
     */
    public PrincetonJSIPConnector getPrincetonJSIPConnector() {
        return princetonJSIPConnector;
    }

    /**
     * Gets nypl api connector.
     *
     * @return the nypl api connector
     */
    public NyplApiConnector getNyplAPIConnector() {
        return nyplAPIConnector;
    }

    /**
     * Gets jsip connector.
     *
     * @param institutionId the institution id
     * @return the jsip connector
     */
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
