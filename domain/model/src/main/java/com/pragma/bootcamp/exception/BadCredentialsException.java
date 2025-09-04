package com.pragma.bootcamp.exception;

public class BadCredentialsException extends BusinessException {
    public BadCredentialsException(String message) {
        super(message);
    }
}