package com.salon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class UploadedFileDto {
    private String originalFileName;
    private String fileName;
    private String fileUrl;
}
