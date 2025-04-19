# LCOM (Lack of Cohesion of Methods) Metric Calculation Logic

**LCOM (Lack of Cohesion of Methods)** measures how methods of a class are related to each other through shared fields.

### Main Calculation Logic in LackOfCohesionOfMethodsVisitor

1. **Identify Methods and Fields:**
   - All applicable methods and fields of the class are determined.

2. **Analyze Field Usage:**
   - For each method, the set of fields it accesses is recorded.
   - Methods are linked if they access at least one common field.

3. **Calculate Components:**
   - Methods are grouped into components based on shared field usage.
   - LCOM is the number of such disconnected components.

4. **Result:**
   - A higher LCOM means less cohesion among methods.

### Example
If methods do not share any fields, LCOM equals the number of methods.

### Purpose of the Metric
A high LCOM value indicates poor cohesion, suggesting the class may be doing too many unrelated things.
