package com.salon.dto.management;

import com.salon.entity.management.master.Attendance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class AttendanceStatusDto {

    private boolean isWorking;
    private String clockIn;
    private String clockOut;

}
