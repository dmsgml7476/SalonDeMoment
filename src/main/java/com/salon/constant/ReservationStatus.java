package com.salon.constant;

public enum ReservationStatus {
    RESERVED("예약중"),CANCELED("예약취소"),COMPLETED("예약완료"),NOSHOW("노쇼"); // 순서대로 예약중, 예약취소, 예약완료, 노쇼

    private final String label;

    ReservationStatus(String label){
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }
}
