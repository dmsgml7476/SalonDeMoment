package com.salon.dto.admin;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ApplyDto {
    private Long id;
    private String name;
    private String applyNumber;
    private LocalDate issuedDate;
}
