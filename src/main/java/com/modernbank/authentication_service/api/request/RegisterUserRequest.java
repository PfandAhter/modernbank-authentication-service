package com.modernbank.authentication_service.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class RegisterUserRequest {
    private String name;

    private String email;

    private String password;

    private String confirmPassword;

    private String gsm;
}