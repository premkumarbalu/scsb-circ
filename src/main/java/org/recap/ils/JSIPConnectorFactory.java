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
    public IJSIPConnector getJSIPConnector(String institutionId){
        IJSIPConnector ijsipConnector = null;
        if(institutionId.equalsIgnoreCase(ReCAPConstants.PRINCETON)){
            ijsipConnector =princetonJSIPConnector;
        }else if(institutionId.equalsIgnoreCase(ReCAPConstants.COLUMBIA)){
            ijsipConnector =columbiaJSIPConnector;
        }if(institutionId.equalsIgnoreCase(ReCAPConstants.NYPL)){
        }
        return ijsipConnector;
    }
}
