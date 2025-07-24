package com.salon.service.admin;

import com.salon.constant.Role;
import com.salon.constant.UploadType;
import com.salon.dto.UploadedFileDto;
import com.salon.dto.admin.AncCreateDto;
import com.salon.dto.admin.AncDetailDto;
import com.salon.dto.admin.AncFileDto;
import com.salon.dto.admin.AncListDto;
import com.salon.entity.Member;
import com.salon.entity.admin.Announcement;
import com.salon.entity.admin.AnnouncementFile;
import com.salon.repository.MemberRepo;
import com.salon.repository.admin.AnnouncementFileRepo;
import com.salon.repository.admin.AnnouncementRepo;
import com.salon.util.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AncService {
    private final MemberRepo memberRepo;
    private final AnnouncementRepo announcementRepo;
    private final AnnouncementFileRepo announcementFileRepo;
    private final FileService fileService;

    @Value("${file.anc-file-path}")
    private String ancPath;


    public void registration(AncCreateDto ancCreateDto, Member member, List<MultipartFile> files) {

        Announcement announcement = AncCreateDto.to(ancCreateDto, member);
        announcement.setWriteAt(LocalDateTime.now());

        if(files != null && !files.isEmpty()) {
            for(MultipartFile file : files) {
                if (!file.isEmpty()) {
                    UploadedFileDto image = fileService.upload(file, UploadType.ANNOUNCEMENT);

                    AnnouncementFile announcementFile = new AnnouncementFile();
                    announcementFile.setOriginalName(image.getOriginalFileName());;
                    announcementFile.setFileName(image.getFileName());
                    announcementFile.setFileUrl(image.getFileUrl());
                    announcementFile.setAnnouncement(announcement);

                    announcementRepo.save(announcement);
                    announcementFileRepo.save(announcementFile);
                }
            }
        }
    }

    public List<AncListDto> list() {
        /*List<Announcement> announcementList = announcementRepo.findAll();*/
        List<Announcement> announcementList = announcementRepo.findAllByOrderByWriteAtDesc();
        List<AncListDto> ancListDtoList = new ArrayList<>();
        for(Announcement announcement : announcementList){
            /*ancListDtoList.add(AncListDto.from(announcement));*/
            AncListDto ancListDto = AncListDto.from(announcement);

            List<AnnouncementFile> files = announcementFileRepo.findByAnnouncement(announcement);
            if(!files.isEmpty()){

                AnnouncementFile file = files.get(0);
                String originalName = file.getOriginalName();

                if(originalName != null && originalName.contains(".")) {
                    String extension = originalName.substring(originalName.lastIndexOf(".") + 1);
                    String uuid = UUID.randomUUID().toString();
                    String fileName = uuid + "." + extension;
                    String fileUrl = ancPath + fileName;
                    ancListDto.setFileName(fileName);
                    ancListDto.setFileUrl(fileUrl);
                }
            }

            ancListDtoList.add(ancListDto);
        }

        return ancListDtoList;
    }

    public AncDetailDto detail(Long id) {
        Announcement announcement = announcementRepo.findById(id).get();
        AncDetailDto ancDetailDto = AncDetailDto.from(announcement);

        List<AnnouncementFile> files = announcementFileRepo.findByAnnouncement(announcement);
        List<AncFileDto> fileDtos = new ArrayList<>();
        for(AnnouncementFile file : files){
            fileDtos.add(AncFileDto.from(file));
        }

        ancDetailDto.setFiles(fileDtos);

        Role role = announcement.getRole();

        boolean hasPrev = announcementRepo.findTopByRoleAndIdLessThanOrderByIdDesc(role, id).isPresent();
        boolean hasNext = announcementRepo.findTopByRoleAndIdGreaterThanOrderByIdAsc(role, id).isPresent();

        ancDetailDto.setHasPrev(hasPrev);
        ancDetailDto.setHasNext(hasNext);

        announcementRepo.findTopByRoleAndIdLessThanOrderByIdDesc(role, id)
                .ifPresent(prev -> ancDetailDto.setPrevId(prev.getId()));
        announcementRepo.findTopByRoleAndIdGreaterThanOrderByIdAsc(role, id)
                .ifPresent(next -> ancDetailDto.setNextId(next.getId()));
        return ancDetailDto;
    }

    public AncCreateDto updateForm(Long id) {
        Announcement announcement = announcementRepo.findById(id).orElseThrow();

        return AncCreateDto.from(announcement);
    }

    public void update(AncCreateDto ancCreateDto, Member member) {
        Announcement announcement = announcementRepo.findById(ancCreateDto.getId())
                .orElseThrow();
        announcement.setTitle(ancCreateDto.getTitle());
        announcement.setContent(ancCreateDto.getContent());
        announcement.setRole(ancCreateDto.getRole());
        announcement.setAdmin(member);

        announcementRepo.save(announcement);
    }

    public void delete(Long id) {
        Announcement announcement = announcementRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지가 존재하지 않습니다."));
        List<AnnouncementFile> files = announcementFileRepo.findByAnnouncement(announcement);
        for(AnnouncementFile file : files) {
            announcementFileRepo.delete(file);
        }
        announcementRepo.delete(announcement);
    }


    public List<AncListDto> findByRole(Role role) {
        return announcementRepo.findByRole(role)
                .stream()
                .map(AncListDto::from)
                .collect(Collectors.toList());
    }
}
