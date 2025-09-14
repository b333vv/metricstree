# NOAM (Number Of Added Methods) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinNumberOfAddedMethodsVisitor`

Heuristic:
- Count non-constructor functions declared in the class body that are not overrides, plus private functions (considered "added").

Result:
- `NOAM = number of added methods`.

Note: Kotlin has no static methods; metric focuses on member functions only.
