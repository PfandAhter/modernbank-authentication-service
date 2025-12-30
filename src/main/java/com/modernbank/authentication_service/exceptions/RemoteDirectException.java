package com.modernbank.authentication_service.exceptions;

import lombok.Getter;

@Getter
public class RemoteDirectException extends BusinessException {
  private final String originalErrorCode;
  private final String originalMessage;
  private final int httpStatus;

  public RemoteDirectException(String originalErrorCode, String originalMessage, int httpStatus) {
    super(originalMessage);
    this.originalErrorCode = originalErrorCode;
    this.originalMessage = originalMessage;
    this.httpStatus = httpStatus;
  }
}