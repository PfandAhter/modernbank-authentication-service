package com.modernbank.authentication_service.api.client;

import com.modernbank.authentication_service.api.request.RegisterUserRequest;
import com.modernbank.authentication_service.api.response.BaseResponse;
import com.modernbank.authentication_service.api.response.UserDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "account-service" , url = "${client.feign.account-service.path}")
public interface AccountServiceClient {

    @PostMapping(value = "${client.feign.account-service.registerUser}")
    BaseResponse registerUser(@RequestBody RegisterUserRequest request);

    @GetMapping(value = "${client.feign.account-service.getUserDetailsForAuthentication}")
    UserDetailsResponse getUserDetailsForAuthentication(@RequestParam("email") String email);
}