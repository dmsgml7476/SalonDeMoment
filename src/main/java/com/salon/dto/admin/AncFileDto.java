package com.salon.dto.admin;

import com.salon.entity.admin.AnnouncementFile;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AncFileDto {

    private Long id;
    private String fileName;
    private String fileUrl;

    public static AncFileDto from(AnnouncementFile announcementFile){
        AncFileDto dto = new AncFileDto();
        dto.setId(announcementFile.getId());
        dto.setFileName(announcementFile.getFileName());
        dto.setFileUrl(announcementFile.getFileUrl());
        return dto;
    }


}
