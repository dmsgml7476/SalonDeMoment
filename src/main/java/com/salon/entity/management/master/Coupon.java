package com.salon.entity.management.master;

import com.salon.constant.CouponType;
import com.salon.entity.shop.Shop;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@Entity
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    private String name;
    private int minimumAmount;

    @Enumerated(EnumType.STRING)
    private CouponType discountType;

    private int discountValue;
    private boolean isActive;
    private LocalDate expireDate;

}
