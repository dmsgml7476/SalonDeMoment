package com.salon.service.user;

import com.salon.config.CustomUserDetails;
import com.salon.constant.Role;
import com.salon.dto.user.SignUpDto;
import com.salon.entity.Member;
import com.salon.repository.MemberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepo memberRepo;
    private final PasswordEncoder passwordEncoder;


//    @Override
//    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
//        Member member= memberRepo.findByLoginId(loginId);
//
//        if(member == null) {
//            throw  new UsernameNotFoundException(loginId);
//        }
//
//        return new CustomUserDetails(member);
//    }

    @Transactional
    public void register(SignUpDto dto) {
        Member member = dto.to(passwordEncoder);
        member.setCreateAt(LocalDateTime.now());
        member.setRole(Role.USER);

        memberRepo.save(member);
    }

    // 로그인아이디 중복 확인
    public boolean existsByLoginId(String loginId) {
        return memberRepo.existsByLoginId(loginId);
    }


    // 알림 동의
    @Transactional
    public void updateAgreeLocation(Long memberId) {
        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        member.setAgreeLocation(true); // 동의 처리
    }
}
