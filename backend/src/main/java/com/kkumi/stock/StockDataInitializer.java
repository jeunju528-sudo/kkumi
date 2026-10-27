package com.kkumi.stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
// 서버 준비 끝난 직후 한 번 실행 (ApplicationRunner)
public class StockDataInitializer implements ApplicationRunner {
    
    // 값만 담는 용도로 record 사용, static entity는 save때 id가 채워짐
    private record StockSeed(String code, String name, StockType type, long basePrice) {

    }

    private static final List<StockSeed> SEEDS = List.of(
            new StockSeed("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L),
            new StockSeed("KK0002", "마르덴전자", StockType.STABLE, 248_000L),
            new StockSeed("KK0003", "쿠르텍", StockType.ROLLER_COASTER, 40_000L),
            new StockSeed("KK0004", "브론델배터리", StockType.ROLLER_COASTER, 187_000L),
            new StockSeed("KK0005", "다이온모터스", StockType.STABLE, 192_000L),
            new StockSeed("KK0006", "벨라전기차", StockType.ROLLER_COASTER, 241_000L),
            new StockSeed("KK0007", "도르빈바이오", StockType.JACKPOT, 331_000L),
            new StockSeed("KK0008", "소티제약", StockType.JACKPOT, 301_000L),
            new StockSeed("KK0009", "아젤뷰티", StockType.JACKPOT, 689_000L),
            new StockSeed("KK0010", "카르넬게임즈", StockType.ROLLER_COASTER, 247_000L),
            new StockSeed("KK0011", "트리아엔터", StockType.ROLLER_COASTER, 38_200L),
            new StockSeed("KK0012", "제노텔레콤", StockType.STABLE, 24_500L),
            new StockSeed("KK0013", "라미로보틱스", StockType.ROLLER_COASTER, 126_000L),
            new StockSeed("KK0014", "아르카홀딩스", StockType.STABLE, 8_730L),
            new StockSeed("KK0015", "유르파트너스", StockType.STABLE, 15_400L),
            new StockSeed("KK0016", "칼리건설", StockType.STABLE, 31_200L),
            new StockSeed("KK0017", "모르조선", StockType.ROLLER_COASTER, 87_300L),
            new StockSeed("KK0018", "펠로항공", StockType.JACKPOT, 19_800L),
            new StockSeed("KK0019", "시벨푸드", StockType.STABLE, 54_800L),
            new StockSeed("KK0020", "데니마트", StockType.STABLE, 12_950L)
    );

    private final StockRepository stockRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int added = 0;
        for (StockSeed seed : SEEDS) {
            // 서버 재시작 해도 있는 코드는 continue 하게해둬서 중복 종목 안생김
            if (stockRepository.findByCode(seed.code()).isPresent()) {
                continue;
            }
            stockRepository.save(Stock.of(seed.code(), seed.name(), seed.type(), seed.basePrice()));
            added++;
        }
        log.info("가상 종목 등록 : {} 개 추가", added);
    }
}
