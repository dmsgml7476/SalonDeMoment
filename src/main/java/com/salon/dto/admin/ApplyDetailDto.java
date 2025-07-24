package com.salon.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApplyDetailDto {
    private Long id;
    private String name;
    private String applyNumber;
    private String issuedDate;
    private String adminName;
    private LocalDateTime approveAt;
}
