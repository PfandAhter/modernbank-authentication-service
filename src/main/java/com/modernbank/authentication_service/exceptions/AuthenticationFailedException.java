package com.modernbank.authentication_service.exceptions;

public class AuthenticationFailedException extends BusinessException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
