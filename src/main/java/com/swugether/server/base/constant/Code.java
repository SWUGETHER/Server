package com.swugether.server.base.constant;

import com.swugether.server.base.GeneralException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Code {
  OK(200, HttpStatus.OK, "OK."),
  BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "Bad request."),
  VALIDATION_ERROR(401, HttpStatus.BAD_REQUEST, "Validation error."),
  NOT_FOUND(404, HttpStatus.NOT_FOUND, "Requested resource is not found."),
  INTERNAL_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "Internal error."),
  DATA_ACCESS_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "Data access error."),
  UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "User unauthorized.");

  private final Integer code;
  private final HttpStatus httpStatus;
  private final String message;

  public String getMessage(Throwable e) {
    return this.getMessage("[" + this.getMessage() + "] " + e.getMessage());
  }

  public String getMessage(String message) {
    return Optional.ofNullable(message)
        .filter(Predicate.not(String::isBlank))
        .orElse(this.getMessage());
  }

  public static Code valueOf(HttpStatus httpStatus) {
    if (httpStatus == null) {
      throw new GeneralException("HttpStatus is null.");
    }

    return Arrays.stream(values())
        .filter(errCode -> errCode.getHttpStatus() == httpStatus)
        .findFirst()
        .orElseGet(() -> {
          if (httpStatus.is4xxClientError()) {
            return Code.BAD_REQUEST;
          } else if (httpStatus.is5xxServerError()) {
            return Code.INTERNAL_ERROR;
          } else {
            return Code.OK;
          }
        });
  }

  @Override
  public String toString() {
    return String.format("%s (%d)", this.name(), this.getCode());
  }
}
