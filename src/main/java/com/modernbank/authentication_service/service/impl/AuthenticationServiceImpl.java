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

import static com.modernbank.authentication_service.constants.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor

public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final AccountServiceClient accountServiceClient;

    private final MapperService mapperService;

    private final JwtService jwtService;

    @Override
    public UserAuthModel authUser(AuthUserRequest request) {
        User user = userRepository.findByEmailOptional(request.getEmail())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        if (!user.isEnabled()) {
            throw new AuthenticationFailedException(USER_IS_NOT_ENABLED);
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        if (!authentication.isAuthenticated()) {
            throw new AuthenticationFailedException(AUTHENTICATION_FAILED);
        }

        return UserAuthModel.builder()
                .token(jwtService.generateToken(user))
                .role(user.getAuthorities().iterator().next()).build();
    }

    @Override
    public BaseResponse registerUser(RegisterUserRequest request) {
        return accountServiceClient.registerUser(request);
    }

    public UserInfoModel validateToken(String token) {
        User response = userRepository.findByEmailOptional(jwtService.extractUsername(jwtService.decryptJwt(token)))
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        if (!jwtService.isTokenValid(jwtService.decryptJwt(token))) {
            throw new AuthenticationFailedException(TOKEN_IS_NOT_VALID);
        }

        return mapperService.map(response, UserInfoModel.class);
    }
}