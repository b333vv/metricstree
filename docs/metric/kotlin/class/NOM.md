# NOM (Number Of Methods) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinNumberOfMethodsVisitor`

Counts methods for a Kotlin class:
- +1 for primary constructor if present.
- +1 for each secondary constructor.
- +1 for each named function declared in the class body.

Note:
- Functions in companion/nested objects are not counted toward the enclosing class’s NOM in this initial version.

Result:
- `NOM = total methods as defined above`.
