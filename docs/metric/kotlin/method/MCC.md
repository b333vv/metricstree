# MCC (McCabe Cyclomatic Complexity) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.method.KotlinMcCabeCyclomaticComplexityVisitor`

Rules:
- Start at 1 per function/constructor.
- +1 for: `if`.
- +entries for `when` (each entry counts at least 1, default/else included).
- +1 for each loop: `for`, `while`, `do-while`.
- +1 per `catch` clause.
- +1 per boolean operator occurrence `&&`, `||`.
- +1 for Elvis `?:` when the right side is a `return` or `throw`.
- +1 for safe call `?.` when used in a conditional context (heuristic).

Result:
- `MCC = 1 + sum of decision increments`.
