package com.salon.constant;

public enum AttendanceStatus {
    PRESENT("출근"), LEFT("퇴근"), LATE("지각"), LEFT_EARLY("조퇴"), ABSENT("결근");
    
    private final String label;

    AttendanceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
