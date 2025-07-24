package com.salon.dto.management.master;

import com.salon.dto.management.TodayScheduleDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class MainDesignerPageDto {

    private int designerCount;
    private int todayReservationCount;
    private String todayPay;
    private String monthlyPay;

    private List<DesignerSummaryDto> designerList;
    private List<TodayScheduleDto> todaySchedules;




}
