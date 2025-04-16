package com.github.istin.dmtools.report.model;

public enum TimelinePeriod {
    WEEK("Weekly"),
    TWO_WEEKS("Bi-Weekly"),
    MONTH("Monthly"),
    QUARTER("Quarterly");

    private final String description;

    TimelinePeriod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}