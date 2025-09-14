# MPC (Message Passing Coupling) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinMessagePassingCouplingVisitor`

Counts method call expressions inside the class body (functions and initializers). No resolution is attempted.

Result:
- `MPC = total number of call expressions within the class body`.

Note: Includes calls in nested/companion objects only when their bodies are traversed as part of the class body PSI (visitor scopes the class body itself).
