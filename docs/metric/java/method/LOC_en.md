# LOC (Lines of Code) Metric Calculation Logic

**LOC (Lines of Code)** counts the number of lines in a method, excluding abstract methods.

### Main Calculation Logic

1. **Check Method Type:**
   - Ignore abstract methods.
2. **Count Lines:**
   - Count all lines in the method body.
3. **Result:**
   - LOC is the total number of lines in the method.

### Example
If a method has 12 lines of code, LOC = 12.

### Purpose of the Metric
LOC measures code size and is a basic indicator of complexity and maintainability.
