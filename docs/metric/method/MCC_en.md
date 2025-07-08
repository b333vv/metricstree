# MCC (McCabe Cyclomatic Complexity) Metric Calculation Logic

**MCC (McCabe Cyclomatic Complexity)** measures the number of linearly independent paths through a method.

### Main Calculation Logic

1. **Traverse Method:**
   - Visit all decision points (if, while, for, case, catch, etc.).
2. **Increment for Decisions:**
   - Each decision point increases the complexity count.
3. **Result:**
   - MCC is the total number of independent paths, starting from 1.

### Example
A method with one if and one for loop has MCC = 3 (1 + 2 decisions).

### Purpose of the Metric
A high MCC value indicates more complex, harder-to-test code.
