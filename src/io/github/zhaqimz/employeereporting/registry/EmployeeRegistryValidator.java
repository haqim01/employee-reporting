package io.github.zhaqimz.employeereporting.registry;

import io.github.zhaqimz.employeereporting.model.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EmployeeRegistryValidator {
    private static final Logger logger = Logger.getLogger(EmployeeRegistryValidator.class.getName());

    /**
     * Validates a list of employees against business rules and constraints.
     *
     * @param employees             The list of {@link Employee} objects to validate.
     * @param maxPermittedEmployees The maximum number of employees permitted; validation will fail if exceeded.
     * @return                      A list of {@link ValidationError} instances representing any validation issues found.
     *                              The list is empty if all employees pass validation.
     */
    public static List<ValidationError> validateEmployees(List<Employee> employees, int maxPermittedEmployees) {
        List<ValidationError> errors = new ArrayList<>();

        errors.addAll(validateMaxEmployees(employees, maxPermittedEmployees));
        errors.addAll(validateEmployeeIds(employees));
        errors.addAll(validateManagerIds(employees));
        errors.addAll(validateSingleTopLevelManager(employees));

        return errors;
    }

    /**
     * Validates that the number of employees does not exceed the maximum permitted limit.
     *
     * @param employees             The list of {@link Employee} objects to validate.
     * @param maxPermittedEmployees The maximum allowed number of employees.
     * @return                      A list of {@link ValidationError} instances if the employee count exceeds the limit;
     *                              otherwise, an empty list.
     */
    static List<ValidationError> validateMaxEmployees(List<Employee> employees, int maxPermittedEmployees) {
        logger.info("Executing validation of maximum employees restriction");
        List<ValidationError> errors = new ArrayList<>();
        if (employees.size() > maxPermittedEmployees) {
            errors.add(new ValidationError(
                    ValidationErrorType.MAXIMUM_EMPLOYEES_EXCEEDED,
                    String.format("Number of employees [%d] exceeds the maximum permitted [%d]", employees.size(), maxPermittedEmployees)
            ));
        }
        return errors;
    }

    /**
     * Validates the uniqueness of employee IDs within the given list of employees.
     *
     * @param employees The list of {@link Employee} objects to validate.
     * @return          A list of {@link ValidationError} instances found with duplicate employee IDs.
     *                  Returns an empty list if all IDs are valid and unique.
     */
    static List<ValidationError> validateEmployeeIds(List<Employee> employees) {
        logger.info("Executing validation of employee id integrity");
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::id, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(entry -> new ValidationError(
                        ValidationErrorType.DUPLICATE_EMPLOYEE_ID,
                        String.format("Duplicate Employee Id [%d] found", entry.getKey())
                ))
                .toList();
    }

    /**
     * Validates that all manager IDs referenced by employees exist within the provided list of employees.
     *
     * <p>This ensures that each non-null {@code managerId} in the employee records corresponds to a valid employee ID
     * in the same list.
     *
     * @param employees The list of {@link Employee} objects to validate.
     * @return          A list of {@link ValidationError} instances for any invalid or unresolvable manager IDs.
     *                  Returns an empty list if all manager IDs are valid or null.
     */
    static List<ValidationError> validateManagerIds(List<Employee> employees) {
        logger.info("Executing validation of manager id integrity");
        Set<Integer> employeeIds = employees.stream()
                .map(Employee::id)
                .collect(Collectors.toSet());

        List<ValidationError> errors = new ArrayList<>();
        for (Employee employee : employees) {
            Integer managerId = employee.managerId();
            if (managerId != null && !employeeIds.contains(managerId)) {
                errors.add(new ValidationError(
                        ValidationErrorType.UNKNOWN_MANAGER_ID,
                        String.format("Manager Id [%d] for Employee Id [%d] could not be identified", managerId, employee.id())
                ));
            }
        }
        return errors;
    }

    /**
     * Validates that there is exactly one top-level manager in the list.
     *
     * <p>A top-level manager is defined as an employee whose {@code managerId} is {@code null}.
     *
     * @param employees The list of {@link Employee} objects to validate.
     * @return          A list of {@link ValidationError} instances if there are zero or multiple top-level managers;
     *                  otherwise, an empty list if exactly one top-level manager is found.
     */
    static List<ValidationError> validateSingleTopLevelManager(List<Employee> employees) {
        logger.info("Executing validation of single top line manager");
        List<Employee> topLevelManagers = employees.stream()
                .filter(e -> e.managerId() == null)
                .toList();

        List<ValidationError> errors = new ArrayList<>();
        if (topLevelManagers.size() > 1) {
            errors.add(new ValidationError(
                    ValidationErrorType.MULTIPLE_TOP_LEVEL_MANAGERS,
                    String.format("Only one top-level manager (null managerId) is allowed, but found [%d]", topLevelManagers.size())
            ));
        }
        return errors;
    }
}