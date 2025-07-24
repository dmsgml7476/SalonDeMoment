package com.salon.dto.admin;

import com.salon.constant.Role;
import com.salon.entity.Member;
import com.salon.entity.admin.Announcement;
import com.salon.entity.admin.AnnouncementFile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AncCreateDto {
    private Long id;
    private String title;
    private String content;
    private Role role;
    private String originalName;
    private String fileName;
    private String fileUrl;



    public static Announcement to(AncCreateDto ancCreateDto, Member member){
        Announcement announcement = new Announcement();
        announcement.setTitle(ancCreateDto.getTitle());
        announcement.setContent(ancCreateDto.getContent());
        announcement.setRole(ancCreateDto.getRole());
        announcement.setAdmin(member);
        return announcement;
    }

    public static AncCreateDto from(Announcement announcement) {
        AncCreateDto ancCreateDto = new AncCreateDto();
        ancCreateDto.setId(announcement.getId());
        ancCreateDto.setTitle(announcement.getTitle());
        ancCreateDto.setContent(announcement.getContent());
        ancCreateDto.setRole(announcement.getRole());
        return ancCreateDto;
    }
}
