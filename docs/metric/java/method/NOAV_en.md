# NOAV (Number of Accessed Variables) Metric Calculation Logic

**NOAV (Number of Accessed Variables)** counts the number of unique variables (local or field) accessed in the method.

### Main Calculation Logic

1. **Track Variable Accesses:**
   - Visit all variable references in the method.
2. **Count Unique Variables:**
   - For each reference, determine if the variable is unique.
   - Count the total number of unique variables accessed.

### Example
If a method accesses 2 local variables and 1 field (all unique), NOAV = 3.

### Purpose of the Metric
A higher NOAV may indicate a method that is doing too much or is hard to understand.
