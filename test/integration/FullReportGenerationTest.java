import io.github.zhaqimz.employeereporting.model.Employee;
import io.github.zhaqimz.employeereporting.model.ManagerRelativeSalaryAssessment;
import io.github.zhaqimz.employeereporting.model.ReportingLineDepthBreach;
import io.github.zhaqimz.employeereporting.model.SalaryMarginStatus;
import io.github.zhaqimz.employeereporting.registry.EmployeeCsvParser;
import io.github.zhaqimz.employeereporting.registry.EmployeeRegistryValidator;
import io.github.zhaqimz.employeereporting.registry.ParsedEmployeesResult;
import io.github.zhaqimz.employeereporting.registry.ValidationError;
import io.github.zhaqimz.employeereporting.reports.ReportingLineAnalyser;
import io.github.zhaqimz.employeereporting.reports.SalaryAnalyser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FullReportGenerationTest {

    @Test
    void generateAllReports_verifyOutput() throws Exception {
        // Given
        var minRelativeSalaryPercentage = Optional.of(new BigDecimal(0.2));
        var maxRelativeSalaryPercentage = Optional.of(new BigDecimal(0.5));
        var depthToCompare = 4;
        var maxPermittedEmployees = 1000;
        var csvHeaderIncluded = true;
        var employeesRegistryCsvFile = Paths.get(getClass().getResource("/integration-test-employees.csv").toURI());

        // When
        ParsedEmployeesResult parsedEmployeesRegistryFile = EmployeeCsvParser.parse(employeesRegistryCsvFile, csvHeaderIncluded);
        List<Employee> employees = parsedEmployeesRegistryFile.employees();
        List<ValidationError> parseErrors = parsedEmployeesRegistryFile.errors();
        List<ValidationError> validationErrors = EmployeeRegistryValidator.validateEmployees(employees, maxPermittedEmployees);

        List<ManagerRelativeSalaryAssessment> salaryAssessments = SalaryAnalyser.assessManagerSalary(
                employees,
                minRelativeSalaryPercentage,
                maxRelativeSalaryPercentage
        );

        List<ReportingLineDepthBreach> reportingLineBreaches = ReportingLineAnalyser.findEmployeesBreachingReportingLineDepth(
                employees,
                depthToCompare
        );

        // Then
        List<ValidationError> allErrors = Stream.concat(parseErrors.stream(), validationErrors.stream()).toList();
        assertTrue(allErrors.isEmpty());

        var underpaidReport = SalaryAnalyser.generateSalaryMarginStatusReport(salaryAssessments, SalaryMarginStatus.UNDERPAID);
        String expectedUnderpaidReportOutput = """
                Following managers have a current salary status of : Underpaid
                Name                 ID         ManagerID  Salary          Breach   \s
                ----------------------------------------------------------------------
                Brett Hardleaf       305        300        34000.00        266073.60\s
                Shahid Afridi        340        335        34234.00        377656.40\s
                Martin Chekov        124        123        45000.00        15000.00 \s
                Richie Richardson    335        320        34234.00        6846.80  \s
                """;
        assertEquals(expectedUnderpaidReportOutput, underpaidReport);

        var overpaidReport = SalaryAnalyser.generateSalaryMarginStatusReport(salaryAssessments, SalaryMarginStatus.OVERPAID);
        String expectedOverpaidReportOutput = """
                Following managers have a current salary status of : Overpaid
                Name                 ID         ManagerID  Salary          Breach   \s
                ----------------------------------------------------------------------
                Ricky Ponting        320        305        332423.00       66006.99 \s
                Joe Doe              123        N/A        69001.00        1.00     \s
                """;
        assertEquals(expectedOverpaidReportOutput, overpaidReport);

        var reportingLineDepthBreachReport = ReportingLineAnalyser.generateReportingLineDepthBreachReport(reportingLineBreaches);
        String expectedReportingLineDepthBreachReportOutput = """
                Following managers are breaching the prescribed reporting line depth:
                Name                 ID         ManagerID  Depth      Breached Amount
                ----------------------------------------------------------------------
                Mark Waugh           325        320        4          1        \s
                Steve Waugh          330        320        4          1        \s
                Richie Richardson    335        320        4          1        \s
                Shahid Afridi        340        335        4          2        \s
                Majid Khan           345        340        4          3        \s                     
                """;
        assertEquals(expectedReportingLineDepthBreachReportOutput, reportingLineDepthBreachReport);
    }
}