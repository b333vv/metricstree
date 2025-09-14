# WOC (Weight Of a Class) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinWeightOfAClassVisitor`

Definition:
- WOC = number of functional methods / total declared methods (excluding constructors).

Functional method heuristic:
- Exclude property accessors (`get*`, `set*`, `is*`) with trivial signatures.
- Exclude abstract/external methods (no body).
- Exclude trivial delegations and empty bodies: single `return x`, `return foo()`, single call `foo()`, or simple `x = y`.
- Otherwise, count as functional.

Result:
- `WOC ∈ [0,1]`, with 0 when there are no declared methods.
