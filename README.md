![header](https://capsule-render.vercel.app/api?type=waving&&height=200&text=물품%20서비스&fontAlign=80&fontAlignY=40&color=gradient&customColorList=23)

## 📎 관련 문서
- [📄 물품 서비스 API 문서](./docs/api-summary.md)
- <a href="https://hahyeon-cho.notion.site/52panda-1f0a0fd714508061b9e5c86faad933ee?pvs=74">
  <img src="https://upload.wikimedia.org/wikipedia/commons/4/45/Notion_app_logo.png" alt="Notion" width="18" height="18" style="vertical-align:middle;"/> 
  Notion 포트폴리오 </a>
<br>


## ◾목차
- 개요
- 서비스 구조
- 주요 API
- 기능 상세
- 트러블 슈팅 및 성능 개선
<br>


## ◾개요
중고 물품 등록, 검색, 개인화된 물품 목록 제공 등 물품에 관한 처리 기능을 담당하는 서비스입니다.

- **물품 등록 및 관리**
- **물품 상세 기능**:
  - 물품 상세 정보 조회
  - 찜 등록/삭제
  - **문의글 및 답변 관리**:
    - 문의글 등록/삭제
    - 문의 답변 등록/삭제
- **물품 목록 조회**
  - **메인 페이지**:
    - 지역 내 인기 물품 목록 조회
    - 지역 내 신규 등록 물품 목록 조회
  - **물품 목록 페이지**: 물품에 대한 키워드 검색 및 필터링
  - **마이페이지**: 특정 사용자가 등록/입찰/낙찰/찜한 물품 목록 조회
- **추천 서비스 연동**: 추천 서비스와 연동하여 현재 물품과 유사한 물품 추천

※ 현재 알림 기능은 본 서비스에 통합되어 제공됩니다.
<br>


## ◾서비스 구조
물품 서비스의 물품 등록 처리 및 물품 검색 흐름은 다음과 같습니다.

**※ 물품 등록**  
<img src="https://github.com/user-attachments/assets/52fcf299-4c55-4f74-9c3b-ca498ba768bb" alt="Register Sequence" width="95%" height="95%"/> 
<br>
<br>
<br>

**※ 물품 검색**  
<img src="https://github.com/user-attachments/assets/862a5d9e-81c5-43e5-b3db-c3d9f25af747" alt="Search Flow" width="68%" height="68%"/> 
<br>
<br>

## ◾주요 API 
| 구분        | 메서드   | 경로                                                | 설명               |
|------------|--------|----------------------------------------------------|-------------------|
| 물품        | POST   | /auth/auction/form                                 | 물품 등록           |
|            | GET    | /no-auth/auction                                   | 물품 검색           |
|            | GET    | /no-auth/hot-item                                  | 인기 물품 목록 조회    |
|            | GET    | /no-auth/new-item                                  | 신규 물품 목록 조회    |
|            | GET    | /no-auth/auction/{itemId}                          | 물품 상세 조회        |
| 찜 기능      | POST   | /auth/auction/{itemId}/like                        | 찜 추가             |
|            | DELETE | /auth/auction/{itemId}/like                        | 찜 제거             |
| 문의 기능    | POST   | /auth/auction/{itemId}/question                    | 문의글 등록           |
|            | DELETE | /auth/auction/{itemId}/question/{questionId}       | 문의글 삭제           |
|            | POST   | /auth/auction/{itemId}/qna/{questionId}            | 문의 답글 등록        |
|            | DELETE | /auth/auction/{itemId}/qna/{questionId}/{answerId} | 문의 답글 삭제        |
| 마이페이지    | GET    | /auth/mypage/like                                  | 찜한 물품 목록 조회    |
|            | GET    | /auth/mypage/auction                               | 등록한 물품 목록 조회   |
|            | GET    | /auth/mypage/bid                                   | 입찰 참여 목록 조회     |
|            | GET    | /auth/mypage/award                                 | 낙찰 받은 물품 목록 조회 |

> 자세한 내용은 [물품 서비스 API 문서](./docs/api-summary.md)에서 확인할 수 있습니다.
<br>

## ◾기능 상세
### ◎ Function 1: 물품 관리
<img src="https://github.com/user-attachments/assets/cfe1e590-38a1-438d-8a03-a4dbad7b2828" alt="AF1_01" width="70%" height="70%"/>
<img src="https://github.com/user-attachments/assets/fbeeadf9-3191-4c2c-b65b-60ea4242a7e2" alt="AF3_01" width="65%" height="65%"/>
<br>

- 물품 등록
- 물품 상세 정보 조회
- 찜 추가/제거
- 문의글 추가/제거
- 문의답글 추가/제거
<br>

### ◎ Function 2: 물품 목록 조회
<img src="https://github.com/user-attachments/assets/88c4cfd6-528e-4671-9551-e1bc2a59e549" alt="AF2_01" width="80%" height="80%"/> 
<br>
<img src="https://github.com/user-attachments/assets/504f8cf5-7751-410e-b67a-406150e31843" alt="AF2_02" width="85%" height="85%"/>

- 물품 검색
- 인기/신규 물품 목록 조회
- 개인화된 물품 목록 조회
<br>

### ◎ Function 3: 추천 서비스 연동
<br>
<img src="https://github.com/user-attachments/assets/5b20dfea-c118-4ee8-960f-52e56c1c55b3" alt="AF3_01" width="65%" height="65%"/> 
<br>
<br>

- 현재 물품과 유사한 물품 목록 조회 (추천 서비스와 연동)
<br>

## ◾트러블슈팅 및 성능 개선
> 본 프로젝트 전반에 걸친 주요 문제 해결 사례와 성능 개선 내역은  
[포트폴리오 페이지 내 '주요 문제 상황 및 해결'](https://hahyeon-cho.notion.site/52panda-1f0a0fd714508061b9e5c86faad933ee#1f0a0fd7145080c68c20c8f15f4415d3)에서 확인하실 수 있습니다.
