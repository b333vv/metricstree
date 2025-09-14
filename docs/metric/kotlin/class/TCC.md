# TCC (Tight Class Cohesion) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinTightClassCohesionVisitor`

TCC is the ratio of directly connected method pairs to all possible method pairs.

Definitions:
- Applicable methods: non-abstract member functions declared in the class body.
- Two methods are directly connected if they share access to at least one common instance property.

Computation:
- Let `n` be the number of applicable methods.
- Let `connected` be the count of pairs (i, j) that share at least one accessed instance property.
- TCC = `connected / (n*(n-1)/2)`; 0 if there are fewer than 2 methods.

Property access detection is name-based (unqualified or `this.prop`).
