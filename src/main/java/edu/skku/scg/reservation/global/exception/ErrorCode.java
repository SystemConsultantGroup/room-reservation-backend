package edu.skku.scg.reservation.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    OAUTH_LOGIN_FAIL(UNAUTHORIZED, "AUTH-001", "구글 로그인 처리에 실패했습니다."),
    INVALID_TOKEN(UNAUTHORIZED, "AUTH-002", "유효하지 않은 토큰입니다."),
    UNAUTHENTICATED(UNAUTHORIZED, "AUTH-401", "인증되지 않았습니다."),
    ACCESS_DENIED(FORBIDDEN, "AUTH-403", "접근 권한이 없습니다."),

    USER_NOT_FOUND(NOT_FOUND, "USER-001", "존재하지 않는 사용자입니다."),

    SERVER_ERROR(INTERNAL_SERVER_ERROR, "SERVER-500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}