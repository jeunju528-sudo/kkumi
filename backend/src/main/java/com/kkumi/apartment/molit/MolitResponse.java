package com.kkumi.apartment.molit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MolitResponse(Response response) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(Header header, Body body) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(String resultCode, String resultMsg) {

    }

    // items나 item 이 null 이면 빈 목록을 돌려줌, 호출하는 쪽에서 null검사 필요없음
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(Items items, int numOfRows, int pageNo, int totalCount) {
        public List<MolitDealItem> deals() {
            if (items == null || items.item() == null) {
                return List.of();
            }
            return items.item();
        }
    }

    // ACCEPT_SINGLE_VALUE_AS_ARRAY : 1건일 때 "item":{...} 로 응답오는 객체를
    // 원소 1개짜리 목록으로 받음
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(
            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            List<MolitDealItem> item
    ) {

    }
}
