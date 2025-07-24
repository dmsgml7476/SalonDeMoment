package com.salon.dto.management.master;

import com.salon.constant.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CategorySalesDto { // 카테고리별 매출

    private ServiceCategory category;
    private String label;
    private Long amount;

    public static CategorySalesDto from(ServiceCategory category, Long amount){

        CategorySalesDto dto = new CategorySalesDto();
        dto.setCategory(category);
        dto.setLabel(category.getLabel());
        dto.setAmount(amount);

        return dto;
    }

}
