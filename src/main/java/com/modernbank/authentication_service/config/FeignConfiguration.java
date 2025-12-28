package com.modernbank.authentication_service.config;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomFeignErrorDecoder();
    }

    @Bean
    public Retryer retryer() {
        // 100ms bekle, max 1sn bekle, 3 kere dene
        return new Retryer.Default(100, 1000, 3);
    }
}
