# TCC (Tight Class Cohesion) Metric Calculation Logic

**TCC (Tight Class Cohesion)** measures the relative number of directly connected method pairs in a class, where methods are connected if they access at least one common field.

### Main Calculation Logic in TightClassCohesionVisitor

1. **Identify Methods:**
   - All non-abstract methods of the class are collected.

2. **Count Connected Pairs:**
   - For each pair of methods, check if they share access to at least one common field.
   - Count the number of such connected pairs.

3. **Calculate Possible Pairs:**
   - The total number of possible method pairs is calculated as n(n-1)/2, where n is the number of methods.

4. **Result:**
   - TCC is the ratio of connected pairs to possible pairs. If there are no possible pairs, TCC = 0.

### Example
If 2 out of 3 possible method pairs are connected, TCC = 2/3 ≈ 0.67.

### Purpose of the Metric
A higher TCC value indicates stronger cohesion within the class.
