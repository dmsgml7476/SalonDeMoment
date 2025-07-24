package com.salon.entity.management;

import com.salon.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter
public class Designer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "designer_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String originalImgName;
    private String imgName;
    private String imgUrl;

    // 경력 시작일
    private LocalDate startAt;
    // 디자이너 연차
    private int workingYears;

}
