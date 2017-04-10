package com.pkrete.jsip2.variables;

/**
 * The enum Circulation status.
 */
public enum CirculationStatus {
    /**
     * Item barcode not found circulation status.
     */
    ITEM_BARCODE_NOT_FOUND("00"),
    /**
     * Other circulation status.
     */
    OTHER("01"),
    /**
     * On order circulation status.
     */
    ON_ORDER("02"),
    /**
     * Available circulation status.
     */
    AVAILABLE("03"),
    /**
     * Charged circulation status.
     */
    CHARGED("04"),
    /**
     * Charged not to be recalled until earliest recall date circulation status.
     */
    CHARGED_NOT_TO_BE_RECALLED_UNTIL_EARLIEST_RECALL_DATE("05"),
    /**
     * In process circulation status.
     */
    IN_PROCESS("06"),
    /**
     * Recalled circulation status.
     */
    RECALLED("07"),
    /**
     * Waiting on hold shelf circulation status.
     */
    WAITING_ON_HOLD_SHELF("08"),
    /**
     * Waiting to be reshelved circulation status.
     */
    WAITING_TO_BE_RESHELVED("09"),
    /**
     * In transit circulation status.
     */
    IN_TRANSIT("10"),
    /**
     * Claimed returned circulation status.
     */
    CLAIMED_RETURNED("11"),
    /**
     * Lost circulation status.
     */
    LOST("12"),
    /**
     * Missing circulation status.
     */
    MISSING("13");

    private String value;

    private CirculationStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
