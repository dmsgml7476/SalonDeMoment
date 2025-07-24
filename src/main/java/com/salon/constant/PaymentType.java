package com.salon.constant;

public enum PaymentType {
    CARD("카드"), CASH("현금"), TICKET("정액권");

    private final String label;

    PaymentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
