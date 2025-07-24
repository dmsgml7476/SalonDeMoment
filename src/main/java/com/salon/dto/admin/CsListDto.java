package com.salon.dto.admin;

import com.salon.constant.ApplyStatus;
import com.salon.constant.CsCategory;
import com.salon.constant.CsStatus;
import com.salon.entity.admin.CsCustomer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CsListDto {
    private Long id;
    private String questionText;
    private CsCategory csCategory;
    public String getCategoryLabel(){
        return csCategory != null ? csCategory.getLabel() : "";
    }

    public CsCategory getCategory(){
        return csCategory;
    }

    public void setCsCategory(CsCategory csCategory){
        this.csCategory = csCategory;
    }
    private LocalDateTime questionAt;
    private CsStatus csStatus;

    public String getStatusLabel(){
        return csStatus != null ? csStatus.getLabel() : "";
    }

    public CsStatus getStatus(){
        return csStatus;
    }

    public void setStatus(CsStatus status){
        this.csStatus = status;
    }

    private String loginId;


    public static CsListDto from(CsCustomer csCustomer) {
        CsListDto csListDto = new CsListDto();
        csListDto.setId(csCustomer.getId());
        csListDto.setQuestionText(csCustomer.getQuestionText());
        csListDto.setCsCategory(csCustomer.getCategory());
        csListDto.setQuestionAt(csCustomer.getQuestionAt());
        csListDto.setCsStatus(csCustomer.getStatus());

        String loginId = csCustomer.getMember().getLoginId();
        csListDto.setLoginId(loginId.startsWith("designer") ? "디자이너" : "회원");
        return csListDto;
    }
}
