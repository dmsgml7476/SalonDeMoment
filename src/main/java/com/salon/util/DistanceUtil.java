package com.salon.util;

import java.math.BigDecimal;

public class DistanceUtil {

    public static BigDecimal calculateDistance(BigDecimal userLat, BigDecimal userLon, BigDecimal shopLat, BigDecimal shopLon) {
        // 지구 반지름 (미터단위)
        double R = 6371000;
        // 위도 경도 decimal에서 double로 변환
        double lat1 = Math.toRadians(userLat.doubleValue());
        double lat2 = Math.toRadians(shopLat.doubleValue());
        // 위도/경도 차이 계산도 decimal에서 double로 변환
        double deltaLat = Math.toRadians(shopLat.doubleValue() - userLat.doubleValue());
        double deltaLon = Math.toRadians(shopLon.doubleValue() - userLon.doubleValue());

        // c (두 위도 경도 사이의 각)을 구하기 전단계
        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) + Math.cos(lat1)*Math.cos(lat2)*Math.sin(deltaLon/2)*Math.sin(deltaLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double distance= R * c;


        return BigDecimal.valueOf(distance);
    }

}
