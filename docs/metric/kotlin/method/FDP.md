# FDP (Foreign Data Providers) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.method.KotlinForeignDataProvidersVisitor`

Counts distinct foreign providers whose properties are accessed within a function.

Rules:
- Qualified property accesses `a.b` or `a?.b` are considered; unqualified names are local.
- Prefer resolving selector to owning class FQN; otherwise, use the textual receiver as provider key.
- Exclude `this`/`super` receivers.

Result:
- `FDP = number of distinct providers` in the function body.
