package com.salon.dto.management;

import com.salon.entity.shop.Reservation;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter @Setter
public class TodayScheduleDto {

    private Long reservationId;
    private String designerName;
    private String customerName;
    private String serviceName;
    private String time;
    private String status;

    public static TodayScheduleDto from(Reservation reservation){

        TodayScheduleDto dto = new TodayScheduleDto();

        dto.setReservationId(reservation.getId());
        dto.setDesignerName(reservation.getShopDesigner().getDesigner().getMember().getName());
        dto.setCustomerName(reservation.getMember().getName());
        dto.setServiceName(reservation.getShopService() != null
                ? reservation.getShopService().getName() : reservation.getServiceName());
        dto.setTime(reservation.getReservationDate().format(DateTimeFormatter.ofPattern("HH:mm")));
        dto.setStatus(reservation.getStatus().getLabel());

        return dto;
    }


}
