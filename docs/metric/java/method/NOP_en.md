# NOP (Number of Parameters) Metric Calculation Logic

**NOP (Number of Parameters)** counts the number of parameters declared in the method signature.

### Main Calculation Logic

1. **Check Method Signature:**
   - Identify all parameters listed in the method's signature.
2. **Count Parameters:**
   - Count the total number of parameters.
3. **Result:**
   - NOP is the total number of parameters in the method.

### Example
A method with three parameters (int a, String b, double c) has NOP = 3.

### Purpose of the Metric
A high NOP value may indicate a method with too many responsibilities or poor design.
