package io.github.zhaqimz.employeereporting.registry;

import java.util.Optional;

/**
 * Represents the result of parsing or validating a single CSV field.
 *
 * @param <T>    The type of the parsed value.
 * @param value  The parsed value of the CSV field; may be {@code null} if parsing failed.
 * @param error  The validation error associated with this field, if any;
 *               {@code null} or empty if the field is valid.
 */
public record CsvFieldResult<T>(T value, ValidationError error) {

    public static <T> CsvFieldResult<T> success(T value) {
        return new CsvFieldResult<>(value, null);
    }

    public static <T> CsvFieldResult<T> failure(ValidationError error) {
        return new CsvFieldResult<>(null, error);
    }

    public Optional<ValidationError> getError() {
        return Optional.ofNullable(error);
    }

    public boolean isValid() {
        return error == null;
    }
}