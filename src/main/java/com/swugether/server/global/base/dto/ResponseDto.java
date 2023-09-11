package com.swugether.server.global.base.dto;

import com.swugether.server.global.base.constant.Code;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class ResponseDto {
    private final Integer code;
    private final String message;

    public static ResponseDto of(Code code) {
        return new ResponseDto(code.getStatus(), code.getMessage());
    }

    public static ResponseDto of(Code errCode, Exception e) {
        return new ResponseDto(errCode.getStatus(), errCode.getMessage(e));
    }

    public static ResponseDto of(Code errCode, String message) {
        return new ResponseDto(errCode.getStatus(), errCode.getMessage(message));
    }
}
