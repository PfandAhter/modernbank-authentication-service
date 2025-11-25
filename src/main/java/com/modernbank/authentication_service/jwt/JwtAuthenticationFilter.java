package com.modernbank.authentication_service.jwt;

import com.modernbank.authentication_service.service.BlackListService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
@WebFilter(filterName = "RequestCachingFilter", urlPatterns = "/*")
// @WebFilter(filterName = "RequestCachingFilter" , urlPatterns = "/??")

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    private final BlackListService blackListService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.split(" ")[1];
        jwt = jwtService.decryptJwt(jwt);

        if (blackListService.isBlackListed(jwt)) {
            log.warn("Token is blacklisted: {}", jwt);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            username = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            log.error("Jwt expired: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // fetch :almak , gidip getirmek

        // if the user already connected we dont want to do all proccess of
        // securityContext
        // when the authentication is null means that the user is not yet
        // authenticated./ The user not connected yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // i want to
                                                                                                            // build the
                                                                                                            // details
                                                                                                            // out of
                                                                                                            // our
                                                                                                            // requests
                                                                                                            // out of
                                                                                                            // our HTTP
                                                                                                            // request
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("User authenticated via JWT: {}", username);
            } else {
                log.warn("Invalid JWT token: {}", jwt);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            log.warn("Username is null or SecurityContextHolder already contains authentication");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    // https://www.baeldung.com/spring-exclude-filter
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/authentication/login") || path.startsWith("/actuator")
                || path.startsWith("/authentication/register");
    }
}