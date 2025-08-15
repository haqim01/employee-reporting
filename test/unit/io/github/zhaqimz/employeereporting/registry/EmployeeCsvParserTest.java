package io.github.zhaqimz.employeereporting.registry;

import io.github.zhaqimz.employeereporting.model.Employee;
import io.github.zhaqimz.employeereporting.model.EmployeeBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeCsvParserTest {

    @Test
    void parseStringField_shouldReturnValue_whenFieldIsNonBlankAndRequired() {
        // Given
        String input = "John";
        CsvFieldSchema schema = new CsvFieldSchema<>("firstName", 0, CsvFieldType.STRING, true, EmployeeBuilder::firstName);
        int lineNumber = 1;

        // When
        CsvFieldResult<String> result = EmployeeCsvParser.parseStringField(input, schema, lineNumber);

        // Then
        assertTrue(result.isValid());
        assertEquals("John", result.value());
        assertTrue(result.getError().isEmpty());
    }

    @Test
    void parseStringField_shouldReturnValue_whenFieldIsBlankAndNotRequired() {
        // Given
        String input = "";
        CsvFieldSchema schema = new CsvFieldSchema<>("firstName", 0, CsvFieldType.STRING, false, EmployeeBuilder::firstName);
        int lineNumber = 1;

        // When
        CsvFieldResult<String> result = EmployeeCsvParser.parseStringField(input, schema, lineNumber);

        // Then
        assertTrue(result.isValid());
        assertEquals("", result.value());
        assertTrue(result.getError().isEmpty());
    }

    @Test
    void parseStringField_shouldReturnError_whenFieldIsBlankAndRequired() {
        // Given
        String input = "";
        CsvFieldSchema schema = new CsvFieldSchema<>("firstName", 0, CsvFieldType.STRING, true, EmployeeBuilder::firstName);
        int lineNumber = 1;

        // When
        CsvFieldResult<String> result = EmployeeCsvParser.parseStringField(input, schema, lineNumber);

        // Then
        assertFalse(result.isValid());
        assertEquals(null, result.value());
        assertFalse(result.getError().isEmpty());
        Optional<String> errorMessage = result.getError().map(m -> m.message());
        assertEquals("Invalid value [] for field [firstName] on line 1", errorMessage.get());
    }

    @Test
    void parseIntAbsField_shouldReturnValue_whenFieldIsCorrectlyDefinedAndRequired() {
        // Given
        String input = "100";
        CsvFieldSchema schema = new CsvFieldSchema<>("id", 0, CsvFieldType.INTEGER_ABS, true, EmployeeBuilder::id);
        int lineNumber = 1;

        // When
        CsvFieldResult<Integer> result = EmployeeCsvParser.parseIntAbsField(input, schema, lineNumber);

        // Then
        assertTrue(result.isValid());
        assertEquals(100, result.value());
        assertTrue(result.getError().isEmpty());
    }

    @Test
    void parseIntAbsField_shouldReturnValue_whenFieldIsCorrectlyDefinedAndNotRequired() {
        // Given
        String input = "100";
        CsvFieldSchema schema = new CsvFieldSchema<>("id", 0, CsvFieldType.INTEGER_ABS, false, EmployeeBuilder::id);
        int lineNumber = 1;

        // When
        CsvFieldResult<Integer> result = EmployeeCsvParser.parseIntAbsField(input, schema, lineNumber);

        // Then
        assertTrue(result.isValid());
        assertEquals(100, result.value());
        assertTrue(result.getError().isEmpty());
    }

    @Test
    void parseIntAbsField_shouldReturnValue_whenFieldIsBlankAndNotRequired() {
        // Given
        String input = "";
        CsvFieldSchema schema = new CsvFieldSchema<>("id", 0, CsvFieldType.INTEGER_ABS, false, EmployeeBuilder::id);
        int lineNumber = 1;

        // When
        CsvFieldResult<Integer> result = EmployeeCsvParser.parseIntAbsField(input, schema, lineNumber);

        // Then
        assertTrue(result.isValid());
        assertEquals(null, result.value());
        assertTrue(result.getError().isEmpty());
    }

    @Test
    void parseIntAbsField_shouldReturnError_whenFieldIsBlankAndRequired() {
        // Given
        String input = "";
        CsvFieldSchema schema = new CsvFieldSchema<>("id", 0, CsvFieldType.INTEGER_ABS, true, EmployeeBuilder::id);
        int lineNumber = 1;

        // When
        CsvFieldResult<Integer> result = EmployeeCsvParser.parseIntAbsField(input, schema, lineNumber);

        // Then
        assertFalse(result.isValid());
        assertEquals(null, result.value());
        assertFalse(result.getError().isEmpty());
        Optional<String> errorMessage = result.getError().map(m -> m.message());
        assertEquals("Invalid value [] for field [id] on line 1", errorMessage.get());
    }

    @Test
    void parseIntAbsField_shouldReturnError_whenFieldIsInvalid() {
        // Given
        String input = "abc";
        CsvFieldSchema schema = new CsvFieldSchema<>("id", 0, CsvFieldType.INTEGER_ABS, true, EmployeeBuilder::id);
        int lineNumber = 1;

        // When
        CsvFieldResult<Integer> result = EmployeeCsvParser.parseIntAbsField(input, schema, lineNumber);

        // Then
        assertFalse(result.isValid());
        assertEquals(null, result.value());
        assertFalse(result.getError().isEmpty());
        Optional<String> errorMessage = result.getError().map(m -> m.message());
        assertEquals("Invalid value [abc] for field [id] on line 1", errorMessage.get());
    }

    @Test
    void parseIntAbsField_shouldReturnError_whenFieldIsNegative() {
        // Given
        String input = "-100";
        CsvFieldSchema schema = new CsvFieldSchema<>("id", 0, CsvFieldType.INTEGER_ABS, true, EmployeeBuilder::id);
        int lineNumber = 1;

        // When
        CsvFieldResult<Integer> result = EmployeeCsvParser.parseIntAbsField(input, schema, lineNumber);

        // Then
        assertFalse(result.isValid());
        assertEquals(null, result.value());
        assertFalse(result.getError().isEmpty());
        Optional<String> errorMessage = result.getError().map(m -> m.message());
        assertEquals("Invalid value [-100] for field [id] on line 1", errorMessage.get());
    }

    @Test
    void parseBigDecimalAbsField_shouldReturnValue_whenFieldIsCorrectlyDefinedAndRequired() {
        // Given
        String input = "123.456";
        CsvFieldSchema schema = new CsvFieldSchema<>("salary", 0, CsvFieldType.BIG_DECIMAL_ABS, true, EmployeeBuilder::salary);
        int lineNumber = 1;

        // When
        CsvFieldResult<BigDecimal> result = EmployeeCsvParser.parseBigDecimalAbsField(input, schema, lineNumber);

        // Then
        assertTrue(result.isValid());
        assertEquals(BigDecimal.valueOf(123.456), result.value());
        assertTrue(result.getError().isEmpty());
    }

    @Test
    void parseBigDecimalAbsField_shouldReturnValue_whenFieldIsCorrectlyDefinedAndNotRequired() {
        // Given
        String input = "123.456";
        CsvFieldSchema schema = new CsvFieldSchema<>("salary", 0, CsvFieldType.BIG_DECIMAL_ABS, false, EmployeeBuilder::salary);
        int lineNumber = 1;

        // When
        CsvFieldResult<BigDecimal> result = EmployeeCsvParser.parseBigDecimalAbsField(input, schema, lineNumber);

        // Then
        assertTrue(result.isValid());
        assertEquals(BigDecimal.valueOf(123.456), result.value());
        assertTrue(result.getError().isEmpty());
    }

    @Test
    void parseBigDecimalAbsField_shouldReturnValue_whenFieldIsBlankAndNotRequired() {
        // Given
        String input = "";
        CsvFieldSchema schema = new CsvFieldSchema<>("salary", 0, CsvFieldType.BIG_DECIMAL_ABS, false, EmployeeBuilder::salary);
        int lineNumber = 1;

        // When
        CsvFieldResult<BigDecimal> result = EmployeeCsvParser.parseBigDecimalAbsField(input, schema, lineNumber);

        // Then
        assertTrue(result.isValid());
        assertEquals(null, result.value());
        assertTrue(result.getError().isEmpty());
    }

    @Test
    void parseBigDecimalAbsField_shouldReturnError_whenFieldIsBlankAndRequired() {
        // Given
        String input = "";
        CsvFieldSchema schema = new CsvFieldSchema<>("salary", 0, CsvFieldType.BIG_DECIMAL_ABS, true, EmployeeBuilder::salary);
        int lineNumber = 1;

        // When
        CsvFieldResult<BigDecimal> result = EmployeeCsvParser.parseBigDecimalAbsField(input, schema, lineNumber);

        // Then
        assertFalse(result.isValid());
        assertEquals(null, result.value());
        assertFalse(result.getError().isEmpty());
        Optional<String> errorMessage = result.getError().map(m -> m.message());
        assertEquals("Invalid value [] for field [salary] on line 1", errorMessage.get());
    }

    @Test
    void parseBigDecimalAbsField_shouldReturnError_whenFieldIsInvalid() {
        // Given
        String input = "xyz";
        CsvFieldSchema schema = new CsvFieldSchema<>("salary", 0, CsvFieldType.BIG_DECIMAL_ABS, true, EmployeeBuilder::salary);
        int lineNumber = 1;

        // When
        CsvFieldResult<BigDecimal> result = EmployeeCsvParser.parseBigDecimalAbsField(input, schema, lineNumber);

        // Then
        assertFalse(result.isValid());
        assertEquals(null, result.value());
        assertFalse(result.getError().isEmpty());
        Optional<String> errorMessage = result.getError().map(m -> m.message());
        assertEquals("Invalid value [xyz] for field [salary] on line 1", errorMessage.get());
    }

    @Test
    void parseBigDecimalAbsField_shouldReturnError_whenFieldIsNegative() {
        // Given
        String input = "-123.456";
        CsvFieldSchema schema = new CsvFieldSchema<>("salary", 0, CsvFieldType.BIG_DECIMAL_ABS, true, EmployeeBuilder::salary);
        int lineNumber = 1;

        // When
        CsvFieldResult<BigDecimal> result = EmployeeCsvParser.parseBigDecimalAbsField(input, schema, lineNumber);

        // Then
        assertFalse(result.isValid());
        assertEquals(null, result.value());
        assertFalse(result.getError().isEmpty());
        Optional<String> errorMessage = result.getError().map(m -> m.message());
        assertEquals("Invalid value [-123.456] for field [salary] on line 1", errorMessage.get());
    }

    @Test
    void parse_testFileWithOnlyValidData() throws IOException {
        // Given
        String csvData = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                124,Martin,Chekov,45000,123
                125,Bob,Ronstad,47000,123
                300,Alice,Hasacat,50000,124
                305,Brett,Hardleaf,34000,300
                """;
        Path testFile = Files.createTempFile("employees", ".csv");
        Files.writeString(testFile, csvData);

        // When
        ParsedEmployeesResult result = EmployeeCsvParser.parse(testFile, true);

        // Then
        assertNotNull(result);
        assertTrue(result.errors().isEmpty());
        assertEquals(5, result.employees().size());
        List<Employee> expectedEmployees = List.of(
                new Employee(123, "Joe", "Doe", new BigDecimal(60000), null),
                new Employee(124, "Martin", "Chekov", new BigDecimal(45000), 123),
                new Employee(125, "Bob", "Ronstad", new BigDecimal(47000), 123),
                new Employee(300, "Alice", "Hasacat", new BigDecimal(50000), 124),
                new Employee(305, "Brett", "Hardleaf", new BigDecimal(34000), 300)
        );
        assertEquals(expectedEmployees, result.employees());

        // Cleanup
        Files.deleteIfExists(testFile);
    }

    @Test
    void parse_testFileWithInvalidData() throws IOException {
        // Given
        String csvData = """
                Id,firstName,lastName,salary,managerId
                ,Joe,Doe,60000,
                124,,Chekov,45000,123
                125,Bob,,47000,123
                300,Alice,Hasacat,,124
                xyz305,Brett,Hardleaf,-34000,-300
                """;
        Path testFile = Files.createTempFile("employees", ".csv");
        Files.writeString(testFile, csvData);

        // When
        ParsedEmployeesResult result = EmployeeCsvParser.parse(testFile, true);

        // Then
        assertNotNull(result);
        assertFalse(result.errors().isEmpty());
        assertEquals(5, result.employees().size());
        List<Employee> expectedEmployees = List.of(
                new Employee(null, "Joe", "Doe", new BigDecimal(60000), null),
                new Employee(124, null, "Chekov", new BigDecimal(45000), 123),
                new Employee(125, "Bob", null, new BigDecimal(47000), 123),
                new Employee(300, "Alice", "Hasacat", null, 124),
                new Employee(null, "Brett", "Hardleaf", null, null)
        );
        List<ValidationError> expectedErrors = List.of(
                new ValidationError(ValidationErrorType.INVALID_FIELD, "Invalid value [] for field [id] on line 2"),
                new ValidationError(ValidationErrorType.INVALID_FIELD, "Invalid value [] for field [firstName] on line 3"),
                new ValidationError(ValidationErrorType.INVALID_FIELD, "Invalid value [] for field [lastName] on line 4"),
                new ValidationError(ValidationErrorType.INVALID_FIELD, "Invalid value [] for field [salary] on line 5"),
                new ValidationError(ValidationErrorType.INVALID_FIELD, "Invalid value [xyz305] for field [id] on line 6"),
                new ValidationError(ValidationErrorType.INVALID_FIELD, "Invalid value [-34000] for field [salary] on line 6"),
                new ValidationError(ValidationErrorType.INVALID_FIELD, "Invalid value [-300] for field [managerId] on line 6")
        );
        assertEquals(expectedEmployees, result.employees());
        assertEquals(expectedErrors, result.errors());

        // Cleanup
        Files.deleteIfExists(testFile);
    }

    @Test
    void parse_testFileMalformedData() throws IOException {
        // Given
        String csvData = """
                ItsAFunDay
                """;
        Path testFile = Files.createTempFile("employees", ".csv");
        Files.writeString(testFile, csvData);

        // When
        ParsedEmployeesResult result = EmployeeCsvParser.parse(testFile, false);

        // Then
        assertTrue(result.employees().isEmpty());
        List<ValidationError> expectedErrors = List.of(
                new ValidationError(
                        ValidationErrorType.INCOMPLETE_DATA_ROW,
                        "Incomplete employee data row on line 1")
        );
        assertEquals(expectedErrors, result.errors());

        // Cleanup
        Files.deleteIfExists(testFile);
    }

    @Test
    void parse_testFileWithIncompleteData() throws IOException {
        // Given
        String csvData = """
                Id,firstName,lastName,salary,managerId
                124,Joe,Doe,60000
                124,Martin,Chekov,45000,123
                """;
        Path testFile = Files.createTempFile("employees", ".csv");
        Files.writeString(testFile, csvData);

        // When
        ParsedEmployeesResult result = EmployeeCsvParser.parse(testFile, true);

        // Then
        assertFalse(result.employees().isEmpty());
        List<Employee> expectedEmployees = List.of(
                new Employee(124, "Martin", "Chekov", new BigDecimal(45000), 123)
        );
        List<ValidationError> expectedErrors = List.of(
                new ValidationError(ValidationErrorType.INCOMPLETE_DATA_ROW, "Incomplete employee data row on line 2")
        );
        assertEquals(expectedEmployees, result.employees());
        assertEquals(expectedErrors, result.errors());

        // Cleanup
        Files.deleteIfExists(testFile);
    }
}