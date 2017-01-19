package com.pkrete.jsip2.variables;

public enum CirculationStatus {
    ITEM_BARCODE_NOT_FOUND("00"),
    OTHER("01"),
    ON_ORDER("02"),
    AVAILABLE("03"),
    CHARGED("04"),
    CHARGED_NOT_TO_BE_RECALLED_UNTIL_EARLIEST_RECALL_DATE("05"),
    IN_PROCESS("06"),
    RECALLED("07"),
    WAITING_ON_HOLD_SHELF("08"),
    WAITING_TO_BE_RESHELVED("09"),
    IN_TRANSIT("10"),
    CLAIMED_RETURNED("11"),
    LOST("12"),
    MISSING("13");

    private String value;

    private CirculationStatus(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
