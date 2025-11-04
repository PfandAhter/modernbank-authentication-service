package com.modernbank.authentication_service.service;

import com.modernbank.authentication_service.api.request.AuthUserRequest;
import com.modernbank.authentication_service.api.request.RegisterUserRequest;
import com.modernbank.authentication_service.api.response.BaseResponse;
import com.modernbank.authentication_service.model.UserAuthModel;
import com.modernbank.authentication_service.model.UserInfoModel;

public interface AuthenticationService {
    UserAuthModel authUser(AuthUserRequest request);

    BaseResponse registerUser(RegisterUserRequest request);

    UserInfoModel validateToken(String token);
}