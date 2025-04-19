# SIZE2 (Number of Attributes and Methods) Metric Calculation Logic

**SIZE2 (Number of Attributes and Methods)** sums the total number of attributes (fields) and operations (methods) in a class.

### Main Calculation Logic in NumberOfAttributesAndMethodsVisitor

1. **Count Methods:**
   - All operations (methods) of the class, including inherited ones, are counted.

2. **Count Fields:**
   - All attributes (fields) of the class, including inherited ones, are counted.

3. **Result:**
   - SIZE2 is the sum of the number of operations and attributes.

### Example
If a class has 4 methods and 3 fields, SIZE2 = 7.

### Purpose of the Metric
SIZE2 provides an overall measure of the class's size in terms of its interface and state.
