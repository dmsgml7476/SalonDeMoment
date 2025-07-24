package com.salon.entity.management;

import com.salon.entity.Member;
import com.salon.entity.shop.Shop;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
@Entity
public class ShopDesigner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_designer_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "designer_id")
    private Designer designer;

    private String position;
    private LocalTime scheduledStartTime;
    private LocalTime scheduledEndTime;
    private boolean isActive;
    private String description;

}
