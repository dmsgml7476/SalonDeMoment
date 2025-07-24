package com.salon.dto.shop;

import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class AvailableTimeSlotDto {

    private LocalDate date;
    private List<LocalTime> availableTimes;
    private boolean isHoliday;
}
