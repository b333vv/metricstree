# MND (Maximum Nesting Depth) Metric Calculation Logic

**MND (Maximum Nesting Depth)** is the maximum depth of all nested code blocks (loops, conditionals, etc.) in a method.

### Main Calculation Logic

1. **Traverse Method:**
   - Visit all code blocks in the method (loops, ifs, etc.).
2. **Track Nesting:**
   - Increment depth for each nested block.
   - Keep track of the deepest nesting encountered.
3. **Result:**
   - MND is the maximum nesting depth found in the method.

### Example
A method with an if inside a for inside a while has MND = 3.

### Purpose of the Metric
A high MND value indicates complex logic and potential maintenance challenges.
