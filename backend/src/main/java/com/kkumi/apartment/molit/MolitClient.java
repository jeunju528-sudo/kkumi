package com.kkumi.apartment.molit;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.LogicalType;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// 국토부에 신고된 아파트 매매 계약 기록 조회 (저장은 배치 서비스)

// 시군구 1곳 + 계약연월 1개의 계약 목록을 배치로 받아옴
// 각 항목 : 계약1건(단지명, 법정동, 지번, 전용면적, 층, 가격(만원), 계약일, 해제여부 등
// 시군구에 신고된 계약이 0건인 경우 국토부에서 "items":"" 로 보내서 예외처리 필요

@Component
// 배치 서비스가 주입 받아서 사용
public class MolitClient {
    // 한 번에 받을 거래 수
    private static final int PAGE_SIZE = 1000;
    // 국토부 정상 응답코드
    private static final String RESULT_OK = "000";
    // YearMonth (2026-10)을 API가 원하는 202610 형태로 바꿈
    private static final DateTimeFormatter DEAL_YMD = DateTimeFormatter.ofPattern("yyyyMM");
    // 연결 타임아웃, 서버와 연결 자체가 안 될 때 기다림 5초
    private static final int CONNECT_TIMEOUT_MS = 5_000;
    // 읽기 타임아웃, 연결 후 응답이 안 올 때 기다림 30초
    private static final int READ_TIMEOUT_MS = 30_000;
    
    // Thread Safe : 여러 스레드가 한 객체를 동시에 써도 결과가 꼬이지 않는 다는 뜻
    // DateTimeFormatter, JsonMapper, RestClient 는 내부 상태가 안 바뀌어서 안전
    // 그래서 static final이나 필드로 1개만 만들어서 공유 가능
    
    // JSON 바이트를 MolitResponse record로 바꾸기 위해 사용
    // withCoercionConfig : 타입이 안 맞을 때 규칙 설정
    // 객체(POJO) 자리에 빈 문자열이 오면 null로 처리
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .withCoercionConfig(LogicalType.POJO,
                    config -> config.setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull))
            .build();

    // RestClient : HTTP 클라이언트 도구
    // HTTP 클라이언트 : 다른 서버에 HTTP 요청을 보내고 응답을 받는 프로그램
    // 브라우저 주소창에 URL 치고 결과를 받는 일을 자바 코드로 수행함
    // 평소에는 Spring 서버가 요청을 받는 서버쪽이지만, 
    // 배치에서는 우리 서버가 요청을 보내는 클라이언트가 됨
    // RestClient는 Spring이 제공하는 HTTP 클라이언트 도구
    private final RestClient restClient;
    private final MolitProperties properties;

    public MolitClient(MolitProperties properties){
        // 실제로 Http 연결을 열 때 사용
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.properties = properties;
    }
    
    // 한 시군구의 한 달 거래 전부를 가지고 옴
    public List<MolitDealItem> fetchDeals(String sigunguCode, YearMonth month){
        List<MolitDealItem> deals = new ArrayList<>();
        int pageNo = 1;
        while(true){
            MolitResponse.Body body = fetchPage(sigunguCode, month, pageNo);
            List<MolitDealItem> page = body.deals();
            deals.addAll(page);
            if(page.isEmpty() || deals.size() >= body.totalCount()) {
                return deals;
            }
            pageNo++;
        }
    }

    // 바이트 → MolitResponse 변환 + 결과 코드 검사
    static MolitResponse parse(byte[] raw) {
        MolitResponse response;
        try {
            // byte -> record 변환
            // *.class 메서드에 인자로 넘기는 자리에 작성, 타입은 그냥 넘길 수 없어서 타입 정보를 담은 class 객체로 만들어 넘기는 것
            // MolitResponse.class : MolitResponse 모양으로 만들어서 줘
            // byte[].class : 응답을 byte[] 로 줘
            response = MAPPER.readValue(raw, MolitResponse.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("국토부 응답 파싱 실패 (키 오류 시 XML 응답)", e);
        }

        String resultCode = response.response().header().resultCode();
        if(!RESULT_OK.equals(resultCode)){
            throw new IllegalStateException("국토부 응답 오류: " + resultCode + " "
                    + response.response().header().resultMsg());
        }
        return response;
    }

    // 페이지 1개 호출 후 body만 반환
    private MolitResponse.Body fetchPage(String sigunguCode, YearMonth month, int pageNo){
        // String으로 받으면 서버가 charset은 안 안알려줄 때 ISO-8859-1로 읽혀서 한글이 깨질 수 있음
        // byte[]로 주면 Jackson이 UTF-8로 직접 읽을 수 있음
        byte[] raw = restClient.get() // get 요청 생성
                .uri(properties.baseUrl()
                                + "?serviceKey={key}&LAWD_CD={code}&DEAL_YMD={ymd}&numOfRows={rows}&pageNo={page}&_type=json",
                        properties.serviceKey(), sigunguCode, month.format(DEAL_YMD), PAGE_SIZE, pageNo)
                .retrieve() // 요청을 보냄
                .body(byte[].class); // 응답 바디를 가공하지 않은 byte 그대로 받음
        return parse(raw).response().body();
    }
}
