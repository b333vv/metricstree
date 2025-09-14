# NOA (Number Of Attributes) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinNumberOfAttributesVisitor`

Counts attributes (properties) of a Kotlin class.

Included:
- `val/var` parameters in the primary constructor.
- `KtProperty` declarations in the class body.

Excluded:
- Properties inside companion/nested objects.
- Inherited properties.

Result:
- `NOA = count of included properties`.
