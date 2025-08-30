# NOA (Number of Attributes)

NOA is the count of attributes (fields) for a class type.

It is a basic class-level metric used by multiple higher-level analyses and fitness functions.

## Definition
* __What is counted__
  - All fields declared in the class itself.
  - All inherited fields from superclasses.
  - All fields declared in directly and indirectly implemented interfaces.
  - All variables in a multi-variable field declaration. Example: `int a, b;` contributes 2.

* __What is not counted__
  - Local variables inside methods/initializers and method/constructor parameters.
  - Fields of nested/inner classes (they are counted for those classes, not for the enclosing class).
  - Annotations themselves (only fields are counted).

* __Visibility and modifiers__
  - Visibility is irrelevant: public, protected, package-private, and private fields are all counted.
  - Static vs instance is irrelevant: both are counted.
  - Overridden/hidden names are not de-duplicated. If a subclass hides a field with the same name as a superclass field, both are counted.

* __Enum constants__
  - Per Java semantics, enum constants are fields of the enum type, thus they are counted.

## Implementation Details

The metric is computed in two engines to keep behavior consistent across environments.

* __PSI-based implementation__
  - File: `src/main/java/org/b333vv/metric/model/visitor/type/NumberOfAttributesVisitor.java`
  - Logic: if the class is concrete, use `PsiClass#getAllFields()` and take its length.
  - This directly includes declared and inherited fields (superclasses and interfaces).

* __JavaParser-based implementation__
  - File: `src/main/java/org/b333vv/metric/model/javaparser/visitor/type/JavaParserNumberOfAttributesVisitor.java`
  - Primary logic: resolve the class and use `resolve().getAllFields().size()` to mirror PSI semantics.
  - Safeguards when resolution is partial/unavailable:
    1) Compute declared-only count from AST (sum of `FieldDeclaration#getVariables().size()`).
    2) If the resolved count is less than or equal to declared-only count (indicating unresolved ancestors), try a reflection-based fallback:
       - Build FQN from `CompilationUnit` package + class name if `getFullyQualifiedName()` is empty.
       - Load the class using Thread Context ClassLoader (TCCL) first, then fall back to the plugin classloader.
       - Count declared fields along the full class and interface hierarchy with reflection and take `max(resolvedCount, reflectCount)`.
    3) If neither resolution nor reflection works, return declared-only count.

  - Type solving setup used by the JavaParser engine:
    - File: `src/main/java/org/b333vv/metric/model/javaparser/util/TypeSolverProvider.java`
    - Solvers included:
      - `ReflectionTypeSolver` (JRE types)
      - `JavaParserTypeSolver` for all project source roots
      - `JarTypeSolver` for project library jars
      - `ClassLoaderTypeSolver` for classes available at runtime (e.g., IntelliJ Platform SDK types)

## Edge Cases and Notes
* __Multi-variable declarations__: Each variable counts as one attribute.
* __Name collisions/hiding__: No deduplication by name; every field in the inheritance chain contributes.
* __Interfaces__: Fields from interfaces and their super-interfaces are included (PSI and JavaParser reflection fallback both traverse interfaces).
* __Synthetic fields__: The PSI engine excludes compiler-generated synthetic fields. The JavaParser reflection fallback uses `Class#getDeclaredFields()`, which may include synthetic members for some external classes; it is only used when symbol resolution cannot provide inherited members, and the resolved count is weaker than declared-only.
* __Anonymous/local classes__: Count is computed per class declaration encountered; local variables are never counted as attributes.
* __Nested/inner classes__: Attributes are counted per class independently; outer class attributes are not added to inner classes (and vice versa).

## Rationale for the safeguards
* In plugin/runtime contexts, symbol resolution for external ancestors (e.g., IntelliJ SDK) may intermittently be unavailable. Without safeguards, JavaParser would return only declared fields, undercounting NOA (e.g., 4 instead of 14). The reflection fallback ensures inherited members are counted even when the symbol solver cannot resolve them, aligning JavaParser results with PSI.

## References (source files)
* PSI visitor: `org.b333vv.metric.model.visitor.type.NumberOfAttributesVisitor`
* JavaParser visitor: `org.b333vv.metric.model.javaparser.visitor.type.JavaParserNumberOfAttributesVisitor`
* Type solver setup: `org.b333vv.metric.model.javaparser.util.TypeSolverProvider`
