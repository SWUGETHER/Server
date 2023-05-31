package com.swugether.server.base.constant;

import com.swugether.server.base.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public enum Code {
    OK(200, HttpStatus.OK, "OK."),
    BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "Bad request."),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "User unauthorized."),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "Permission denied."),
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "Requested resource is not found."),
    INTERNAL_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "Internal error.");

    private final Integer status;
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
                        return switch (httpStatus.value()) {
                            case 401 -> Code.UNAUTHORIZED;
                            case 403 -> Code.FORBIDDEN;
                            case 404 -> Code.NOT_FOUND;
                            default -> Code.BAD_REQUEST;
                        };
                    } else if (httpStatus.is5xxServerError()) {
                        return Code.INTERNAL_ERROR;
                    } else {
                        return Code.OK;
                    }
                });
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", this.name(), this.getStatus());
    }
}
