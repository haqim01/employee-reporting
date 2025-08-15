# Employee Reporting App

## Assumptions

1. An input CSV schema will be created in accordance with the example provided.
2. `managerId` on employee record will follow idiomatic `null` assignment if it does not exist, given the rarity of this scenario.
3. All validation errors from the employee registry (CSV) are treated with equal severity.
4. CSV file delimiter will be taken to be a comma, in accordance with the example provided.
5. If there are any validation errors in the input CSV subsequent report generation will not occur.
6. It will be taken that no number values in the csv schema can be negative presently.
7. It should be expected that a managerId represents a valid employeeId. (when not null)
8. It should be expected that any given employeeId is not duplicated.
9. It should be expected that there is only one top-level manager.
10. Output generation will be a simple print to the console.
11. A sample app will be provided to represent the characteristics of an eventual service API.

---

## Usage

Sample `EmployeeReportingApp` added in `service` package.

Usage:

```bash
java EmployeeReportingApp </path/to/employee_registry_csv_file>
App config is defined in: resources/config.properties

Example:
Config data
reports.manager.min.relative.salary.percentage=0.2
reports.manager.max.relative.salary.percentage=0.5
reports.employee.max.reporting.line.depth=2
reports.employee.max.permitted.employees=1000
employee.registry.csv.header.included=true

Input file (employee_registry.csv)
Id,firstName,lastName,salary,managerId
123,Joe,Doe,69001,
124,Martin,Chekov,45000,123
125,Bob,Ronstad,47000,123
300,Alice,Hasacat,50000,124
305,Brett,Hardleaf,34000,300

Report output
Following managers have a current salary status of : Underpaid
Name                 ID         ManagerID  Salary          Breach
----------------------------------------------------------------------
Martin Chekov        124        123        45000.00        15000.00

Following managers have a current salary status of : Overpaid
Name                 ID         ManagerID  Salary          Breach
----------------------------------------------------------------------
Joe Doe              123        N/A        69001.00        1.00

Following managers are breaching the prescribed reporting line depth:
Name                 ID         ManagerID  Depth      Breached Amount
----------------------------------------------------------------------
Brett Hardleaf       305        300        2          1