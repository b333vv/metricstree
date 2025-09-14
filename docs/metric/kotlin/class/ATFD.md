# ATFD (Access To Foreign Data) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinAccessToForeignDataVisitor`

ATFD counts the number of distinct external classes whose data (properties/fields via direct access or via accessor calls) is accessed within the class body.

Main rules (PSI heuristics):
- Traverse the entire class body and collect provider identifiers where a property access is detected.
- Prefer resolution of the accessed property to discover the declaring class/object FQN:
  - If selector resolves to `PsiField`, use its containing class qualified name.
  - If selector resolves to `KtProperty`, use the owner `KtClassOrObject` FQN.
- If resolution is not available, fall back to a textual receiver key from the qualified receiver expression, e.g., for `a.b` use `a` as a key.
- Exclude implicit/explicit `this` and `super` receivers from providers.
- De-duplicate providers using the resolved FQN or the textual receiver key.
- Exclude the class itself by FQN when present.

Result:
- `ATFD = number of distinct providers`.

Notes and limitations:
- Accessor-like calls (`getX()/setX()/isX`) are not specially resolved and are only counted when part of a qualified access captured by the qualified-expression visitors.
- This PSI-only approach may include approximations when symbol resolution is unavailable.
