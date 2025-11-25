package com.modernbank.authentication_service.config;

import com.modernbank.authentication_service.entity.ErrorCodes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfiguration {

    @Value("${spring.data.redis.host}")
    private String redistHost;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redistHost, 6379);
    }

    @Bean
    public RedisTemplate<String, ErrorCodes> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, ErrorCodes> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        Jackson2JsonRedisSerializer<ErrorCodes> jsonSerializer = new Jackson2JsonRedisSerializer<>(ErrorCodes.class);
        template.setValueSerializer(jsonSerializer);
        return template;
    }
}