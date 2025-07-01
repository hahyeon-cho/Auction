# 물품 서비스 API 목록
52panda 플랫폼의 물품 서비스에서 제공하는 API 목록입니다.
- Base URL: `/api/v1`
- 공통 응답 구조: `{ success, data, error }`  
※ 아래 예시에서는 data 필드의 내부 값만 표시합니다.

---

## 물품
### 인기 물품 목록 조회
GET /no-auth/hot-item  
파라미터:
- regionName: 사용자의 현재 지역 (선택)
```Json
res body
{
  "redisItemDtos": [
    {
      "itemId": "Number",
      "itemTitle": "String",
      "category": "String",
      "thumbnail": "String",
      "startPrice": 10000,
      "buyNowPrice": 150000
    },
    ...
  ]
}
```
  
### 신규 물품 목록 조회
GET /no-auth/new-item  
파라미터:
- regionName: 사용자의 현재 지역 (선택)
```
res body
// 응답 형식은 '인기 물품 목록 조회' API와 동일합니다.
```
  
### 물품 등록
POST /auth/auction/form
```Json
req body
{
  "title": "String",
  "contents": "String",
  "category": "String",
  "tradingMethod": "Number",    // 거래 방식 코드
  "region": "String",
  "startPrice": 10000,
  "buyNowPrice": 150000,    // (선택)
  "finishTime": "0000-00-00T00:00:00"
}
```
  
### 물품 검색
GET /no-auth/auction  
파라미터:
- pageable: 페이지 정보 (기본 20개씩)
- keyword: 검색 키워드 (선택)
- category: 카테고리 (선택)
- tradingMethod: 거래 방식 코드 (선택)
- region: 물품 등록 지역 (선택)
- status: 경매 종료 여부
```Json
res body
{
  "content": [
    {
      "itemId": "Number",
      "title": "String",
      "thumbnailUrl": "String",
      "startPrice": "Number",
      "currentPrice": "Number",
      "region": "String",
      "tradingMethod": "Number",
      "isAuctionComplete": "Boolean",
      "finishTime": "0000-00-00T00:00:00"
    }
    ...
  ],
  "pageable": {
    "pageNumber": "Number",
    "pageSize": 20
  }
}
```
  
### 물품 상세 정보 조회
GET /no-auth/auction/{itemId}
```Json
res body
{
  "itemId": "Number",
  "isAuctionComplete": "Boolean",
  "itemCreatedAt": "0000-00-00T00:00:00",
  "sellerId": "Number",
  "nickname": "String",
  "categoryName": "String",
  "tmCode": "Number",   // 거래 방식 코드
  "location": "String",
  "itemTitle": "String",
  "startPrice": 100000,
  "buyNowPrice": 150000,
  "maxPrice": 120000,   // 현재 최고 입찰가
  "bidFinishTime": "0000-00-00T00:00:00",
  "itemDetailContent": "String",
  "images": [
    {
      "url": "String"
    }
    ...
  ],
  "questions": [
    {
      "question": "String",
      "answer": "String"
    }
    ...
  ]
}
```

## 찜
### 찜 추가
POST /auth/auction/{itemid}/like
```
req body
// Void (요청 본문 필요 없음)
```

### 찜 제거
DELETE /auth/auction/{itemid}/like
```
req body
// Void (요청 본문 필요 없음)
```

## 문의
### 문의글 등록
POST /auth/auction/{itemId}/question
```Json
req body
{
    "questionContent": "String"
}
```
  
### 문의글 삭제
DELETE /auth/auction/{itemId}/question/{questionId}
```
req body
// Void (요청 본문 필요 없음)
```

### 문의답글 등록
POST /auth/auction/{itemId}/qna/{questionId}
```Json
req body
{
    "answerContent": "String"
}
```
  
### 문의답글 삭제
DELETE /auth/auction/{itemId}/qna/{questionId}/{answerId}
```
req body
// Void (요청 본문 필요 없음)
```

## 마이페이지
### 찜한 물품 목록 조회
GET /auth/mypage/like
```Json
req body
{
  "content": [
    {
      "itemId": "Number",
      "title": "String",
      "thumbnailUrl": "String",
      "startPrice": "Number",
      "currentPrice": "Number",
      "region": "String",
      "tradingMethod": "Number",
      "isAuctionComplete": "Boolean",
      "finishTime": "0000-00-00T00:00:00"
    }
    ...
  ],
  "pageable": {
    "pageNumber": "Number",
    "pageSize": 20
  }
}
```

### 등록한 물품 목록 조회
GET /auth/mypage/auction
```
res body
// 응답 형식은 '찜한 물품 목록 조회' API와 동일합니다.
```

### 입찰 참여한 물품 목록 조회
GET /auth/mypage/bid
```
res body
// 응답 형식은 '찜한 물품 목록 조회' API와 동일합니다.
```

### 사용자가 낙찰한 물품 목록 조회
GET /auth/mypage/award
```
res body
// 응답 형식은 '찜한 물품 목록 조회' API와 동일합니다.
```
