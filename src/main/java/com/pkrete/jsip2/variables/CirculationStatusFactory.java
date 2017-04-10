//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pkrete.jsip2.variables;

import com.pkrete.jsip2.exceptions.InvalidSIP2ResponseValueException;
import com.pkrete.jsip2.variables.CirculationStatus;

/**
 * The type Circulation status factory.
 */
public class CirculationStatusFactory {
    private static CirculationStatusFactory ref;

    private CirculationStatusFactory() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static CirculationStatusFactory getInstance() {
        if(ref == null) {
            ref = new CirculationStatusFactory();
        }

        return ref;
    }

    /**
     * Gets circulation status.
     *
     * @param code the code
     * @return the circulation status
     * @throws InvalidSIP2ResponseValueException the invalid sip 2 response value exception
     */
    public CirculationStatus getCirculationStatus(String code) throws InvalidSIP2ResponseValueException {
        if("00".equals(code)) {
            return CirculationStatus.ITEM_BARCODE_NOT_FOUND;
        } else if("01".equals(code)) {
            return CirculationStatus.OTHER;
        } else if("02".equals(code)) {
            return CirculationStatus.ON_ORDER;
        } else if("03".equals(code)) {
            return CirculationStatus.AVAILABLE;
        } else if("04".equals(code)) {
            return CirculationStatus.CHARGED;
        } else if("05".equals(code)) {
            return CirculationStatus.CHARGED_NOT_TO_BE_RECALLED_UNTIL_EARLIEST_RECALL_DATE;
        } else if("06".equals(code)) {
            return CirculationStatus.IN_PROCESS;
        } else if("07".equals(code)) {
            return CirculationStatus.RECALLED;
        } else if("08".equals(code)) {
            return CirculationStatus.WAITING_ON_HOLD_SHELF;
        } else if("09".equals(code)) {
            return CirculationStatus.WAITING_TO_BE_RESHELVED;
        } else if("10".equals(code)) {
            return CirculationStatus.IN_TRANSIT;
        } else if("11".equals(code)) {
            return CirculationStatus.CLAIMED_RETURNED;
        } else if("12".equals(code)) {
            return CirculationStatus.LOST;
        } else if("13".equals(code)) {
            return CirculationStatus.MISSING;
        } else {
            throw new InvalidSIP2ResponseValueException("Invalid circulation status code! The given code \"" + code + "\" doesn\'t match with any circulation status!");
        }
    }
}
