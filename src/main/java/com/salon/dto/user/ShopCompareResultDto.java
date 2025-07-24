package com.salon.dto.user;

import com.salon.constant.ServiceCategory;
import com.salon.dto.management.ServiceForm;
import com.salon.dto.shop.ShopListDto;
import com.salon.entity.shop.Shop;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class ShopCompareResultDto {
    private Long Id; // 선택된 미용실 아이디
    private ShopListDto shopListDto;
    private List<ServiceForm> serviceForms;
    private Map<ServiceCategory, List<ServiceForm>> categorizedServices;

    private BigDecimal distance;

    public static ShopCompareResultDto from(Shop shop, ShopListDto shopListDto, List<ServiceForm> serviceForms) {
        ShopCompareResultDto dto = new ShopCompareResultDto();
        dto.setId(shop.getId());
        dto.setShopListDto(shopListDto);
        dto.setServiceForms(serviceForms);

        dto.setDistance(shopListDto.getDistance());

        // 카테고리별로 그룹핑
        Map<ServiceCategory, List<ServiceForm>> categorized = new EnumMap<>(ServiceCategory.class);
        for (ServiceCategory category : ServiceCategory.values()) {
            categorized.put(category,
                    serviceForms.stream()
                            .filter(s -> s.getCategory() == category)
                            .collect(Collectors.toList())
            );
        }



        dto.setCategorizedServices(categorized);

        return dto;
    }
}
