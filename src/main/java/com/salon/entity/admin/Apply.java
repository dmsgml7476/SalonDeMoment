package com.salon.entity.admin;

import com.salon.constant.ApplyStatus;
import com.salon.constant.ApplyType;
import com.salon.constant.CsCategory;
import com.salon.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name="apply")
public class Apply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name="apply_type")
    private ApplyType applyType;

    private String applyNumber;
    private String issuedDate;
    private LocalDateTime createAt;

    @ManyToOne
    @JoinColumn(name="admin_id")
    private Member admin;

    @Enumerated(EnumType.STRING)
    @Column(name="apply_status")
    private ApplyStatus status;

    public String getStatusLabel(){
        return status != null ? status.getLabel() : "";
    }

    public ApplyStatus getStatus(){
        return status;
    }

    public void setApplyStatus(ApplyStatus status){
        this.status = status;
    }

    private LocalDateTime approveAt;

}
