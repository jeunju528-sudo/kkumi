package com.kkumi.trade.dto;

import com.kkumi.trade.TradeHistory;
import java.util.List;
import org.springframework.data.domain.Slice;

// 거래내역 한 묶음 (전체 건수 없이 다음 여부만)
public record TradeHistoryPageResponse(List<TradeHistoryResponse> items, int page, boolean hasNext) {

    public static TradeHistoryPageResponse from(Slice<TradeHistory> slice) {
        return new TradeHistoryPageResponse(slice.map(TradeHistoryResponse::from).getContent(),
            slice.getNumber(), slice.hasNext());
    }
}
