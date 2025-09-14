# LAA (Locality of Attribute Accesses) Metric Calculation Logic

**LAA (Locality of Attribute Accesses)** is the ratio of the number of own class fields accessed to the total number of fields accessed in the method.

### Main Calculation Logic

1. **Track Field Accesses:**
   - Visit all field accesses in the method.
2. **Count Own vs. Foreign Fields:**
   - Count how many accessed fields belong to the method's own class.
   - Count the total number of fields accessed.
3. **Calculate Ratio:**
   - LAA = (number of own fields accessed) / (total fields accessed).
   - If no fields accessed, LAA = 0.

### Example
If a method accesses 3 fields, 2 of which are its own, LAA = 2/3 ≈ 0.67.

### Purpose of the Metric
A high LAA value indicates good encapsulation and low coupling.
