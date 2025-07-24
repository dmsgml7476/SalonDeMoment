package com.salon.dto.management;

import com.salon.entity.management.MemberCard;
import com.salon.entity.management.Payment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MemberCardListDto {

    private Long id; // MemberCard ID
    private Long paymentId; // 회원만 (visitor X)
    private String serviceName;
    private int totalPrice;
    private LocalDateTime payDate;
    private String memo;
    private LocalDateTime createAt;

    public static MemberCardListDto from(MemberCard memberCard, Payment payment) {

        MemberCardListDto dto = new MemberCardListDto();

        if(memberCard != null){
            dto.setId(memberCard.getId());
            dto.setMemo(memberCard.getMemo());
            dto.setCreateAt(memberCard.getCreateAt());
        }
        dto.setPaymentId(payment.getId());
        dto.setServiceName(payment.getServiceName());
        dto.setTotalPrice(payment.getTotalPrice());
        dto.setPayDate(payment.getPayDate());

        return dto;
    }

}
