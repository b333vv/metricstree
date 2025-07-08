# MPC (Message Passing Coupling) Metric Calculation Logic

**MPC (Message Passing Coupling)** counts the number of method calls made by a class to methods of other classes.

### Main Calculation Logic in MessagePassingCouplingVisitor

1. **Identify Concrete Classes:**
   - Only concrete, non-anonymous classes are considered.
2. **Count Method Calls:**
   - For each method call expression in the class, increment the counter.
3. **Result:**
   - MPC is the total number of method calls to other classes made within the class.

### Example
If a class makes 5 method calls to other classes, MPC = 5.

### Purpose of the Metric
A high MPC value indicates a high degree of interaction with other classes, which can increase complexity and reduce encapsulation.
