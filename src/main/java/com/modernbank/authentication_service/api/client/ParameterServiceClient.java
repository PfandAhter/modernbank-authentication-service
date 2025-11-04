package com.modernbank.authentication_service.api.client;

import com.modernbank.authentication_service.api.request.LogErrorRequest;
import com.modernbank.authentication_service.api.response.BaseResponse;
import com.modernbank.authentication_service.api.response.GetAllErrorCodesResponse;
import com.modernbank.authentication_service.api.response.GetErrorCodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "ParameterService" , url = "${client.feign.parameter-service.path}")
public interface ParameterServiceClient {

    @GetMapping(value = "${client.feign.parameter-service.getErrorCode}")
    GetErrorCodeResponse getErrorCode(@RequestParam("code") String code);

    @PostMapping(value = "${client.feign.parameter-service.logError}")
    BaseResponse logError(@RequestBody LogErrorRequest request);

    @GetMapping(value = "${client.feign.parameter-service.getAllErrorCodes}")
    GetAllErrorCodesResponse getAllErrorCodes(@RequestParam("name") String serviceName);
}