package com.salon.dto.admin;

import com.salon.constant.CsCategory;
import com.salon.constant.CsStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyListDto {
    private Long id;
    private String loginId;
    private String LocalDateTime;
    private CsStatus status;

    public String getStatusLabel(){
        return status != null ? status.getLabel() : "";
    }

    public CsStatus getStatus(){
        return status;
    }

    public void setCsStatus(CsStatus csStatus){
        this.status = csStatus;
    }
}
