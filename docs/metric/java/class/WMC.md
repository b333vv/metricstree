# WMC (Weighted Method Count)

WMC is the sum of the cyclomatic complexity of all declared methods and constructors in a class. It reflects how complex a class is to understand, test, and maintain.

This plugin computes WMC both via PSI (IntelliJ PSI) and via JavaParser. The implementations are aligned to produce identical results.

## Where it is implemented

- PSI implementation:
  - Class-level aggregator: `org.b333vv.metric.model.visitor.type.WeightedMethodCountVisitor`
    - Uses `PsiClass.getMethods()` and sums each method's complexity via `MethodComplexityVisitor`.
  - Method CC rules: `org.b333vv.metric.model.visitor.method.MethodComplexityVisitor`

- JavaParser implementation:
  - Class-level aggregator: `org.b333vv.metric.model.javaparser.visitor.type.JavaParserWeightedMethodCountVisitor`
    - Sums over `ClassOrInterfaceDeclaration.getMethods()` and `getConstructors()`.
  - Method/constructor CC rules: `org.b333vv.metric.model.javaparser.visitor.method.JavaParserMcCabeCyclomaticComplexityVisitor`

## What is included in WMC

- All declared instance, static, and private/protected/public methods in the class.
- All declared constructors of the class.
- Overloaded methods and multiple constructors are each counted separately.

Note: In PSI, `PsiClass.getMethods()` returns all methods including constructors. JavaParser explicitly iterates both methods and constructors to match PSI semantics.

## Cyclomatic Complexity baseline

- Each method/constructor starts with a baseline complexity of 1.

## Control-flow elements that INCREASE complexity by +1

- if statement: `if (...)` (each `if` adds 1; `else` does not add)
- for loop: `for (...) { ... }`
- enhanced for: `for (T x : xs) { ... }`
- while loop: `while (...) { ... }`
- do-while loop: `do { ... } while (...);`
- catch clause: each `catch (...) { ... }`
- ternary conditional expression: `cond ? a : b`
- logical AND/OR short-circuit boolean operations: `&&`, `||`
  - PSI counts a chain like `a && b && c` as `operands - 1`.
  - JavaParser counts each `BinaryExpr` node; the AST of `a && b && c` contains two `BinaryExpr`s, so also +2. This aligns with PSI.

## switch statement handling

- PSI (`MethodComplexityVisitor`): counts 1 per switch "case group" (including `default`). Multiple labels collapsed into one group still add only +1. Fall-through between labeled groups does not change counts beyond the groups themselves.
- JavaParser (`JavaParserMcCabeCyclomaticComplexityVisitor`): counts +1 per `SwitchEntry`, including `default`, regardless of the number of labels attached to that entry. This aligns with PSI behavior.

Examples:
- `switch(x) { case A: ... break; case B: ... break; default: ... }` adds +3.
- `switch(x) { case A: case B: ... break; default: ... }` adds +2 (one entry for A/B group, one for default).

## Elements that DO NOT change complexity

- else, else-if as a separate `else if` chain only counts the `if` nodes themselves.
- default parameters, annotations, simple assignments, declarations, returns, breaks/continues, method calls, field accesses.
- try/finally do not add; only `catch` adds.
- lambda/method references do not inherently add unless they contain counted constructs.

## WMC calculation steps

1. For each declared method and constructor in the class, compute cyclomatic complexity using the rules above (baseline 1 + increments).
2. Sum these complexities across all methods/constructors to produce the class WMC.

Formally: `WMC(class) = Σ CC(m)` for all methods and constructors `m` declared in the class.

## Example

If a class has:
- constructor with no branches → CC = 1
- method with `if (...)` → CC = 2
- method with `for (...)` and `a && b && c` → CC = 1 (baseline) + 1 (for) + 2 (`&&` chain has 3 operands → +2) = 4

Then WMC = 1 + 2 + 4 = 7.

## Notes and edge cases

- Methods without bodies (e.g., interface methods, abstract methods) still have baseline CC = 1 if they are included in the class being measured. Project-level logic typically evaluates WMC for concrete classes; however, both implementations align on how method CC is computed.
- Generated or synthetic methods are not considered unless present in source and parsed/visible via PSI/JavaParser.

## References to source

- PSI visitors:
  - `src/main/java/org/b333vv/metric/model/visitor/type/WeightedMethodCountVisitor.java`
  - `src/main/java/org/b333vv/metric/model/visitor/method/MethodComplexityVisitor.java`
- JavaParser visitors:
  - `src/main/java/org/b333vv/metric/model/javaparser/visitor/type/JavaParserWeightedMethodCountVisitor.java`
  - `src/main/java/org/b333vv/metric/model/javaparser/visitor/method/JavaParserMcCabeCyclomaticComplexityVisitor.java`
