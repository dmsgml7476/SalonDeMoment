package com.salon.entity.admin;

import com.salon.constant.WebTarget;
import jakarta.persistence.*;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name="web_notification")
public class WebNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "member_id", nullable = true)
    private Long memberId;  // 알림대상 아이디

    private String message; // 메세지


    @Enumerated(EnumType.STRING)
    @Column(name = "web_target", length = 30)
    private WebTarget webTarget;  // 알림 종류
    private Long targetId;     // 해당 알림이 발생한 테이블의 아이디

    private boolean isRead;  // 읽음
    private LocalDateTime createAt;  // 알림 생성 날짜
}
