package com.modernbank.authentication_service.exceptions;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message);
    }
}
