package com.salon.config;

import com.salon.dto.WebNotificationDto;
import com.salon.repository.WebNotificationRepo;
import com.salon.service.WebNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.salon.config.CustomUserDetails;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

    private final WebNotificationService webNotificationService;

    @ModelAttribute("isAdmin")
    public boolean addIsAdmin(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            return userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    @ModelAttribute("userRole")
    public String populateUserRole(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getMember() == null) {
            return null;
        }
        System.out.println("==> userRole: " + userDetails.getMember().getRole().name());
        return userDetails.getMember().getRole().name(); // Enum → String
    }


    // 이거 웹알림 전역

    @ModelAttribute
    public void addNotificationInfo(@AuthenticationPrincipal CustomUserDetails user,
                                    org.springframework.ui.Model model) {

        if (user == null) {
            model.addAttribute("currentUserId", null);
            model.addAttribute("unreadCnt", 0L);
            model.addAttribute("unreadTop3", List.of());   // ← 빈 목록
        } else {
            Long memberId  = user.getId();
            long unreadCnt = webNotificationService.countUnreadByMemberId(memberId);

            model.addAttribute("currentUserId", memberId);
            model.addAttribute("unreadCnt", unreadCnt);

            // ✨ 여기서 3건 조회해 모델에 추가
            List<WebNotificationDto> top3 = webNotificationService.getUnreadTop3(memberId);
            model.addAttribute("unreadTop3", top3);
        }
    }

}