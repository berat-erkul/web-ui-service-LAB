package com.cydeo.enums;

public enum Status {

    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String displayValue;

    Status(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
