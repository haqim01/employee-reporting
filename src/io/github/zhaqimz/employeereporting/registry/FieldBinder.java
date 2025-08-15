package io.github.zhaqimz.employeereporting.registry;

import io.github.zhaqimz.employeereporting.model.EmployeeBuilder;

/**
 * Functional interface for binding a value to a target field or object.
 *
 * <p>This interface abstracts the operation of assigning or mapping
 * a parsed field value of type {@code T} to its corresponding destination.
 *
 * @param <T> The type of the value to be bound.
 */
@FunctionalInterface
interface FieldBinder<T> {
    void bind(EmployeeBuilder builder, T value);
}