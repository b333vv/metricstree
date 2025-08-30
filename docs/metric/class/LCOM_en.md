# LCOM (Lack of Cohesion of Methods) Metric Calculation Logic

**LCOM (Lack of Cohesion of Methods)** measures how methods of a class are related to each other through shared fields.

### Main Calculation Logic in LackOfCohesionOfMethodsVisitor

1. **Identify applicable methods and fields** (`org.b333vv.metric.model.visitor.type.CohesionUtils`, PSI; `org.b333vv.metric.model.javaparser.visitor.type.JavaParserLackOfCohesionOfMethodsVisitor`, JavaParser)
   - Methods: only instance (non-static) methods declared in the class, excluding boilerplate.
   - Fields: only instance (non-static) fields declared in the same class (inherited fields are excluded).

2. **Analyze field usage (same-class instance fields only)**
   - For each applicable method, collect the set of same-class instance fields it reads or writes.
   - Both qualified (e.g., `this.f`, `obj.f` when `obj` is `this`) and unqualified (`f`) references are counted.
   - Methods that do not use any same-class instance field are excluded from the LCOM graph.

3. **Build connectivity graph (two types of edges)**
   - Shared-field edge: connect two methods if their used-field sets intersect (at least one shared field).
   - Method-call linkage edge: connect two methods if one applicable method directly calls the other applicable method within the same class.

4. **Compute LCOM**
   - LCOM is the number of connected components in the undirected graph over the remaining methods.
   - Interpretation: higher LCOM ⇒ lower cohesion.

### Example
If methods do not share any fields, LCOM equals the number of methods.

### Purpose of the Metric
A high LCOM value indicates poor cohesion, suggesting the class may be doing too many unrelated things.

---

## Exact Inclusion/Exclusion Rules

• **Methods included**
  - Instance (non-static) methods declared in the class.
  - Constructors are not included.
  - Boilerplate methods are excluded (by name): `toString`, `equals`, `hashCode`, `finalize`, `clone`, `readObject`, `writeObject`.

• **Fields included**
  - Instance (non-static) fields declared in the same class.
  - Inherited fields are excluded from consideration.

• **Method must use fields**
  - Only methods that use at least one included field participate in the LCOM graph.
  - Access type does not matter (read/write/both), and both qualified and unqualified PSI/AST references count.

---

## Connectivity Graph Details

• **Shared-field connectivity**
  - Let `U(m)` be the set of included fields used by method `m`.
  - For any two methods `m1`, `m2`, add an undirected edge if `U(m1) ∩ U(m2) ≠ ∅`.

• **Method-call linkage**
  - Add an undirected edge between `caller` and `callee` if `caller` invokes `callee` and both are applicable methods of the same class.
  - PSI: resolution via IntelliJ PSI.
  - JavaParser: resolution via symbol solver; when resolution is unavailable, a best-effort fallback by method name and arity (number of parameters) is used.

• **Result**
  - LCOM = number of connected components in this graph.
  - Edge cases:
    - No included fields or no methods using fields ⇒ LCOM = 0.
    - Exactly one method uses fields ⇒ LCOM = 1.

---

## Alignment Between PSI and JavaParser (Current Behavior)

The PSI and JavaParser implementations are aligned to the same semantics:

• Exclude boilerplate instance methods by name (see list above).

• Exclude static methods and only consider methods declared in the same class.

• Only count non-static fields declared in the same class; exclude inherited fields.

• Count both qualified and unqualified field references.

• Include method-call linkage between applicable methods of the same class when building the graph.

• Only methods that use at least one included field are placed in the graph.

These alignments were made in:
- PSI utilities: `CohesionUtils` and `LackOfCohesionOfMethodsVisitor`.
- JavaParser visitor: `JavaParserLackOfCohesionOfMethodsVisitor` (added boilerplate filtering; restricted fields to same class; added method-call linkage with resolution/fallback).

---

## Worked Examples From This Codebase

• `org.b333vv.metric.model.code.JavaClass`
  - One instance field (`psiClass`), effectively used by one instance method (`getPsiClass`).
  - LCOM = 1.

• `org.b333vv.metric.model.metric.Metric`
  - Multiple method clusters; after excluding boilerplate and applying edges, LCOM = 3.

• `org.b333vv.metric.service.CacheService`
  - Several instance methods call others (e.g., VFS events → cache invalidation methods), collapsing components via call-linkage.
  - LCOM = 1 with aligned PSI/JavaParser implementations.

---

## Notes and Limitations

• Method-call linkage does not follow transitive resolution across other types; only same-class direct calls are considered for edges.

• JavaParser name/arity fallback may over-approximate in rare overload scenarios if symbol resolution is unavailable; PSI resolution is typically precise. In practice, both implementations now align on the tested project classes.

• The metric intentionally focuses on cohesion around same-class instance state; static members and inherited state are outside this LCOM definition.
