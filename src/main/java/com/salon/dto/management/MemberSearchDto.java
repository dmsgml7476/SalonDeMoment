package com.salon.dto.management;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class MemberSearchDto {
    private Long id;
    private String name;
    private String tel;
}
