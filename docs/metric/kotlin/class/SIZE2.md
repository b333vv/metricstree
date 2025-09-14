# SIZE2 (Number Of Attributes And Methods) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinNumberOfAttributesAndMethodsVisitor`

Heuristic approximation counting in the class body:
- Attributes: properties not located in a companion object.
- Methods: declared functions (excluding constructors).

Result:
- `SIZE2 = attributes + methods` for the class body scope.
