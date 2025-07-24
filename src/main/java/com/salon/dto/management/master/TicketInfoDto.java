package com.salon.dto.management.master;

import com.salon.entity.management.master.Ticket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class TicketInfoDto {
    private Long ticketId;
    private int totalAmount;
    private int usedAmount;
    private int remainingAmount;


    // 정액권 잔액 조회를 위한 계산 메서드
    public static TicketInfoDto from (Ticket ticket, int usedAmount) {
        int remainingAmount = ticket.getTotalAmount() - usedAmount;

        return new TicketInfoDto(
          ticket.getId(),
          ticket.getTotalAmount(),
          usedAmount,
          remainingAmount
        );
    }
}