package com.salon.entity.management;

import com.salon.constant.LeaveStatus;
import com.salon.constant.LeaveType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@Entity
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_request_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_designer_id")
    private ShopDesigner shopDesigner;

    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;

    @Lob // varchar -> text
    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    private LocalDateTime requestAt;
    private LocalDateTime approvedAt;



}
