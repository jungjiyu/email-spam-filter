# 🔥 Bloom Filter 기반 이메일 스팸 필터링 프로젝트🔥
- OAuth 2.0 인증을 통해 Gmail API와 통신하여 구한 사용자의 메시지 목록과 상세 정보를 대상으로, Bloom filter 자료구조를 활용하여 구현한 3가지 종류의 스팸 필터링 로직을 적용하는 프로젝트

# 🧩 아키텍처
![image](https://github.com/user-attachments/assets/71802405-7d40-4c4a-898a-25ed1debc276)

# ✅ Project Info
- 개발 도구 : `SpringBoot`, `Spring Security (OAuth 2.0 Client)`, `RestTemplate`,`MySQL`, `Google OAuth 2.0`, `Gmail API v1`

# 🥶주요 기능
### 1. OAuth 2.0 인증
- Google OAuth 2.0 인증을 사용하여 Gmail API와 안전하게 통신.
- Access Token을 발급받아 Gmail API 요청에 활용.

### 2. Gmail API 메시지 관리
- Gmail 메시지 ID 목록 가져오기: 사용자의 모든 메시지 ID를 조회.
- Gmail 메시지 상세 정보 가져오기: 메시지 ID를 사용해 각 메시지의 상세 정보를 조회.

### 3. RestTemplate 기반 API 요청
- Google API 엔드포인트와 통신하여 필요한 데이터 요청 및 처리.

# ❓Bloom Filter 를 활용한 스팸 필터링  구현

### 1. SPF(Sender Policy Framework) 검사
- SPF란? : 이메일 도메인 소유자가 자신이 사용하는 메일 서버 IP 정보를 DNS(SPF 레코드)에 미리 등록하고, 수신 측 메일 서버는 발신 IP가 해당 SPF 레코드에 있는지 확인하는 방식. 등록되어 있지 않다면 발신자가 위조된 도메인을 사용했을 가능성이 큼.
![image](https://github.com/user-attachments/assets/9791158e-04b8-43ee-8d9f-cd3ef1d93ff2)

- 구현 : 
    - 신뢰할 수 있는 발신 IP 목록을 Bloom Filter에 저장
    - 메일 수신 시, 발신 IP가 Bloom Filter에 존재하는지 빠르게 확인
    - Bloom Filter 조회 결과, 신뢰 목록에 없는 IP라면 => SPF 위조 가능성이 높다고 판단 (스팸 플래그 설정)
- 추가 확인: 실환경에서는 DNS 조회를 통해 최종 확정 가능

### 2. Received 헤더 기반 서버 경로 분석
- Received 헤더란? : 이메일이 거친 서버(IP 혹은 호스트) 정보를 기록한 헤더로, 여러 개의 Received 헤더가 순차적으로 쌓임. 가장 하단(첫 번째) Received 헤더가 실제 발신 서버(IP)를 나타내므로, 해당 정보를 통해 출발지를 확인 가능
![image](https://github.com/user-attachments/assets/c9ce8848-ef4d-4b00-ad4d-a9b7ef617f6f)

- 구현: 
    - 신뢰할 수 없는 서버 IP 목록을 Bloom Filter에 저장
    - 가장 하단의 Received 헤더에서 발신 서버 IP 추출
    - Bloom Filter 조회 결과, 불신 목록에 있는 IP라면 => 스팸일 가능성 높다고 판단 (스팸 플래그 설정)

### 3. 스팸 단어(Bloom Filter) 필터링
- “로또, 무료, 광고, 사은품” 등 스팸으로 자주 사용되는 단어를 빠르고 간편하게 조회
- 구현:
    - 스팸 의심 단어 목록을 Bloom Filter에 저장
    - 이메일 제목(또는 본문)에서 단어들을 추출하여 Bloom Filter 조회
    - 하나라도 등록된 스팸 단어가 발견되면 => 스팸일 가능성 높다고 판단 (스팸 플래그 설정)


# 📝 Commit Convention
- add : 새로운 기능 추가
- fix : 버그 수정
- docs : 문서 수정
- style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
- refactor : 코드 리펙토링
- test : 테스트 코드, 리펙토링 테스트 코드 추가
- chore : 빌드 업무 수정, 패키지 매니저 수정

