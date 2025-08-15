package io.github.zhaqimz.employeereporting.model;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Represents the result of a salary assessment for a manager
 * in relation to their direct subordinates' average salary.
 * <p>
 * This record captures the manager under review, the average salary
 * of their direct subordinates, and the expected minimum and maximum
 * relative salary percentage boundaries. It also includes the outcome
 * of the salary evaluation.
 *
 * <p>Typical use case: identifying managers who are underpaid or overpaid
 * in comparison to their team.
 *
 * @param manager                         The manager being assessed.
 * @param directSubordinatesAvgSalary     The average salary of the manager's direct subordinates.
 * @param minRelativeSalaryPercentage     The minimum expected relative salary as a percentage (optional).
 * @param maxRelativeSalaryPercentage     The maximum expected relative salary as a percentage (optional).
 * @param assessment                      The result of the salary assessment (e.g., UNDERPAID, FAIR, OVERPAID).
 */
public record ManagerRelativeSalaryAssessment(
        Employee manager,
        BigDecimal directSubordinatesAvgSalary,
        Optional<BigDecimal> minRelativeSalaryPercentage,
        Optional<BigDecimal> maxRelativeSalaryPercentage,
        SalaryAssessment assessment
) {}