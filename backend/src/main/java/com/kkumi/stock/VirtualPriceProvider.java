package com.kkumi.stock;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class VirtualPriceProvider implements StockPriceProvider {

    // 종목 하나의 상태
    /*
     * basePrice : 주식의 상장가 => 값 안바뀜
     * dayOpenPrice : 오늘 주식 시작가 => 매일 바뀜
     * rawPrice : 5초뒤 가격의 raw 가격 (반올림 전 double) => 5초마다 바뀜
     * price : 화면 가격 = 체결가 => 5초마다 바뀜
     * */
    // 이 상태는 해당 클래스 안에서만 사용하기 때문에 DTO로 따로 정의안하고
    // 이 엔진 내부에서만 사용하는 용으로 정의한 것
    // record는 불변이라 갱신할 때 아예 새로만들어서 교체하는 방식과 잘 맞음
    // 중간에 값이 바뀌고 있는 상태를 볼 수 있지 않음
    // record는 생성자,getter, equals를 자바가 내부적으로 자동으로 만들어줌
    private record PriceState(StockType type, long basePrice, long dayOpenPrice, double rawPrice, long price) {
        // long : 사람이 보거나 가지는 금액은 원 단위 금액
        // double : 계산 중간에 쓰이는 비율, 확률 모델 값은 double
        // 정수 / 정수는 0이 나올 수 있기 때문에 계산에 쓰는 값은 double로 세팅하는 것
    }

    private final VirtualPriceProperties properties;

    private final StockRepository stockRepository;

    private final StockPriceSnapshotService snapshotService;

    private final Random random = new Random();
    private final Map<String, PriceState> states = new ConcurrentHashMap<>();

    // ZoneId : 세계 시간 중에 어느 지역을 콕 찝을 때 사용
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 오늘 시작가의 날짜 (기동 쓰레드가 쓰고 스케쥴러 스레드가 읽음)
    private volatile LocalDate today;

    // 서버준비가 완전히 다 끝났을 때(ApplicationRunner 도 다 돌은 후에)
    // 스프링이 보내는 신호를 받음
    @EventListener(ApplicationReadyEvent.class)
    public void loadStocks() {
        today = LocalDate.now(KST);
        int restored = 0;
        for (Stock stock : stockRepository.findAll()) {
            states.put(stock.getCode(), restore(stock));
            if (stock.getCurrentPrice() != null) {
                restored++;
            }
        }
        log.info("가상 시세 시작: {}개 종목 ", states.size());
        log.info("저장 시세 복구: {}개 종목, 기준일 {}", restored, today);
    }

    // DB 저장본으로 상태 복구
    private PriceState restore(Stock stock) {
        long base = stock.getBasePrice();
        if (stock.getCurrentPrice() == null) {
            return new PriceState(stock.getType(), base, base, base, base);
        }
        long current = stock.getCurrentPrice();
        long dayOpen = today.equals(stock.getPriceDate()) ? stock.getDayOpenPrice() : current;
        return new PriceState(stock.getType(), base, dayOpen, current, current);
    }

    @Scheduled(fixedRateString = "${virtual-price.tick-interval}")
    // 모든 종목에 대해서 한 번만 tick 함수를 실행해서 나온 값이라
    // 모든 종목이 같은 장 분위기를 씀
    public void tick() {
        rollOverDayIfNeeded();
        // nextGaussian() : Random이 주는 랜덤 숫자 뽑기 중 하나로 
        // 정규분포(종모양)에서 숫자를 뽑음. 평균은 0, 표준편차는 1
        // 작은 랜덤 요인이 많이 합쳐지면 종 모양이 되기 때문에 게임이나 주가 모델에서 가장 기본으로 쓰는 가정
        // 대부분은 조금, 가끔 크게 움직인다는 느낌을 만들어 줄수 있음
        // nextDouble은 0~1 사이에서 모든 값이 똑같은 확률로 나옴(균등분포)
        double market = random.nextGaussian();
        /*
        for(Map.Entry<String, PriceState> entry : states.entrySet()){
            String code = entry.getKey();
            PriceState state = entry.getValue();
            states.put(code, next(state, market));
        }
        */
        // states의 code의 state값을 next(state, market) 값으로 바꿔라
        states.replaceAll((code, state) -> next(state, market));
    }

    // 날짜(KST 기준)가 바뀌면 오늘 시작가를 지금 가격으로 변경
    private void rollOverDayIfNeeded() {
        LocalDate now = LocalDate.now(KST);
        if (now.equals(today)) {
            return;
        }
        states.replaceAll((code, state) -> new PriceState(
                state.type(), state.basePrice(), state.price(), state.rawPrice(), state.price()));
        today = now;
        log.info("오늘 시작가 갱신: {}", now);
    }


    // 1분마다 메모리 시세를 DB에 저장
    // 기동 직후에 바로 저장하지 않고 1분 뒤에 저장
    @Scheduled(fixedRate = 60_000L, initialDelay = 60_000)
    public void saveSnapshot() {
        LocalDate date = today;
        Map<String, PriceSnapshot> snapshots = new HashMap<>();
        states.forEach((code, state) ->
                snapshots.put(code, new PriceSnapshot(state.price(), state.dayOpenPrice(), date)));
        snapshotService.savePrices(snapshots);
        log.info("시세 저장: {}개 종목", snapshots.size());
    }

    // 서버 종료 직전 마지막 저장(배포 재시작 때 손실 방지)
    @EventListener(ContextClosedEvent.class)
    public void saveOnShutdown() {
        saveSnapshot();
    }

    @Override
    public long getCurrentPrice(String code) {
        PriceState state = states.get(code);
        if (state == null) {
            throw new BusinessException(ErrorCode.STOCK_NOT_FOUND);
        }
        return state.price();
    }

    // 종목 하나의 5초 뒤 상태를 만드는 메서드
    private PriceState next(PriceState state, double market) {
        // 1. 랜덤 흔들림 만들기
        // 타입별 하루 변동성 / √(하루 갱신 횟수) = 5초에 보통 움직이는 크기
        // 랜덤 변화는 서로 상쇄돼서 횟수가 아니라 √횟수로 나눔
        double tickVolatility = properties.volatilityOf(state.type()) / Math.sqrt(properties.ticksPerDay());
        // √0.3 * 시장 분위기 랜덤 + √0.7 * 종목 고유 랜덤
        // 제곱근을 곱해야 섞어도 하루 변동성이 정한 값 그대로 유지됨
        double shock = Math.sqrt(properties.marketWeight()) * market
                + Math.sqrt(1 - properties.marketWeight()) * random.nextGaussian();
        // 이번 5초에 변화될 총량
        double randomReturn = tickVolatility * shock;

        // 2. 평균 회귀
        // 바깥 온도가 차면 지금 커피온도랑 바깥온도의 차이가 큰 만큼 빨리 식고
        // 차이가 작아지면 온도가 천천히 식음
        // 매 순간, 남은 차이의 일정 비율만큼 식는것
        // => 가격이 시작가에서 멀어진 만큼(차이)의 비율을 5초마다 시작가 쪽으로 되돌림
        // 예를 들어 5초에 0.008%씩 8640(=17280번/2, 하루에 반 12시간)번 쌓이면 차이가 정확히 절반이 됨 = 반감기 0.5일

        // 5초마다 되돌릴 비율. 8,640번(12시간) 쌓이면 차이가 절반 (반감기 0.5일)
        // Math.log(2) : 매번 차이의 f% 씩 줄일 때 8640번 뒤에 딱 절반이 되는 f를 구하는 공식
        // log(2) / 8640
        // log(2) = X2 여서 거꾸로 쓰면 /2 가 됨
        double pullPerTick = Math.log(2) / properties.halfLifeDays() / properties.ticksPerDay();

        // 이번 5초에 되돌릴양 = -되돌리는 비율 * 지금의 차이(현재금액/시작가) %
        // 260000 / 248000 = +4.8% -> log 값 0.0473 정도
        // -0.0000802 * 0.0473 = -0.0000038 시작가보다 금액이 올랐으니까 시작가 쪽으로 아래로 아주 조금 땅기는 것
        double reversion = -pullPerTick * Math.log(state.rawPrice() / state.basePrice());

        // 3. 이번 5초 동안의 새 가격
        double raw = state.rawPrice() * Math.exp(randomReturn + reversion + jackpotJump(state.type()));

        // 4. +-30% 안으로 맞추기
        long upper = PriceRules.upperLimit(state.dayOpenPrice());
        long lower = PriceRules.lowerLimit(state.dayOpenPrice());
        // 상한보다 크면 상한으로 내리고
        // 하한보다 작으면 하한으로 올림
        raw = Math.max(lower, Math.min(upper, raw));

        // 5. 새 상태 만들어서 돌려줌
        return new PriceState(state.type(), state.basePrice(), state.dayOpenPrice(), raw, PriceRules.roundToTick(raw));
    }

    // 5초 안에 급등락이 일어나면 그 크기를, 아니면 0을 돌려주는 함수
    private double jackpotJump(StockType type) {
        // 잭팟 타입아니면 급등락 X
        if (type != StockType.JACKPOT) {
            return 0;
        }

        // 급등락 일어날 확률은 하루에 0.3 번인데 하루에는 17280번 값이 바뀌기 때문에
        // 5초 한 번에 일어날 확률은 0.3 / 17280 = 0.0000174
        // nextDouble은 0~1까지 숫자가 나오는 균등분포라서
        // 0.0000174보다 작게 나올 확률도 0.0000174 임
        // 그니까 대부분의 경우는 0이 나옴(꽝~)
        if (random.nextDouble() >= properties.jackpot().eventsPerDay() / properties.ticksPerDay()) {
            return 0;
        }
        // 15~40% 사이에서 균등하게 뽑음
        // 최솟값 + 랜덤 * (최댓값 - 최솟값) : 원하는 구간에서 균등하게 뽑는 기본 공식
        // 최댓값(0.40) - 최솟값(0.15) : 구간 폭
        // nextDouble() : 0 ----------- 1
        // 구간 폭       : 0 ----------- 0.25
        // +0.15 해서 맞추기: 0.15 ---------- 0.40
        // 0.15[0.15 + 0 * 0.25] ~ 0.40[0.15 + 1 * 0.25]
        // 이 사이의 어떠한 값이 나오게 되어있음
        double size = properties.jackpot().minSize() + random.nextDouble() * (properties.jackpot().maxSize() - properties.jackpot().minSize());
        // 0 또는 1 동전던기지로 오를지 내릴지를 고름
        double direction = random.nextBoolean() ? 1 : -1;
        // +40% 하고서 -40% 하는 것 -> (실제) * 1.4 * 0.6 = 0.84 즉 -16%가 됨
        // 그래서 퍼센트 끼리는 더하거나 빼거나 하면 안되는 것
        // 곱하기를 더하기로 바꿔주는 도구는 log와 exp 가 있음
        // 여러나라 돈은 함부로 더하거나 빼거나 할 수 없어서
        // 하나의 돈으로 맞춰주고(공통 통화 - log) 그 다음 계산하고
        // 다시 원래 통화(배수) 로 되돌려야 함 (exp = e^)
        return Math.log(1 + direction * size);
        // 1 + -1 * 0.20 = 1 - 0.2 = 0.8 => 가격이 떨어지는 것
        // 1 + 1 * 0.30 = 1 + 0.3 = 1.3 => 가격이 오르는 것
    }
}
