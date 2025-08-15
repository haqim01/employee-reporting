package io.github.zhaqimz.employeereporting.reports;

import io.github.zhaqimz.employeereporting.model.Employee;
import io.github.zhaqimz.employeereporting.model.ReportingLineDepthBreach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportingLineAnalyserTest {

    @Test
    void noEmployees_returnsEmptyList() {
        // Given
        final int depthToCompare = 2;
        List<Employee> employees = List.of();

        // When
        List<ReportingLineDepthBreach> breaches = ReportingLineAnalyser.findEmployeesBreachingReportingLineDepth(employees, depthToCompare);

        // Then
        assertTrue(breaches.isEmpty(), "No breaches expected for empty employees list");
    }

    @Test
    void allEmployeesWithinDepth_returnsEmptyList() {
        // Given
        final int depthToCompare = 2;
        List<Employee> employees = List.of(
                new Employee(123, "Joe", "Doe", new BigDecimal(60000), null),
                new Employee(124, "Martin", "Chekov", new BigDecimal(45000), 123),
                new Employee(125, "Bob", "Ronstad", new BigDecimal(47000), 123),
                new Employee(300, "Alice", "Hasacat", new BigDecimal(50000), 124),
                new Employee(305, "Brett", "Hardleaf", new BigDecimal(34000), 125)
        );

        // When
        List<ReportingLineDepthBreach> breaches = ReportingLineAnalyser.findEmployeesBreachingReportingLineDepth(employees, depthToCompare);

        // Then
        assertTrue(breaches.isEmpty());
    }

    @Test
    void employeesExceedingDepth_areReportedCorrectly() {
        // Given
        final int depthToCompare = 2;
        var employee1 = new Employee(123, "Joe", "Doe", new BigDecimal(60000), null);
        var employee2 = new Employee(124, "Martin", "Chekov", new BigDecimal(45000), 123);
        var employee3 = new Employee(125, "Bob", "Ronstad", new BigDecimal(47000), 124);
        var employee4 = new Employee(300, "Alice", "Hasacat", new BigDecimal(50000), 125);
        var employee5 = new Employee(305, "Brett", "Hardleaf", new BigDecimal(34000), 300);
        List<Employee> employees = List.of(
                employee1,
                employee2,
                employee3,
                employee4,
                employee5
        );

        // When
        List<ReportingLineDepthBreach> breaches = ReportingLineAnalyser.findEmployeesBreachingReportingLineDepth(employees, 2);

        // Then
        List<ReportingLineDepthBreach> expectedBreaches = List.of(
                new ReportingLineDepthBreach(employee4, 2, 1),
                new ReportingLineDepthBreach(employee5, 2, 2)
        );
        assertEquals(2, breaches.size());
        assertEquals(expectedBreaches, breaches);
    }
}