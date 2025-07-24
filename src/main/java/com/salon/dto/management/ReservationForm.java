package com.salon.dto.management;


import com.salon.constant.ReservationStatus;
import com.salon.entity.Member;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.Coupon;
import com.salon.entity.management.master.ShopService;
import com.salon.entity.management.master.Ticket;
import com.salon.entity.shop.Reservation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ReservationForm {

    private Long id; // Reservation ID
    private Long memberId;
    private Long serviceId;
    private Long couponId;

    private String memberName;
    private String serviceName;
    private int servicePrice;
    private String couponName;
    private LocalDateTime reservationDate;
    private int couponDiscount; // 쿠폰 할인금액
    private boolean ticketIsUsed; // 정액권 사용유무
    private int ticketUsedAmount; // 정액권 사용금액
    private int finalPrice;
    private String comment;
    private ReservationStatus status;

    public static ReservationForm from(Reservation reservation) {

        ReservationForm dto = new ReservationForm();
        
        int discountAmount;
        int ticketUsedAmount;

        dto.setId(reservation.getId());
        dto.setMemberId(reservation.getMember().getId());
        dto.setServiceId(reservation.getShopService().getId());

        dto.setMemberName(reservation.getMember().getName());
        dto.setServiceName(reservation.getShopService().getName());
        dto.setServicePrice(reservation.getShopService().getPrice());

        if(reservation.getCoupon() != null){ // 쿠폰 있을시
            dto.setCouponId(reservation.getCoupon().getId());
            dto.setCouponName(reservation.getCoupon().getName());
            discountAmount = reservation.getDiscountAmount();
            dto.setCouponDiscount(discountAmount);
        } else { // 쿠폰 없을시
            dto.setCouponName(null);
            discountAmount = 0;
            dto.setCouponDiscount(0);
        }

        if(reservation.getTicket() != null){ // 정액권 있을시
            dto.setTicketIsUsed(true);
            ticketUsedAmount = reservation.getTicketUsedAmount();
            dto.setTicketUsedAmount(ticketUsedAmount);
        } else { // 정액권 없을시
            dto.setTicketIsUsed(false);
            ticketUsedAmount = 0;
            dto.setTicketUsedAmount(0);
        }

        // 서비스가격 - 할인가격 - 정액권금액
        dto.setFinalPrice(reservation.getShopService().getPrice() - discountAmount - ticketUsedAmount);

        dto.setStatus(reservation.getStatus());

        return dto;
    }

    public Reservation to(Member member, ShopDesigner designer, ShopService service, Coupon coupon, Ticket ticket) {
        Reservation reservation = new Reservation();

        reservation.setId(this.id); // 수정 시 사용
        reservation.setMember(member);
        reservation.setShopDesigner(designer);
        reservation.setShopService(service);
        reservation.setCoupon(coupon);
        reservation.setTicket(ticket);

        reservation.setDiscountAmount(this.couponDiscount);
        reservation.setTicketUsedAmount(this.ticketIsUsed ? this.ticketUsedAmount : 0);
        reservation.setServiceName(service != null ? service.getName() : this.serviceName);

        reservation.setReservationDate(this.reservationDate != null ?  this.reservationDate : LocalDateTime.now());
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setComment(this.comment); // 요청사항 없으면 null 처리 (필요 시 set 가능)

        return reservation;
    }


}
