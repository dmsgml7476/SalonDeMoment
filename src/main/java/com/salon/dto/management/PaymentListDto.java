package com.salon.dto.management;

import com.salon.entity.management.Payment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class PaymentListDto {

    private Long id;
    private String memberName;
    private String serviceName;
    private LocalDateTime payDate;
    private String totalPrice;
    private String memo;
    private String category;

    public static PaymentListDto from(Payment payment){

        PaymentListDto dto = new PaymentListDto();
        dto.setId(payment.getId());
        if(payment.getReservation() != null) { // 예약시
            dto.setMemberName(payment.getReservation().getMember().getName());
            dto.setCategory("예약 결제");
        } else if(payment.getShopDesigner() != null) { // 방문시
            dto.setMemberName(payment.getVisitorName());
            dto.setCategory("방문 결제");
        }


        dto.setServiceName(payment.getServiceName());
        dto.setPayDate(payment.getPayDate());
        dto.setTotalPrice(String.format("%,d원", payment.getTotalPrice()));
        dto.setMemo(payment.getMemo());

        return dto;
    }

}
