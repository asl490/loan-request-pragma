package com.pragma.bootcamp.model.loantype.exception;

public class LoanTypeNotFoundException extends RuntimeException {

    public LoanTypeNotFoundException(String name) {
        super("Loan type with name " + name + " not found");
    }
}