package com.kkumi.global.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.time.Duration;

/*
* 키 : String : StringSerializer 가 UTF-8로 바로 변환 가능
* 값 : record는 메모리 속의 객체라 그대로는 보낼 수 없어서 JacksonJsonSerializer가 JSON 글자로 바꾼다음 UTF-8 바이트로 바꿈
* record 안의 String·Instant 필드에는 실제 값이 아니라 다른 객체가 있는 주소만 들어있고, 이 주소는 내 서버 메모리에서만 의미가 있음
* 받는 쪽에 객체 주소를 넘겨봐야 거기엔 아무것도 없음
* 그래서 주소를 따라가서 실제 값을 꺼내 정해진 순서와 모양으로 한 줄로 펼쳐 써야함(직렬화)
* 펼치는 방식에는 JSON, Avro, 자바 기본 직렬화등 다양한 방법이 있음
* JSON 방식은 사람이 읽을 수 있고 아무 언어나도 읽을 수 잇지만 크기가 크다는 단점이 있음
* Avro는 바이너리로 생겨서 작고 빠르지만 스키마 파일과 관리 도구가 필요하고 콘솔에선 안읽힘
* 자바 기본 직렬화는 받는 쪽도 자바여야되고 클래스가 바뀌면 깨지고, 보안 문제도 있음
* * 받는 쪽(컨슈머)는 바이트를 JSON 글자로 읽고 다시 객체로 만듦(역직렬화)
* kafka 메세지는 키, 값, 헤더 세칸으로 되어있음
* (보내는쪽)JacksonJsonSerializer는 기본으로 헤더에 TypeId 클래스명을 붙여서 보내고
* 받는 쪽에서는 이 클래스로 조립하면 되겠구나 함
* 우리는 타입 헤더를 꺼서(spring.json.add.type.headers: false) 메세지 약속을 자바 클래스 이름이 아니라 JSON 필드 이름으로 정함,
* 보내는 쪽과 받는 쪽이 서로의 코드 구조에 묶이지 않게 설정함
* */

@Configuration
public class KafkaTopicConfig {
    public static final String STOCK_PRICE_TICKED = "stock-price-ticked";

    // 앱 기동 때 KafkaAdmin(Spring Boot 자동 생성)이 NewTopic 빈을 찾아 브로커에 토픽 생성 요청, 이미 있으면 생성 안 함
    @Bean
    public NewTopic stockPriceTickedTopic() {
        return TopicBuilder.name(STOCK_PRICE_TICKED)
                .partitions(3)
                // 파티션 하나를 몇개의 브로커에 복사해 둘지 정하는 갯수, 브로커가 1개라서 1로 설정함
                .replicas(1)
                // retention : 메시지 보관 기간 1일. 세그먼트(로그 파일 묶음) 단위로 지움, 세그먼트는 기본으로 7일이 지나거나 1G가 차야 닫히고, 닫힌 파일만 삭제 대상이 됨
                // RabbitMQ와 다른점은 큐는 읽으면 지우지만 카프카는 안지움
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(1).toMillis()))
                // 기본값(7일)이면 우리 데이터 양으로는 7일이 지나야 세그먼트가 닫혀서 최대 약 8일 보관됨, 세그먼트를 1시간마다 닫는 설정 한 줄 추가함
                .config(TopicConfig.SEGMENT_MS_CONFIG, String.valueOf(Duration.ofHours(1).toMillis()))
                .build();
    }
}
