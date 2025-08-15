package io.github.zhaqimz.employeereporting.registry;

import io.github.zhaqimz.employeereporting.model.Employee;

import java.util.List;

/**
 * Represents the result of parsing a collection of employees from an input source.
 * <p>
 * Contains the successfully parsed employees along with any validation errors
 * encountered during parsing.
 *
 * @param employees the list of successfully parsed {@link Employee} objects
 * @param errors    the list of {@link ValidationError} instances representing issues found during parsing
 */
public record ParsedEmployeesResult(
        List<Employee> employees,
        List<ValidationError> errors
) {}