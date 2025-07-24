package com.salon.constant;

public enum ServiceCategory {
    CUT("커트"), COLOR("염색"), PERM("펌"), UPSTYLE("업스타일"), DRY("드라이"), HAIR_EXTENSION("붙임머리"), CLINIC("클리닉");

    private final String label;

    ServiceCategory(String label){
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }
}
