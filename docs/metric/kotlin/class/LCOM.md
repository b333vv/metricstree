# LCOM (Lack of Cohesion of Methods) for Kotlin

Kotlin visitor: `org.b333vv.metric.model.visitor.kotlin.type.KotlinLackOfCohesionOfMethodsVisitor`

Computes LCOM as the number of connected components among methods where an edge exists if two methods access at least one common instance property.

Steps:
- Collect instance properties: primary constructor `val/var` parameters and class body `KtProperty` (excluding nested/companion objects).
- For each class member function, collect accessed properties by name (unqualified or `this.prop`).
- Build an undirected graph connecting two methods if their accessed-property sets intersect.
- LCOM = number of connected components. If there are no methods, returns 0.

Simplifications:
- Name-based access detection; companion/nested object members are ignored for cohesion.
