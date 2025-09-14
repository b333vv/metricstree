# NOAC (Number Of Accessor Methods) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinNumberOfAccessorMethodsVisitor`

Counts accessor methods by detecting properties that declare custom accessors with bodies.

Rule:
- For each class body property, if getter has a body → +1; if setter has a body → +1.

Result:
- `NOAC = number of custom accessor bodies` (getter + setter counted separately).
