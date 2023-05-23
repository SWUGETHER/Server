package com.swugether.server.base;

import com.swugether.server.base.constant.Code;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

  private final Code errCode;

  public GeneralException() {
    super(Code.INTERNAL_ERROR.getMessage());
    this.errCode = Code.INTERNAL_ERROR;
  }

  public GeneralException(String message) {
    super(Code.INTERNAL_ERROR.getMessage(message));
    this.errCode = Code.INTERNAL_ERROR;
  }

  public GeneralException(String message, Throwable cause) {
    super(Code.INTERNAL_ERROR.getMessage(message), cause);
    this.errCode = Code.INTERNAL_ERROR;
  }

  public GeneralException(Throwable cause) {
    super(Code.INTERNAL_ERROR.getMessage(cause));
    this.errCode = Code.INTERNAL_ERROR;
  }

  public GeneralException(Code errCode) {
    super(errCode.getMessage());
    this.errCode = errCode;
  }

  public GeneralException(Code errCode, String message) {
    super(errCode.getMessage(message));
    this.errCode = errCode;
  }

  public GeneralException(Code errCode, String message, Throwable cause) {
    super(errCode.getMessage(message), cause);
    this.errCode = errCode;
  }

  public GeneralException(Code errCode, Throwable cause) {
    super(errCode.getMessage(cause), cause);
    this.errCode = errCode;
  }
}
