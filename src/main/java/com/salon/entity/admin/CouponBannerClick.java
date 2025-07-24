package com.salon.entity.admin;

import com.salon.entity.Member;
import com.salon.entity.admin.CouponBanner;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name="coupon_banner_click")
public class CouponBannerClick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name="coupon_banner_id")
    private CouponBanner couponBanner;
    private LocalDateTime clickAt;
}
