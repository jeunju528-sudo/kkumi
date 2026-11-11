package com.kkumi.apartment;

import static org.assertj.core.api.Assertions.assertThat;

import com.kkumi.apartment.molit.MolitDealItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AptDealBatchServiceTest {

    @Autowired
    private AptDealBatchService batchService;

    @Autowired
    private AptDealRepository aptDealRepository;

    @Autowired
    private AptComplexRepository aptComplexRepository;

    private MolitDealItem item(String sggCd, String area, String floor, String dealAmount, String cdealType) {
        return new MolitDealItem(sggCd, "익선동", "55", "현대뜨레비앙", "2003",
                area, floor, dealAmount, "2026", "10", "29", cdealType);
    }

    @Test
    @DisplayName("새 거래면 단지와 거래 저장, 지역명은 코드표")
    void insertNewDeal() {
        // when
        DealSaveResult result = batchService.saveDeals(List.of(item("11110", "84.858", "7", "165,000", " ")));

        // then
        assertThat(result.inserted()).isEqualTo(1);
        AptDeal deal = aptDealRepository.findAll().get(0);
        assertThat(deal.getPrice()).isEqualTo(1_650_000_000L);
        assertThat(deal.getArea()).isEqualByComparingTo(new BigDecimal("84.86"));
        assertThat(deal.getComplex().getRegionName()).isEqualTo("서울 종로구");
    }

    @Test
    @DisplayName("같은 거래가 두 번 오면 1건만")
    void sameDealTwice() {
        // given
        batchService.saveDeals(List.of(item("11110", "84.858", "7", "165,000", " ")));

        // when
        DealSaveResult result = batchService.saveDeals(List.of(item("11110", "84.858", "7", "165,000", " ")));

        // then
        assertThat(result.unchanged()).isEqualTo(1);
        assertThat(aptDealRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("면적이 반올림 후 같으면 같은 거래")
    void sameAfterRounding() {
        // when
        batchService.saveDeals(List.of(
                item("11110", "84.858", "7", "165,000", " "),
                item("11110", "84.86", "7", "165,000", " ")));

        // then
        assertThat(aptDealRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("해제 표시가 나중에 오면 기존 거래를 해제로 갱신")
    void cancelLater() {
        // given
        batchService.saveDeals(List.of(item("11110", "84.858", "7", "165,000", " ")));

        // when
        DealSaveResult result = batchService.saveDeals(List.of(item("11110", "84.858", "7", "165,000", "O")));

        // then
        assertThat(result.updated()).isEqualTo(1);
        assertThat(aptDealRepository.findAll().get(0).isCanceled()).isTrue();
    }

    @Test
    @DisplayName("처음부터 해제된 거래는 해제로 저장")
    void insertCanceled() {
        // when
        batchService.saveDeals(List.of(item("11110", "84.858", "7", "165,000", "O")));

        // then
        assertThat(aptDealRepository.findAll().get(0).isCanceled()).isTrue();
    }

    @Test
    @DisplayName("같은 단지 다른 층 거래면 단지 1개, 거래 2건")
    void sameComplexTwoDeals() {
        // when
        batchService.saveDeals(List.of(
                item("11110", "84.858", "7", "165,000", " "),
                item("11110", "84.858", "8", "166,000", " ")));

        // then
        assertThat(aptComplexRepository.count()).isEqualTo(1);
        assertThat(aptDealRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("코드표에 없는 시군구는 건너뜀")
    void unknownSigungu() {
        // when
        DealSaveResult result = batchService.saveDeals(List.of(item("99999", "84.858", "7", "165,000", " ")));

        // then
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(aptDealRepository.count()).isZero();
    }

    @Test
    @DisplayName("변환 실패 거래만 건너뛰고 나머지는 저장")
    void skipInvalidOnly() {
        // when
        DealSaveResult result = batchService.saveDeals(List.of(
                item("11110", "84.858", "7", "가나다", " "),
                item("11110", "84.858", "8", "166,000", " ")));

        // then
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.inserted()).isEqualTo(1);
    }
}