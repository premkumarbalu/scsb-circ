//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pkrete.jsip2.variables;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.variables.FeeType;

public class FeeTypeFactory {
    private static FeeTypeFactory ref;

    private FeeTypeFactory() {
    }

    public static FeeTypeFactory getInstance() {
        if (ref == null) {
            ref = new FeeTypeFactory();
        }

        return ref;
    }

    public FeeType getFeeType(String code) throws InvalidSIP2ResponseValueException {

        if (code.equals("00")) {
            return FeeType.EMPTY;
        } else if (code.equals("01")) {
            return FeeType.OTHER_UNKNONW;
        } else if (code.equals("02")) {
            return FeeType.ADMINISTRATIVE;
        } else if (code.equals("03")) {
            return FeeType.DAMAGE;
        } else if (code.equals("04")) {
            return FeeType.OVERDUE;
        } else if (code.equals("05")) {
            return FeeType.PROCESSING;
        } else if (code.equals("06")) {
            return FeeType.RENTAL;
        } else if (code.equals("07")) {
            return FeeType.REPLACEMENT;
        } else if (code.equals("08")) {
            return FeeType.COMPUTER_ACCESS_CHARGE;
        } else if (code.equals("09")) {
            return FeeType.HOLD_FEE;
        } else {
            return FeeType.EMPTY;
        }
    }
}
