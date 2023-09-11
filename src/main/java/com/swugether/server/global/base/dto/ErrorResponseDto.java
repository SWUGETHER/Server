package com.swugether.server.global.base.dto;

import com.swugether.server.global.base.constant.Code;

public class ErrorResponseDto extends ResponseDto {

    private ErrorResponseDto(Code errCode) {
        super(errCode.getStatus(), errCode.getMessage());
    }

    private ErrorResponseDto(Code errCode, Exception e) {
        super(errCode.getStatus(), errCode.getMessage(e));
    }

    private ErrorResponseDto(Code errCode, String message) {
        super(errCode.getStatus(), errCode.getMessage(message));
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
