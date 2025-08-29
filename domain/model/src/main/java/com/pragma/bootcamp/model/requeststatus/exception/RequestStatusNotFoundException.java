package com.pragma.bootcamp.model.requeststatus.exception;

public class RequestStatusNotFoundException extends RuntimeException {

    public RequestStatusNotFoundException(String request) {
        super("Request status not found: " + request);
    }
}
