//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pkrete.jsip2.variables;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.variables.CirculationStatus;

public class CirculationStatusFactory {
    private static CirculationStatusFactory ref;

    private CirculationStatusFactory() {
    }

    public static CirculationStatusFactory getInstance() {
        if(ref == null) {
            ref = new CirculationStatusFactory();
        }

        return ref;
    }

    public CirculationStatus getCirculationStatus(String code) throws InvalidSIP2ResponseValueException {
        if(code.equals("00")) {
            return CirculationStatus.ITEM_BARCODE_NOT_FOUND;
        } else if(code.equals("01")) {
            return CirculationStatus.OTHER;
        } else if(code.equals("02")) {
            return CirculationStatus.ON_ORDER;
        } else if(code.equals("03")) {
            return CirculationStatus.AVAILABLE;
        } else if(code.equals("04")) {
            return CirculationStatus.CHARGED;
        } else if(code.equals("05")) {
            return CirculationStatus.CHARGED_NOT_TO_BE_RECALLED_UNTIL_EARLIEST_RECALL_DATE;
        } else if(code.equals("06")) {
            return CirculationStatus.IN_PROCESS;
        } else if(code.equals("07")) {
            return CirculationStatus.RECALLED;
        } else if(code.equals("08")) {
            return CirculationStatus.WAITING_ON_HOLD_SHELF;
        } else if(code.equals("09")) {
            return CirculationStatus.WAITING_TO_BE_RESHELVED;
        } else if(code.equals("10")) {
            return CirculationStatus.IN_TRANSIT;
        } else if(code.equals("11")) {
            return CirculationStatus.CLAIMED_RETURNED;
        } else if(code.equals("12")) {
            return CirculationStatus.LOST;
        } else if(code.equals("13")) {
            return CirculationStatus.MISSING;
        } else {
            throw new InvalidSIP2ResponseValueException("Invalid circulation status code! The given code \"" + code + "\" doesn\'t match with any circulation status!");
        }
    }
}
