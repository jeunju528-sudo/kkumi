package com.kkumi.trade;

import com.kkumi.trade.dto.StockProfitResponse;
import com.kkumi.trade.dto.TradeHistoryPageResponse;
import com.kkumi.trade.dto.TradeHistorySummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 내 거래 화면 조회 전용 (매매 TradeService와 분리)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeHistoryService {

    // 최신순, 같은 시각이면 id로 순서 고정 (페이지 사이 중복·누락 방지)
    private static final Sort LATEST_FIRST = Sort.by(Sort.Order.desc("tradedAt"), Sort.Order.desc("id"));

    private final TradeHistoryRepository tradeHistoryRepository;

    // 거래내역
    public TradeHistoryPageResponse findHistories(Long memberId, Period period, TradeType type, int page, int size){
        LocalDateTime from = period.toFrom(LocalDateTime.now());
        Pageable pageable = PageRequest.of(page, size, LATEST_FIRST);
        return TradeHistoryPageResponse.from(tradeHistoryRepository.findHistorySlice(memberId, from, type, pageable));
    }

    // 기간 합계
    public TradeHistorySummaryResponse findSummary(Long memberId, Period period){
        return tradeHistoryRepository.findSummary(memberId, period.toFrom(LocalDateTime.now()));
    }

    // 종목별 실현손익
    public List<StockProfitResponse> findProfitByStock(Long memberId, Period period){
        return tradeHistoryRepository.findProfitByStock(memberId, period.toFrom(LocalDateTime.now()));
    }
}
