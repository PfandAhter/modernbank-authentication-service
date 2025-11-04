package com.modernbank.authentication_service.exceptions;

import com.modernbank.authentication_service.api.client.ParameterServiceClient;
import com.modernbank.authentication_service.api.request.LogErrorRequest;
import com.modernbank.authentication_service.api.response.BaseResponse;
import com.modernbank.authentication_service.entity.ErrorCodes;
import com.modernbank.authentication_service.service.ErrorCodeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import java.time.LocalDateTime;

import static com.modernbank.authentication_service.constants.ErrorCodeConstants.BAD_CREDENTIALS_PROVIDED;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j

public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ParameterServiceClient parameterServiceClient;

    private final ErrorCodeService errorCodeService;

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<BaseResponse> handleException(BadCredentialsException e,HttpServletRequest request) {
        logError(e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorResponseBody(e,request));
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<BaseResponse> handleException(RuntimeException e,HttpServletRequest request) {
        logError(e);
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(createErrorResponseBody(e,request));
    }
//TODO: ADD RETRYABLE EXCEPTION FEIGN
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<BaseResponse> handleException(Exception e,HttpServletRequest request) {
        logError(e);
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(createErrorResponseBody(e,request));
    }

    private BaseResponse createErrorResponseBody(Exception exception,HttpServletRequest request){
        String errorCode = exception.getMessage();

        if(exception instanceof BadCredentialsException){
            errorCode = BAD_CREDENTIALS_PROVIDED;
        }
        ErrorCodes errorCodes = getErrorCodeByErrorId(errorCode);
        logError(exception,request.getHeader("X-User-Id"));

        return new BaseResponse("FAILED", errorCodes.getError(), errorCodes.getDescription());
    }

    private ErrorCodes getErrorCodeByErrorId(String code){
        return errorCodeService.getErrorCodeByErrorId(code);
    }

    private void logError(Exception exception, String userId){
        try{
            LogErrorRequest request = LogErrorRequest.builder()
                    .errorCode(exception.getMessage())
                    .serviceName("authentication-service")
                    .timestamp(LocalDateTime.now().toString())
                    .stackTrace(exception.getStackTrace().toString())
                    .exceptionName(exception.getClass().getName())
                    .build();

            request.setUserId(userId);
            parameterServiceClient.logError(request);
        }catch (Exception e){
            log.error("Error log process failed " + e.getMessage());
        }
    }

    private void logError(Exception exception){
        log.error("Error: " + exception.getMessage());
    }
}