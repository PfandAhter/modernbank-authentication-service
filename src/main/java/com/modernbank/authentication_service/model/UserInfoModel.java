package com.modernbank.authentication_service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class UserInfoModel {
    private String id;
    private String email;
    private List<String> authorities;
}