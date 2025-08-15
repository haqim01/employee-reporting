package io.github.zhaqimz.employeereporting.model;

import java.math.BigDecimal;

/**
 * Represents the outcome of a salary margin assessment for an employee.
 * <p>
 * The assessment includes the status indicating whether the employee is
 * underpaid, fairly paid, or overpaid, and the monetary amount by which
 * their salary deviates from the expected range.
 *
 * @param status        The result of the salary evaluation, such as UNDERPAID, FAIR, or OVERPAID.
 * @param breachAmount  The amount by which the salary breaches the expected threshold.
 *                      May be zero if the salary is within acceptable margins.
 */
public record SalaryAssessment(SalaryMarginStatus status, BigDecimal breachAmount) {}