package com.salon.constant;

public enum LeaveStatus {
    REQUESTED("대기"), APPROVED("승인"), REJECTED("반려");

    private final String label;

    LeaveStatus(String label){
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }
}
