# DIT (Depth of Inheritance Tree) Metric Calculation Logic

**DIT (Depth of Inheritance Tree)** measures the number of ancestor classes from the analyzed class up to the root of the inheritance hierarchy.

### Main Calculation Logic in DepthOfInheritanceTreeVisitor

1. **Inheritance Traversal:**
   - Starting from the class, repeatedly traverse to its superclass.

2. **Count Depth:**
   - For each superclass found, increment the depth counter.
   - If there is no superclass, the depth is 0.

3. **Result:**
   - DIT is the total number of ancestor classes.

### Example
If a class extends another class, which in turn extends a base class, DIT = 2.

### Purpose of the Metric
A higher DIT value indicates deeper inheritance, which can increase complexity and affect understandability.
