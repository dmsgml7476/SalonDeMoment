package com.salon.dto.management.master;

import com.salon.entity.management.ShopDesigner;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter @Setter
public class DesignerScheduleSettingDto {

    private Long designerId;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;

    public static DesignerScheduleSettingDto from(ShopDesigner designer){

        DesignerScheduleSettingDto dto = new DesignerScheduleSettingDto();
        dto.setDesignerId(designer.getId());
        dto.setName(designer.getDesigner().getMember().getName());
        dto.setStartTime(designer.getScheduledStartTime());
        dto.setEndTime(designer.getScheduledEndTime());

        return dto;
    }

    public ShopDesigner to (ShopDesigner designer) {

        designer.setScheduledStartTime(this.startTime);
        designer.setScheduledEndTime(this.endTime);

        return designer;
    }



}
