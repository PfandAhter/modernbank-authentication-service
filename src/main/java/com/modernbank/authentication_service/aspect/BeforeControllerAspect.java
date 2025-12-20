package com.modernbank.authentication_service.aspect;

import com.modernbank.authentication_service.api.request.BaseRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import static com.modernbank.authentication_service.constants.HeaderKey.CORRELATION_ID;


@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class BeforeControllerAspect {

    private final HttpServletRequest request;

    @Before(value = "execution(* com.modernbank.authentication_service.controller.AuthenticationController.*(..))")
    public void setTokenBeforeController(JoinPoint joinPoint){
        Object[] parameters = joinPoint.getArgs();
        for(Object param : parameters){
            String correlationId = request.getHeader(CORRELATION_ID);

            if(correlationId == null){
                correlationId = java.util.UUID.randomUUID().toString();
            }
            log.info("The request is received with correlation id: {}", correlationId);

            if(param instanceof BaseRequest baseRequest){
                baseRequest.setCorrelationId(correlationId);
            }
        }
    }
}