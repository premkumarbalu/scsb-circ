//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pkrete.jsip2.variables;

/**
 * The enum Fee type.
 */
public enum FeeType {
    /**
     * Empty fee type.
     */
    EMPTY("00"),
    /**
     * Other unknonw fee type.
     */
    OTHER_UNKNONW("01"),
    /**
     * Administrative fee type.
     */
    ADMINISTRATIVE("02"),
    /**
     * Damage fee type.
     */
    DAMAGE("03"),
    /**
     * Overdue fee type.
     */
    OVERDUE("04"),
    /**
     * Processing fee type.
     */
    PROCESSING("05"),
    /**
     * Rental fee type.
     */
    RENTAL("06"),
    /**
     * Replacement fee type.
     */
    REPLACEMENT("07"),
    /**
     * Computer access charge fee type.
     */
    COMPUTER_ACCESS_CHARGE("08"),
    /**
     * Hold fee fee type.
     */
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
