# NOOM (Number of Overridden Methods) Metric Calculation Logic

**NOOM (Number of Overridden Methods)** counts how many methods in a class override methods from its superclasses.

### Main Calculation Logic in NumberOfOverriddenMethodsVisitor

1. **Identify Methods:**
   - All concrete methods in the class are examined.

2. **Filter Overridden Methods:**
   - Methods are counted if they override a concrete method from a superclass.

3. **Result:**
   - NOOM is the number of overridden methods.

### Example
If a class overrides 2 methods from its superclass, NOOM = 2.

### Purpose of the Metric
NOOM reflects the degree to which a class customizes or replaces inherited behavior.
