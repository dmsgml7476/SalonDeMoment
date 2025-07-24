package com.salon.entity.shop;

import com.salon.constant.LikeType;
import com.salon.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SalonLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salon_like_id")
    private Long id; // 찜(즐겨찾기) 테이블 아이디

    @JoinColumn(name = "member_id")
    @ManyToOne
    private Member member; // 사용자 아이디

    @Enumerated(EnumType.STRING)
    private LikeType likeType; // 즐겨찾기 유형
    
    private Long typeId; // 즐겨찾기 유형 구분 아이디

}
