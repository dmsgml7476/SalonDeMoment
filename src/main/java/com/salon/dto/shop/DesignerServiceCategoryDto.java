package com.salon.dto.shop;

import com.salon.constant.ServiceCategory;
import com.salon.entity.management.master.ShopService;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DesignerServiceCategoryDto {

    private ServiceCategory category;
    private List<ShopServiceDto> services;
}
