package io.github.zhaqimz.employeereporting.model;

public enum SalaryMarginStatus {
    UNDERPAID("Underpaid"),
    FAIRLY_PAID("Fair"),
    OVERPAID("Overpaid");

    private final String displayValue;

    // Constructor
    SalaryMarginStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    // Getter
    public String getDisplayValue() {
        return displayValue;
    }
}