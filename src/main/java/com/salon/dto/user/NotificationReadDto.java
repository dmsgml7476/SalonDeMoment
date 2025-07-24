package com.salon.dto.user;

import com.salon.constant.WebTarget;
import lombok.Getter;

@Getter
public class NotificationReadDto {
    private WebTarget webTarget;
    private Long targetId;
}
