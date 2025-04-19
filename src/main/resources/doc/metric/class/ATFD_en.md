# ATFD (Access To Foreign Data) Metric Calculation Logic

**ATFD (Access To Foreign Data)** is a metric that measures the number of different classes whose data is accessed directly by the analyzed class. "Foreign data" means fields and methods of other classes that are accessed not through the class's own methods.

### Main Calculation Logic in AccessToForeignDataVisitor

1. **Class Code Traversal:**
   - All methods and fields of the analyzed class are visited.

2. **Detection of Accesses to Foreign Data:**
   - Accesses to fields and methods of other classes (not the current class) are detected.
   - Only direct accesses are counted (e.g., `other.field` or `other.getField()`), not accesses through the class's own fields or methods.
   - Accesses to standard libraries and primitive types are ignored.

3. **Counting Unique Classes:**
   - For each access, the class to which the data belongs is determined.
   - Only unique classes are counted, i.e., the metric tracks the number of different external classes whose data is accessed.

4. **Result:**
   - The ATFD value is the number of unique external classes whose data is accessed directly by the analyzed class.

### Example
If class `A` accesses fields or methods of classes `B` and `C`, the ATFD for class `A` is 2, even if there are multiple accesses to these classes' data.

### Purpose of the Metric
A high ATFD value indicates strong coupling of the class with other classes, which may suggest a violation of the encapsulation principle and poor design.
