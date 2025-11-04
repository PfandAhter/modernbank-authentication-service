package com.modernbank.authentication_service.service;

public interface BlackListService {

    void add(String token);

    boolean isBlackListed(String token);

    void remove(String token);
}