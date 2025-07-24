package com.salon.dto.management.master;

import com.salon.entity.shop.Shop;
import com.salon.util.DayOffUtil;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class ShopReservationSettingDto {

    private Long shopId;
    private LocalTime openTime;
    private LocalTime closeTime;

    // 예약마감 시간 설정용
    private int timeBeforeClosing;

    // 예약시간 간격
    private int reservationInterval;

    // 2진법 표기 ex) 1111011 == 금요일 휴무일
    private int dayOff;

    // 휴무일 요일 ==> dayOff 변환 (DayOffUtil)
    private List<DayOfWeek> dayOffList;

    // 지각 설정용 (분단위)
    private int lateMin;

    // 조퇴 설정용 (분단위)
    private int earlyLeaveMin;

    public static ShopReservationSettingDto from (Shop shop) {
        ShopReservationSettingDto dto = new ShopReservationSettingDto();

        dto.setShopId(shop.getId());
        dto.setOpenTime(shop.getOpenTime());
        dto.setCloseTime(shop.getCloseTime());
        dto.setTimeBeforeClosing(shop.getTimeBeforeClosing());
        dto.setReservationInterval(shop.getReservationInterval());
        dto.setDayOffList( DayOffUtil.decodeDayOff( shop.getDayOff() ) );
        dto.setLateMin(shop.getLateMin());
        dto.setEarlyLeaveMin(shop.getEarlyLeaveMin());

        return dto;
    }

    public Shop update(Shop shop, int dayOff){

        shop.setOpenTime(this.openTime);
        shop.setCloseTime(this.closeTime);
        shop.setTimeBeforeClosing(this.timeBeforeClosing);
        shop.setReservationInterval(this.reservationInterval);
        shop.setDayOff(dayOff);

        return shop;
    }


}
