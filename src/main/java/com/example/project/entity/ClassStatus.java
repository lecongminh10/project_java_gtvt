package com.example.project.entity;

public enum ClassStatus {
    MOI("Mới"),
    DANG_HOC("Đang học"),
    KET_THUC("Kết thúc");

    private final String label;

    ClassStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
