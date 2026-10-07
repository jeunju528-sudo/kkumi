# 030. 카카오 로그인 구현: 카카오 API 직접 호출 대신 Spring Security oauth2-client

## 상태
확정 (2026-10-07)

## 배경
카카오 로그인(OAuth 2.0 인가 코드 방식)을 붙여야 한다. 인가 요청, 인가 코드 수신, 토큰 교환, 사용자 정보 조회의 전 과정을 Spring Security oauth2-client에 맡길지, 카카오 API를 직접 호출해 구현할지 정해야 했다.

## 결정
Spring Security oauth2-client를 사용한다. `application.yaml`에 카카오 registration과 provider(인가·토큰·사용자 정보 URI)를 적고, `SecurityConfig`에서 `oauth2Login()`을 켠다. 클라이언트 ID와 시크릿은 환경변수(`KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`)로 주입한다.

## 이유
- state 생성·검증, 토큰 교환, 사용자 정보 조회를 프레임워크가 처리해 인증 흐름의 구현 실수 가능성이 줄어듦
- `spring-boot-starter-security-oauth2-client`가 이미 의존성에 포함되어 있어 새 기술 추가(024)에 해당하지 않음
- 설정 항목(client-id, redirect-uri, scope, provider URI)이 인가 코드 방식의 각 단계와 1:1로 대응되어, 개발자가 설정을 근거로 흐름을 설명할 수 있음

## 포기한 대안
- 카카오 API 직접 호출(RestClient 등): 흐름이 코드에 그대로 보임, 프레임워크 내부를 몰라도 됨 / state 생성·검증, 토큰 요청, 에러 처리를 직접 구현해야 하고 보안 실수 가능성이 큼. 포기

## 결과
프레임워크 내부 동작이 코드에 드러나지 않으므로, 개발자가 인가 코드 방식 6단계(인가 요청, 카카오 로그인·동의, 코드 전달, 토큰 요청, 사용자 정보 조회, 회원 처리)를 설정과 연결해 설명할 수 있어야 함 (감수). 카카오는 Spring 내장 provider가 아니라 URI를 yaml에 직접 적어야 함 (감수).
nginx 뒤에서 동작하므로 `nginx.conf`에 `/oauth2/`, `/login/oauth2/` 경로 전달을 추가하고, `server.forward-headers-strategy: framework`를 설정한다.
이 결정을 번복하려면 먼저 개발자와 논의 필요. 에이전트는 oauth2-client 대신 직접 호출 구현을 먼저 제안하지 않는다. 관련: 024.
