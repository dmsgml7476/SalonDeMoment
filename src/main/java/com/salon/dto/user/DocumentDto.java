package com.salon.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentDto {

    // 카카오 응답용. 배열로 오니까 배열을 하나씩 표현하기 위한 dto

    private AddressDto address;
}
