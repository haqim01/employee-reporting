package io.github.zhaqimz.employeereporting.reports;

import io.github.zhaqimz.employeereporting.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.math.RoundingMode;
public class SalaryAnalyser {
    private static final Logger logger = Logger.getLogger(SalaryAnalyser.class.getName());

    /**
     * Returns a list of managers whose salary is not at least (1 + percentageMargin) * average subordinate salary.
     *
     * @param employees         List of all employees
     * @param percentageMargin  e.g. 0.20 for 20% minimum salary margin
     * @return List of managers who violate the salary margin rule
     */
    public static List<ManagerRelativeSalaryAssessment> assessManagerSalary(
            List<Employee> employees,
            Optional<BigDecimal> minRelativeSalaryPercentage,
            Optional<BigDecimal> maxRelativeSalaryPercentage
    ) {
        logger.info("Executing Manager Salary assessment");
        // Validate inputs
        Objects.requireNonNull(employees, "Employee list is null");

        minRelativeSalaryPercentage.ifPresent(min -> {
            if (min.compareTo(BigDecimal.ZERO) < 0) {
                logger.severe("Minimum Relative Salary Percentage must be >= 0.0");
                throw new IllegalArgumentException("Minimum Relative Salary Percentage must be >= 0.0");
            }
        });

        maxRelativeSalaryPercentage.ifPresent(max -> {
            if (max.compareTo(BigDecimal.ZERO) < 0) {
                logger.severe("Maximum Relative Salary Percentage must be >= 0.0");
                throw new IllegalArgumentException("Maximum Relative Salary Percentage must be >= 0.0");
            }
        });

        if (maxRelativeSalaryPercentage.flatMap(max ->
                minRelativeSalaryPercentage.map(min -> max.compareTo(min) < 0)
                ).orElse(false)) {
            logger.severe("Maximum Relative Salary Percentage must be greater than or equal to Minimum Relative Salary Percentage");
            throw new IllegalArgumentException("Maximum Relative Salary Percentage must be greater than or equal to Minimum Relative Salary Percentage");
        }

        // Group employees by their manager ID
        Map<Integer, List<Employee>> reportsByManagerId = employees.stream()
                .filter(e -> e.managerId() != null)
                .collect(Collectors.groupingBy(Employee::managerId));

        // Setup employee lookup
        Map<Integer, Employee> employeeById = employees.stream()
                .collect(Collectors.toMap(Employee::id, Function.identity()));

        // Analyse each manager
        return reportsByManagerId.entrySet().stream()
                .map(entry -> {
                    int managerId = entry.getKey();
                    List<Employee> subordinates = entry.getValue();
                    Employee manager = employeeById.get(managerId);

                    if (manager == null || subordinates.isEmpty()) return null;

                    BigDecimal avgDirectSubSalary = subordinates.isEmpty()
                            ? BigDecimal.ZERO
                            : subordinates.stream()
                            .map(Employee::salary) // assuming salary() returns BigDecimal
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(subordinates.size()), 2, RoundingMode.HALF_UP);

                    BigDecimal actualSalary = manager.salary();
                    BigDecimal expectedMinSalary = minRelativeSalaryPercentage
                            .map(min -> avgDirectSubSalary.multiply(BigDecimal.ONE.add(min)))
                            .orElse(new BigDecimal(0)).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal expectedMaxSalary = maxRelativeSalaryPercentage
                            .map(max -> avgDirectSubSalary.multiply(BigDecimal.ONE.add(max)))
                            .orElse(new BigDecimal(Double.MAX_VALUE)).setScale(2, RoundingMode.HALF_UP);

                    SalaryAssessment assessment = assessSalary(
                            actualSalary,
                            minRelativeSalaryPercentage,
                            expectedMinSalary,
                            maxRelativeSalaryPercentage,
                            expectedMaxSalary
                    );

                    return new ManagerRelativeSalaryAssessment(
                            manager,
                            avgDirectSubSalary,
                            minRelativeSalaryPercentage,
                            maxRelativeSalaryPercentage,
                            assessment
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Evaluates an employee's actual salary against expected minimum and maximum salary thresholds
     * derived from relative margin percentages, and determines their salary status.
     *
     * <p>This method compares the given {@code actualSalary} with calculated salary bounds
     * and returns a {@link SalaryAssessment} indicating whether the salary is
     * {@code UNDERPAID}, {@code OVERPAID}, or {@code FAIR}, along with the breach amount if applicable.
     *
     * @param actualSalary         The employee's actual salary.
     * @param minMarginPercentage  The optional minimum relative salary margin percentage (e.g., 110% of subordinates' average).
     * @param expectedMinSalary    The calculated expected minimum salary based on the minimum margin.
     * @param maxMarginPercentage  The optional maximum relative salary margin percentage (e.g., 200% of subordinates' average).
     * @param expectedMaxSalary    The calculated expected maximum salary based on the maximum margin.
     * @return                     A {@link SalaryAssessment} representing the status and amount by which the salary deviates from expected bounds.
     */
    private static SalaryAssessment assessSalary(
            BigDecimal actualSalary,
            Optional<BigDecimal> minMarginPercentage,
            BigDecimal expectedMinSalary,
            Optional<BigDecimal> maxMarginPercentage,
            BigDecimal expectedMaxSalary
    ) {
        if (minMarginPercentage.isPresent() && (actualSalary.compareTo(expectedMinSalary) < 0)) {
            return new SalaryAssessment(SalaryMarginStatus.UNDERPAID, expectedMinSalary.subtract(actualSalary));
        } else if (maxMarginPercentage.isPresent() && (actualSalary.compareTo(expectedMaxSalary) > 0)) {
            return new SalaryAssessment(SalaryMarginStatus.OVERPAID, actualSalary.subtract(expectedMaxSalary));
        } else {
            return new SalaryAssessment(SalaryMarginStatus.FAIRLY_PAID, new BigDecimal(0));
        }
    }

    /**
     * Generates a formatted report of managers whose salary margin status matches the specified type.
     *
     * <p>The report includes manager details along with their salary, average salary of direct subordinates,
     * and the assessment result.
     *
     * @param assessments              The list of {@link ManagerRelativeSalaryAssessment} containing salary assessments for managers.
     * @param reportSalaryMarginStatus The specific {@link SalaryMarginStatus} to filter by (e.g., {@code UNDERPAID}, {@code OVERPAID}).
     * @return                         A {@link String} containing the formatted report for the matching assessments.
     *                                 Returns an empty string if no matches are found.
     */
    public static String generateSalaryMarginStatusReport(
            List<ManagerRelativeSalaryAssessment> assessments,
            SalaryMarginStatus reportSalaryMarginStatus) {
        logger.info("Generating salary margin status report.");

        if (assessments == null) {
            return "No assessment data found to report";
        }
        var reportAssessments = assessments.stream()
                .filter(mrsa -> mrsa.assessment().status() == reportSalaryMarginStatus)
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("Following managers have a current salary status of : " + reportSalaryMarginStatus.getDisplayValue() + "\n");
        sb.append(String.format("%-20s %-10s %-10s %-15s %-10s%n", "Name", "ID", "ManagerID", "Salary", "Breach"));
        sb.append("----------------------------------------------------------------------\n");
        for (ManagerRelativeSalaryAssessment a : reportAssessments) {
            sb.append(String.format("%-20s %-10d %-10s %-15.2f %-10.2f%n",
                    a.manager().fullName(),
                    a.manager().id(),
                    a.manager().managerId() != null ? a.manager().managerId().toString() : "N/A",
                    a.manager().salary(),
                    a.assessment().breachAmount()));
        }

        return sb.toString();
    }
}