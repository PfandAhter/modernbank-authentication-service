package com.modernbank.authentication_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.modernbank.authentication_service.entity.enums.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class User implements UserDetails {

    private String id;

    private String tckn;

    private String firstName;

    private String secondName;

    private String lastName;

    private String email;

    private String password;

    private String gsm;

    private String dateOfBirth;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private boolean isAccountNonExpired;

    private boolean isAccountNonLocked;

    private boolean isCredentialsNonExpired;

    private boolean isEnabled;

    private Set<Role> authorities;

    @JsonManagedReference
    private List<Account> accounts;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword(){
        return password;
    }
}