//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pkrete.jsip2.variables;

public enum FeeType {
    EMPTY("00"),
    OTHER_UNKNONW("01"),
    ADMINISTRATIVE("02"),
    DAMAGE("03"),
    OVERDUE("04"),
    PROCESSING("05"),
    RENTAL("06"),
    REPLACEMENT("07"),
    COMPUTER_ACCESS_CHARGE("08"),
    HOLD_FEE("09");

    private String value;

    private FeeType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
