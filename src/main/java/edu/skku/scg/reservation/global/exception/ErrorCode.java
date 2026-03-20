package edu.skku.scg.reservation.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    OAUTH_LOGIN_FAIL(UNAUTHORIZED, "AUTH-001", "구글 로그인 처리에 실패했습니다."),
    UNAUTHENTICATED(UNAUTHORIZED, "AUTH-401", "인증되지 않았습니다."),
    INVALID_TOKEN(UNAUTHORIZED, "AUTH-002", "유효하지 않은 토큰입니다."),
    INVALID_REDIRECT_URL(BAD_REQUEST, "AUTH-003", "허용되지 않은 리다이렉트 주소입니다."),
    ACCESS_DENIED(FORBIDDEN, "AUTH-403", "접근 권한이 없습니다."),

    INVALID_STUDENT_ID_FORMAT(BAD_REQUEST, "USER-001", "학생의 학번은 10자리 숫자여야 합니다."),
    STUDENT_ID_NOT_ALLOWED(BAD_REQUEST, "USER-002", "교직원은 학번을 입력할 수 없습니다."),
    ALREADY_REGISTERED_USER(BAD_REQUEST, "USER-003", "이미 가입된 유저입니다."),
    INVALID_USER_TYPE(BAD_REQUEST, "USER-004", "잘못된 유저 타입입니다."),
    USER_NOT_FOUND(NOT_FOUND, "USER-404", "존재하지 않는 사용자입니다."),

    ROOM_NOT_FOUND(NOT_FOUND, "ROOM-404", "존재하지 않는 방입니다."),

    SERVER_ERROR(INTERNAL_SERVER_ERROR, "SERVER-500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}