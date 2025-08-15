package io.github.zhaqimz.employeereporting.service;

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
import io.github.zhaqimz.employeereporting.utility.Config;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class EmployeeReportingApp {
    private static final Logger logger = Logger.getLogger(EmployeeReportingApp.class.getName());

    public static void main(String[] args) {
        logger.info("Application started...");
        // Get App Args
        AppArguments appArgs = null;
        try {
            appArgs = new AppArguments(args);
        } catch (IllegalArgumentException e) {
            logger.severe("Error processing application arguments: " + e.getMessage());
            AppArguments.printUsage();
            System.exit(1);
        }
        Path employeesRegistryCsvFile = appArgs.getCsvFile();
        logger.info("Employee Registry CSV file to be processed: " + employeesRegistryCsvFile.toAbsolutePath().toString());

        try {
            // Read App Config
            logger.info("Reading application config...");
            Config config = new Config("config.properties");
            var minRelativeSalaryPercentage = Optional.of(
                    new BigDecimal(config.get("reports.manager.min.relative.salary.percentage"))
            );
            var maxRelativeSalaryPercentage = Optional.of(
                    new BigDecimal(config.get("reports.manager.max.relative.salary.percentage"))
            );
            var depthToCompare = config.getInt("reports.employee.max.reporting.line.depth");
            var maxPermittedEmployees = config.getInt("reports.employee.max.permitted.employees");
            var csvHeaderIncluded = config.getBoolean("employee.registry.csv.header.included");

            // Parse Employee Registry file
            logger.info("Parsing employee registry file...");
            ParsedEmployeesResult parsedEmployeesRegistryFile = EmployeeCsvParser.parse(employeesRegistryCsvFile, csvHeaderIncluded);
            List<Employee> employees = parsedEmployeesRegistryFile.employees();
            List<ValidationError> parseErrors = parsedEmployeesRegistryFile.errors();

            // Perform Employee Registry validations
            List<ValidationError> validationErrors = EmployeeRegistryValidator.validateEmployees(employees, maxPermittedEmployees);

            // Proceed to Reporting if there are no errors
            List<ValidationError> allErrors = Stream.concat(parseErrors.stream(), validationErrors.stream()).toList();
            if (allErrors.isEmpty()) {
                List<ManagerRelativeSalaryAssessment> salaryAssessments = SalaryAnalyser.assessManagerSalary(
                        employees,
                        minRelativeSalaryPercentage,
                        maxRelativeSalaryPercentage
                );

                List<ReportingLineDepthBreach> reportingLineBreaches = ReportingLineAnalyser.findEmployeesBreachingReportingLineDepth(
                        employees,
                        depthToCompare
                );
                var underpaidReport = SalaryAnalyser.generateSalaryMarginStatusReport(salaryAssessments, SalaryMarginStatus.UNDERPAID);
                System.out.println(underpaidReport);

                var overpaidReport = SalaryAnalyser.generateSalaryMarginStatusReport(salaryAssessments, SalaryMarginStatus.OVERPAID);
                System.out.println(overpaidReport);

                var reportingLineDepthBreachReport = ReportingLineAnalyser.generateReportingLineDepthBreachReport(reportingLineBreaches);
                System.out.println(reportingLineDepthBreachReport);
            } else {
                logger.warning("Reports could not be generated due to errors detected in the parsing and validation of the file");
                System.out.println("Following errors were detected in the parsing and validation of the file:\n");
                for (ValidationError error : allErrors) {
                    System.out.println(error.message() + "\n");
                }
            }

        } catch (Exception e) {
            logger.severe("Exiting execution due to the following error: " + e);
            System.exit(1);
        }
        System.exit(0);
    }

    private static class AppArguments {
        private final Path csvFile;

        public AppArguments(String[] args) {
            if (args.length < 1) {
                throw new IllegalArgumentException("Missing required argument: <employee_csv_file>");
            }

            Path path = Path.of(args[0]);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new IllegalArgumentException("File not found or is not a regular file: " + path);
            }

            this.csvFile = path;
        }

        public Path getCsvFile() {
            return csvFile;
        }

        public static void printUsage() {
            System.out.println("Usage: java EmployeeReportingApp </path/to/employee_registry_csv_file>");
            System.out.println("  <employee_csv_file> - Path to the input CSV file with employee registry data.");
        }
    }
}