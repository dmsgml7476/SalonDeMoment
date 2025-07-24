package com.salon.dto.management;

import com.salon.constant.AttendanceStatus;
import com.salon.entity.management.master.Attendance;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter @Setter
public class AttendanceListDto {

    private Long id; // Attendance Id
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String status;
    private String workTimeStr; // 근무시간 9시간 50분

    public static AttendanceListDto from (Attendance attendance) {

        AttendanceListDto dto = new AttendanceListDto();
        dto.setId(attendance.getId());
        dto.setClockIn(attendance.getClockIn());
        dto.setClockOut(attendance.getClockOut());
        dto.setStatus(attendance.getStatus().getLabel());

        // 근무 시간 계산
        if (attendance.getClockIn() != null && attendance.getClockOut() != null) {
            Duration duration = Duration.between(attendance.getClockIn(), attendance.getClockOut());
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart(); // Java 9+
            dto.setWorkTimeStr(hours + "시간 " + minutes + "분");
        } else {
            dto.setWorkTimeStr("-");
        }

        return dto;
    }

}
