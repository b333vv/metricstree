# NOAV (Number Of Accessed Variables) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.method.KotlinNumberOfAccessedVariablesVisitor`

Definition:
- Counts the number of unique variables accessed within a function body.

Heuristic (no full resolve):
- Track distinct simple name references that look like variables/properties.
- Exclude names used as function call callees (qualified or unqualified).
- Allowable names come from parameters, local properties, and owner class properties (including primary constructor `val/var`).

Result:
- `NOAV = |{accessed variable names}|` in the function body.
