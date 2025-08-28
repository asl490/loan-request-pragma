package com.pragma.bootcamp.model.client.exception;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(String dni) {
        super("Client with DNI " + dni + " not found");
    }
}