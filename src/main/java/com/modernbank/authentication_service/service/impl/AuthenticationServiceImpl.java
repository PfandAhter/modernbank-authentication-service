package com.modernbank.authentication_service.service.impl;

import com.modernbank.authentication_service.api.client.AccountServiceClient;
import com.modernbank.authentication_service.api.dto.UserDetailsDTO;
import com.modernbank.authentication_service.api.request.AuthUserRequest;
import com.modernbank.authentication_service.api.request.RegisterUserRequest;
import com.modernbank.authentication_service.api.response.BaseResponse;
import com.modernbank.authentication_service.api.response.UserDetailsResponse;
import com.modernbank.authentication_service.entity.User;
import com.modernbank.authentication_service.entity.enums.Role;
import com.modernbank.authentication_service.exceptions.AuthenticationFailedException;
import com.modernbank.authentication_service.exceptions.NotFoundException;
import com.modernbank.authentication_service.jwt.JwtService;
import com.modernbank.authentication_service.model.UserAuthModel;
import com.modernbank.authentication_service.model.UserInfoModel;
import com.modernbank.authentication_service.service.AuthenticationService;
import com.modernbank.authentication_service.service.MapperService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import static com.modernbank.authentication_service.constants.ErrorCodeConstants.*;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final AccountServiceClient accountServiceClient;

    private final JwtService jwtService;

    @Override
    public UserAuthModel authUser(AuthUserRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());

        UserDetailsResponse userDetailsResponse = accountServiceClient.getUserDetailsForAuthentication(request.getEmail());

        if (userDetailsResponse == null || userDetailsResponse.getUserDetails() == null) {
            log.error("User not found for email: {}", request.getEmail());
            throw new NotFoundException(USER_NOT_FOUND);
        }

        UserDetailsDTO userDto = userDetailsResponse.getUserDetails();

        if (!userDto.isEnabled()) {
            log.warn("User is not enabled: {}", request.getEmail());
            throw new AuthenticationFailedException(USER_IS_NOT_ENABLED);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        if (!authentication.isAuthenticated()) {
            log.error("Authentication failed for user: {}", request.getEmail());
            throw new AuthenticationFailedException(AUTHENTICATION_FAILED);
        }

        log.info("User authenticated successfully: {}", request.getEmail());

        // UserDetails oluştur
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        userDto.getEmail(),
                        userDto.getPassword(),
                        userDto.isEnabled(),
                        userDto.isAccountNonExpired(),
                        userDto.isCredentialsNonExpired(),
                        userDto.isAccountNonLocked(),
                        userDto.getRoles()
                );

        return UserAuthModel.builder()
                .token(jwtService.generateToken(userDetails))
                .role(userDto.getRoles().iterator().next())
                .build();
    }

    @Override
    public BaseResponse registerUser(RegisterUserRequest request) {
        log.info("Registering user with email: {}", request.getEmail());
        return accountServiceClient.registerUser(request);
    }

    public UserInfoModel validateToken(String token) {
        UserDetailsResponse response = accountServiceClient.getUserDetailsForAuthentication(jwtService.extractUsername(jwtService.decryptJwt(token)));

        if (response == null || response.getUserDetails() == null) {
            log.warn("User not found for token validation");
            throw new NotFoundException(USER_NOT_FOUND);
        }

        if (!jwtService.isTokenValid(jwtService.decryptJwt(token))) {
            log.warn("Token is not valid");
            throw new AuthenticationFailedException(TOKEN_IS_NOT_VALID);
        }

        UserInfoModel userInfo = new UserInfoModel();

        userInfo.setId(response.getUserDetails().getId());
        userInfo.setEmail(response.getUserDetails().getEmail());

        userInfo.setAuthorities(response.getUserDetails().getRoles().stream()
                .map(Role::getAuthority)
                .toList()
        );

        return userInfo;
    }
}