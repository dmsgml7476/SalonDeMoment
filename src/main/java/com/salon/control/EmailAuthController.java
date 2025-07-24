package com.salon.control;

import com.salon.entity.Member;
import com.salon.repository.MemberRepo;
import com.salon.service.user.EmailAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.StringTokenizer;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailAuthController {

    private final EmailAuthService emailAuthService;
    private final MemberRepo memberRepo;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/check")
    public ResponseEntity<?> checkEmailExists(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        boolean exists = memberRepo.existsByEmail(email);

        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendAuthCode(@RequestBody Map<String, String> request, HttpSession session) {
        String email = request.get("email");
        String context = request.get("context");

        boolean result = emailAuthService.sendCodeEmail(email, context, session);
        if (result) {
            return ResponseEntity.ok(Map.of("status", "sent"));
        } else {
            return ResponseEntity.status(500).body(Map.of("status", "fail"));
        }
    }


    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request, HttpSession session) {
        String inputCode = request.get("code");
        String sessionCode = (String) session.getAttribute("authCode");
        String email = (String) session.getAttribute("authEmail");

        boolean success = sessionCode != null && sessionCode.equals(inputCode);
        if (success) {
            session.setAttribute("authSuccess", true);
        }

        return ResponseEntity.ok(Map.of("success", success));
    }


    @PostMapping("/find-id")
    public ResponseEntity<?> findLoginIdByEmail(HttpSession session) {
        String email = (String) session.getAttribute("authEmail");
        Boolean verified = (Boolean) session.getAttribute("authSuccess");

        if (email == null || verified == null || !verified) {
            return ResponseEntity.status(403).body(Map.of("error", "이메일 인증이 필요합니다."));
        }

        Member member = memberRepo.findByEmail(email);

        if(member == null) {
            return ResponseEntity.status(404).body(Map.of("error", "존재하지 않는 이메일입니다."));
        }

        return ResponseEntity.ok(Map.of("loginId", member.getLoginId()));
    }


    @PostMapping("/reset-complete")
    public ResponseEntity<?> sendPasswordResetNotice(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        boolean result = emailAuthService.sendPasswordResetNoticeEmail(email);
        return ResponseEntity.ok(Map.of("sent", result));
    }

    @PostMapping("/editPw")
    @ResponseBody
    public String findPwUpdatePw(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        Member member = memberRepo.findByEmail(email);
        if(member==null) {
            return "not_found";
        }

        String encodePw = passwordEncoder.encode(newPassword);
        member.setPassword(encodePw);
        memberRepo.save(member);

        System.out.println("비밀번호 변경 성공: " + email);
        emailAuthService.sendPasswordResetNoticeEmail(email);

        return "success";
    }

}
