package com.salon.entity.management.master;

import com.salon.constant.AttendanceStatus;
import com.salon.entity.management.ShopDesigner;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_designer_id")
    private ShopDesigner shopDesigner;

    private LocalDateTime clockIn;
    private LocalDateTime clockOut;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @Lob
    private String note;



}