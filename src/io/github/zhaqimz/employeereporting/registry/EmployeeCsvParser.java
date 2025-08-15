package io.github.zhaqimz.employeereporting.registry;

import io.github.zhaqimz.employeereporting.model.Employee;
import io.github.zhaqimz.employeereporting.model.EmployeeBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EmployeeCsvParser {
    private static final Logger logger = Logger.getLogger(EmployeeCsvParser.class.getName());

    private static final CsvSchema employeeCsvSchema = new CsvSchema(List.of(
            new CsvFieldSchema<>("id", 0, CsvFieldType.INTEGER_ABS, true, EmployeeBuilder::id),
            new CsvFieldSchema<>("firstName", 1, CsvFieldType.STRING, true, EmployeeBuilder::firstName),
            new CsvFieldSchema<>("lastName", 2, CsvFieldType.STRING, true, EmployeeBuilder::lastName),
            new CsvFieldSchema<>("salary", 3, CsvFieldType.BIG_DECIMAL_ABS, true, EmployeeBuilder::salary),
            new CsvFieldSchema<>("managerId", 4, CsvFieldType.INTEGER_ABS, false, EmployeeBuilder::managerId)
    ));

    /**
     * Parses the employee data from the specified CSV file.
     *
     * @param filePath           the path to the CSV file containing employee data
     * @param headerRowIncluded  whether the first row in the CSV file is a header row and should be skipped
     * @return                   a {@link ParsedEmployeesResult} containing the list of parsed employees and any validation errors encountered
     * @throws IOException       if an I/O error occurs reading the file
     */
    public static ParsedEmployeesResult parse(Path filePath, Boolean headerRowIncluded) throws IOException {
        List<Employee> employees = new ArrayList<>();
        List<ValidationError> errors = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            // Check for header row
            int lineNumber = 0;
            if (headerRowIncluded) {
                lineNumber++;
                reader.readLine();
            }

            // Process file data
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fieldValues = line.split(",", -1);
                if (fieldValues.length != employeeCsvSchema.fields().size()) {
                    errors.add(new ValidationError(
                            ValidationErrorType.INCOMPLETE_DATA_ROW,
                            String.format("Incomplete employee data row on line %d", lineNumber)
                    ));
                    continue;
                }
                EmployeeBuilder builder = new EmployeeBuilder();
                for (CsvFieldSchema fieldSchema : employeeCsvSchema.fields()) {
                    String fieldValue = fieldValues[fieldSchema.position()].trim();
                    // Perform validation based on type
                    CsvFieldResult<?> fieldResult = switch (fieldSchema.type()) {
                        case STRING -> parseStringField(fieldValue, fieldSchema, lineNumber);
                        case INTEGER_ABS -> parseIntAbsField(fieldValue, fieldSchema, lineNumber);
                        case BIG_DECIMAL_ABS -> parseBigDecimalAbsField(fieldValue, fieldSchema, lineNumber);
                    };
                    // Add errors if any
                    fieldResult.getError().ifPresent(errors::add);
                    // Assign field value to employee instance via builder using the typed binder defined in the schema
                    bindField(fieldSchema, builder, fieldResult);
                }
                // Add even partially complete employees for further validation
                employees.add(builder.build());
            }
        }

        return new ParsedEmployeesResult(employees, errors);
    }

    /**
     * Parses and validates a string field value from a CSV input according to the given field schema.
     *
     * @param stringFieldValue The raw string value extracted from the CSV field.
     * @param fieldSchema      The schema definition for the CSV field, including validation rules.
     * @param lineNumber       The line number in the CSV file from which this field value was read, used for error reporting.
     * @return                 A {@link CsvFieldResult} containing the parsed string value and any validation error encountered.
     */
    static CsvFieldResult<String> parseStringField(String stringFieldValue, CsvFieldSchema fieldSchema, int lineNumber) {
        if (stringFieldValue.isBlank() && fieldSchema.required())
            return CsvFieldResult.failure(new ValidationError(
                    ValidationErrorType.INVALID_FIELD,
                    String.format("Invalid value [%s] for field [%s] on line %d", stringFieldValue, fieldSchema.name(), lineNumber)
            ));
        return CsvFieldResult.success(stringFieldValue);
    }

    /**
     * Parses and validates an integer field value from a CSV input as an absolute (non-negative) integer,
     * according to the provided field schema.
     *
     * @param intFieldValue The raw string value extracted from the CSV field representing an integer.
     * @param fieldSchema   The schema definition for the CSV field, including validation rules.
     * @param lineNumber    The line number in the CSV file from which this field value was read, used for error reporting.
     * @return              A {@link CsvFieldResult} containing the parsed integer value and any validation error encountered.
     */
    static CsvFieldResult<Integer> parseIntAbsField(String intFieldValue, CsvFieldSchema fieldSchema, int lineNumber) {
        try {
            int value = Integer.parseInt(intFieldValue);
            if (value >= 0) {
                return CsvFieldResult.success(value);
            } else {
                throw new NumberFormatException("Field should be >= 0");
            }
        } catch (NumberFormatException e) {
            if (!intFieldValue.isBlank() || fieldSchema.required())
                return CsvFieldResult.failure(new ValidationError(
                        ValidationErrorType.INVALID_FIELD,
                        String.format("Invalid value [%s] for field [%s] on line %d", intFieldValue, fieldSchema.name(), lineNumber)
                ));
        }
        return CsvFieldResult.success(null);
    }

    /**
     * Parses and validates a BigDecimal field value from a CSV input as an absolute (non-negative) number,
     * according to the provided field schema.
     *
     * @param bigDecimalFieldValue The raw string value extracted from the CSV field representing a decimal number.
     * @param fieldSchema          The schema definition for the CSV field, including validation rules.
     * @param lineNumber           The line number in the CSV file from which this field value was read, used for error reporting.
     * @return                     A {@link CsvFieldResult} containing the parsed BigDecimal value and any validation error encountered.
     */
    static CsvFieldResult<BigDecimal> parseBigDecimalAbsField(String bigDecimalFieldValue, CsvFieldSchema fieldSchema, int lineNumber) {
        try {
            BigDecimal value = new BigDecimal(bigDecimalFieldValue);
            if (value.compareTo(BigDecimal.ZERO) >= 0) {
                return CsvFieldResult.success(value);
            } else {
                throw new NumberFormatException("Field should be >= 0");
            }
        } catch (NumberFormatException e) {
            if (!bigDecimalFieldValue.isBlank() || fieldSchema.required())
                return CsvFieldResult.failure(new ValidationError(
                        ValidationErrorType.INVALID_FIELD,
                        String.format("Invalid value [%s] for field [%s] on line %d", bigDecimalFieldValue, fieldSchema.name(), lineNumber)
                ));
        }
        return CsvFieldResult.success(null);
    }

    @SuppressWarnings("unchecked")
    private static <T> void bindField(CsvFieldSchema<T> schema, EmployeeBuilder builder, CsvFieldResult<?> result) {
        if (result.isValid()) {
            T value = (T) result.value();
            schema.binder().bind(builder, value);
        }
    }
}