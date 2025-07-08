# NOO (Number of Operations) Metric Calculation Logic

**NOO (Number of Operations)** counts the number of operations (methods) defined in a class, including inherited ones.

### Main Calculation Logic in NumberOfOperationsVisitor

1. **Count Operations:**
   - All methods available in the class (declared and inherited) are counted.

2. **Result:**
   - NOO is the total number of operations.

### Example
If a class declares 3 methods and inherits 2, NOO = 5.

### Purpose of the Metric
NOO shows the total operational interface of the class.
