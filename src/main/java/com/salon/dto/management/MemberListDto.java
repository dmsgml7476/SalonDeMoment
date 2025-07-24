package com.salon.dto.management;

import com.salon.entity.Member;
import com.salon.entity.management.MemberMemo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class MemberListDto {

    private Long id; // memberMemo ID
    private Long memberId;
    private String memberName;
    private String memberTel;
    private String memo;
    private List<MemberCardListDto> cardList;

    public static MemberListDto from(Member member, MemberMemo memberMemo, List<MemberCardListDto> cardList) {
        MemberListDto dto = new MemberListDto();

        // Member 정보는 항상 유효하므로 먼저 설정
        dto.setMemberId(member.getId());
        dto.setMemberName(member.getName());
        dto.setMemberTel(member.getTel());

        // MemberMemo 정보는 있을 수도 있고 없을 수도 있으므로 null 체크
        if (memberMemo != null) {
            dto.setId(memberMemo.getId()); // memberMemo ID
            dto.setMemo(memberMemo.getMemo());
        } else {
            dto.setId(null); // 메모가 없으므로 ID도 null
            dto.setMemo(null); // 메모 내용도 null
        }

        dto.setCardList(cardList);
        return dto;
    }



}
