# MCOGNITIVE_COMPLEXITY (Method Cognitive Complexity) Metric Calculation Logic

**MCOGNITIVE_COMPLEXITY (Method Cognitive Complexity)** is an alternative calculation of cognitive complexity for methods, possibly using a different scoring algorithm than standard Cognitive Complexity.

### Main Calculation Logic

1. **Traverse Method:**
   - Visit all control flow structures and nesting levels.
2. **Apply Scoring Rules:**
   - Increment complexity for each structure and for nesting, according to a specific algorithm.
3. **Result:**
   - The sum is the Method Cognitive Complexity value.

### Example
A method with deeply nested loops and conditionals will have a higher MCOGNITIVE_COMPLEXITY.

### Purpose of the Metric
High values indicate code that is harder to understand and maintain.
