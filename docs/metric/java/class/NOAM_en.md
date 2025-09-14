# NOAM (Number of Added Methods) Metric Calculation Logic

**NOAM (Number of Added Methods)** counts the number of methods added in a class that are not inherited or overridden from superclasses.

### Main Calculation Logic in NumberOfAddedMethodsVisitor

1. **Identify Methods:**
   - All methods of the class are examined.

2. **Filter Added Methods:**
   - Methods are counted if they are not constructors, not abstract, and either private, static, or do not override any superclass method.

3. **Result:**
   - NOAM is the number of such added methods.

### Example
If a class declares 3 unique methods (not inherited or overridden), NOAM = 3.

### Purpose of the Metric
NOAM reflects the extent of new functionality introduced by the class.
