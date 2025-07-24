package com.salon.dto;

import com.salon.constant.WebTarget;
import com.salon.entity.admin.WebNotification;
import com.salon.util.DateTimeUtil;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class WebNotificationDto {

    private String message; // 웹 알림으로 보낼 메세지
    private String createAt; // 생성 날짜 + 시간
    private WebTarget webTarget; // 대상
    private Long targetId; // 대상 아이디
    private Long receiverId; // 받는 사람 아이디

    private Long unreadTotal;

    public static WebNotificationDto from (WebNotification entity) {
        WebNotificationDto dto = new WebNotificationDto();

        dto.setMessage(entity.getMessage());
        dto.setWebTarget(entity.getWebTarget());
        dto.setTargetId(entity.getTargetId());
        dto.setCreateAt(DateTimeUtil.getTimeAgo(entity.getCreateAt(), false));
        dto.setReceiverId(entity.getMemberId());

        return dto;
    }


    //웹알림 저장 코드

    public WebNotification to() {
        WebNotification entity = new WebNotification();
        entity.setMessage(this.message);
        entity.setWebTarget(this.webTarget);
        entity.setTargetId(this.targetId);
        entity.setRead(false);
        entity.setCreateAt(LocalDateTime.now());
        entity.setMemberId(this.receiverId);

        return entity;
    }

}
