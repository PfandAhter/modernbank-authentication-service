package com.modernbank.authentication_service.api;

import com.modernbank.authentication_service.api.request.AuthUserRequest;
import com.modernbank.authentication_service.api.request.BaseRequest;
import com.modernbank.authentication_service.api.request.RegisterUserRequest;
import com.modernbank.authentication_service.api.response.AuthUserResponse;
import com.modernbank.authentication_service.api.response.BaseResponse;
import com.modernbank.authentication_service.api.response.UserInfoResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface AuthenticationControllerApi {

    @PostMapping(path = "/login")
    ResponseEntity<AuthUserResponse> authUser(@RequestBody AuthUserRequest authUserRequest);

    @PostMapping(path = "/register")
    BaseResponse registerUser(@RequestBody RegisterUserRequest registerUserRequest);

    @PostMapping("/logout")
    ResponseEntity<BaseResponse> logoutUser(@RequestHeader("Authorization") String token);

    @GetMapping("/validate")
    ResponseEntity<UserInfoResponse> validateToken(@RequestParam String token, HttpServletRequest request);

    @PostMapping("/validate/with-body")
    ResponseEntity<UserInfoResponse> validateTokenWithBody(@RequestBody BaseRequest baseRequest, HttpServletRequest request);
}