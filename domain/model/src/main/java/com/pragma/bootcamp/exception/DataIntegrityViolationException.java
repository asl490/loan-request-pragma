package com.pragma.bootcamp.exception;

import com.pragma.bootcamp.enums.ErrorCode;

public class DataIntegrityViolationException extends RuntimeException {
    public DataIntegrityViolationException(ErrorCode errorCode) {
        super(errorCode.getCode());
    }
}