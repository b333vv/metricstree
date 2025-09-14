# RFC (Response For Class) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinResponseForClassVisitor`

RFC is the size of the set consisting of:
- Methods declared in the class (functions in the class body; constructors are counted in code but the doc mirrors Java method semantics—this implementation only collects functions).
- Plus unique method calls from within the class bodies (by simple name and arity), gathered by walking call expressions.

PSI-only approach:
- Uses callee simple names and arity; no resolution attempted.

Result:
- `RFC = |declared methods ∪ called methods|` by name/arity pairs.
