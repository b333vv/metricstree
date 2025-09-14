# FDP (Foreign Data Providers) Metric Calculation Logic

**FDP (Foreign Data Providers)** counts the number of foreign classes (excluding self and superclasses) whose fields are accessed in the method.

### Main Calculation Logic

1. **Track Field Accesses:**
   - Visit all field accesses in the method.
2. **Identify Foreign Classes:**
   - For each field, determine its class.
   - Exclude self and superclasses.
   - Count unique foreign classes accessed.
3. **Result:**
   - FDP is the number of unique foreign classes providing data.

### Example
If a method accesses fields from classes B and C, FDP = 2.

### Purpose of the Metric
A high FDP value suggests the method is tightly coupled to external data, which can be a design issue.
