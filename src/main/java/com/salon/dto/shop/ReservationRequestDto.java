package com.salon.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter

public class ReservationRequestDto {
    // 예약 정보 저장시 넘길 dto

    private Long memberId; // 예약자 정보
    private Long shopDesignerId; // 선택한 디자이너
    private Long shopServiceId; // 선택힌 시술
    private LocalDateTime dateTime; // 선택한 날짜 및 시간


    // 예약 확인 페이지에서 작성할 목록
    private String requestMemo; // 고객 요청사항
    private Long selectCouponId; // 선택한 쿠폰 id
    private Long selectTicketId; // 선택한 정액권 id


}
