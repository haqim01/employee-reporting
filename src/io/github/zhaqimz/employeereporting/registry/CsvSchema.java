package io.github.zhaqimz.employeereporting.registry;

import java.util.List;

/**
 * Represents the schema definition for a CSV file.
 * <p>
 * This schema consists of a list of {@link CsvFieldSchema} objects,
 * each describing an individual field’s name, type, and validation rules.
 * The schema defines how to parse, validate, and interpret each column
 * in the CSV input.
 *
 * @param fields The ordered list of CSV field schemas that define
 *               the structure and validation of the CSV file.
 */
public record CsvSchema(List<CsvFieldSchema> fields) {

    public CsvFieldSchema getByName(String name) {
        return fields.stream()
                .filter(f -> f.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown field: " + name));
    }
}