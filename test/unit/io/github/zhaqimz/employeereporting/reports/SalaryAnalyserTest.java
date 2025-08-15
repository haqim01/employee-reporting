package io.github.zhaqimz.employeereporting.reports;

import io.github.zhaqimz.employeereporting.model.Employee;
import io.github.zhaqimz.employeereporting.model.ManagerRelativeSalaryAssessment;
import io.github.zhaqimz.employeereporting.model.SalaryAssessment;
import io.github.zhaqimz.employeereporting.model.SalaryMarginStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SalaryAnalyserTest {

    @Test
    void assessManagerSalary_withinExpectedRange_returnsFAIRLY_PAID() {
        // Given
        var manager = new Employee(123, "Joe", "Doe", new BigDecimal(110), null);
        List<Employee> employees = List.of(
                manager,
                new Employee(124, "Martin", "Chekov", new BigDecimal(100), 123),
                new Employee(125, "Bob", "Ronstad", new BigDecimal(100), 123)
        );
        //manager should earn at least 10% more of avg
        var minRelativeSalaryPercentage = Optional.of(BigDecimal.valueOf(0.1));
        //manager should not earn more than 15% of avg
        var maxRelativeSalaryPercentage = Optional.of(BigDecimal.valueOf(0.15));

        // When
        List<ManagerRelativeSalaryAssessment> result = SalaryAnalyser.assessManagerSalary(
                employees,
                minRelativeSalaryPercentage,
                maxRelativeSalaryPercentage
        );

        // Then
        assertEquals(1, result.size());
        List<ManagerRelativeSalaryAssessment> expectedSalaryAssessments = List.of(
                new ManagerRelativeSalaryAssessment(
                        manager,
                        BigDecimal.valueOf(100).setScale(2),
                        minRelativeSalaryPercentage,
                        maxRelativeSalaryPercentage,
                        new SalaryAssessment(SalaryMarginStatus.FAIRLY_PAID, BigDecimal.valueOf(0))
                )
        );
        assertEquals(expectedSalaryAssessments, result);
    }

    @Test
    void assessManagerSalary_belowMinimum_returnsUNDERPAID() {
        // Given
        var manager = new Employee(123, "Joe", "Doe", new BigDecimal("109.99"), null);
        List<Employee> employees = List.of(
                manager,
                new Employee(124, "Martin", "Chekov", new BigDecimal(100), 123),
                new Employee(125, "Bob", "Ronstad", new BigDecimal(100), 123)
        );
        //manager should earn at least 10% more of avg
        var minRelativeSalaryPercentage = Optional.of(BigDecimal.valueOf(0.1));
        //manager should not earn more than 15% of avg
        var maxRelativeSalaryPercentage = Optional.of(BigDecimal.valueOf(0.15));

        // When
        List<ManagerRelativeSalaryAssessment> result = SalaryAnalyser.assessManagerSalary(
                employees,
                minRelativeSalaryPercentage,
                maxRelativeSalaryPercentage
        );

        // Then
        assertEquals(1, result.size());
        List<ManagerRelativeSalaryAssessment> expectedSalaryAssessments = List.of(
                new ManagerRelativeSalaryAssessment(
                        manager,
                        BigDecimal.valueOf(100).setScale(2),
                        minRelativeSalaryPercentage,
                        maxRelativeSalaryPercentage,
                        new SalaryAssessment(SalaryMarginStatus.UNDERPAID, BigDecimal.valueOf(0.01))
                )
        );
        assertEquals(expectedSalaryAssessments, result);
    }

    @Test
    void assessManagerSalary_aboveMaximum_returnsOVERPAID() {
        // Given
        var manager = new Employee(123, "Joe", "Doe", new BigDecimal("115.01"), null);
        List<Employee> employees = List.of(
                manager,
                new Employee(124, "Martin", "Chekov", new BigDecimal(100), 123),
                new Employee(125, "Bob", "Ronstad", new BigDecimal(100), 123)
        );
        //manager should earn at least 10% more of avg
        var minRelativeSalaryPercentage = Optional.of(BigDecimal.valueOf(0.1));
        //manager should not earn more than 15% of avg
        var maxRelativeSalaryPercentage = Optional.of(BigDecimal.valueOf(0.15));

        // When
        List<ManagerRelativeSalaryAssessment> result = SalaryAnalyser.assessManagerSalary(
                employees,
                minRelativeSalaryPercentage,
                maxRelativeSalaryPercentage
        );

        // Then
        assertEquals(1, result.size());
        List<ManagerRelativeSalaryAssessment> expectedSalaryAssessments = List.of(
                new ManagerRelativeSalaryAssessment(
                        manager,
                        BigDecimal.valueOf(100).setScale(2),
                        minRelativeSalaryPercentage,
                        maxRelativeSalaryPercentage,
                        new SalaryAssessment(SalaryMarginStatus.OVERPAID, BigDecimal.valueOf(0.01))
                )
        );
        assertEquals(expectedSalaryAssessments, result);
    }

    @Test
    void assessManagerSalary_returns_mixtureOfOutcomes() {
        // Given
        var manager1 = new Employee(123, "Joe", "Doe", new BigDecimal("27044.28"), null);
        var manager2 = new Employee(125, "Bob", "Ronstad", new BigDecimal("46933.51"), manager1.id());
        var manager3 = new Employee(310, "Don", "Bradman", new BigDecimal("44000.99"), manager2.id());
        List<Employee> employees = List.of(
                manager1,
                new Employee(124, "Martin", "Chekov", new BigDecimal(100), manager1.id()),
                manager2,
                new Employee(300, "Alice", "Hasacat", new BigDecimal(50000), manager2.id()),
                new Employee(305, "Brett", "Hardleaf", new BigDecimal(34000), manager2.id()),
                manager3,
                new Employee(315, "Garfield", "Sobers", new BigDecimal(40500.99), manager3.id()),
                new Employee(320, "Viv", "Richards", new BigDecimal(38500.99), manager3.id())
        );
        //manager should earn at least 10% more of avg
        var minRelativeSalaryPercentage = Optional.of(BigDecimal.valueOf(0.1));
        //manager should not earn more than 15% of avg
        var maxRelativeSalaryPercentage = Optional.of(BigDecimal.valueOf(0.15));

        // When
        List<ManagerRelativeSalaryAssessment> result = SalaryAnalyser.assessManagerSalary(
                employees,
                minRelativeSalaryPercentage,
                maxRelativeSalaryPercentage
        );

        // Then
        assertEquals(3, result.size());
        List<ManagerRelativeSalaryAssessment> expectedSalaryAssessments = List.of(
                new ManagerRelativeSalaryAssessment(
                        manager3,
                        BigDecimal.valueOf(39500.99),
                        minRelativeSalaryPercentage,
                        maxRelativeSalaryPercentage,
                        new SalaryAssessment(SalaryMarginStatus.FAIRLY_PAID, BigDecimal.valueOf(0))
                ),
                new ManagerRelativeSalaryAssessment(
                        manager1,
                        BigDecimal.valueOf(23516.76),
                        minRelativeSalaryPercentage,
                        maxRelativeSalaryPercentage,
                        new SalaryAssessment(SalaryMarginStatus.OVERPAID, BigDecimal.valueOf(0.01))
                ),
                new ManagerRelativeSalaryAssessment(
                        manager2,
                        BigDecimal.valueOf(42667).setScale(2),
                        minRelativeSalaryPercentage,
                        maxRelativeSalaryPercentage,
                        new SalaryAssessment(SalaryMarginStatus.UNDERPAID, BigDecimal.valueOf(0.19))
                )
        );
        assertEquals(expectedSalaryAssessments, result);
    }
}