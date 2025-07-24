package com.salon.dto.user;

import com.salon.constant.PaymentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPaymentDto {
    private Long reservationId;  // 결제 금액
    private int couponDiscountPrice;
    private int ticketUsedPrice;
    private int totalPrice;
    private int finalPrice;
    private PaymentType paymentType;
    private String payServiceName;

}
