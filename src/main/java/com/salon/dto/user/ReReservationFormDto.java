package com.salon.dto.user;

import com.salon.entity.shop.Reservation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReReservationFormDto {
    private Long originReservationId;

    private Long shopDesignerId;
    private Long shopId;
    private Long designerId;

    private String serviceName;

    private LocalDateTime reservationDate;


    public static ReReservationFormDto from(Reservation reservation) {
        ReReservationFormDto dto = new ReReservationFormDto();
        dto.setOriginReservationId(reservation.getId());

        if (reservation.getShopDesigner() != null) {
            dto.setShopDesignerId(reservation.getShopDesigner().getId());
            dto.setShopId(reservation.getShopDesigner().getShop().getId());
            dto.setDesignerId(reservation.getShopDesigner().getDesigner().getId());
        }

        dto.setServiceName(reservation.getServiceName());

        return dto;
    }



}
