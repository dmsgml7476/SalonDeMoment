package com.salon.dto.management.master;

import com.salon.entity.management.Designer;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DesignerResultDto {

    private Long id;
    private String designerName;
    private String imgUrl;
    private int workingYears;
    private boolean isAffiliation; // 미용실 소속 여부

    public static DesignerResultDto from(Designer designer, boolean isAffiliation){

        DesignerResultDto dto = new DesignerResultDto();
        dto.setId(designer.getId());
        dto.setDesignerName(designer.getMember().getName());
        dto.setImgUrl(designer.getImgUrl());
        dto.setWorkingYears(designer.getWorkingYears());
        dto.setAffiliation(isAffiliation);

        return dto;

    }

}
