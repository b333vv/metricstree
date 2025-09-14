# LND (Loop Nesting Depth) Metric Calculation Logic

**LND (Loop Nesting Depth)** measures the maximum depth of nested loops (for, while, foreach) in a method.

### Main Calculation Logic

1. **Traverse Method:**
   - Visit all loop statements in the method.
2. **Track Nesting:**
   - Increment depth for each nested loop.
   - Keep track of the deepest nesting encountered.
3. **Result:**
   - LND is the maximum loop nesting depth found.

### Example
A method with a for loop containing a while loop has LND = 2.

### Purpose of the Metric
High LND indicates complex looping logic, which may hinder readability and maintainability.
