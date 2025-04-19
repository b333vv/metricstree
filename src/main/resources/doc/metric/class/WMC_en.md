# WMC (Weighted Method Count) Metric Calculation Logic

**WMC (Weighted Method Count)** sums the complexities of all methods in a class, providing a measure of overall class complexity.

### Main Calculation Logic in WeightedMethodCountVisitor

1. **Identify Methods:**
   - All methods of the class are examined.

2. **Calculate Complexity:**
   - The complexity of each method is calculated (typically using cyclomatic complexity or a similar metric).

3. **Sum Complexities:**
   - The complexities of all methods are summed to obtain WMC.

### Example
If a class has 3 methods with complexities 1, 2, and 3, WMC = 6.

### Purpose of the Metric
A high WMC value indicates a class with complex logic, which may be harder to maintain and test.
