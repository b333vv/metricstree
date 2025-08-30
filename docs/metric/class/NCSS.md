# NCSS (Non-Commenting Source Statements) Metric Calculation Logic

**NCSS (Non-Commenting Source Statements)** counts the number of executable statements in a class, excluding comments and empty statements.

## Scope and Source of Truth

* **Scope:** Statements lexically contained within a class declaration. Statements belonging to nested named classes are excluded from the enclosing class’s NCSS.
* **Anonymous/local classes:** Statements inside anonymous/local classes are considered part of the enclosing context (consistent with PSI behavior), while named inner classes are counted separately for that inner class.
* **Source of truth:** IntelliJ PSI visitor `NonCommentingSourceStatementsVisitor` defines the authoritative counting rules. The JavaParser visitor mirrors these rules exactly.

## What Counts as a Statement

NCSS for a class is the sum of the following components:

1) **Base statements (inside method/initializer/constructor bodies):** count every executable `PsiStatement`, excluding the items in the “Exclusions” section below. This includes, for example:
   - Return (`return x;`), throw (`throw e;`)
   - Expression statements (method calls, assignments, object creation, ++/--, etc.)
   - Control-flow statements: `if`, `while`, `do`, `for`, `foreach`, `try`, `switch`, `break`, `continue`
   - Local variable declarations (with or without initializer)

2) **Else branches:** each `else` branch of an `if` counts as +1 in addition to the `if` statement itself.

3) **Switch entries:** each `case` and `default` label counts as +1 (in addition to the `switch` statement counted in base statements).

4) **Exception handlers:** each `catch` section counts as +1; a `finally` block counts as +1.

5) **Class and member declarations:**
   - Class/interface/enum declaration of the current (non-anonymous) class: +1
   - Each method declaration: +1
   - Each constructor declaration: +1
   - Field declarations: +1 per variable declarator (e.g., `int a, b;` contributes 2)

6) **For-loop header details:**
   - The `for` statement itself is a base statement (+1).
   - If the `for` initializer contains a variable declaration, add +1 (once per `for`, not per declared variable).
   - Each expression in the `for` update section (e.g., `i++`, `j+=2`, `f(x)`) counts as +1.

## Exclusions (not counted as base statements)

* Comments (line, block, Javadoc).
* Empty statements (`;`).
* Block statements (container blocks) such as method bodies, `if { ... }` bodies, loop bodies, `try/catch/finally` blocks, etc. The statements inside these blocks are counted, but the blocks themselves are not.
* Switch label statements themselves are excluded from base statements; they are instead counted in the “Switch entries” component.
* Expression-bodied lambda bodies (e.g., `x -> f(x)`): the single expression used as the body is not counted as a separate statement. For block-bodied lambdas (`x -> { ... }`), the statements inside the block are counted normally.

## Alignment Between PSI and JavaParser

The JavaParser visitor `JavaParserNonCommentingSourceStatementsVisitor` is implemented to match PSI counts exactly:

* Excludes `EmptyStmt` and `BlockStmt` (container blocks) from base statements.
* Skips statements that belong to nested named classes.
* Adds components for: else branches, switch entries, catch sections, finally blocks.
* Counts the current class declaration, method and constructor declarations, and field variable declarators.
* Handles `for` headers precisely: +1 for a declaration in the initializer (once per `for`), and +1 per update expression.
* Does not count the expression body of expression-bodied lambdas; counts statements inside block-bodied lambdas.

## Worked Example: for-loop header

```java
for (int i = 0; i < 3; i++) {
    System.out.println(i);
}
```

Contribution to NCSS for the enclosing class:
* Base statements: `for (...) { ... }` => +1
* For initializer declaration: `int i = 0` => +1
* For update expressions: `i++` => +1
* Inside body: `System.out.println(i);` => +1 (as an expression statement)
Total from this snippet: 4

## Notes

* Explicit constructor invocation (`this(...)` / `super(...)`) is counted as a statement.
* Explicit `return;` in a `void` method is counted.
* Multiple variable declarators in a single field declaration line count individually.
* The metric is attached to non-interface, non-anonymous classes; interfaces are traversed but NCSS may not be produced as a class-level metric for interfaces.

## Purpose

NCSS provides a measure of class size and complexity, helping to assess maintainability and code volume. The precise rules above ensure consistency across PSI and JavaParser implementations used in this project.
