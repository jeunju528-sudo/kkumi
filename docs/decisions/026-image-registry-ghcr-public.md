# 026. 이미지 저장소: Docker Hub, EC2 직접 빌드 대신 GHCR(public)

## 상태
확정 (2026-10-06)

## 배경
CD에서 빌드한 Docker 이미지를 EC2로 전달하는 방식이 필요했음. 운영은 EC2 1대 + RDS이고 EC2는 t3.small급. CI/CD는 GitHub Actions를 쓴다 (001).

## 결정
GitHub Container Registry(GHCR)에 이미지를 올리고 EC2는 pull만 한다. 이미지는 public으로 둔다. 빌드와 push는 GitHub Actions에서 한다.

## 이유
- 빌드를 서버 밖(Actions)에서 하므로 EC2 메모리를 쓰지 않고, 소스와 빌드 도구가 서버에 남지 않음
- Actions의 `GITHUB_TOKEN`으로 push할 수 있어 별도 계정·토큰을 Secrets에 추가하지 않아도 됨
- public이면 EC2에서 로그인 없이 pull 가능. 레포가 public이고, 이미지에 비밀값이 없음 (DB 정보 등은 EC2 `.env`로 주입)

## 포기한 대안
- Docker Hub: 개발자가 써본 경험이 있음 / 별도 계정과 토큰을 따로 관리해야 함. 막히면 이미지 이름과 로그인 단계만 바꿔 전환 가능
- EC2에서 직접 빌드: 설정이 단순함 / 빌드 부하가 작은 서버에 걸리고, 소스·빌드 도구가 서버에 남고, 이전 버전으로 되돌리기 번거로움
- GHCR private: 이미지 비공개 / EC2에 읽기 토큰을 넣고 `docker login`하는 단계가 늘어남

## 결과
GHCR은 처음 써보는 도구라 설정 실수가 날 수 있음 (감수). 워크플로에 `permissions: packages: write`가 필요함. 이미지 이름은 `ghcr.io/jeunju528-sudo/` 아래 소문자로 쓴다.
이 결정을 번복하려면 먼저 개발자와 논의 필요. 에이전트는 이미지에 비밀값(DB 비밀번호, 카카오 시크릿, API 키)을 넣지 않고, 이미지를 private으로 바꾸거나 EC2 직접 빌드로 되돌리자고 먼저 제안하지 않는다. 관련: 001, 002.
