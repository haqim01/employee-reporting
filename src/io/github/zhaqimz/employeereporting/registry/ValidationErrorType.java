package io.github.zhaqimz.employeereporting.registry;

public enum ValidationErrorType {
    UNKNOWN_MANAGER_ID,
    MULTIPLE_TOP_LEVEL_MANAGERS,
    MAXIMUM_EMPLOYEES_EXCEEDED,
    INVALID_FIELD,
    INCOMPLETE_DATA_ROW,
    DUPLICATE_EMPLOYEE_ID
}