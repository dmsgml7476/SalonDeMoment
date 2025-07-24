package com.salon.entity.admin;

import com.salon.constant.Role;
import com.salon.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Getter @Setter
@Table(name="announcement")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="admin_id")
    private Member admin;

    private String title;
    private String content;
    private LocalDateTime writeAt;

    @Enumerated(EnumType.STRING)
    @Column(name="role")
    private Role role;

}
