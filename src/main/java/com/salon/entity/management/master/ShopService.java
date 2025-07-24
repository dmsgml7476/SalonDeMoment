package com.salon.entity.management.master;

import com.salon.constant.ServiceCategory;
import com.salon.entity.shop.Shop;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
public class ShopService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_service_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    private String name;
    private int price;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private ServiceCategory category;

    private String originalImgName;
    private String imgName;
    private String imgUrl;

    private boolean isRecommended;

}
