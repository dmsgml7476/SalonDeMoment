package com.salon.dto.management;

import com.salon.constant.PaymentType;
import com.salon.constant.ServiceCategory;
import com.salon.entity.management.Payment;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.shop.Reservation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class PaymentForm {

    private Long id;
    private Long reservationId; // 예약 시

    private Long designerId; // 방문시
    private String customerName; // 고객 이름
    private String customerTel; // 고객 연락처
    private String serviceName; // 시술 설명

    private LocalDateTime payDate;

    private int totalPrice; // 총 금액
    private int couponDiscountPrice; // 쿠폰할인가격
    private int ticketUsedPrice; // 정액권 사용금액
    private int finalPrice;

    private PaymentType paymentType;
    private ServiceCategory serviceCategory;
    private String memo;

    public static PaymentForm from(Payment payment){

        PaymentForm form = new PaymentForm();

        form.setId(payment.getId());
        if(payment.getReservation() != null){
            form.setReservationId(payment.getReservation().getId());
            form.setCustomerName(payment.getReservation().getMember().getName());
            form.setCustomerTel(payment.getReservation().getMember().getTel());
            form.setServiceName(payment.getReservation().getShopService().getName());
            form.setDesignerId(payment.getShopDesigner().getId());
        } else {
            form.setDesignerId(payment.getShopDesigner().getId());
            form.setCustomerName(payment.getVisitorName());
            form.setCustomerTel(payment.getVisitorTel());
            form.setServiceName(payment.getServiceName());
        }
        form.setPayDate(payment.getPayDate());
        form.setTotalPrice(payment.getTotalPrice());
        form.setCouponDiscountPrice(payment.getCouponDiscountPrice());
        form.setTicketUsedPrice(payment.getTicketUsedPrice());
        form.setFinalPrice(payment.getFinalPrice());
        form.setPaymentType(payment.getPaymentType());
        form.setServiceCategory(payment.getServiceCategory());
        form.setMemo(payment.getMemo());

        return form;
    }

    public Payment to (Reservation reservation, ShopDesigner shopDesigner){

        Payment payment = new Payment();

        payment.setReservation(reservation); // 예약 결제라면 reservation 주입
        payment.setShopDesigner(shopDesigner); // 방문 결제라면 designer 주입

        if (reservation == null) {
            payment.setVisitorName(this.customerName);
            payment.setVisitorTel(this.customerTel);
            payment.setServiceName(this.serviceName);
        }
        payment.setPayDate(this.payDate != null ? this.payDate : LocalDateTime.now());
        payment.setTotalPrice(this.totalPrice);
        payment.setCouponDiscountPrice(this.couponDiscountPrice);
        payment.setTicketUsedPrice(this.ticketUsedPrice);
        payment.setFinalPrice(this.finalPrice);
        payment.setPaymentType(this.paymentType);
        payment.setServiceCategory(this.serviceCategory);
        payment.setMemo(this.memo);
        payment.setCreatedAt(LocalDateTime.now());

        return payment;

    }

    


}
