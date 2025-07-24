package com.salon.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {
    //지역 필터링용
    private String address_name;  // 전체 주소
    private String region_1depth_name;   // 시/도
    private String region_2depth_name;  // 구/군
}
