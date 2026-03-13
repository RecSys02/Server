# 어슬렁
![Image](https://github.com/user-attachments/assets/d89aadd7-2dc0-4a46-837a-514413b8e69b)


---

## Overview

어슬렁은 AI 추천 시스템과 대화형 인터페이스를 결합해 사용자가 쉽고 빠르게 여행 계획을 세울 수 있도록 돕는 서비스입니다.  
사용자는 지역, 기간, 취향 등의 정보를 입력하고, 추천 결과를 바탕으로 여행지를 탐색하거나 일정을 생성할 수 있습니다.  
백엔드는 추천 요청 처리, 사용자 및 플랜 관리, 인증/인가, 인기 랭킹 반영, AI 서버 연동을 담당합니다.

---

## Core Features

- AI 기반 여행지 추천
- 개인 맞춤 여행 일정 생성
- 자연어 기반 챗봇 인터페이스
- 사용자 계정 및 플랜 관리
- 좋아요 기반 인기 플랜 랭킹 제공

---

## Documentation

- Prototype  
  <img width="11154" height="6522" alt="Image" src="https://github.com/user-attachments/assets/ef17d775-ba8c-4249-922d-9f10d4ef96bf" />

- API Specification  
  [API 명세서 바로가기](https://www.notion.so/API-2f4136f77e8a80559f62f50d1aacc770?source=copy_link)

---

## Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring MVC
- Spring WebFlux
- Spring Security
- JPA Hibernate

### Database / Infra
- PostgreSQL
- Redis
- Apache Kafka
- Docker

### AI / Communication
- FastAPI
- REST API
- Kafka

---

## ERD
<img width="1482" height="1112" alt="Image" src="https://github.com/user-attachments/assets/33e90f9e-0074-4ea2-abd7-e66ccb7fa4b6" />


---

## Architecture
<img width="1069" height="789" alt="Image" src="https://github.com/user-attachments/assets/09d76720-c54a-444b-838f-78799c379be8" />


### Components

- Core Server  
  사용자, 플랜, 좋아요 등 핵심 비즈니스 로직과 DB 처리 담당

- Chat Server  
  추천 요청 처리, AI 서버 연동, 응답 조합 담당

- AI Server  
  사용자 입력을 바탕으로 추천 결과와 챗봇 응답 생성

- Redis  
  인기 플랜 랭킹, 사용자 컨텍스트 등 빠른 조회가 필요한 데이터를 관리

- Kafka  
  좋아요, 사용자 컨텍스트 변경 등 주요 이벤트를 비동기로 전달하고 후속 작업을 분리

---

## Backend Engineering Highlights

### 1. Non-Blocking 기반 추천 처리 구조 분리
- 기존에는 Core 서버가 AI 호출과 추천 상세 조회를 함께 처리
- 외부 응답 대기 동안 요청 스레드 점유가 길어져 동시 요청 증가 시 지연이 빠르게 누적
- 추천 처리 경로를 Spring WebFlux 기반 Chat Server로 분리
- 응답 조합 과정을 비동기 파이프라인으로 재구성
- 결과
  - AI 호출 대기 구간에서 스레드 점유를 줄여 동시 요청 처리에 유리한 구조로 개선
  - 추천 API 처리 경로를 Core 서버와 분리해 역할을 명확히 나눔

### 2. Bulk 조회 기반 추천 상세 조회 개선
- 기존에는 추천 결과로 반환된 장소 ID를 개별 조회
- 추천 개수에 비례해 DB 조회 수가 증가하는 구조
- 추천 ID 목록을 한 번에 조회하는 Bulk 방식으로 변경
- AI가 반환한 추천 순서를 유지할 수 있도록 응답 조합 로직 구성
- 결과
  - 요청 1건당 DB 조회 횟수를 줄여 조회 비용 절감
  - 추천 개수 증가에 따른 응답 지연과 DB 부하를 완화

### 3. Redis ZSET 기반 인기 플랜 랭킹 구현
- 인기 플랜 조회 시마다 DB에서 정렬과 집계를 수행하던 구조
- Redis Sorted Set에 점수를 반영해 랭킹을 정렬된 상태로 유지
- 조회 시 상위 planId를 Redis에서 조회하고 필요한 상세 정보만 DB에서 batch 조회
- 결과
  - 랭킹 조회 경로를 단순화해 Top-N 조회 성능 개선
  - 반복적인 DB 정렬 및 집계 부담을 줄여 조회 부하 완화

### 4. Kafka 기반 비동기 후속 처리 분리
- 좋아요, 사용자 컨텍스트 변경 등의 후속 작업이 요청 처리 경로와 강하게 결합되어 있었음
- Kafka 이벤트 기반 구조를 적용해 후속 작업을 비동기로 분리
- 랭킹 반영, 사용자 컨텍스트 갱신 등을 별도 처리 흐름으로 구성
- 결과
  - 요청-응답 경로를 단순화해 응답 지연을 줄이기 쉬운 구조로 개선
  - 후속 처리 로직의 결합도를 낮춰 확장성과 유지보수성 향상

### 5. 트랜잭션 커밋 이후 이벤트 발행으로 정합성 보장
- DB 변경이 롤백됐는데 이벤트가 먼저 발행되면 데이터 불일치 가능성 발생
- 트랜잭션이 실제로 커밋된 이후에만 Kafka 이벤트가 발행되도록 설계
- 비동기 처리 환경에서도 원본 데이터와 파생 데이터 간 정합성을 유지하도록 구성
- 결과
  - DB 상태와 이벤트 발행 간 불일치 가능성을 줄임
  - 비동기 후속 처리에서도 정합성을 더 안정적으로 보장

### 6. Spring Security와 JWT 기반 인증/인가 구현
- Spring Security 기반 인증/인가 구조 구성
- JWT Access Token과 Refresh Token 방식으로 로그인 흐름 구현
- Access Token은 응답 바디, Refresh Token은 쿠키로 분리
- 인증 필터, 예외 처리, 토큰 재발급 로직 분리
- 보호가 필요한 API를 인증 기반으로 일관되게 제어
- 결과
  - 로그인, 인증, 재발급 흐름을 명확히 분리해 보안 처리 일관성 확보
  - 인증이 필요한 API를 안정적으로 보호할 수 있는 구조 마련


---





## Project Structure

```text
backend/
├── api-gateway/       # 외부 클라이언트 요청 진입점, 라우팅 및 공통 인증 처리
├── core/              # 사용자, 플랜, 좋아요 등 핵심 비즈니스 로직과 DB 처리
├── chat-server/       # 추천 요청 처리, AI 서버 통신, 응답 조합
└── docker-compose.yml # Redis, Kafka, PostgreSQL 등 인프라 실행 설정
