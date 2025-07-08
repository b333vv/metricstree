# WOC (Weight of a Class) Metric Calculation Logic

**WOC (Weight of a Class)** measures the proportion of functional (non-trivial) public methods to all public methods in a class.

### Main Calculation Logic in WeightOfAClassVisitor

1. **Identify Public Methods:**
   - All public methods of the class are collected, excluding abstract methods.

2. **Filter Functional Methods:**
   - Functional methods are those that are not simple getters or setters.

3. **Calculate Ratio:**
   - WOC is calculated as the number of functional public methods divided by the total number of public methods.
   - If there are no public methods, WOC = 0.

### Example
If a class has 2 functional public methods and 4 public methods in total, WOC = 0.5.

### Purpose of the Metric
A higher WOC value indicates that a greater proportion of the class's interface is functional rather than trivial.
