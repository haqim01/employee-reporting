package io.github.zhaqimz.employeereporting.model;

import java.math.BigDecimal;

/**
 * A builder for creating {@link Employee} instances.
 * <p>
 * This class follows the builder pattern to simplify the construction
 * of Employee objects, especially when not all fields are required
 * or when chaining is preferred.
 *
 */
public class EmployeeBuilder {
        private Integer id;
        private String firstName;
        private String lastName;
        private BigDecimal salary;
        private Integer managerId;

        public EmployeeBuilder id(Integer id) { this.id = id; return this; }
        public EmployeeBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public EmployeeBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public EmployeeBuilder salary(BigDecimal salary) { this.salary = salary; return this; }
        public EmployeeBuilder managerId(Integer managerId) { this.managerId = managerId; return this; }

        public Employee build() {
                return new Employee(id, firstName, lastName, salary, managerId);
        }
}