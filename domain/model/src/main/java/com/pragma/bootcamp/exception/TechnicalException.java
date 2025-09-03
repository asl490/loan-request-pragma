package com.pragma.bootcamp.exception;

public class TechnicalException extends RuntimeException {
    public TechnicalException(String message, DataIntegrityViolationException ex) {
        super(message);
    }
}
