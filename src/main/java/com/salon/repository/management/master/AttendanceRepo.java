package com.salon.repository.management.master;

import com.salon.entity.management.master.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepo extends JpaRepository<Attendance, Long> {

    // 해당 디자이너 근태목록
    List<Attendance> findByShopDesignerIdAndClockInBetweenOrderByIdDesc(Long designerId, LocalDateTime start, LocalDateTime end);

    // 출근 여부 (출근용)
    boolean existsByShopDesignerIdAndClockInBetween(Long id, LocalDateTime clockIn, LocalDateTime clockOut);

    // 출근 여부 (퇴근용)
    Optional<Attendance> findByShopDesignerIdAndClockInBetween(Long id, LocalDateTime dayStart, LocalDateTime dayEnd);

    // 디자이너 출근 목록
    List<Attendance> findByShopDesignerIdOrderByIdDesc(Long id);
}
