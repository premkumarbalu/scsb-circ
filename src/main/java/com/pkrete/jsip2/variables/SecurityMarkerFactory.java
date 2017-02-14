//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pkrete.jsip2.variables;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.variables.SecurityMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityMarkerFactory {

    private Logger logger = LoggerFactory.getLogger(SecurityMarkerFactory.class);
    private static SecurityMarkerFactory ref;

    private SecurityMarkerFactory() {
    }

    public static SecurityMarkerFactory getInstance() {
        if(ref == null) {
            ref = new SecurityMarkerFactory();
        }

        return ref;
    }

    public SecurityMarker getSecurityMarker(String code) throws InvalidSIP2ResponseValueException {
        if(code.equals("00")) {
            return SecurityMarker.OTHER;
        } else if(code.equals("01")) {
            return SecurityMarker.NONE;
        } else if(code.equals("02")) {
            return SecurityMarker.TATTLE_TAPE_SECURITY_STRIP_3M;
        } else if(code.equals("03")) {
            return SecurityMarker.WHISPER_TAPE_3M;
        } else {
            return SecurityMarker.OTHER;
        }
    }
}
