package com.salon.repository.admin;

import com.salon.entity.admin.Announcement;
import com.salon.entity.admin.AnnouncementFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementFileRepo extends JpaRepository<AnnouncementFile, Long> {

    List<AnnouncementFile> findByAnnouncement(Announcement announcement);

}
