package com.salon.constant;

public enum CsCategory {
    REVIEW("리뷰"), RESERVATION("예약"), ETC("기타");

    private final String label;

    CsCategory(String label){
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }
}
