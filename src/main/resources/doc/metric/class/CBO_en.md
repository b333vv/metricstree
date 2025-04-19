# CBO (Coupling Between Objects) Metric Calculation Logic

**CBO (Coupling Between Objects)** measures the number of classes to which a given class is coupled, either by depending on them or being depended upon by them.

### Main Calculation Logic in CouplingBetweenObjectsVisitor

1. **Identify Dependencies and Dependents:**
   - For each concrete class, collect all classes it depends on (uses) and all classes that depend on it.
2. **Combine Sets:**
   - Take the union of the dependencies and dependents sets to avoid double-counting.
3. **Result:**
   - CBO is the size of this union set.

### Example
If a class depends on 3 classes and is depended on by 2 others (with 1 overlap), CBO = 4.

### Purpose of the Metric
A high CBO value indicates strong coupling, which can reduce modularity and increase maintenance complexity.
