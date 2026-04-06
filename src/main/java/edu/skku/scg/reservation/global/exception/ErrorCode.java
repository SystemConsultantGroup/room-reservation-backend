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
    UNREGISTERED_ORIGIN(FORBIDDEN, "AUTH-004", "등록되지 않은 출처입니다."),
    ACCESS_DENIED(FORBIDDEN, "AUTH-403", "접근 권한이 없습니다."),

    INVALID_STUDENT_ID_FORMAT(BAD_REQUEST, "USER-001", "학생의 학번은 10자리 숫자여야 합니다."),
    STUDENT_ID_NOT_ALLOWED(BAD_REQUEST, "USER-002", "교직원은 학번을 입력할 수 없습니다."),
    ALREADY_REGISTERED_USER(BAD_REQUEST, "USER-003", "이미 가입된 유저입니다."),
    INVALID_USER_TYPE(BAD_REQUEST, "USER-004", "잘못된 유저 타입입니다."),
    INVALID_STUDENT_MAJOR_TYPE(BAD_REQUEST, "USER-005", "학생의 전공 유형은 비어있을 수 없습니다."),
    INVALID_FACULTY_MAJOR_TYPE(BAD_REQUEST, "USER-006", "교직원은 전공 유형을 입력할 수 없습니다."),
    USER_NOT_FOUND(NOT_FOUND, "USER-404", "존재하지 않는 사용자입니다."),

    NOT_AVAILABLE_TIME(BAD_REQUEST, "ROOM-001", "예약 가능한 시간이 아닙니다."),
    EXCEED_MAX_BOOKING_TIME(BAD_REQUEST, "ROOM-002", "최대 예약 시간을 초과했습니다."),
    DUPLICATE_DAY_OF_WEEK(BAD_REQUEST, "ROOM-003", "중복된 요일의 운영 시간이 존재합니다."),
    INVALID_TIME_ORDER(BAD_REQUEST, "ROOM-004", "운영 시작 시간이 운영 종료 시간보다 빨라야 합니다."),
    ROOM_NOT_FOUND(NOT_FOUND, "ROOM-404", "존재하지 않는 공간입니다."),

    INVALID_TIME_RANGE(BAD_REQUEST, "TIME-001", "시작 시간이 종료 시간보다 늦습니다."),
    DATE_MISMATCH(BAD_REQUEST, "TIME-002", "시작 날짜와 종료 날짜가 일치하지 않습니다."),
    PAST_TIME_NOT_ALLOWED(BAD_REQUEST, "TIME-003", "현재 시간 이전으로는 예약할 수 없습니다."),

    MAJOR_ALREADY_APPLIED(BAD_REQUEST, "MAJOR-001", "이미 신청한 전공입니다."),
    USER_MAJOR_NOT_FOUND(NOT_FOUND, "MAJOR-002", "전공 신청 정보가 존재하지 않습니다."),
    ALREADY_PROCESSED_MAJOR_REGISTRATION(BAD_REQUEST, "MAJOR-003", "이미 처리된 전공 신청입니다."),
    ALREADY_HELD_MAJOR_TYPE(BAD_REQUEST, "MAJOR-004", "이미 승인된 동일한 전공 유형이 존재합니다."),
    DUPLICATE_MAJOR_TYPE_REQUEST(BAD_REQUEST, "MAJOR-005", "동일한 전공 유형을 중복해서 신청할 수 없습니다."),
    DUPLICATE_MAJOR_REQUEST(BAD_REQUEST, "MAJOR-006", "동일한 전공을 중복해서 신청할 수 없습니다."),
    MAJOR_NOT_FOUND(NOT_FOUND, "MAJOR-404", "존재하지 않는 전공입니다."),

    MANAGEMENT_UNIT_NOT_FOUND(NOT_FOUND, "UNIT-404", "존재하지 않는 관리 단위입니다."),

    SERVER_ERROR(INTERNAL_SERVER_ERROR, "SERVER-500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}