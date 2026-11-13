# 057. HTTPS: EC2 호스트 certbot(webroot) + nginx에서 TLS 종료

## 상태
확정 (2026-11-13)

## 배경
- 실서버가 `http://IP`로만 열려 있어 인증 쿠키를 `Secure`로 못 켜고(`auth.cookie.secure: false`), Chrome이 https로 먼저 시도해 타임아웃이 나는 문제가 있었음
- 11/13 결정: 도메인은 사지 않음(단기 운영). 무료 서브도메인 DuckDNS `kkumi.duckdns.org` → 탄력적 IP
- 조건: EC2 1대, 추가 비용 없음, 새 기술 추가 없음(기술 상한선)

## 결정
- 인증서: Let's Encrypt, EC2 호스트에 certbot(snap) 설치, webroot 방식(HTTP-01). 시험 파일 폴더 `/var/www/certbot`을 nginx 컨테이너에 읽기 전용 연결 (#73)
- 갱신: certbot 자동 갱신 타이머 + deploy 훅(`/etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh`)으로 `nginx -s reload`
- TLS 종료: 기존 nginx 컨테이너가 443 처리, backend까지는 도커 내부 HTTP. Spring은 `forward-headers-strategy: framework`로 `X-Forwarded-Proto` 사용 (`/ws`에도 헤더 추가)
- 80: 시험 파일 경로만 처리, 나머지는 `https://kkumi.duckdns.org`로 301 (IP 접속 포함)
- 운영 쿠키 `Secure=true` (compose `AUTH_COOKIE_SECURE`), 로컬은 `false` 유지
- `server_tokens off` (응답 헤더 버전 숨김)
- CI `nginx -t`는 일회용 자체 서명 인증서로 검사

## 이유
- webroot는 돌고 있는 nginx가 시험 파일을 서빙해서 발급·갱신 때 서비스 중단 없음
- 호스트 certbot은 snap 설치 시 갱신 타이머가 같이 등록돼 compose 서비스를 늘리지 않음
- 인증서 개인 키가 EC2 밖(CI, GitHub Secrets)으로 나가지 않음. `nginx -t`는 파일 존재·형식·키 짝만 확인해서 가짜 인증서로 충분

## 포기한 대안
- ALB + ACM: 인증서 관리를 AWS가 함 / 월 고정 비용, DuckDNS는 ACM DNS 검증용 CNAME 미지원
- Caddy: 인증서 자동 발급·갱신 내장 / 새 기술 추가(기술 상한선), 기존 nginx 설정 이전 필요
- certbot 컨테이너: 레포 안에서 설정 관리 / 갱신 루프용 서비스 추가, 갱신 후 nginx reload 연결이 더 복잡
- standalone 방식: 설정 단순 / certbot이 80번 포트를 직접 잡아 발급·갱신 때 nginx 중단
- HSTS: https 강제 / 도메인 변경·인증서 문제 시 브라우저가 사이트를 막고 되돌리기 어려움. 단기 운영이라 제외

## 결과
- 인증서가 EC2 호스트 디스크에 있어 EC2를 새로 만들면 재발급 필요
- 갱신이 호스트 타이머·deploy 훅에 의존. 발급 직후 `certbot renew --dry-run` 통과(2026-11-13), 만료 2027-02-11
- 서버가 2대 이상(무중단 배포, 런칭 후 과제 8)이 되면 ALB + ACM 재검토
- 이 결정을 번복하려면 먼저 개발자와 논의 필요. 에이전트는 CI나 레포에 실제 인증서·개인 키를 넣지 않는다.
