# CINT (Coupling Intensity) Metric Calculation Logic

**CINT (Coupling Intensity)** counts the number of external methods (from other classes) called by the method.

### Main Calculation Logic

1. **Track Method Calls:**
   - Visit all method calls in the method.
2. **Filter External Calls:**
   - Only count calls to methods not in the same class.
   - Count unique external methods called.
3. **Result:**
   - CINT is the number of unique external method calls.

### Example
If a method calls 3 methods from other classes, CINT = 3.

### Purpose of the Metric
High CINT means the method relies heavily on other classes, increasing coupling.
