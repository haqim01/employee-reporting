package io.github.zhaqimz.employeereporting.registry;

/**
 * Represents a validation error encountered during processing or parsing.
 *
 * @param type    The type or category of the validation error.
 * @param message A descriptive message providing details about the error.
 */
public record ValidationError(
        ValidationErrorType type,
        String message
) {}