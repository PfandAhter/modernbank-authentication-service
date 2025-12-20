package com.modernbank.authentication_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.modernbank.authentication_service.entity.enums.AccountStatus;
import com.modernbank.authentication_service.entity.enums.Currency;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Account {

    private String id;

    private String iban;

    private String name;

    private double balance;

    private Currency currency;

    @JsonBackReference
    private User user;

    private String description;

    private AccountStatus status;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}