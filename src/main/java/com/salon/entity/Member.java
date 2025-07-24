package com.salon.entity;

import com.salon.constant.Gender;
import com.salon.constant.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String loginId;
    private String password;
    private String name;
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String email;
    private String tel;

    @Enumerated(EnumType.STRING)
    private Role role;
    // 계정 생성일
    private LocalDateTime createAt;

    // 위치정보제공동의 여부
    private boolean agreeLocation;
    // 웹알림전송동의 여부
    private boolean agreeAlarm;

}
