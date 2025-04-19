# COGNITIVE_COMPLEXITY Metric Calculation Logic

**Cognitive Complexity** measures how difficult a method is to understand, based on the control flow structures and their nesting.

### Main Calculation Logic

1. **Traverse Method Code:**
   - Visit all control flow structures (if, for, while, catch, etc.).
2. **Increment for Structures:**
   - Increase complexity for each control structure.
3. **Increment for Nesting:**
   - Add extra complexity for nested structures.
   - Special handling for else-if chains to avoid double-counting.
4. **Result:**
   - The sum is the Cognitive Complexity value for the method.

### Example
A method with an if inside a for loop has higher cognitive complexity than one with only a single if.

### Purpose of the Metric
High Cognitive Complexity indicates code that is harder to read, test, and maintain.
