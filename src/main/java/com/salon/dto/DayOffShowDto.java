package com.salon.dto;

import com.salon.util.DayOffUtil;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DayOffShowDto {

        private final int dayOff;
        private final String dayOffText;


        public DayOffShowDto(int dayOff) {
            this.dayOff = dayOff;
            this.dayOffText = generateDayOffText(dayOff);
        }

        private String generateDayOffText(int dayOffBit) {
            List<DayOfWeek> days = DayOffUtil.decodeDayOff(dayOffBit);
            if(days.isEmpty()) return "연중무휴";

            return days.stream()
                    .map(DayOffUtil::getKoreanDay)
                    .collect(Collectors.joining(", ", "매주 ", ""));
        }
}
