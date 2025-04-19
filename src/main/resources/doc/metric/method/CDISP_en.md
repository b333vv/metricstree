# CDISP (Coupling Dispersion) Metric Calculation Logic

**CDISP (Coupling Dispersion)** is the ratio of distinct classes whose methods are called to the total number of method calls from the method.

### Main Calculation Logic

1. **Count Method Calls:**
   - Track all method calls made by the method.
2. **Identify Distinct Classes:**
   - For each method call, determine the class it belongs to.
   - Count unique classes called (excluding self).
3. **Calculate Ratio:**
   - CDISP = (number of distinct classes called) / (number of method calls).
   - If no method calls, CDISP = 0.

### Example
If a method calls 4 methods from 2 different classes, CDISP = 0.5.

### Purpose of the Metric
A higher CDISP indicates more dispersed coupling, which can increase maintenance complexity.
