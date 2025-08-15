package io.github.zhaqimz.employeereporting.registry;

import io.github.zhaqimz.employeereporting.model.Employee;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeRegistryValidatorTest {

    @Test
    void validateMaxEmployees_withUnderLimit_returnsNoErrors() {
        // Given
        final int MAX_EMPLOYEES = 1000;
        List<Employee> employees = createDummyEmployees(MAX_EMPLOYEES);

        // When
        List<ValidationError> errors = EmployeeRegistryValidator.validateMaxEmployees(employees, MAX_EMPLOYEES);

        // Then
        assertTrue(errors.isEmpty(), "Expected no validation errors when under max employee limit");
    }

    @Test
    void validateMaxEmployees_withOverLimit_returnsValidationError() {
        // Given
        final int MAX_EMPLOYEES = 1000;
        List<Employee> employees = createDummyEmployees(MAX_EMPLOYEES + 1);

        // When
        List<ValidationError> errors = EmployeeRegistryValidator.validateMaxEmployees(employees, MAX_EMPLOYEES);

        // Then
        assertEquals(1, errors.size());
        List<ValidationError> expectedErrors = List.of(
                new ValidationError(ValidationErrorType.MAXIMUM_EMPLOYEES_EXCEEDED, "Number of employees [1001] exceeds the maximum permitted [1000]")
        );
        assertEquals(expectedErrors, errors);
    }

    @Test
    void validateEmployeeIds_noDuplicates_returnsEmptyList() {
        // Given
        List<Employee> employees = createDummyEmployees(5);

        // When
        List<ValidationError> errors = EmployeeRegistryValidator.validateEmployeeIds(employees);

        // Then
        assertTrue(errors.isEmpty(), "Expected no validation errors for unique IDs");
    }

    @Test
    void validateEmployeeIds_withDuplicates_returnsErrors() {
        // Given
        List<Employee> employees = List.of(
                new Employee(1, "Joe", "Doe", new BigDecimal(60000), null),
                new Employee(2, "Martin", "Chekov", new BigDecimal(45000), 123),
                new Employee(1, "Bob", "Ronstad", new BigDecimal(47000), 123),
                new Employee(3, "Alice", "Hasacat", new BigDecimal(50000), 124),
                new Employee(2, "Brett", "Hardleaf", new BigDecimal(34000), 300)
        );

        // When
        List<ValidationError> errors = EmployeeRegistryValidator.validateEmployeeIds(employees);

        // Then
        assertEquals(2, errors.size());
        List<ValidationError> expectedErrors = List.of(
                new ValidationError(ValidationErrorType.DUPLICATE_EMPLOYEE_ID, "Duplicate Employee Id [1] found"),
                new ValidationError(ValidationErrorType.DUPLICATE_EMPLOYEE_ID, "Duplicate Employee Id [2] found")
        );
        assertEquals(expectedErrors, errors);
    }

    @Test
    void validateManagerIds_withValidManagers_returnsEmptyList() {
        // Given
        List<Employee> employees = List.of(
                new Employee(1, "Joe", "Doe", new BigDecimal(60000), null),
                new Employee(2, "Martin", "Chekov", new BigDecimal(45000), 1),
                new Employee(3, "Bob", "Ronstad", new BigDecimal(47000), 1),
                new Employee(4, "Alice", "Hasacat", new BigDecimal(50000), 2),
                new Employee(5, "Brett", "Hardleaf", new BigDecimal(34000), 3)
        );

        // When
        List<ValidationError> errors = EmployeeRegistryValidator.validateManagerIds(employees);

        // Then
        assertTrue(errors.isEmpty(), "Expected no validation errors for valid manager IDs");
    }

    @Test
    void validateManagerIds_withUnknownManager_returnsError() {
        // Given
        List<Employee> employees = List.of(
                new Employee(1, "Joe", "Doe", new BigDecimal(60000), null),
                new Employee(2, "Martin", "Chekov", new BigDecimal(45000), 1),
                new Employee(3, "Bob", "Ronstad", new BigDecimal(47000), 1),
                new Employee(4, "Alice", "Hasacat", new BigDecimal(50000), 6), //unknown manager
                new Employee(5, "Brett", "Hardleaf", new BigDecimal(34000), 7) //unknown manager
        );

        // When
        List<ValidationError> errors = EmployeeRegistryValidator.validateManagerIds(employees);

        // Then
        assertEquals(2, errors.size());
        List<ValidationError> expectedErrors = List.of(
                new ValidationError(ValidationErrorType.UNKNOWN_MANAGER_ID, "Manager Id [6] for Employee Id [4] could not be identified"),
                new ValidationError(ValidationErrorType.UNKNOWN_MANAGER_ID, "Manager Id [7] for Employee Id [5] could not be identified")
        );
        assertEquals(expectedErrors, errors);
    }

    @Test
    void oneTopLevelManager_returnsNoErrors() {
        // Given
        List<Employee> employees = List.of(
                new Employee(1, "Joe", "Doe", new BigDecimal(60000), null),
                new Employee(2, "Martin", "Chekov", new BigDecimal(45000), 1),
                new Employee(3, "Bob", "Ronstad", new BigDecimal(47000), 1),
                new Employee(4, "Alice", "Hasacat", new BigDecimal(50000), 6), //unknown manager
                new Employee(5, "Brett", "Hardleaf", new BigDecimal(34000), 7) //unknown manager
        );

        // When
        List<ValidationError> errors = EmployeeRegistryValidator.validateSingleTopLevelManager(employees);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    void multipleTopLevelManagers_returnsErrorsForEach() {
        // Given
        List<Employee> employees = List.of(
                new Employee(1, "Joe", "Doe", new BigDecimal(60000), null),
                new Employee(2, "Martin", "Chekov", new BigDecimal(45000), null),
                new Employee(3, "Bob", "Ronstad", new BigDecimal(47000), 1),
                new Employee(4, "Alice", "Hasacat", new BigDecimal(50000), 6), //unknown manager
                new Employee(5, "Brett", "Hardleaf", new BigDecimal(34000), 7) //unknown manager
        );

        // When
        List<ValidationError> errors = EmployeeRegistryValidator.validateSingleTopLevelManager(employees);

        // Then
        assertEquals(1, errors.size());
        List<ValidationError> expectedErrors = List.of(
                new ValidationError(ValidationErrorType.MULTIPLE_TOP_LEVEL_MANAGERS, "Only one top-level manager (null managerId) is allowed, but found [2]")
        );
        assertEquals(expectedErrors, errors);
    }

    @Test
    void noTopLevelManager_returnsNoErrors() {
        // Given
        List<Employee> employees = List.of(
                new Employee(1, "Joe", "Doe", new BigDecimal(60000), 1),
                new Employee(2, "Martin", "Chekov", new BigDecimal(45000), 1),
                new Employee(3, "Bob", "Ronstad", new BigDecimal(47000), 1),
                new Employee(4, "Alice", "Hasacat", new BigDecimal(50000), 1),
                new Employee(5, "Brett", "Hardleaf", new BigDecimal(34000), 1)
        );

        // When
        List<ValidationError> errors = EmployeeRegistryValidator.validateSingleTopLevelManager(employees);

        // Then
        assertTrue(errors.isEmpty(), "No top-level manager is a valid scenario");
    }

    @Test
    void validateEmployees_withValidData_returnsNoErrors() {
        // Given
        List<Employee> employees = List.of(
                new Employee(1, "Joe", "Doe", new BigDecimal(60000), null),
                new Employee(2, "Martin", "Chekov", new BigDecimal(45000), 1),
                new Employee(3, "Bob", "Ronstad", new BigDecimal(47000), 2),
                new Employee(4, "Alice", "Hasacat", new BigDecimal(50000), 3),
                new Employee(5, "Brett", "Hardleaf", new BigDecimal(34000), 4)
        );

        // When
        List<ValidationError> errors = EmployeeRegistryValidator.validateEmployees(employees, 100);

        // Then
        assertTrue(errors.isEmpty(), "No validation errors expected");
    }

    @Test
    void validateEmployees_withMixtureOfInvalidData_returnsErrors() {
        // Given
        List<Employee> employees = List.of(
                new Employee(1, "Joe", "Doe", new BigDecimal(60000), null),
                new Employee(2, "Martin", "Chekov", new BigDecimal(45000), null),
                new Employee(3, "Bob", "Ronstad", new BigDecimal(47000), 1),
                new Employee(3, "Alice", "Hasacat", new BigDecimal(50000), 2),
                new Employee(5, "Brett", "Hardleaf", new BigDecimal(34000), 6)
        );

        // When
        List<ValidationError> errors = EmployeeRegistryValidator.validateEmployees(employees, 2);

        // Then
        assertEquals(4, errors.size());
        List<ValidationError> expectedErrors = List.of(
                new ValidationError(ValidationErrorType.MAXIMUM_EMPLOYEES_EXCEEDED, "Number of employees [5] exceeds the maximum permitted [2]"),
                new ValidationError(ValidationErrorType.DUPLICATE_EMPLOYEE_ID, "Duplicate Employee Id [3] found"),
                new ValidationError(ValidationErrorType.UNKNOWN_MANAGER_ID, "Manager Id [6] for Employee Id [5] could not be identified"),
                new ValidationError(ValidationErrorType.MULTIPLE_TOP_LEVEL_MANAGERS, "Only one top-level manager (null managerId) is allowed, but found [2]")
        );
        assertEquals(expectedErrors, errors);
    }

    private List<Employee> createDummyEmployees(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new Employee(
                        i,
                        "FirstName" + i,
                        "LastName" + i,
                        BigDecimal.valueOf(i),
                        i
                ))
                .toList();
    }
}