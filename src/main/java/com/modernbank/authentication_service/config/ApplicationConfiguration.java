package com.modernbank.authentication_service.config;

import com.modernbank.authentication_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@EnableAspectJAutoProxy
@EnableWebSecurity
@Slf4j
public class ApplicationConfiguration {

    private final UserRepository userRepository;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(false);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        return modelMapper;
    }

    @Value("${security.encryption.secret-key}")
    private String secretKey;

    @Bean
    public Pbkdf2PasswordEncoder passwordEncoder() {
        // parametreler: secret, iteration, hash width (bit)
        return new Pbkdf2PasswordEncoder(
                secretKey,                           // secret
                16,                                       // salt length (bytes)
                240000,                                   // iterations
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256 // algorithm (enum)
        );
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;

    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        try {
            return userRepository::findByEmail;
        } catch (Exception e) {
            log.warn(UsernameNotFoundException.class.getName());
            throw new UsernameNotFoundException("USER_NOT_FOUND"); //TODO: Error Codes
        }
    }
}
