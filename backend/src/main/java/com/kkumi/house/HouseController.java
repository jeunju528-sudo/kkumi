package com.kkumi.house;

import com.kkumi.house.dto.BuyHouseRequest;
import com.kkumi.house.dto.BuyHouseResponse;
import com.kkumi.house.dto.MyHousesResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/houses")
public class HouseController {

    private final HouseService houseService;

    // 집 사기 (계약서 도장)
    @PostMapping
    public BuyHouseResponse buyHouse(@AuthenticationPrincipal Long memberId,
                                     @Valid @RequestBody BuyHouseRequest request) {
        return houseService.buyHouse(memberId, request.dealId());
    }

    // 내 마을
    @GetMapping
    public MyHousesResponse findMyHouses(@AuthenticationPrincipal Long memberId) {
        return houseService.findMyHouses(memberId);
    }
}
