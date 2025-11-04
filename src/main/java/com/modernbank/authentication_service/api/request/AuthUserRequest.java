package com.modernbank.authentication_service.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUserRequest extends BaseRequest {

    private String email;

    private String password;
}