package com.modernbank.authentication_service.api.dto;

import com.modernbank.authentication_service.entity.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter

public class UserDetailsDTO {
    private String id;

    private String email;

    private String password;

    private boolean enabled;

    private boolean accountNonExpired;

    private boolean accountNonLocked;

    private boolean credentialsNonExpired;

    private Set<Role> roles;
}