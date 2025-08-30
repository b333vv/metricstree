# TCC (Tight Class Cohesion) Metric Calculation Logic

**TCC (Tight Class Cohesion)** measures the fraction of method pairs in a class that are directly connected by sharing access to at least one common instance field declared in that class.

This document specifies the exact rules our implementation uses (PSI and JavaParser), including edge cases and exclusions, to ensure repeatability and alignment across both calculation paths.

## Sources in Code
- PSI implementation: `src/main/java/org/b333vv/metric/model/visitor/type/TightClassCohesionVisitor.java`
- PSI utilities: `src/main/java/org/b333vv/metric/model/visitor/type/CohesionUtils.java`
- JavaParser implementation: `src/main/java/org/b333vv/metric/model/javaparser/visitor/type/JavaParserTightClassCohesionVisitor.java`

## 1) Method Set (Eligible Methods)
We build the set M of applicable methods of the class with these rules:

- Exclude constructors.
- Exclude static methods.
- Exclude abstract methods.
- Exclude boilerplate methods by name: `toString`, `equals`, `hashCode`, `finalize`, `clone`, `readObject`, `writeObject`.
- Include methods of any visibility (public/protected/package/private).

These rules align PSI (`CohesionUtils.getApplicableMethods()`) and JavaParser after the 2025-08-30 fix.

If |M| < 2, there are no possible pairs and the metric is defined as TCC = 0.0.

## 2) Fields Considered
Only instance fields declared in the same class are considered. Specifically:

- Include only fields declared in the class under measurement (exclude inherited fields).
- Exclude static fields.

PSI identifies fields via `PsiReferenceExpression` resolution and filters to non-static fields with `field.getContainingClass() == targetClass`.
JavaParser enumerates declared fields of the class and keeps their names when `!field.isStatic()`.

## 3) What Counts as "Field Usage" by a Method
A method is considered to use a field if its body accesses that field in any of the following forms:

- Unqualified access: `field` (parsed as a reference/name to a class field).
- Qualified with `this`: `this.field`.

Notes:

- PSI: uses resolution from `PsiReferenceExpression` to detect `PsiField` and then applies the same-class + non-static filters.
- JavaParser: detects `NameExpr` matching declared instance field names, and `FieldAccessExpr` where scope is `ThisExpr`. This avoids reliance on fragile symbol resolution and aligns behavior with PSI for typical Java code style (unqualified instance field accesses).
- Accesses through other qualifiers (e.g., `other.field`) do not count as usage of the class's own field.

## 4) Connectivity Between Methods
Two methods m1, m2 ∈ M are "directly connected" if the intersection of their used-field sets is non-empty:

- Let F(m) be the set of instance field names used by method m (as per section 3).
- Methods are connected iff `F(m1) ∩ F(m2) ≠ ∅`.

The number of connected pairs is the count of such unordered pairs among methods in M.

## 5) Formula
- Let n = |M| be the number of applicable methods.
- Possible pairs P = n × (n − 1) / 2.
- Connected pairs C = number of unordered method pairs sharing at least one instance field.
- TCC = C / P, with the convention:
  - If P = 0 (i.e., n < 2), TCC = 0.0.

## 6) Edge Cases and Clarifications
- Methods that do not use any instance fields still participate in the denominator (P), but they only contribute to the numerator (C) if paired with a method that shares at least one instance field (which they do not), hence such pairs are not connected.
- Static field usages and inherited field usages are ignored.
- Method calls do not contribute to TCC connectivity (only shared field usage matters for TCC; method linkage is used in LCOM, not TCC).
- Boilerplate methods are excluded from M (see list above).
- Constructors are excluded from M.

## 7) Examples
Example class:

```java
class A {
    private int x, y;
    void m1() { x = 1; }
    void m2() { y = 2; }
    void m3() { x++; y++; }
}
```

- M = {m1, m2, m3}, n = 3, P = 3.
- F(m1) = {x}, F(m2) = {y}, F(m3) = {x, y}.
- Connected pairs: (m1, m3) share x; (m2, m3) share y; (m1, m2) share none.
- C = 2, TCC = 2 / 3 ≈ 0.6667.

## 8) Consistency Between PSI and JavaParser
Historically, JavaParser returned 0.0 in some cases due to only scanning `FieldAccessExpr` with full symbol resolution, missing unqualified field usages. As of 2025-08-30, JavaParser detection was updated to:

- Align method filtering with PSI (exclude static/abstract/boilerplate; any visibility).
- Detect unqualified and `this.` field accesses without requiring symbol resolution.
- Restrict fields to instance fields declared in the same class.

This aligns JavaParser results with PSI for TCC and resolves prior discrepancies such as 0.0 results for classes that do share instance fields across methods.
