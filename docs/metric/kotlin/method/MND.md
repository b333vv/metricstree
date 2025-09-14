# MND (Maximum Nesting Depth) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.method.KotlinMaximumNestingDepthVisitor`

Definition:
- Counts the deepest nesting level of control structures within a method.

Control structures considered:
- `if`
- `when`
- `for`
- `while`
- `do-while`
- `try`/`catch`

Result:
- `MND = maximum depth` found during traversal (0 if no body).
