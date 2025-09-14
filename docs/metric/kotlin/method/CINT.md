# CINT (Coupling Intensity) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.method.KotlinCouplingIntensityVisitor`

Heuristic definition:
- Counts distinct foreign method calls within a function body.

Counting rules:
- Qualified calls like `a.foo()` or `a?.bar()` where the receiver is not `this`/`super`.
- Distinctness by `(receiver textual key + callee name)`.
- Unqualified calls are treated as local and ignored.

Result:
- `CINT = number of distinct foreign qualified calls`.
