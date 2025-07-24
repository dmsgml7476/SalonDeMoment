package com.salon.dto.user;

import com.salon.constant.Gender;
import com.salon.entity.Member;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Getter
@Setter
public class SignUpDto {
    private String loginId;
    private String password;
    private String name;

    @Email
    private String email;
    private String tel;
    private Gender gender;
    private LocalDate birthDate;
    private boolean agreeAlarm;
    private boolean agreeLocation;

    public Member to(PasswordEncoder passwordEncoder) {
        Member member = new Member();

        member.setLoginId(this.loginId);
        member.setPassword(passwordEncoder.encode(this.password));
        member.setName(this.name);
        member.setEmail(this.email);
        member.setTel(this.tel);
        member.setGender(this.gender);
        member.setBirthDate(this.birthDate);
        member.setAgreeAlarm(this.agreeAlarm);
        member.setAgreeLocation(this.agreeLocation);

        return member;


    }

}
