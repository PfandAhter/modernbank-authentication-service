package com.modernbank.authentication_service.model;

import com.modernbank.authentication_service.entity.enums.Role;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthModel {
    private String token;

    private Role role;
}