# MCOMP (Method Complexity) Metric Calculation Logic

**MCOMP (Method Complexity)** counts control flow statements and logical operators in the method.

### Main Calculation Logic

1. **Traverse Method:**
   - Count each control flow statement (if, for, while, catch, etc.).
   - Count logical operators (&&, ||).
2. **Sum Counts:**
   - Each occurrence adds to the complexity score.
3. **Result:**
   - MCOMP is the total complexity score for the method.

### Example
A method with 2 ifs and 1 for loop has MCOMP = 3.

### Purpose of the Metric
Higher MCOMP values indicate more complex and potentially harder-to-maintain methods.
