package com.swugether.server.base.dto;

import com.swugether.server.base.constant.Code;

public class ErrorResponseDto extends ResponseDto {

  private ErrorResponseDto(Code errCode) {
    super(errCode.getCode(), errCode.getMessage());
  }

  private ErrorResponseDto(Code errCode, Exception e) {
    super(errCode.getCode(), errCode.getMessage(e));
  }

  private ErrorResponseDto(Code errCode, String message) {
    super(errCode.getCode(), errCode.getMessage(message));
  }

  public static ErrorResponseDto of(Code errCode) {
    return new ErrorResponseDto(errCode);
  }

  public static ErrorResponseDto of(Code errCode, Exception e) {
    return new ErrorResponseDto(errCode, e);
  }

  public static ErrorResponseDto of(Code errCode, String message) {
    return new ErrorResponseDto(errCode, message);
  }
}
