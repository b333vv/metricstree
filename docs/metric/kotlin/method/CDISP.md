# CDISP (Coupling Dispersion) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.method.KotlinCouplingDispersionVisitor`

Heuristic definition:
- Ratio of the number of distinct foreign providers invoked to the number of distinct foreign method calls in a function body.

Counting rules:
- Only qualified calls where the receiver is not `this`/`super` are considered foreign.
- Provider identity is approximated by the textual receiver expression.
- Distinct foreign method calls are identified by `providerKey#calleeName`.

Result:
- `CDISP = |providers| / |foreignCalls|`, 0 when there are no foreign calls.
