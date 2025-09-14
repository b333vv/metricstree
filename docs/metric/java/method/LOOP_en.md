# LOOP (Number of Loops) Metric Calculation Logic

**LOOP (Number of Loops)** counts the total number of loop constructs (for, while, foreach) in the method.

### Main Calculation Logic

1. **Traverse Method:**
   - Visit all loop statements in the method (for, while, foreach).
2. **Count Loops:**
   - Increment the count for each loop encountered.
3. **Result:**
   - LOOP is the total number of loops in the method.

### Example
If a method contains 2 for loops and 1 while loop, LOOP = 3.

### Purpose of the Metric
A high LOOP value may indicate complex iteration logic and potential maintainability issues.
