# WOC (Weight of a Class)

## Origin & Intent
* __Origin__: Bansiya & Davis, QMOOD model (2002).
* __Intent__: Evaluate design quality through class responsibility and abstraction.
  - High WOC → class is behavior-oriented (domain logic, computations).
  - Low WOC → class is data-oriented (DTO/data holder/struct-like).

## Formula
For a class C:

WOC(C) = Number of Functional Methods / Total Number of Methods

## Counting rules (Java)
* __Include in denominator (Total Number of Methods)__
  - All declared methods in the class (instance or static; any visibility).
  - Exclude constructors.

* __Exclude from numerator (Functional Methods)__
  - Simple getters: trivially return a field (e.g., `return x;`).
  - Simple setters: trivially assign a field (e.g., `this.x = x;`).
  - Trivial methods: empty methods, one-line delegations (`return foo();`, `foo();`, `x = y;`).
  - Boilerplate methods: `toString`, `equals`, `hashCode`, `finalize`, `clone`, `readObject`, `writeObject`.

* __Include in numerator (Functional Methods)__
  - Methods with non-trivial logic (arithmetic, branches, loops, transformations).
  - Overridden methods with meaningful behavior changes.
  - Utility/domain methods (sorting, validation, calculations, etc.).

## Implementation details
This repository has two implementations that follow the same rules:

* __PSI-based__: `src/main/java/org/b333vv/metric/model/visitor/type/WeightOfAClassVisitor.java`
  - Denominator: all declared methods excluding constructors.
  - Numerator: excludes IntelliJ-detected simple getters/setters, boilerplate, and trivial one-liners/empty bodies.

* __JavaParser-based__: `src/main/java/org/b333vv/metric/model/javaparser/visitor/type/JavaParserWeightOfAClassVisitor.java`
  - Denominator: all declared methods (JavaParser `MethodDeclaration`s) excluding constructors (handled implicitly as they are separate nodes).
  - Numerator: excludes simple getters (`getX()` returning a field), boolean getters (`isX()` returning a field), setters (`setX(...)` with `void`), boilerplate names, and trivial one-liners/empty bodies.

## Notes
* If the class declares no methods (after excluding constructors), WOC = 0.
* Accessor detection includes boolean `isX` getters.
* Public fields are not part of the calculation.

## Example
If a class declares 7 methods (excluding constructors): 2 getters, 1 setter, 1 `toString`, 1 trivial delegation, and 2 non-trivial logic methods →

- Total methods = 7
- Functional methods = 2
- WOC = 2 / 7 ≈ 0.2857
