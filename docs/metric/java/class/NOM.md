# NOM (Number of Methods) Metric Calculation Logic

NOM (Number of Methods) is the count of callable members declared directly in a class, including both regular methods and explicitly declared constructors.

This document describes exactly what is counted and what is excluded, and how both PSI- and JavaParser-based calculations are aligned.

## What is counted

• __Declared methods__: Every method declared directly in the class body. Overloads are counted individually. Getters/setters are ordinary methods and are counted.

• __Declared constructors__: Every constructor explicitly declared in the class body. Multiple constructors are counted individually.

## What is not counted

• __Implicit default constructor__: If a class has no explicitly declared constructor, the compiler-provided default constructor is NOT counted.

• __Inherited methods__: Methods coming from superclasses or interfaces are NOT counted toward the subclass’s NOM unless they are overridden in the subclass (the override is a new declaration and is counted).

• __Members of nested/inner/anonymous/local classes__: Only members declared in the target class are counted. Methods that belong to its inner, local, anonymous, or nested classes are NOT included in the target class’s NOM.

• __Initializer blocks and fields__: Static/instance initializer blocks and fields are not methods and are NOT counted.

## Class kinds where NOM is defined

PSI-side calculation uses `ClassUtils.isConcrete(psiClass)` to determine applicability. NOM is calculated when the target is a concrete class per the following rule:

• __Included__: Regular classes (including abstract classes and records) that are not interfaces/enums/anonymous/type-parameters/local classes.

• __Excluded (metric reported as UNDEFINED)__:
  - Interfaces
  - Enums
  - Anonymous classes
  - Type parameters
  - Local classes (declared inside methods/initializers)

Note: Inner/nested member classes are eligible (their own NOM is calculated), but their methods are not added to the outer class’s NOM.

## Source of truth in code

• __PSI implementation__: `src/main/java/org/b333vv/metric/model/visitor/type/NumberOfMethodsVisitor.java`
  - Logic: If `ClassUtils.isConcrete(psiClass)` is true, NOM = `psiClass.getMethods().length`.
  - Important nuance: IntelliJ PSI exposes constructors as `PsiMethod` instances, so `getMethods()` includes constructors. Therefore PSI-side NOM inherently counts constructors.

• __JavaParser implementation__: `src/main/java/org/b333vv/metric/model/javaparser/visitor/type/JavaParserNumberOfMethodsVisitor.java`
  - Logic (after fix): NOM = `n.getMethods().size()` + `n.getConstructors().size()`.
  - Rationale: JavaParser separates methods and constructors; adding constructors aligns the count with PSI.

## Edge cases and examples

• __Only implicit constructor__
```java
class A { }
```
NOM = 0 (no declared methods/constructors).

• __One declared constructor__
```java
class A { A() {} }
```
NOM = 1.

• __Overloaded methods and constructors__
```java
class A { A() {} A(int x) {} void m() {} void m(int x) {} }
```
NOM = 4 (two constructors + two methods).

• __Overriding__
```java
class B { void m() {} }
class C extends B { @Override void m() {} }
```
NOM(C) = 1 (the override in C is a declaration in C). Inherited `m()` from B does not add to C’s NOM.

• __Inner/anonymous/local classes__
```java
class A {
  void a() {}
  class B { void b() {} }
  void f() { Runnable r = new Runnable(){ public void run() {} }; }
}
```
NOM(A) = 1 (only `a()`); methods of `B` and the anonymous class are not counted for A.

• __Interfaces/enums__
Interfaces and enums are excluded by PSI rule and reported as UNDEFINED. JavaParser logic is aligned at the aggregation step so that metrics are only attached where PSI deems them applicable.

## Summary

• NOM counts declared methods + declared constructors in the target class only.

• No implicit default constructor, no inherited methods, and no members of nested/local/anonymous classes are counted.

• PSI and JavaParser implementations are aligned: PSI counts constructors via `getMethods()`, and JavaParser explicitly adds `getConstructors()` to match.
