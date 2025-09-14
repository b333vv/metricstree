# WMC (Weighted Methods per Class) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinWeightedMethodCountVisitor`

WMC is computed by summing cyclomatic complexities of:
- Primary constructor (treated as baseline complexity 1).
- All secondary constructors.
- All member functions declared in the class body.
- Functions in companion and nested objects (their complexities are included recursively).

Cyclomatic complexity is delegated to `KotlinMcCabeCyclomaticComplexityVisitor`.

Result:
- `WMC = Σ MCC(method/constructor)` across the scopes above.
