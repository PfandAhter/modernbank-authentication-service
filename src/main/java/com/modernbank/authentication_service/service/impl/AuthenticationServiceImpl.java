package com.modernbank.authentication_service.service.impl;

import com.modernbank.authentication_service.api.client.AccountServiceClient;
import com.modernbank.authentication_service.api.request.AuthUserRequest;
import com.modernbank.authentication_service.api.request.RegisterUserRequest;
import com.modernbank.authentication_service.api.response.BaseResponse;
import com.modernbank.authentication_service.entity.User;
import com.modernbank.authentication_service.exceptions.AuthenticationFailedException;
import com.modernbank.authentication_service.exceptions.NotFoundException;
import com.modernbank.authentication_service.jwt.JwtService;
import com.modernbank.authentication_service.model.UserAuthModel;
import com.modernbank.authentication_service.model.UserInfoModel;
import com.modernbank.authentication_service.repository.UserRepository;
import com.modernbank.authentication_service.service.AuthenticationService;
import com.modernbank.authentication_service.service.MapperService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import static com.modernbank.authentication_service.constants.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final AccountServiceClient accountServiceClient;

    private final MapperService mapperService;

    private final JwtService jwtService;

    @Override
    public UserAuthModel authUser(AuthUserRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());
        User user = userRepository.findByEmailOptional(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", request.getEmail());
                    return new NotFoundException(USER_NOT_FOUND);
                });

        if (!user.isEnabled()) {
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

        return UserAuthModel.builder()
                .token(jwtService.generateToken(user))
                .role(user.getAuthorities().iterator().next()).build();
    }

    @Override
    public BaseResponse registerUser(RegisterUserRequest request) {
        log.info("Registering user with email: {}", request.getEmail());
        return accountServiceClient.registerUser(request);
    }

    public UserInfoModel validateToken(String token) {
        User response = userRepository.findByEmailOptional(jwtService.extractUsername(jwtService.decryptJwt(token)))
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        if (!jwtService.isTokenValid(jwtService.decryptJwt(token))) {
            log.warn("Token is not valid");
            throw new AuthenticationFailedException(TOKEN_IS_NOT_VALID);
        }

        return mapperService.map(response, UserInfoModel.class);
    }
}