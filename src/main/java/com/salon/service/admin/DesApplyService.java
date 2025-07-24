package com.salon.service.admin;

import com.salon.constant.ApplyStatus;
import com.salon.constant.ApplyType;
import com.salon.constant.Role;
import com.salon.dto.admin.ApplyDto;
import com.salon.entity.Member;
import com.salon.entity.admin.Apply;
import com.salon.entity.management.Designer;
import com.salon.repository.admin.ApplyRepo;
import com.salon.repository.management.DesignerRepo;
import com.salon.util.OcrRestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DesApplyService {

    private final ApplyRepo applyRepo;
    private final DesignerRepo designerRepo;

    @Value("${ocr.api}")
    private String ocrApiKey;

    @Transactional
    public void Apply(ApplyDto applyDto, Member member, MultipartFile file) {

        Apply apply = new Apply();
        apply.setMember(member);
        apply.setApplyType(ApplyType.DESIGNER);
        apply.setApplyNumber(applyDto.getApplyNumber());
        apply.setIssuedDate(LocalDate.now().toString());
        apply.setCreateAt(LocalDateTime.now());
        apply.setStatus(ApplyStatus.WAITING);

        if(file != null && !file.isEmpty()) {
            try{
                String ocrText = OcrRestUtil.extractText(file, ocrApiKey);
                System.out.println("OCR 결과:\n" + ocrText);

                String extractedNumber = extractNumberFromText(ocrText);
                if(extractedNumber != null){
                    apply.setApplyNumber(extractedNumber);
                } else {
                    System.out.println("OCR 결과에서 자격번호를 찾지 못했습니다.");
                }

                String uploadDir = "uploads/certificates/";
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String savedFilename = UUID.randomUUID().toString() + extension;
                File uploadPath = new File(uploadDir);

                if (!uploadPath.exists()) {
                uploadPath.mkdirs();
                }

                File savedFile = new File(uploadPath, savedFilename);
                try (FileOutputStream fos = new FileOutputStream(savedFile)) {
                fos.write(file.getBytes());
                }
            }catch (Exception e){
                throw new RuntimeException("파일 저장 중 오류 발생", e);
            }
        }

        applyRepo.save(apply);
    }

    private String extractNumberFromText(String text) {
        if(text == null) return null;

        String[] lines = text.split("\\R");
        for(String line : lines){
            line = line.trim();
            if(line.matches(".*\\d{4}[- ]?\\d{5,8}.*")){
                return line.replaceAll("[^\\d-]", "");
            }
        }
        return null;
    }

    public List<Apply> list() {
        return applyRepo.findByStatus(ApplyStatus.WAITING);
    }

    @Transactional
    public Long approve(Long id, Member member) {
        Apply apply = applyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));
        apply.setStatus(ApplyStatus.APPROVED);
        apply.setApproveAt(LocalDateTime.now());
        apply.setAdmin(member);

        Member applicant = apply.getMember();
        Designer designer = new Designer();
        designer.setMember(applicant);
        designer.setWorkingYears(new Random().nextInt(10)+1);
        designerRepo.save(designer);
        applicant.setRole(Role.DESIGNER);
//        return apply.getMember().getId();

        return apply.getMember().getId();
    }

    // 원래 void 였던거 반환 타입 Apply로 변경 -
    @Transactional
    public Long reject(Long id, Member member) {
        System.out.println("reject()호출됨, id: " + id);
        Apply apply = applyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));
        System.out.println("현재 상태 : " + apply.getStatus());
        apply.setStatus(ApplyStatus.REJECTED);
        apply.setApproveAt(LocalDateTime.now());
        apply.setAdmin(member);

        System.out.println("상태 변경 후 : " + apply.getStatus());

        return apply.getMember().getId();
    }
}
