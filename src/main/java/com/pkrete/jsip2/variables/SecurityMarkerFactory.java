//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pkrete.jsip2.variables;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;

/**
 * The type Security marker factory.
 */
public class SecurityMarkerFactory {

    private static SecurityMarkerFactory ref;

    private SecurityMarkerFactory() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static SecurityMarkerFactory getInstance() {
        if(ref == null) {
            ref = new SecurityMarkerFactory();
        }

        return ref;
    }

    /**
     * Gets security marker.
     *
     * @param code the code
     * @return the security marker
     * @throws InvalidSIP2ResponseValueException the invalid sip 2 response value exception
     */
    public SecurityMarker getSecurityMarker(String code) throws InvalidSIP2ResponseValueException {
        if("00".equals(code)) {
            return SecurityMarker.OTHER;
        } else if("01".equals(code)) {
            return SecurityMarker.NONE;
        } else if("02".equals(code)) {
            return SecurityMarker.TATTLE_TAPE_SECURITY_STRIP_3M;
        } else if("03".equals(code)) {
            return SecurityMarker.WHISPER_TAPE_3M;
        } else {
            return SecurityMarker.OTHER;
        }
    }
}
