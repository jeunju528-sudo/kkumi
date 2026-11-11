package com.kkumi.apartment;

import com.kkumi.apartment.molit.MolitDealItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
// 클래스에 붙인 Transactional 은 모든 public 메서드에 기본값으로 적용됨
// readOnly = true : 읽기 전용, 읽기 전용의 장점은 변경감지용 스냅샷도 안만들고 flush도 안함
// 메서드에 따로 붙이면 @Transactional 붙이면 쓰기 트랜젝션으로 덮어씀
@Transactional(readOnly = true)
public class AptDealBatchService {

    private final AptComplexRepository aptComplexRepository;
    private final AptDealRepository aptDealRepository;
    private final SigunguCodes sigunguCodes;

    public boolean hasNoDeals() {
        // apt_deal 테이블 조회 count(*) 한 값이 0이면 true 아니면 false
        return aptDealRepository.count() == 0;
    }

    @Transactional
    public DealSaveResult saveDeals(List<MolitDealItem> items) {
        int inserted = 0;
        int updated = 0;
        int unchanged = 0;
        int skipped = 0;

        for (MolitDealItem item : items) {
            Optional<String> regionName = sigunguCodes.findName(item.toSigunguCode());
            if (regionName.isEmpty()) {
                log.warn("코드표에 없는 시군구 건너뜀: {}", item.sggCd());
                skipped++;
                continue;
            }
            try {
                switch (saveDeal(item, regionName.get())) {
                    case INSERTED -> inserted++;
                    case UPDATED -> updated++;
                    case UNCHANGED -> unchanged++;
                }
            } catch (IllegalArgumentException | DateTimeException e){
                log.warn("변환 실패 거래 건너뜀: {} {}, reason={}", item.sggCd(), item.aptNm(), e.getMessage());
                skipped++;
            }
        }
        return new DealSaveResult(inserted, updated, unchanged, skipped);
    }

    private SaveOutcome saveDeal(MolitDealItem item, String regionName) {
        String sigunguCode = item.toSigunguCode();
        String dong = item.toDong();
        String jibun = item.toJibun();
        String name = item.toComplexName();
        Integer builtYear = item.toBuiltYear();
        BigDecimal area = item.toArea();
        int floor = item.toFloor();
        long price = item.toPriceWon();
        LocalDate contractDate = item.toContractDate();
        boolean isCanceled = item.isCanceled();
        
        // 해당 아파트가 있는지 찾아서 있으면 그대로, 없으면 아파트 단지 목록에 넣음
        // orElseGet : Optional에 값이 있으면 그 값을 쓰고 없으면 괄호 안의 람다를 실행해서 그 결과를 씀
        AptComplex complex = aptComplexRepository.findBySigunguCodeAndDongAndJibunAndName(sigunguCode, dong, jibun, name)
                .orElseGet(() -> aptComplexRepository.save(
                        AptComplex.of(sigunguCode, dong, jibun, name, builtYear, regionName)));

        // 아파트 거래 내역이 있는지 확인해서
        Optional<AptDeal> found = aptDealRepository.findByComplexAndAreaAndFloorAndPriceAndContractDate(
                complex, area, floor, price, contractDate);

        // 거래 내역이 있는데 값(해제여부)이 변경 되었으면 UPDATE 상태로, 값이 변경된게 없으면 UNCHANGED 상태로
        if(found.isPresent()) {
            return found.get().updateCanceled(isCanceled) ? SaveOutcome.UPDATED : SaveOutcome.UNCHANGED;
        }

        // 매물 목록 insert
        AptDeal deal = AptDeal.of(complex, area, floor, price, contractDate);
        deal.updateCanceled(isCanceled);
        aptDealRepository.save(deal);

        return SaveOutcome.INSERTED;
    }

    private enum SaveOutcome {
        INSERTED, UPDATED, UNCHANGED
    }


}
