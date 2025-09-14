# MCOGNITIVE_COMPLEXITY for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.method.KotlinCognitiveComplexityVisitor`

Rules (simplified):
- +1 for each decision: if, when, while, do-while, for, each catch.
- +nesting level when nested inside other decisions.
- +1 for boolean operator transitions: `&&`, `||`.
- +1 for direct recursion (call to same function name and arity).

Result:
- Cognitive complexity per function/constructor with a body.

Notes: PSI-only approximation aligned with Java implementation approach.
