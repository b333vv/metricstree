# COGNITIVE_COMPLEXITY for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.method.KotlinCognitiveComplexityVisitor`

Simplified Campbell-style rules:
- +1 for each decision structure: if, when, while, do-while, for, catch.
- +nesting for being nested inside another decision (nesting depth added on entry).
- +1 for boolean operator transitions (&&, ||).
- +1 for recursion (call to function with same name and arity).

Result:
- Cognitive complexity value per function/constructor with body.

Note: PSI-only approximation aligned with the project’s Java implementation style.
