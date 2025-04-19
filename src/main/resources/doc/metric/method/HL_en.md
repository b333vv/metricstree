# HL (Halstead Length) Metric Calculation Logic

**HL (Halstead Length)** is the total number of operators and operands in a method.

### Main Calculation Logic

1. **Traverse Method:**
   - Count every operator and operand in the method.
2. **Sum Counts:**
   - HL = (number of operators) + (number of operands).

### Example
If a method has 10 operators and 15 operands, HL = 25.

### Purpose of the Metric
HL helps estimate code size and complexity for maintainability analysis.
