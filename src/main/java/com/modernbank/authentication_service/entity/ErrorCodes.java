package com.modernbank.authentication_service.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorCodes {

    private String id;

    private String error;

    private String description;

    private Integer httpStatus;
}