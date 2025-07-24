package com.salon.dto.user;

import com.salon.entity.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto {
    // 예약자 정보 확인용 dto

    private String name;
    private String Phone;


    public static MemberDto from (Member member){
        MemberDto dto = new MemberDto();

        dto.setName(member.getName());
        dto.setPhone(member.getTel());

        return dto;
    }
}
