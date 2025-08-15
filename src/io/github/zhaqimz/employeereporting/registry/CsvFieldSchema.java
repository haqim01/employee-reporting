package io.github.zhaqimz.employeereporting.registry;

/**
 * Defines the schema for a single CSV field, including its name,
 * expected type, and validation rules.
 *
 * @param <T> The type to which the CSV field's string value will be converted.
 */
public record CsvFieldSchema<T>(
        String name,
        int position,
        CsvFieldType type,
        boolean required,
        FieldBinder<T> binder
) {}