package com.salon.constant;

public enum ApplyStatus {
    WAITING("대기"), APPROVED("승인"), REJECTED("거절");

    private final String label;

    ApplyStatus(String label){
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }
}
