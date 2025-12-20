package com.modernbank.authentication_service.api.response;

import com.modernbank.authentication_service.api.dto.UserDetailsDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class UserDetailsResponse {
    UserDetailsDTO userDetails;
}