package com.salon.dto.management.master;

import com.salon.dto.management.AttendanceListDto;
import com.salon.entity.management.ShopDesigner;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class DesAttDto {

    private Long designerId;
    private String name;
    private String tel;
    private String imgUrl;
    private List<AttendanceListDto> attendanceList;

    public static DesAttDto from(ShopDesigner designer,  List<AttendanceListDto> attendanceList){

        DesAttDto dto = new DesAttDto();
        dto.setDesignerId(designer.getId());
        dto.setName(designer.getDesigner().getMember().getName());
        dto.setTel(designer.getDesigner().getMember().getTel());
        dto.setImgUrl(designer.getDesigner().getImgUrl());
        dto.setAttendanceList(attendanceList);

        return dto;
    }
}
