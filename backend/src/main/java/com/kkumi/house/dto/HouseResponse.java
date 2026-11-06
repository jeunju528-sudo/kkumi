package com.kkumi.house.dto;

import com.kkumi.apartment.AptComplex;
import com.kkumi.apartment.AptDeal;
import com.kkumi.house.House;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// 내 마을 집 한 채 (purchasePrice: 산 때 집값)
public record HouseResponse(Long houseId, Long dealId, String complexName, String region, BigDecimal area, int floor,
    long purchasePrice, LocalDateTime purchasedAt) {

    public static HouseResponse from(House house) {
        AptDeal deal = house.getDeal();
        AptComplex complex = deal.getComplex();
        return new HouseResponse(house.getId(), deal.getId(), complex.getName(), complex.getRegionName(),
            deal.getArea(), deal.getFloor(), house.getPurchasePrice(), house.getPurchasedAt());
    }
}
