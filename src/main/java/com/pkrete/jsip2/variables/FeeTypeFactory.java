//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pkrete.jsip2.variables;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.variables.FeeType;

/**
 * The type Fee type factory.
 */
public class FeeTypeFactory {
    private static FeeTypeFactory ref;

    private FeeTypeFactory() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static FeeTypeFactory getInstance() {
        if (ref == null) {
            ref = new FeeTypeFactory();
        }

        return ref;
    }

    /**
     * Gets fee type.
     *
     * @param code the code
     * @return the fee type
     * @throws InvalidSIP2ResponseValueException the invalid sip 2 response value exception
     */
    public FeeType getFeeType(String code) throws InvalidSIP2ResponseValueException {

        if ("00".equals(code)) {
            return FeeType.EMPTY;
        } else if ("01".equals(code)) {
            return FeeType.OTHER_UNKNONW;
        } else if ("02".equals(code)) {
            return FeeType.ADMINISTRATIVE;
        } else if ("03".equals(code)) {
            return FeeType.DAMAGE;
        } else if ("04".equals(code)) {
            return FeeType.OVERDUE;
        } else if ("05".equals(code)) {
            return FeeType.PROCESSING;
        } else if ("06".equals(code)) {
            return FeeType.RENTAL;
        } else if ("07".equals(code)) {
            return FeeType.REPLACEMENT;
        } else if ("08".equals(code)) {
            return FeeType.COMPUTER_ACCESS_CHARGE;
        } else if ("09".equals(code)) {
            return FeeType.HOLD_FEE;
        } else {
            return FeeType.EMPTY;
        }
    }
}
