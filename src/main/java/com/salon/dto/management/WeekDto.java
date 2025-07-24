package com.salon.dto.management;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
public class WeekDto {

    private LocalDate startDate;
    private LocalDate endDate;
    private String label; // e.g. "1주차 (07.01~07.07)"

}
