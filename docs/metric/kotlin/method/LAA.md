# LAA (Locality Of Attribute Accesses) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.method.KotlinLocalityOfAttributeAccessesVisitor`

Definition:
- Fraction of own attribute accesses among all attribute accesses within a function body.

Rules:
- Own accesses: references to properties declared in the same Kotlin class (including implicit `this`).
- Foreign accesses: qualified property accesses where receiver is not `this`/`super`.
- Name-based detection with best-effort resolution to verify ownership.

Result:
- `LAA = ownAccesses / totalAccesses` (0 when no accesses).
