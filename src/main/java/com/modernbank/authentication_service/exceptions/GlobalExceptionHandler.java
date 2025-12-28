package com.modernbank.authentication_service.exceptions;

import com.modernbank.authentication_service.api.client.ParameterServiceClient;
import com.modernbank.authentication_service.api.request.LogErrorRequest;
import com.modernbank.authentication_service.api.response.BaseResponse;
import com.modernbank.authentication_service.constants.HeaderKey;
import com.modernbank.authentication_service.entity.ErrorCodes;
import com.modernbank.authentication_service.service.ErrorCodeService;
import feign.RetryableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import java.time.LocalDateTime;

import static com.modernbank.authentication_service.constants.ErrorCodeConstants.*;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j

public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ParameterServiceClient parameterServiceClient;

    private final ErrorCodeService errorCodeService;

    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<BaseResponse> handleRetryableException(RetryableException e, HttpServletRequest request) {
        log.error("Servis erişim hatası (Retry Failed). Detay: {}", e.getMessage());
        ErrorCodes errorCodes = getErrorCodeSafe(SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(errorCodes.getHttpStatus())
                .body(createErrorResponseBody(e, request, errorCodes));
    }

    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<BaseResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logError(e, request);

        ErrorCodes errorCodes = getErrorCodeSafe(e.getMessage());

        if(errorCodes.getHttpStatus() == null) {
            errorCodes.setHttpStatus(HttpStatus.NOT_ACCEPTABLE.value());
        }

        return ResponseEntity.status(errorCodes.getHttpStatus()).body(createBusinessErrorResponseBody(e, request, errorCodes));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse> handleBadCredentialsException(BadCredentialsException exception, HttpServletRequest request) {
        logError(exception, request);
        ErrorCodes errorCodes = getErrorCodeSafe(BAD_CREDENTIALS_PROVIDED);

        return ResponseEntity
                .status(errorCodes.getHttpStatus())
                .body(createErrorResponseBody(exception, request, errorCodes));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<BaseResponse> handleBadCredentialsException(UsernameNotFoundException exception, HttpServletRequest request) {
        logError(exception, request);
        ErrorCodes errorCodes = getErrorCodeSafe(USER_NOT_FOUND);

        return ResponseEntity
                .status(errorCodes.getHttpStatus())
                .body(createErrorResponseBody(exception, request, errorCodes));
    }

    @ExceptionHandler({RuntimeException.class, Exception.class})
    public ResponseEntity<BaseResponse> handleTechnicalException(Exception exception, HttpServletRequest request) {
        logError(exception, request);
        ErrorCodes errorCodes = getErrorCodeSafe(SYSTEM_ERROR);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponseBody(exception, request, errorCodes));
    }

    private BaseResponse createErrorResponseBody(Exception exception, HttpServletRequest request, ErrorCodes errorCodes) {
        logErrorToParameterService(exception, request, errorCodes);
        return new BaseResponse("FAILED", errorCodes.getError(), errorCodes.getDescription());
    }

    private BaseResponse createBusinessErrorResponseBody(BusinessException exception, HttpServletRequest request, ErrorCodes errorCodes) {
        logErrorToParameterService(exception,request,errorCodes);
        String messageBody = formatMessage(errorCodes.getDescription(), exception.getArgs());
        return new BaseResponse("FAILED", errorCodes.getError(), messageBody);
    }

    private ErrorCodes getErrorCodeSafe(String code) {
        try {
            ErrorCodes ec = errorCodeService.getErrorCodeByErrorId(code);
            if (ec != null) {
                return ec;
            }
        } catch (Exception ex) {
            log.error("Error cache service unreachable: {}", ex.getMessage());
        }

        return ErrorCodes.builder()
                .id(code != null ? code : "UNKNOWN")
                .error("Sistem Hatası")
                .description("Beklenmeyen bir hata oluştu. Lütfen destek ekibiyle iletişime geçin.")
                .httpStatus(500)
                .build();
    }

    private void logErrorToParameterService(Exception exception, HttpServletRequest httpServletRequest, ErrorCodes errorCode) {
        try {
            LogErrorRequest request = LogErrorRequest.builder()
                    .errorCode(exception.getMessage())
                    .serviceName("authentication-service")
                    .requestPath(httpServletRequest.getMethod() + " " + httpServletRequest.getRequestURI())
                    .traceId(httpServletRequest.getHeader(HeaderKey.CORRELATION_ID))
                    .timestamp(LocalDateTime.now())
                    .stackTrace(getTruncatedStackTrace(exception))
                    .exceptionName(exception.getClass().getName())
                    .errorMessage(errorCode.getDescription())
                    .build();

            request.setUserId(httpServletRequest.getHeader(HeaderKey.USER_ID));
            parameterServiceClient.logError(request);
        } catch (Exception e) {
            log.error("Error log process failed " + e.getMessage());
        }
    }

    private String getTruncatedStackTrace(Exception e) {
        String stack = java.util.Arrays.toString(e.getStackTrace());
        return stack.length() > 5000 ? stack.substring(0, 5000) + "..." : stack;
    }

    private void logError(Exception exception, HttpServletRequest httpServletRequest) {
        log.error("TraceId {} got error, Error: {} ",
                httpServletRequest.getHeader(HeaderKey.CORRELATION_ID),
                exception.getMessage());
    }

    private String formatMessage(String template, Object[] args) {
        if (template == null) return "Error details not available.";
        if (args == null || args.length == 0) return template;

        try {
            return java.text.MessageFormat.format(template, args);
        } catch (Exception e) {
            log.warn("Message formatting failed for template: {}", template);
            return template;
        }
    }
}