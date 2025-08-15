package io.github.zhaqimz.employeereporting.model;

/**
 * Represents a breach in the reporting line depth for an employee.
 * <p>
 * This record is used to indicate that an employee's position in the
 * organizational hierarchy exceeds a specified reference depth.
 *
 * @param employee       The employee whose reporting line depth has been evaluated.
 * @param depthComparedTo The reference depth against which the employee's actual depth was compared.
 * @param breachedAmount  The number of levels by which the employee exceeds the reference depth.
 *                        A value of {@code 0} indicates no breach.
 */
public record ReportingLineDepthBreach(
        Employee employee,
        int depthComparedTo,
        int breachedAmount) {}