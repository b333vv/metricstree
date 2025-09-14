# HALSTEAD (Class-level) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinHalsteadClassVisitor`

Computes Halstead metrics for classes by traversing expressions in the class body and counting operators/operands.

Counting rules (heuristics):
- Operators: binary, prefix, postfix operators; function call callee names are treated as operators.
- Operands: identifiers (`KtSimpleNameExpression`), string templates, constant literals.

Derived measures:
- Length (CHL) = N = total operators + operands.
- Vocabulary (CHVC) = n = distinct operators + distinct operands.
- Difficulty (CHD) = (n1/2) * (N2/n2).
- Volume (CHVL) = N * log2(max(1, n)).
- Effort (CHEF) = difficulty * volume.
- Errors (CHER) ≈ effort^(2/3) / 3000.

Result collection:
- `buildMetrics()` returns all six metrics for the class.
