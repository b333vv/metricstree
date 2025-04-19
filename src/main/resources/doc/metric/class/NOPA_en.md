# NOPA (Number of Public Attributes) Metric Calculation Logic

**NOPA (Number of Public Attributes)** counts the number of fields in a class that are public and not static.

### Main Calculation Logic in NumberOfPublicAttributesVisitor

1. **Identify Fields:**
   - All fields of the class are examined.

2. **Filter Public, Non-static Fields:**
   - Only fields that are public and not static are counted.

3. **Result:**
   - NOPA is the number of such fields.

### Example
If a class has 2 public, non-static fields, NOPA = 2.

### Purpose of the Metric
A high NOPA value may indicate poor encapsulation and design.
