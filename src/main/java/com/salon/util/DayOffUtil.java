package com.salon.util;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class DayOffUtil {

    // dayOffList -> dayOff (요일 목록에서 int 로 변환)
    public static int encodeDayOff(List<DayOfWeek> dayOffs) {
        int bit = 0;

        for (DayOfWeek day : dayOffs) {
            int index = day.getValue(); // 월=1, 일=7
            bit |= 1 << (7- index); // 월~일 순으로 비트 맞추기
        }

        return bit; // (월화금 == 64 + 32 + 4 == 100)
    }

    // dayOff -> dayOffList (int 타입의 dayOff 를 List<DayOfWeek> 으로 변환)
    public static List<DayOfWeek> decodeDayOff(int dayOff) {
        List<DayOfWeek> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            if ( (dayOff & (1 << (6 - i)) ) != 0) { // 각 자리수 확인 (월요일부터)
                result.add(DayOfWeek.of((i + 1) % 7 == 0 ? 7 : (i + 1) % 7)); // 월=1~일=7
            }
        }

        return result; // (월화금 == 1100100)
    }

    // 해당 요일이 휴무일인지 확인
    public static boolean isDayOff(int dayOff, DayOfWeek dayOfWeek) {
        int index = dayOfWeek.getValue() % 7; // 월:1 ~ 일:7 → 0~6
        return (dayOff & (1 << (6 - index))) != 0;
    }

    // 한글 요일 변환 함수
    public static String getKoreanDay(DayOfWeek day) {
        return switch (day) {
            case MONDAY    -> "월요일";
            case TUESDAY   -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY  -> "목요일";
            case FRIDAY    -> "금요일";
            case SATURDAY  -> "토요일";
            case SUNDAY    -> "일요일";
        };
    }


}
