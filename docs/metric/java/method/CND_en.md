# CND (Condition Nesting Depth) Metric Calculation Logic

**CND (Condition Nesting Depth)** measures the maximum depth of nested conditional statements (if-else) in a method.

### Main Calculation Logic

1. **Traverse Method:**
   - Visit all if/else statements in the method.
2. **Track Nesting Depth:**
   - Increment depth for each nested if/else.
   - Keep track of the deepest nesting encountered.
3. **Result:**
   - CND is the maximum nesting depth found.

### Example
If an if is nested inside another if, CND = 2.

### Purpose of the Metric
A high CND indicates complex logic that may be hard to follow and maintain.
