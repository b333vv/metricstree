# DAC (Data Abstraction Coupling) Metric Calculation Logic

**DAC (Data Abstraction Coupling)** measures the number of different abstract data types (classes) used as attributes in a class.

### Main Calculation Logic in DataAbstractionCouplingVisitor

1. **Class Fields Traversal:**
   - All fields (attributes) of the analyzed class are examined.

2. **Identify Used Classes:**
   - For each field, the type is resolved to its class (excluding primitive types).
   - Each unique class used as a field type is counted.

3. **Result:**
   - DAC is the number of unique classes used as attribute types in the class.

### Example
If a class has fields of types `List`, `Map`, and `CustomClass`, then DAC = 3.

### Purpose of the Metric
A higher DAC value indicates greater coupling to other data abstractions, which can affect modularity and maintainability.
