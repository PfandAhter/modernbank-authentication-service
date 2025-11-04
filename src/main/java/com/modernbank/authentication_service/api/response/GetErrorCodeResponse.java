package com.modernbank.authentication_service.api.response;

import com.modernbank.authentication_service.api.dto.ErrorCodesDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetErrorCodeResponse {
    private ErrorCodesDTO errorCode;

    private String id;

    private String error;

    private String description;
}