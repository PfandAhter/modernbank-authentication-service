package com.modernbank.authentication_service.api.response;

import com.modernbank.authentication_service.entity.enums.Role;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserResponse extends BaseResponse{

    private String token;

    private Role role;
}
