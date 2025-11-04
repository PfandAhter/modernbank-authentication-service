package com.modernbank.authentication_service.api.client;

import com.modernbank.authentication_service.api.request.RegisterUserRequest;
import com.modernbank.authentication_service.api.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "account-service" , url = "${client.feign.account-service.path}")
public interface AccountServiceClient {

    @PostMapping(value = "${client.feign.account-service.registerUser}")
    BaseResponse registerUser(@RequestBody RegisterUserRequest request);
}