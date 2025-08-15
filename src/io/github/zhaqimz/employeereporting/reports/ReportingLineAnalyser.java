package io.github.zhaqimz.employeereporting.reports;

import io.github.zhaqimz.employeereporting.model.Employee;
import io.github.zhaqimz.employeereporting.model.ReportingLineDepthBreach;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportingLineAnalyser {
    private static final Logger logger = Logger.getLogger(ReportingLineAnalyser.class.getName());

    /**
     * Identifies employees whose reporting line depth exceeds the specified threshold.
     *
     * <p>The reporting line depth is defined as the number of levels between an employee and the top-level manager.
     * This method calculates the depth for each employee and returns a list of those who exceed the given depth,
     * along with the amount by which they exceed it.
     *
     * @param employees         The list of {@link Employee} objects representing the organization.
     * @param depthToCompareTo  The maximum allowed depth in the reporting line hierarchy.
     * @return                  A list of {@link ReportingLineDepthBreach} instances, each representing an employee
     *                          whose reporting line depth exceeds the threshold. Returns an empty list if no breaches are found.
     */
    public static List<ReportingLineDepthBreach> findEmployeesBreachingReportingLineDepth(List<Employee> employees, int depthToCompareTo) {
        logger.info("Executing Find Employees breaching reporting line depth");
        Map<Integer, Employee> employeeMap = employees.stream()
                .collect(Collectors.toMap(Employee::id, Function.identity()));

        List<ReportingLineDepthBreach> result = new ArrayList<>();

        for (Employee employee : employees) {
            int depth = depthToTopLevelManager(employee, employeeMap);

            if (depth > depthToCompareTo) {
                int breach = depth - depthToCompareTo;
                result.add(new ReportingLineDepthBreach(employee, depthToCompareTo, breach));
            }
        }

        return result;
    }

    /**
     * Calculates the reporting line depth of the given employee relative to the top-level manager.
     *
     * <p>The depth is defined as the number of management levels between the employee
     * and the top-level manager (an employee with a {@code null} {@code managerId}).
     * This method traverses the management chain upward using the provided employee map.
     *
     * @param employee     The {@link Employee} whose reporting depth is being calculated.
     * @param employeeMap  A map of employee IDs to {@link Employee} objects, used to resolve manager relationships.
     * @return             The number of levels between the employee and the top-level manager.
     *                     Returns {@code 0} if the employee is the top-level manager.
     */
    private static int depthToTopLevelManager(Employee employee, Map<Integer, Employee> employeeMap) {
        return (int) Stream.iterate(
                employee.managerId(),
                Objects::nonNull, //while managerId != null
                managerId -> {
                    Employee manager = employeeMap.get(managerId);
                    return (manager != null) ? manager.managerId() : null;
                }
        ).count();
    }

    /**
     * Generates a formatted textual report of employees who have breached the allowed reporting line depth.
     *
     * <p>This report includes employee details along with the depth they were compared against
     * and how much they exceeded it by.
     *
     * @param breaches A list of {@link ReportingLineDepthBreach} instances representing employees
     *                 who exceed the permitted reporting line depth.
     * @return         A {@link String} containing the formatted report. Returns an empty string if the input list is empty.
     */
    public static String generateReportingLineDepthBreachReport(
            List<ReportingLineDepthBreach> breaches) {
        logger.info("Executing generation of reporting line depth breach report");
        if (breaches == null) {
            return "No breach data found to report";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Following managers are breaching the prescribed reporting line depth:\n");
        sb.append(String.format("%-20s %-10s %-10s %-10s %-10s%n", "Name", "ID", "ManagerID", "Depth", "Breached Amount"));
        sb.append("----------------------------------------------------------------------\n");
        for (ReportingLineDepthBreach b : breaches) {
            sb.append(String.format("%-20s %-10d %-10s %-10d %-10d%n",
                    b.employee().fullName(),
                    b.employee().id(),
                    b.employee().managerId() != null ? b.employee().managerId().toString() : "N/A",
                    b.depthComparedTo(),
                    b.breachedAmount()));
        }

        return sb.toString();
    }
}