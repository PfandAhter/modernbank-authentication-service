package com.modernbank.authentication_service.api.client;

import com.modernbank.authentication_service.api.request.AuthUserRequest;
import com.modernbank.authentication_service.api.response.AuthUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(value = "SecurityService" , url = "${client.feign.security-service.path}")
public interface SecurityServiceClient {

    /*@PostMapping("${client.feign.security-service.tokenGenerate}")
    AuthUserResponse generateToken (@RequestBody AuthUserRequest authUserRequest);*/
}
