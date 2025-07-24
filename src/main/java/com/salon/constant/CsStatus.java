package com.salon.constant;

public enum CsStatus {
    WAITING("대기"), COMPLETED("완료");

    private final String label;

    CsStatus(String label){
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }
}
