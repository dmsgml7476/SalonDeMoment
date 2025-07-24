package com.salon.repository.admin;

import com.salon.constant.Role;
import com.salon.entity.admin.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepo extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByOrderByWriteAtDesc();

    Optional<Announcement> findTopByRoleAndIdLessThanOrderByIdDesc(Role role, Long id);

    Optional<Announcement> findTopByRoleAndIdGreaterThanOrderByIdAsc(Role role, Long id);

    List<Announcement> findByRole(Role role);
}
