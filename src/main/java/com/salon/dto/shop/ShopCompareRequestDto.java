package com.salon.dto.shop;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ShopCompareRequestDto {
    private List<Long> selectedShopIds;
}
