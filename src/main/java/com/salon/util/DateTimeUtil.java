package com.salon.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static String getTimeAgo(LocalDateTime createAt, boolean shortFormat) {
        if (createAt == null) return "방금 전";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createAt, now);

        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (seconds < 60) return "방금 전";
        if (minutes < 60) return minutes + "분 전";
        if (hours < 24) return hours + "시간 전";

        if (shortFormat) {
            // 리뷰에서는 하루 넘어가면 날짜로 표기
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return createAt.format(formatter);
        }

        // 하루전 이틀전, 등등
        if (days == 1) return "하루 전";
        if (days == 2) return "이틀 전";
        if (days <= 6) return days + "일 전";
        if (days == 7) return "일주일 전";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return createAt.format(formatter);
    }

}
