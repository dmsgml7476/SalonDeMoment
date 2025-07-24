package com.salon.service;

import com.salon.constant.WebTarget;
import com.salon.dto.WebNotificationDto;
import com.salon.entity.admin.WebNotification;
import com.salon.repository.WebNotificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebNotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final WebNotificationRepo webNotificationRepo;


    //웹알림 저장 웹소켓 전송

    public void notify(Long receiverId, String message, WebTarget target, Long targetId) {
        // 알림 객체 생성
        WebNotificationDto dto = new WebNotificationDto();
        dto.setMessage(message);
        dto.setReceiverId(receiverId);
        dto.setWebTarget(target);
        dto.setTargetId(targetId);

        // Entity로 변환 및 저장
        WebNotification saved = webNotificationRepo.save(dto.to());

        // 웹소켓 전송
        sendWebSocketNotification(receiverId, saved);
    }

    // 웹 알림 발생하는 시점에서 receiverId 저장해서(받는 유저) 넘기기
    public void sendWebSocketNotification(Long receiverId, WebNotification entity) {
        String destination = "/topic/notify/" + receiverId;

        long unreadCnt = countUnreadByMemberId(receiverId);
        WebNotificationDto dto = WebNotificationDto.from(entity);
        dto.setUnreadTotal(unreadCnt);

        System.out.println("웹소켓 알림 전송 시도 → 대상: " + destination);
        System.out.println("알림 내용: " + dto.getMessage());

        messagingTemplate.convertAndSend(destination, dto);

        System.out.println("웹소켓 알림 전송 완료");
    }

    // 웹 알림 읽음 표시
    public void markAsReadByTarget(WebTarget webTarget, Long targetId) {
        WebNotification webNotification = webNotificationRepo.findTopByWebTargetAndTargetIdOrderByCreateAtDesc(webTarget, targetId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림이 존재하지 않습니다."));

        if (!webNotification.isRead()) {
            webNotification.setRead(true);
            webNotificationRepo.save(webNotification);
        }
    }

    public void markAllAsReadByMember(Long memberId) {
        List<WebNotification> notifications = webNotificationRepo
                .findAllByMemberIdAndIsReadFalse(memberId);

        for (WebNotification n : notifications) {
            n.setRead(true);
        }

        webNotificationRepo.saveAll(notifications);
    }


    // 회원별 미읽음 알림 건수

    public long countUnreadByMemberId(Long memberId) {
        return webNotificationRepo.countByMemberIdAndIsReadFalse(memberId);
    }

    public List<WebNotificationDto> getUnreadTop3(Long memberId) {
        return webNotificationRepo
                .findTop3ByMemberIdAndIsReadFalseOrderByCreateAtDesc(memberId)
                .stream()
                .map(WebNotificationDto::from)
                .toList();
    }

    public void notify(Long receiverId, String message, WebTarget target, Long targetId, Map<String, String> extraData) {
        WebNotification noti = new WebNotification();
        noti.setMemberId(receiverId);
        noti.setMessage(message);
        noti.setWebTarget(target);
        noti.setTargetId(targetId);

        webNotificationRepo.save(noti);
    }
}
