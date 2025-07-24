package com.salon.constant;

public enum LeaveType {
    VACATION("연차"), SICK("병가"), OTHER("기타");

    private final String label;

    LeaveType(String label){
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }
}
