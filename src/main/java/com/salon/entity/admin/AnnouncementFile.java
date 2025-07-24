package com.salon.entity.admin;

import jakarta.persistence.*;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="announcement_file")
public class AnnouncementFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @ManyToOne
    @JoinColumn(name="announcement_id")
    private Announcement announcement;
    private String originalName;
    private String fileName;
    private String fileUrl;
}

