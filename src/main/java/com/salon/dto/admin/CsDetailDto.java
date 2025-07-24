package com.salon.dto.admin;

import com.salon.constant.CsStatus;
import com.salon.entity.Member;
import com.salon.entity.admin.CsCustomer;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.C;

import java.time.LocalDateTime;

@Getter
@Setter
public class CsDetailDto {
    private Long id;
    private String loginId;
    private String memberName;
    private CsListDto csListDto;
    private String adminName;
    private LocalDateTime replyAt;
    private String replyText;

    public static CsDetailDto from(CsCustomer csCustomer) {
        CsDetailDto csDetailDto = new CsDetailDto();
        csDetailDto.setId(csCustomer.getId());
        csDetailDto.setReplyAt(csCustomer.getReplyAt());
        csDetailDto.setReplyText(csCustomer.getReplyText());
        csDetailDto.setLoginId(csCustomer.getMember().getLoginId());
        return csDetailDto;
    }
}
