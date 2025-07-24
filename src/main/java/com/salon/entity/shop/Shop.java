package com.salon.entity.shop;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
public class Shop {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_id")
    private Long id; // 미용실 테이블 아이디
    private String name; // 미용실 이름
    private String address; // 미용실 주소
    private String addressDetail; // 미용실 상세주소
    @Digits(integer = 3, fraction = 7)
    private BigDecimal Latitude; // 미용실 위도
    @Digits(integer = 3, fraction = 7)
    private BigDecimal Longitude; // 미용실 경도
    private String tel; // 미용실 전화번호
    private LocalTime openTime; // 미용실 오픈시간
    private LocalTime closeTime; // 미용실 마감시간
    private String Description; // 상세 설명
    private int timeBeforeClosing; // 분 기준 예약 마감
    private int dayOff; // 가게 휴무일
    private int reservationInterval; // 예약 간격 (분단위)
    
    // 디자이너 근태관리를 위한 컬럼
    private int lateMin;
    private int earlyLeaveMin;
    
    
}
