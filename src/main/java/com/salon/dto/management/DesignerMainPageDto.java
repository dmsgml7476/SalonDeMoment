package com.salon.dto.management;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class DesignerMainPageDto {

    private int todayReservationCount;  // 오늘 예약 수
    private int todayNewCustomers;      // 오늘 신규 예약 회원 수
    private int todayCompletedPayments; // 오늘 결제된 횟수

    private List<TodayScheduleDto> todayScheduleList;


}
