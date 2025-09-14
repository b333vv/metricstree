# NOPA (Number Of Public Attributes) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinNumberOfPublicAttributesVisitor`

Counts properties declared in the class body that are effectively public instance attributes.

Exclusions:
- Properties inside companion objects.
- Properties with the `const` modifier.

Rule:
- Treat properties without `private` or `protected` as public.

Result:
- `NOPA = count of effectively public properties`.
