package com.salon.dto.admin;



import com.salon.constant.Role;
import com.salon.entity.admin.Announcement;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class AncListDto {
    private Long id;
    private String adminName;
    private String title;
    private String content;
    private LocalDateTime writeAt;
    private Role role;
    private String originalName;
    private String fileName;
    private String fileUrl;

    public String getRoleName() {
        return role != null ? role.name() : "";
    }

    public static AncListDto from(Announcement announcement){
        AncListDto ancListDto = new AncListDto();
        ancListDto.setId(announcement.getId());
        ancListDto.setTitle(announcement.getTitle());
        ancListDto.setWriteAt(announcement.getWriteAt());
        ancListDto.setContent(announcement.getContent());
        ancListDto.setRole(announcement.getRole());

        if (announcement.getAdmin() != null) {
            ancListDto.setAdminName(announcement.getAdmin().getName());
        } else {
            ancListDto.setAdminName("관리자 없음");
        }

        return ancListDto;
    }
}
