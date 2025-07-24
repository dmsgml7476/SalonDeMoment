package com.salon.dto.shop;

import com.salon.constant.CouponType;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class ReservationCheckDto {
    // 예약 확인 페이지에서 직접 보여줄 dto

    private LocalDate reservationDate; // 예약 날짜
    private LocalTime reservationTime; // 예약 시간
    
    
    private String comment; // 고객 요청사항
    private List<CouponListDto> couponList; // 쿠폰 목록
    private int serviceAmount; // 시술 금액
    private CouponType discountType; // 쿠폰 할인 방식
    private int amountDiscount; // 할인된 금액
    private int ticketUsedAmount; // 정액권 사용 금액
    private int finalAmount; // 최종 금액
    private String precaution; // 예약 시 주의사항
    private AvailableTimeSlotDto writeDto; // 예약 작성 Dto





}
