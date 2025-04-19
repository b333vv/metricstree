# NOAC (Number of Accessor Methods) Metric Calculation Logic

**NOAC (Number of Accessor Methods)** counts the number of simple getter and setter methods in a class, excluding static methods.

### Main Calculation Logic in NumberOfAccessorMethodsVisitor

1. **Identify Methods:**
   - All methods of the class are examined.

2. **Filter Accessors:**
   - Methods are filtered to include only simple getters and setters (property accessors), excluding static methods.

3. **Result:**
   - NOAC is the count of such accessor methods.

### Example
If a class has 2 getters and 1 setter, NOAC = 3.

### Purpose of the Metric
A high NOAC value may indicate that the class exposes too much of its internal state, which can reduce encapsulation.
