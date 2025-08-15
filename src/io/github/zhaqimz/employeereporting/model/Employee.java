package io.github.zhaqimz.employeereporting.model;

import java.math.BigDecimal;

/**
 * Represents an employee in the organization.
 * <p>
 * Each employee has a unique ID, name, salary, and an optional manager ID.
 * Used primarily for reporting and salary analysis.
 */
public record Employee(
        Integer id,
        String firstName,
        String lastName,
        BigDecimal salary,
        Integer managerId //null if non-existent
        ) {

        public String fullName() {
                return String.format("%s %s", firstName, lastName);
        }
}