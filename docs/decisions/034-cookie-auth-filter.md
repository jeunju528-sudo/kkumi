# 034. 쿠키 인증 필터: 잘못된 토큰은 막지 않고 인증 없이 통과

## 상태
확정 (2026-10-13)

## 배경
로그인 성공 핸들러(033)가 `accessToken` 쿠키를 내려주므로, 이후 요청에서 쿠키의 JWT를 읽어 누구의 요청인지 등록하는 필터가 필요했다. 쿠키가 없거나 토큰이 잘못된 요청을 필터가 직접 막을지 정해야 했다.

## 결정
- `global/security/CookieAuthenticationFilter`(`OncePerRequestFilter`)가 `accessToken` 쿠키만 읽는다. `signupToken`은 읽지 않는다
- 토큰이 유효하면 `SecurityContextHolder`에 memberId(`Long`)를 principal로 등록한다. 권한 목록은 비워 둔다
- 쿠키가 없거나 토큰이 잘못·만료된 경우는 예외를 내지 않고 인증 없이 다음 필터로 넘긴다. 접근 제한은 `SecurityConfig`의 인증 규칙이 맡는다
- 필터에 `@Component`를 붙이지 않고 `SecurityConfig`에서 직접 생성해 `UsernamePasswordAuthenticationFilter` 앞에 등록한다

## 이유
- 로그인 주소처럼 누구나 접근하는 경로가 만료된 쿠키 하나 때문에 막히면 안 된다
- 인증(누구인가)과 인가(들어가도 되는가)를 나누면 필터는 등록만 하고, 막는 규칙은 한곳(`SecurityConfig`)에서 관리된다
- `@Component` 필터는 서블릿 컨테이너에도 따로 등록되어 요청마다 두 번 실행될 수 있다
- 권한(role) 구분이 아직 필요 없다

## 포기한 대안
- 잘못된 토큰이면 필터에서 401 응답: 막는 위치가 분산되고 공개 경로까지 영향받음. 포기
- `AuthenticationProvider` 구조(Spring Security 표준 인증 흐름): 확장성은 좋지만 지금 규모에는 과함. 포기
- `@Component` 필터 등록: 코드는 짧지만 이중 실행 위험. 포기

## 결과
- 인증 규칙(`anyRequest().authenticated()` 등)을 걸기 전까지는 이 필터가 있어도 모든 요청이 허용된다
- 권한이 필요해지면(예: 관리자) authorities를 채우도록 필터를 확장한다
- 잘못된 토큰이 인증 실패로 보이지 않고 비로그인으로 보이므로, 인증 규칙이 적용된 뒤 응답은 401이 된다
이 결정을 번복하려면 먼저 개발자와 논의 필요. 에이전트는 필터에서 직접 접근을 막거나 필터를 `@Component`로 만드는 방식을 먼저 제안하지 않는다. 관련: 031, 033.
