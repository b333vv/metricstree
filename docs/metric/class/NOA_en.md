# NOA (Number of Attributes) Metric Calculation Logic

**NOA (Number of Attributes)** simply counts the total number of fields (attributes) declared in a class.

### Main Calculation Logic in NumberOfAttributesVisitor

1. **Count Fields:**
   - All fields (attributes) declared in the class are counted.

2. **Result:**
   - NOA is the total number of fields.

### Example
If a class has 3 fields, NOA = 3.

### Purpose of the Metric
NOA gives a direct measure of the data maintained by the class.
