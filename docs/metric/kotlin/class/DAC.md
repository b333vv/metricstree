# DAC (Data Abstraction Coupling) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinDataAbstractionCouplingVisitor`

Counts the number of distinct classes used as attribute/property types of a Kotlin class.

Included types:
- Types of `val/var` parameters in the primary constructor.
- Types of properties declared in the class body.

De-duplication and exclusions:
- Distinctness by simple user type names.
- Skips Kotlin built-ins and the class’s own simple name.

Result:
- `DAC = number of distinct user types used as attributes`.
