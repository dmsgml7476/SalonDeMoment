package com.salon.dto.shop;

import com.salon.constant.ServiceCategory;
import com.salon.dto.management.ServiceForm;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ShopServiceSectionDto {
    private List<ServiceForm> recommended;
    private Map<ServiceCategory,List<ServiceForm>> categoryMap;



}
