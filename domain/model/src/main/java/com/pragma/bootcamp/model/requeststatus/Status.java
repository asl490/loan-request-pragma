package com.pragma.bootcamp.model.requeststatus;

import lombok.Getter;

@Getter
public enum Status {
    PENDING("Pending review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    DISBURSED("Funds disbursed"),
    ACTIVE("Active loan"),
    OVERDUE("Overdue payments"),
    COMPLETED("Loan completed"),
    CANCELED("Canceled");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
