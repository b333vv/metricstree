# MCOMP (Method Complexity) for Kotlin

There is currently no dedicated Kotlin visitor named specifically for MCOMP. The intended definition mirrors the Java doc `docs/metric/java/method/MCOMP_en.md`:

Definition (heuristic):
- Count control flow statements and logical operators within the method body.

What contributes:
- Control flow statements: `if`, `for`, `while`, `do-while`, `when` entries, and `catch` clauses.
- Logical operators: `&&`, `||` occurrences.

Result:
- `MCOMP = total count of the above occurrences`.

Note:
- In the Kotlin pipeline, closely related information is captured by `KotlinMcCabeCyclomaticComplexityVisitor` (MCC). When MCOMP is exposed, it should follow the counting rules above using Kotlin PSI, consistent with the Java definition.
