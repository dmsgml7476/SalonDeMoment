package com.salon.entity.shop;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ShopImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_image_id")
    private Long id; // 미용실 이미지 테이블 아이디
    
    @JoinColumn(name = "shop_id")
    @ManyToOne
    private Shop shop; // 미용실 테이블 아이디
    private String originalName; // 이미지 원본이름
    private String imgName; // 이미지 저장 이름
    private String imgUrl; // 이미지 경로
    private Boolean isThumbnail; // 이미지 썸네일(미리보기)
    
}
