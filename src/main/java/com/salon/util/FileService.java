package com.salon.util;

import com.salon.constant.UploadType;
import com.salon.dto.UploadedFileDto;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.des-img-path}")
    private String desImgPath;

    @Value("${file.shop-img-path}")
    private String shopImgPath;

    @Value("${file.shop-service-img-path}")
    private String shopServiceImgPath;

    @Value("${file.review-img-path}")
    private String reviewImgPath;

    @Value("${file.banner-img-file}")
    private String bannerImgPath;

    @Value("${file.anc-file-path}")
    private String ancFilePath;

    @Value("${file.cs-file}")
    private String csFilePath;

    public UploadedFileDto upload(MultipartFile multipartFile, UploadType type) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        String originalFileName = multipartFile.getOriginalFilename();
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new IllegalArgumentException("유효한 확장자가 없습니다.");
        }

        // 확장자 png, jpg, pdf 등등
        String ext = originalFileName.substring(originalFileName.lastIndexOf(".")+1);
        
        // 랜덤이름 + 확장자
        String uuidFileName = UUID.randomUUID() + "." + ext;

        // 업로드 타입별 폴더 경로
        String folderPath = getFolderPath(type);

        // 저장 될 파일 경로
        String fullPath = folderPath + uuidFileName;

        // 폴더가 없을시 생성
        File dir = new File(folderPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }


        // 경로 + 파일이름 /shopImg/*****.jpg
        String fileUrl = type.getUrlPath() + uuidFileName;

        // 파일 저장
        try(FileOutputStream fos = new FileOutputStream(fullPath)) {
            fos.write(multipartFile.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


        return new UploadedFileDto(originalFileName, uuidFileName, fileUrl);
    }

    private String getFolderPath(UploadType type) {
        return switch (type) {
            case DESIGNER -> desImgPath;
            case SHOP -> shopImgPath;
            case SHOP_SERVICE -> shopServiceImgPath;
            case REVIEW -> reviewImgPath;
            case BANNER -> bannerImgPath;
            case ANNOUNCEMENT -> ancFilePath;
            case CUSTOMER_SERVICE -> csFilePath;
        };
    }
}
