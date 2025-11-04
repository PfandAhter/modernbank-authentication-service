package com.modernbank.authentication_service.controller;

import com.modernbank.authentication_service.api.AuthenticationControllerApi;
import com.modernbank.authentication_service.api.request.AuthUserRequest;
import com.modernbank.authentication_service.api.request.BaseRequest;
import com.modernbank.authentication_service.api.request.RegisterUserRequest;
import com.modernbank.authentication_service.api.response.AuthUserResponse;
import com.modernbank.authentication_service.api.response.BaseResponse;
import com.modernbank.authentication_service.api.response.UserInfoResponse;
import com.modernbank.authentication_service.service.AuthenticationService;
import com.modernbank.authentication_service.service.BlackListService;
import com.modernbank.authentication_service.service.MapperService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authentication")
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationControllerApi {

    private final AuthenticationService authenticationService;

    private final BlackListService blackListService;

    private final MapperService mapperService;

    @Override
    public ResponseEntity<AuthUserResponse> authUser(AuthUserRequest authUserRequest) {
        return ResponseEntity.ok(mapperService.map(authenticationService.authUser(authUserRequest), AuthUserResponse.class));
    }

    @Override
    public BaseResponse registerUser(RegisterUserRequest registerUserRequest) {
        authenticationService.registerUser(registerUserRequest);
        return new BaseResponse("User registered successfully");
    }

    @Override
    public ResponseEntity<BaseResponse> logoutUser(String token) {
        blackListService.add(token);
        return ResponseEntity.ok(new BaseResponse("Logout successful"));
    }

    @Override
    public ResponseEntity<UserInfoResponse> validateToken(String token, HttpServletRequest request) {
        return ResponseEntity.ok(mapperService.map(authenticationService.validateToken(request.getHeader("Authorization").split(" ")[1]), UserInfoResponse.class));
    }

    @Override
    public ResponseEntity<UserInfoResponse> validateTokenWithBody(BaseRequest baseRequest, HttpServletRequest request) {
        return ResponseEntity.ok(mapperService.map(authenticationService.validateToken(request.getHeader("Authorization").split(" ")[1]), UserInfoResponse.class));
    }
}