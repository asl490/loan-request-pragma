package com.pragma.bootcamp.model.loantype.exception;

public class LoanAmountOutOfRangeException extends RuntimeException {

    public LoanAmountOutOfRangeException() {
        super("Loan amount is out of range for the selected loan type");
    }
}
