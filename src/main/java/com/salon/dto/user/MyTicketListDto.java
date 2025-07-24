package com.salon.dto.user;

import com.salon.dto.management.master.TicketInfoDto;
import com.salon.entity.management.Payment;
import com.salon.entity.management.master.Ticket;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;


@Getter
@Setter
public class MyTicketListDto {
    private Long ticketId;
    private TicketInfoDto ticketInfoDto;
    private int usedPrice;
    private String shopName;
    private Long reservationId;
    private LocalDateTime payDate;
    private Long shopId;


    public static MyTicketListDto from(Ticket ticket, Payment payment) {
        MyTicketListDto myTicketListDto = new MyTicketListDto();

        myTicketListDto.setTicketId(ticket.getId());
        myTicketListDto.setTicketInfoDto(TicketInfoDto.from(ticket, ticket.getUsedAmount()));
        myTicketListDto.setShopId(ticket.getShop().getId());
        myTicketListDto.setShopName(ticket.getShop().getName());

        if (payment != null) {
            myTicketListDto.setUsedPrice(payment.getTicketUsedPrice());
            myTicketListDto.setReservationId(payment.getReservation().getId());
            myTicketListDto.setPayDate(payment.getPayDate());
        }

        return myTicketListDto;
    }
}
