# Method-Level Metrics Calculation Logic

This document describes the calculation logic for 16 method-level metrics as implemented in `org.b333vv.metric.model.visitor.method`.

---

**1. Cognitive Complexity (COGNITIVE_COMPLEXITY)**
- Measures how difficult a method is to understand, based on control flow structures and nesting.
- Increments for each control structure (if, loop, catch, etc.) and for nesting depth, with special handling for else-if chains.

**2. Condition Nesting Depth (CND)**
- Maximum depth of nested conditional statements (if-else) in a method.
- Each nested if/else increases the depth.

**3. Coupling Dispersion (CDISP)**
- Ratio of distinct classes whose methods are called to the total number of method calls from the method.
- CDISP = (number of distinct classes called) / (number of method calls).

**4. Coupling Intensity (CINT)**
- Number of external methods (from other classes) called by the method.
- Counts unique method calls to other classes.

**5. Foreign Data Providers (FDP)**
- Number of foreign classes (excluding self and superclasses) whose fields are accessed in the method.
- Counts unique external classes providing data.

**6. Halstead Metrics (HL, HVC, HD, HVL, HER, HEF)**
- Calculated using operators and operands in the method:
  - HL: Length (total operators + operands)
  - HVC: Vocabulary (distinct operators + operands)
  - HD: Difficulty
  - HVL: Volume
  - HER: Estimated errors
  - HEF: Effort

**7. Lines of Code (LOC)**
- Number of lines in the method, excluding abstract methods.

**8. Locality of Attribute Accesses (LAA)**
- Ratio of own class fields accessed to all fields accessed in the method.
- LAA = (number of own fields accessed) / (total fields accessed).

**9. Loop Nesting Depth (LND)**
- Maximum depth of nested loops (for, while, foreach) in the method.

**10. Maximum Nesting Depth (MND)**
- Maximum depth of all nested code blocks (loops, conditionals, etc.) in the method.

**11. McCabe Cyclomatic Complexity (MCC)**
- Number of linearly independent paths through the method.
- Increments for each decision point (if, while, case, etc.).

**12. Method Cognitive Complexity (MCOGNITIVE_COMPLEXITY)**
- Alternative cognitive complexity calculation, may use a different algorithm or scoring than COGNITIVE_COMPLEXITY.

**13. Method Complexity (MCOMP)**
- Counts control flow statements and logical operators (&&, ||) in the method.
- Each adds to the complexity score.

**14. Number of Accessed Variables (NOAV)**
- Number of unique variables (local or field) accessed in the method.

**15. Number of Loops (LOOP)**
- Total number of loop constructs (for, while, foreach) in the method.

**16. Number of Parameters (NOP)**
- Number of parameters declared in the method signature.

---

Each metric provides a quantitative measure of different aspects of method design, complexity, and maintainability.
