package com.salon.config;

import com.salon.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole())); // 예: ROLE_USER, ROLE_ADMIN
    }

    public Member getMember(){
        return this.member;
    }

    public Long getId() {
        return member.getId();
    }

    public String getName() {
        String name = member.getName();
        return name != null ? name : "이름없음";

    }

    @Override
    public String getPassword() {
        return member.getPassword(); // 암호화된 비밀번호
    }

    @Override
    public String getUsername() {
        return member.getLoginId(); // 로그인 ID
    }



    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }



}
