package com.modernbank.authentication_service.service;

import com.modernbank.authentication_service.entity.ErrorCodes;

public interface ErrorCodeService {

    ErrorCodes getErrorCodeByErrorId(String code);
}