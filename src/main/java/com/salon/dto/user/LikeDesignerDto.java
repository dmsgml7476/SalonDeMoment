package com.salon.dto.user;

import com.salon.entity.management.ShopDesigner;
import com.salon.entity.shop.SalonLike;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class LikeDesignerDto {

    // 회원이 좋아요 누른 디자이너 목록
    private Long id;  // Like 테이블 Id
    private String designerName;  // 디자이너 이름
    private int workingYear; // 경력
    private String position; // 포지션
    private String shopName; // 가게 이름
    private LocalTime startTime; // 기본 예약 시작 시간
    private LocalTime endTime;  //  기본 예약 끝나는 시간
    private String imgUrl; // 디자이너 프로필 이미지

    public static LikeDesignerDto from(SalonLike like, ShopDesigner shopDesigner) {
        LikeDesignerDto dto = new LikeDesignerDto();

        dto.setId(like.getId());
        dto.setDesignerName(shopDesigner.getDesigner().getMember().getName());
        dto.setWorkingYear(shopDesigner.getDesigner().getWorkingYears());
        dto.setPosition(shopDesigner.getPosition());
        dto.setShopName(shopDesigner.getShop().getName());
        dto.setStartTime(shopDesigner.getScheduledStartTime());
        dto.setEndTime(shopDesigner.getScheduledEndTime());
        dto.setImgUrl(shopDesigner.getDesigner().getImgUrl());

        return dto;
    }
}
