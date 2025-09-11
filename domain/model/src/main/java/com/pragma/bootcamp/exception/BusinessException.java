package com.pragma.bootcamp.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Type type;

    public BusinessException(Type type) {
        super(type.getMessage());
        this.type = type;
    }

    public enum Type {
        INVALID_STATE_TRANSITION("The new state for the loan request is not valid."),
        REQUEST_LOAN_NOT_FOUND("The requested loan was not found.");
        // Add other specific business errors here in the future

        private final String message;

        Type(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
