# CBO (Coupling Between Objects) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinCouplingBetweenObjectsVisitor`

Counts unique external types referenced by a Kotlin class (PSI-only heuristics).

What is scanned:
- Supertypes from `superTypeListEntries`.
- Primary constructor property parameters (`val/var`) types.
- Properties in class body (`KtProperty`) types.
- Function receiver, parameter, and return types for functions declared in the class body.
- Local variable type references within function bodies (by visiting `KtTypeReference`).

De-duplication and exclusions:
- Distinctness by simple user type names.
- Skips Kotlin built-in types (e.g., `Int`, `String`, `List`, etc.).
- Excludes the class’s own simple name.
- Nested/companion object internals are skipped for the enclosing class.

Result:
- `CBO = number of distinct external user types referenced`.

Limitations:
- No resolve of type aliases/imports; type names are taken as seen in PSI.
- May include approximations without symbol resolution.
