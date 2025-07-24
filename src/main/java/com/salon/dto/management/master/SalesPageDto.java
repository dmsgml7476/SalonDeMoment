package com.salon.dto.management.master;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class SalesPageDto {

    private Long totalSales; // 총 매출액
    private List<CategorySalesDto> categorySales; // 카테고리별 매출액
    private List<DesignerSalesDto> designerSales; // 디자이너별 매출액

}
