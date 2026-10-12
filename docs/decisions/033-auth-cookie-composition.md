# 033. 인증 쿠키 구성: 쿠키 2개 분리, Path 제한, Secure는 설정값

## 상태
확정 (2026-10-12)

## 배경
로그인 성공 핸들러에서 JWT를 HttpOnly 쿠키로 내려야 한다(031). 정식 토큰과 임시 토큰을 어떤 쿠키로, 어떤 속성으로 내릴지와 설정값을 어디에 둘지 정해야 했다.

## 결정
- 쿠키를 2개로 나눈다. `accessToken`(정식 토큰, Path `/`)과 `signupToken`(임시 토큰, Path `/api/members/seed`)
- 공통 속성: HttpOnly, `SameSite=Lax`(코드 고정), Max-Age는 토큰 만료와 같게(정식 3시간, 임시 10분)
- `Secure`는 설정값 `auth.cookie.secure`로 두고, 지금은 false. HTTPS 전환 후 true
- 쿠키 설정은 `JwtProperties`를 고치지 않고 `AuthCookieProperties`를 따로 둔다. 쿠키 생성은 `global/jwt/AuthCookie`가 맡는다

## 이유
- 임시 토큰은 시드 선택(`POST /api/members/seed`) 한 곳에서만 쓰므로 Path를 그 주소로 좁히면 다른 요청에는 실리지 않아 노출 범위가 줄어든다
- 두 토큰을 한 쿠키로 합치면 어느 종류인지 매번 구분해야 하고, 정식 토큰이 발급된 뒤 임시 토큰을 따로 지우기 어렵다
- `Secure`는 http 접속에서 브라우저가 쿠키를 보내지 않으므로(031) 환경별로 바꿀 수 있어야 한다
- `JwtProperties`는 `JwtProviderTest`가 직접 생성하고 있어 필드를 늘리면 기존 테스트가 깨진다

## 포기한 대안
- 쿠키 1개에 두 종류 토큰: 단순함 / 구분과 교체가 번거롭고 임시 토큰이 모든 요청에 실림. 포기
- `Secure`를 코드에 true로 고정: 설정이 없음 / 지금 http 환경에서 로그인이 안 됨. 포기
- `JwtProperties`에 쿠키 설정 추가: 파일이 하나 줄어듦 / 기존 테스트 수정 필요. 포기

## 결과
- 시드 선택 API 경로를 바꾸면 `AuthCookie`의 Path 상수도 같이 바꿔야 한다
- 로그인 성공 후 이동은 기존 회원 `/home`, 신규 회원 `/seed`
- HTTPS 전환 시 `auth.cookie.secure`를 true로 바꾼다 (EC2 설정 확인 필요)
이 결정을 번복하려면 먼저 개발자와 논의 필요. 에이전트는 두 토큰을 한 쿠키로 합치거나 토큰을 프론트 저장소에 두는 방식을 먼저 제안하지 않는다. 관련: 030, 031.
