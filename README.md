# 성균관대학교 공간 예약 서비스 백엔드

![Java](https://img.shields.io/badge/Java-17-blue?logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen?logo=springboot&logoColor=white) ![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-005F99?logo=mysql&logoColor=white)

성균관대학교 교내 공간 예약을 관리하기 위한 Spring Boot 백엔드 API 서비스입니다.

## 핵심 기능

- Google OAuth2 로그인 및 쿠키 기반 JWT 인증 처리
- 공간 CRUD
- 예약 생성, 조회, 취소
- 전공별 등록 신청 프로세스와 관리자 승인 플로우
- 관리 단위(`ManagementUnit`) 기반의 테넌트 분리 및 권한 제어

## 시작하기

### 1. 요구 사항

- Java 17 이상
- MySQL 8.0 이상
- Docker (선택 사항)

### 2. 환경 변수 설정

로컬 구동 시 필요한 최소한의 환경 변수입니다.

| 변수명                 | 설명                     | 예시                                         |
| :--------------------- | :----------------------- | :------------------------------------------- |
| `DB_URL`               | MySQL JDBC 접속 주소     | `jdbc:mysql://localhost:3306/reservation`    |
| `DB_USERNAME`          | 데이터베이스 계정        | `root`                                       |
| `DB_PASSWORD`          | 데이터베이스 비밀번호    | `secret`                                     |
| `JWT_SECRET`           | JWT 서명용 시크릿 키     | `my-secret-key-string`                       |
| `GOOGLE_CLIENT_ID`     | 구글 OAuth 클라이언트 ID |                                              |
| `GOOGLE_CLIENT_SECRET` | 구글 OAuth 시크릿 키     |                                              |
| `GOOGLE_CALLBACK_URI`  | 구글 로그인 콜백 URI     | `http://localhost:8000/auth/callback/google` |

> `dev` 프로파일 환경에서는 Swagger Basic Auth 인증을 위해 `SWAGGER_ID`, `SWAGGER_PASSWORD` 변수가 추가로 필요합니다.

### 3. 로컬 서버 실행

```bash
./gradlew clean build
./gradlew bootRun --args='--spring.profiles.active=local'
```

> 서버는 기본적으로 `8000` 포트에서 구동됩니다.

### 4. Docker 실행 (선택)

```bash
docker build -t room-reservation-backend .
docker run -p 8000:8000 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/reservation \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=secret \
  -e JWT_SECRET=... \
  -e GOOGLE_CLIENT_ID=... \
  -e GOOGLE_CLIENT_SECRET=... \
  -e GOOGLE_CALLBACK_URI=... \
  room-reservation-backend

```

## 멀티테넌시 아키텍처 및 권한 설계

본 프로젝트는 **Shared DB + Shared Schema** 방식의 멀티테넌시를 채택하여 `ManagementUnit`을 기준으로 데이터를 분리합니다.

### 1. 테넌트 식별 및 권한

- **식별:** 공개 API는 HTTP 요청의 `Origin` 헤더를 기반으로 테넌트를 식별합니다 (`ManagementUnitIdArgumentResolver` 사용).
- **관리자 인가:** 로그인 시 부여된 `managingUnitIds`가 JWT Claim으로 저장되며, `@AdminApi`가 적용된 엔드포인트는 이 관리 단위를 보유한 사용자만 접근할 수 있습니다.
- **데이터 격리:** 관리자 API 호출 시, 타 테넌트 데이터에 접근하려 하면 즉시 `ACCESS_DENIED` 처리됩니다.

### 2. 예약 권한 제어 (`RoomAccessPolicy`)

`ReservationService`는 예약을 생성하기 전 사용자의 승인된 전공(`UserMajor`)과 해당 공간에 할당된 전공(`MajorRoom`)'의 교집합을 검증합니다. 게스트(GUEST)는 예약을 생성할 수 없습니다.

| 정책 (Policy)          | 조건 및 설명                                                                                                                                       |
| ---------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`ALL`**              | 사용자의 승인된 전공과 공간 전공 중 하나라도 겹치면 예약 허용                                                                                      |
| **`ONLY_FIRST_MAJOR`** | **학생(STUDENT):** 제1전공(`type=FIRST`)이 공간 전공과 일치해야만 허용<br> **교원(FACULTY):** 제1전공 여부 상관없이 승인 전공 교집합만 있으면 허용 |
| **`ONLY_FACULTY`**     | 교원(FACULTY)만 예약 허용                                                                                                                          |

## API 문서

- **Swagger UI:** `http://localhost:8000/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8000/v3/api-docs`

> **환경별 문서 정책:**
> `local`: 제약 없음 / `dev`: Basic Auth 인증 필요 / `prod`: 비활성화

## 프로젝트 구조

```text
src/main/java/edu/skku/scg/reservation
├── domain
│   ├── auth
│   ├── organization
│   ├── reservation
│   ├── room
│   └── user
└── global
    ├── annotation
    ├── config
    ├── exception
    └── resolver

```

## ERD

![ERD](docs/erd.png)
