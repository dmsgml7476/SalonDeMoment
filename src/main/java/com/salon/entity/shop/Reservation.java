package com.salon.entity.shop;

import com.salon.constant.ReservationStatus;
import com.salon.entity.Member;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.Coupon;
import com.salon.entity.management.master.ShopService;
import com.salon.entity.management.master.Ticket;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id; // 예약테이블 아이디

    @JoinColumn(name = "member_id")
    @ManyToOne
    private Member member; // 유저 아이디

    @JoinColumn(name = "shop_designer_id")
    @ManyToOne
    private ShopDesigner shopDesigner; // 디자이너 아이디

    @JoinColumn(name = "service_id")
    @ManyToOne
    private ShopService shopService; // 서비스 아이디

    private String serviceName; // 디자이너 직접 등록시

    @JoinColumn(name = "coupon_id")
    @ManyToOne
    private Coupon coupon; // 쿠폰 아이디

    @JoinColumn(name = "ticket_id")
    @ManyToOne
    private Ticket ticket; // 정액권 아이디

    private int discountAmount; // 할인된 가격
    private LocalDateTime reservationDate; // 예약 날짜
    private int ticketUsedAmount; // 정액권 사용금액
    
    @Enumerated(EnumType.STRING)
    private ReservationStatus status; // 예약 상태
    private String comment; // 요청사항

}
