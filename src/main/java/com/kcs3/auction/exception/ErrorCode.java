package com.kcs3.auction.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 400 - Bad Request
    // 일반 요청 오류
    MISSING_REQUEST_PARAMETER(40000, HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
    MISSING_REQUEST_HEADER(40001, HttpStatus.BAD_REQUEST, "필수 요청 헤더가 누락되었습니다."),
    INVALID_ARGUMENT(40002, HttpStatus.BAD_REQUEST, "요청에 유효하지 않은 인자입니다."),
    INVALID_PARAMETER_FORMAT(40003, HttpStatus.BAD_REQUEST, "요청에 유효하지 않은 인자 형식입니다."),
    INVALID_HEADER(40004, HttpStatus.BAD_REQUEST, "유효하지 않은 헤더입니다."),
    BAD_REQUEST_PARAMETER(40005, HttpStatus.BAD_REQUEST, "잘못된 요청 파라미터입니다."),
    BAD_REQUEST_JSON(40006, HttpStatus.BAD_REQUEST, "잘못된 JSON 형식입니다."),
    SEARCH_KEYWORD_TOO_SHORT(40007, HttpStatus.BAD_REQUEST, "검색어는 2글자 이상이어야 합니다."),

    // 입찰 관련 요청 오류
    BID_PRICE_TOO_LOW(40008, HttpStatus.BAD_REQUEST, "입찰 가격이 현재 최고 입찰가보다 높아야 합니다."),
    BIDDER_IS_CURRENT_HIGHEST(40009, HttpStatus.BAD_REQUEST, "현재 최고 입찰자와 같은 사용자입니다."),
    BIDDER_IS_SELLER(40010, HttpStatus.BAD_REQUEST, "물품 판매자와 같은 사용자입니다."),
    ITEM_BID_FIELD_MISMATCH(40011, HttpStatus.BAD_REQUEST, "경매 아이템 입찰자 정보 필드에 일관성 문제가 발생했습니다."),

    // 401 - Unauthorized
    LOGIN_FAILED(40100, HttpStatus.UNAUTHORIZED, "잘못된 아이디 또는 비밀번호입니다."),
    TOKEN_EXPIRED(40101, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_INVALID(40102, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_MALFORMED(40103, HttpStatus.UNAUTHORIZED, "토큰이 올바르지 않습니다."),
    TOKEN_TYPE_INVALID(40104, HttpStatus.UNAUTHORIZED, "토큰 타입이 일치하지 않거나 비어있습니다."),
    TOKEN_UNSUPPORTED(40105, HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다."),
    TOKEN_GENERATION_FAILED(40106, HttpStatus.UNAUTHORIZED, "토큰 생성에 실패하였습니다."),
    TOKEN_UNKNOWN_ERROR(40107, HttpStatus.UNAUTHORIZED, "알 수 없는 토큰입니다."),

    // 403 - Forbidden
    ACCESS_DENIED(40300, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    AUTH_CODE_MISMATCH(40301, HttpStatus.FORBIDDEN, "인증 코드가 일치하지 않습니다."),
    USER_MISMATCH(40302, HttpStatus.FORBIDDEN, "사용자가 일치하지 않습니다."),
    PERMISSION_DENIED(40303, HttpStatus.FORBIDDEN, "권한이 없는 사용자입니다."),

    // 404 - Not Found
    ENDPOINT_NOT_FOUND(40400, HttpStatus.NOT_FOUND, "존재하지 않는 API 엔드포인트입니다."),
    NOT_FOUND_RESOURCE(40401, HttpStatus.NOT_FOUND, "해당 리소스가 존재하지 않습니다."),
    USER_NOT_FOUND(40402, HttpStatus.NOT_FOUND, "해당 사용자가 존재하지 않습니다."),
    LOGIN_USER_NOT_FOUND(40403, HttpStatus.NOT_FOUND, "로그인한 사용자가 존재하지 않습니다."),
    AUTH_HEADER_NOT_FOUND(40404, HttpStatus.NOT_FOUND, "Authorization 헤더가 존재하지 않습니다."),
    ITEM_NOT_FOUND(40405, HttpStatus.NOT_FOUND, "해당 경매 물품을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(40406, HttpStatus.NOT_FOUND, "해당 카테고리 정보를 찾을 수 없습니다."),
    TRADING_METHOD_NOT_FOUND(40407, HttpStatus.NOT_FOUND, "해당 거래 방식 정보를 찾을 수 없습니다."),
    DEFAULT_REGION_NOT_FOUND(40408, HttpStatus.NOT_FOUND, "기본값인 '기타' 지역이 존재하지 않습니다."),
    ITEM_DETAIL_NOT_FOUND(40409, HttpStatus.NOT_FOUND, "해당 경매 물품에 관한 상세 정보를 찾을 수 없습니다."),
    ITEM_QUESTION_NOT_FOUND(40410, HttpStatus.NOT_FOUND, "해당 문의글 정보를 찾을 수 없습니다."),
    ITEM_ANSWER_NOT_FOUND(40411, HttpStatus.NOT_FOUND, "해당 답글 정보를 찾을 수 없습니다."),
    ITEM_CACHE_NOT_FOUND(40412, HttpStatus.NOT_FOUND, "캐시에 저장된 경매 물품 리스트를 찾을 수 없습니다."),
    AUCTION_PRICE_NOT_FOUND(40413, HttpStatus.NOT_FOUND, "경매 가격 정보를 찾을 수 없습니다."),

    // 405 - Method Not Allowed
    METHOD_NOT_ALLOWED(40500, HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메소드입니다."),

    // 410 - Gone
    SHARED_URL_EXPIRED(41001, HttpStatus.GONE, "해당 공유 URL이 만료되었습니다."),

    // 500 - Internal Server Error
    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 에러입니다."),
    FILE_UPLOAD_FAILED(50001, HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패하였습니다."),
    SEARCH_EMBEDDING_FAILED(50002, HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI 임베딩 요청에 실패하였습니다."),
    ITEM_ELASTIC_SAVE_FAILED(50003, HttpStatus.INTERNAL_SERVER_ERROR, "물품 정보를 엘라스틱서치에 저장하던 중 오류가 발생하였습니다."),
    RECOMMEND_EMBEDDING_SAVE_FAILED(50004, HttpStatus.INTERNAL_SERVER_ERROR, "임베딩 값을 저장하는 중 오류가 발생하였습니다."),
    RECOMMEND_EMBEDDING_FAILED(50005, HttpStatus.INTERNAL_SERVER_ERROR, "대표 임베딩을 생성하는 외부 API 호출 중 오류가 발생하였습니다."),
    RECOMMENDATION_FAILED(50006, HttpStatus.INTERNAL_SERVER_ERROR, "추천 서버 통신에 실패했습니다."),

    // 503 - Service Unavailable
    REDIS_CONNECTION_FAILED(50301, HttpStatus.SERVICE_UNAVAILABLE, "레디스 서버에 연결할 수 없습니다.");


    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}
