package com.bedsheet.orders.curfox;

public class CurfoxApiException extends RuntimeException {

    public CurfoxApiException(String message) {
        super(message);
    }

    public CurfoxApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
