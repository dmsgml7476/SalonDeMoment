package com.salon.dto.shop;


import com.salon.constant.ServiceCategory;
import com.salon.entity.management.Designer;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.DesignerService;
import com.salon.entity.management.master.ShopService;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReservationSelectDto {


    private Long serviceId; // 시술 테이블 아이디
    private Long shopDesignerId; // 디자이너 테이블 아이디
    private LocalDateTime SelectDateTime; // 선택가능한 날짜 및 시간
    private ServiceCategory serviceCategory; // 시술 카테고리
    private List<DesignerService> designerServiceList; // 디자이너의 시술 전문분야 시술 리스트

    private List<ShopDesigner> designerList ; // 선택할 디자이너 리스트





    public static ReservationSelectDto from (Designer designer, ShopService shopService, List<ShopDesigner> designerList,List<DesignerService>designerServiceList){
        ReservationSelectDto reservationSelectDto = new ReservationSelectDto();
        
        reservationSelectDto.setShopDesignerId(designer.getId());
        reservationSelectDto.setServiceCategory(shopService.getCategory());
        reservationSelectDto.setDesignerList(designerList);
        reservationSelectDto.setDesignerServiceList(designerServiceList);
        return reservationSelectDto;
    }


}
