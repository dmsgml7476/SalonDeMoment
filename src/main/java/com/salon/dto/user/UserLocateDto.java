package com.salon.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UserLocateDto {

    // 유저 위치 정보 표시 and 계산용
    private String userAddress;
    private BigDecimal userLatitude;
    private BigDecimal userLongitude;
    private String region1depth;
    private String region2depth;


}
