package com.salon.dto.shop;

import com.salon.dto.designer.DesignerListDto;
import com.salon.dto.management.MemberCardListDto;
import com.salon.dto.user.MemberDto;
import com.salon.entity.Member;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ReservationPreviewDto {

    private DesignerListDto selectedDesigner;
    private ShopServiceDto selectedService;
    private LocalDate reservationDate;
    private LocalTime reservationTime;

    // 예약자 정보
    private MemberDto member;

    // 예약 요약 정보
    private int totalPrice;
    private String shopName;
    private String shopAddress;

    // 시술 명칭 통합
    private String serviceSummary;

    //추가 요소
    private String designerTitle; // 이름 + 직급





}
