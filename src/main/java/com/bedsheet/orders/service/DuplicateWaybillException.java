package com.bedsheet.orders.service;

public class DuplicateWaybillException extends RuntimeException {

    public DuplicateWaybillException(String message) {
        super(message);
    }
}
