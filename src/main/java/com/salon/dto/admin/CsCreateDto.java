package com.salon.dto.admin;

import com.salon.constant.CsCategory;
import com.salon.constant.CsStatus;
import com.salon.entity.Member;
import com.salon.entity.admin.CsCustomer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CsCreateDto {
    private CsCategory csCategory;
    private String questionText;
    private String originalName;
    private String fileName;
    private String fileUrl;
    private CsStatus csStatus;

    public static CsCustomer to(CsCreateDto csCreateDto, Member member) {
        CsCustomer csCustomer = new CsCustomer();
        csCustomer.setCategory(csCreateDto.getCsCategory());
        csCustomer.setQuestionText(csCreateDto.getQuestionText());
        csCustomer.setQuestionAt(LocalDateTime.now());
        csCustomer.setMember(member);
        csCustomer.setStatus(CsStatus.WAITING);
        return csCustomer;
    }


    public static CsCreateDto from(CsCustomer csCustomer) {
        CsCreateDto csCreateDto = new CsCreateDto();
        csCreateDto.setCsCategory(csCustomer.getCategory());
        csCreateDto.setQuestionText(csCustomer.getQuestionText());
        csCreateDto.setCsStatus(csCustomer.getStatus());
        return csCreateDto;
    }
}
