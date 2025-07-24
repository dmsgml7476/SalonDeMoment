package com.salon.entity.admin;

import com.salon.constant.CsCategory;
import com.salon.constant.CsStatus;
import com.salon.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name="cs_customer")
public class CsCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name="cs_category")
    private CsCategory category;

    private String questionText;
    private LocalDateTime questionAt;

    @Enumerated(EnumType.STRING)
    @Column(name="cs_status")
    private CsStatus status;

    @ManyToOne
    @JoinColumn(name="admin_id")
    private Member admin;

    private LocalDateTime replyAt;
    private String replyText;


}
