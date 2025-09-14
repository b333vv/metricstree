# DIT (Depth Of Inheritance Tree) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinDepthOfInheritanceTreeVisitor`

Heuristic PSI implementation without full resolve:
- 0 if there are no explicit super type list entries.
- 1 if there is at least one explicit super type.

Result:
- `DIT ∈ {0, 1}` in this phase.

Note: A later phase can replace this with a proper inheritance chain depth via indices/resolve.
