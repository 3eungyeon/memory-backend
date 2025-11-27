# 🌿 MEMORY – Social Time Capsule Diary App (Backend)

> **Memory**는 일기, 타임캡슐, 친구 기능, 자동 감정 분석 기능을 결합한  
> 새로운 형태의 감성 SNS **소셜 타임캡슐 일기 앱**입니다.

이 저장소는 MEMORY 앱의 **Spring Boot Backend** 레포지토리입니다.  
JWT 인증 기반 REST API 서버로, 사용자 관리, 일기/타임캡슐 기능, 친구 기능, 감정 분석 API 연동,  
Redis 기반 캐싱 성능 최적화를 포함합니다.


---

# 🎯 프로젝트 목표

### ✔ 1) 소셜 타임캡슐 일기 앱 구현  
단순한 일기/타임캡슐 앱에서 벗어나  
친구 기능 + 감정 분석을 갖춘 **종합 감성 SNS** 개발.

### ✔ 2) 캐시 시스템 도입을 통한 성능 개선  
친구 목록과 감정 분석처럼 반복성 높은 기능에 Redis 캐시를 적용하여  
**최대 7.8배 성능 향상**을 목표로 함.  

---

# ✨ Memory의 주요 차별점  

기존 일기 앱/타임캡슐 앱과 달리, Memory는 **다음 두 가지 핵심 기능**을 제공합니다.

### 🌱 1) SNS형 친구 기능
- 친구 요청/수락 기능 제공  
- 타임캡슐과 일기를 친구와 공유 가능  
- 기존 타임캡슐 앱 대부분에서 제공하지 않는 기능

### 😊 2) 자동 감정 분석 기능
- Twinword API 기반  
- 6가지 감정(기쁨/분노/불안/혐오/슬픔/놀람) 분류  
- 기존 앱들은 감정을 **직접 입력**해야 하지만 Memory는 **AI가 자동 분석**

---

# 🛠 Tech Stack

| 분야 | 기술 |
|------|------|
| Framework | Spring Boot |
| Language | Java |
| Database | MySQL |
| Cache | Redis |
| Authentication | JWT |
| Emotion API | Twinword  |
| Client | Flutter |
| Build Tool | Gradle |

---

 
# 🏗 시스템 아키텍처  


| 구성 요소 | 기술 스택 / 역할 | 설명 |
|-----------|-------------------|-------|
| **Client** | Flutter | • 사용자 UI/UX <br>• JWT 저장 및 전송 <br>• 일기/타임캡슐/친구 기능 요청 |
| **Backend API Server** | Spring Boot (REST API) | • 전체 비즈니스 로직 처리 <br>• 사용자 인증(JWT) <br>• 일기/타임캡슐 CRUD <br>• 친구 요청/수락/목록 조회 <br>• 감정 분석 API 호출 <br>• Redis & MySQL 연동 |
| **Authentication** | JWT | • 로그인 시 JWT 발급 <br>• 모든 요청 헤더에 Authorization 포함 <br>• 토큰 유효성 검사 후 사용자 인증 |
| **Database** | MySQL | • 사용자 정보 저장 <br>• 일기/타임캡슐/친구 관계 저장 <br>• 감정 분석 결과 저장 |
| **Cache Server** | Redis | • 친구 목록 캐싱 <br>• 감정 분석 결과 캐싱 <br>• 반복 요청 API 속도 개선 (약 7.8배 향상) |
| **Emotion Analysis API** | Twinword API  | • 일기 내용 감정 분석 <br>• 6가지 감정 분류 |

---

# 🛠️ 기술 스택 (Tech Stack)

##  프론트엔드
- Flutter

##  백엔드
- Java  
- Spring Boot  
- Spring Boot Security (JWT)  
- Redis  
- Twinword Emotion API  

## DB
- MySQL
- Firebase Storage
