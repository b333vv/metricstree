# LND (Loop Nesting Depth) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.method.KotlinLoopNestingDepthVisitor`

Computes the maximum nesting depth among loop constructs inside a function:
- `for`, `while`, `do-while`.

Result:
- `LND = maximum loop nesting depth` (0 if no body).
