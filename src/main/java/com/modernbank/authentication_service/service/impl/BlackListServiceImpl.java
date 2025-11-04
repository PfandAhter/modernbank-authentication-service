package com.modernbank.authentication_service.service.impl;

import com.modernbank.authentication_service.jwt.JwtService;
import com.modernbank.authentication_service.service.BlackListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class BlackListServiceImpl implements BlackListService {

    private final RedisTemplate<String, String> redisTemplate;

    private final JwtService jwtService;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Override
    public void add(String token) {
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "true",
                (jwtService.extractExpiration(token).getTime() - System.currentTimeMillis()) / 1000);
    }

    @Override
    public boolean isBlackListed(String token) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }

    @Override
    public void remove(String token) {
        redisTemplate.delete(BLACKLIST_PREFIX + token);
    }
}