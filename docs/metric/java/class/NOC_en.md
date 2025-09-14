# NOC (Number of Children) Metric Calculation Logic

**NOC (Number of Children)** counts the number of immediate subclasses (children) that inherit from a given class.

### Main Calculation Logic in NumberOfChildrenVisitor

1. **Identify Subclasses:**
   - All classes that directly inherit from the analyzed class are identified.

2. **Result:**
   - NOC is the number of direct subclasses.

### Example
If a class has 3 direct subclasses, NOC = 3.

### Purpose of the Metric
A high NOC value may indicate that the class is heavily reused as a base class, which can be both a sign of good abstraction or a potential design risk.
