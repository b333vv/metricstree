# Implementation Plan: JavaParser Visitor Suite

## 1. Executive Summary & Goals
This document outlines the detailed plan to implement Task 2.1 from `docs/javaparser/phase2.md`. The primary objective is to create a complete suite of metric calculation visitors that operate on the JavaParser Abstract Syntax Tree (AST), achieving functional parity with the existing PSI-based visitor suite.

-   **Key Goal 1:** Create a new package, `org.b333vv.metric.model.javaparser.visitor`, to house all new JavaParser-based visitors, mirroring the structure of the existing PSI visitor packages.
-   **Key Goal 2:** For every existing PSI visitor in `org.b333vv.metric.model.visitor.*`, implement a corresponding JavaParser visitor that calculates the same metric based on its documented logic.
-   **Key Goal 3:** Establish a parallel unit testing structure for the new visitors to ensure their correctness and maintain code quality.

## 2. Current Situation Analysis
The project currently calculates all metrics using visitors that traverse IntelliJ's Program Structure Interface (PSI) tree. These visitors are located in `org.b333vv.metric.model.visitor.method` and `org.b333vv.metric.model.visitor.type`.

Following the completion of Phase 1 of the parallel calculation refactoring, the core data model (`Metric.java`) has been updated to store two values: `psiValue` and `javaParserValue`. The current system populates only the `psiValue`. This plan details the creation of the visitors necessary to populate the `javaParserValue`.

## 3. Proposed Solution / Refactoring Strategy
### 3.1. High-Level Design / Architectural Overview
The strategy is to build a parallel visitor hierarchy that uses the JavaParser library's AST nodes instead of PSI elements. This new suite of visitors will be invoked by the `JavaParserCalculationStrategy` (to be implemented in a later task) to augment the existing `JavaProject` model.

1.  **New Package Structure:** A new source package `org.b333vv.metric.model.javaparser.visitor` will be created, with `method` and `type` sub-packages, to mirror the existing PSI visitor structure.
2.  **Base Visitors:** Abstract base classes, `JavaParserClassVisitor` and `JavaParserMethodVisitor`, will be created. These will extend JavaParser's `VoidVisitorAdapter<T>` and provide a standardized way to visit a node, calculate a metric, and pass the result back to the calculation strategy. The generic argument `T` will be a context object (e.g., a `JavaClass` or `JavaMethod` model object) where the calculated metric will be stored.
3.  **Concrete Implementations:** Each PSI visitor will have a corresponding JavaParser visitor implemented. The logic for each metric, as described in the `docs/metric/*/*.md` files, will be reimplemented using JavaParser's AST traversal and analysis capabilities.

### 3.2. Key Components / Modules
-   **`org.b333vv.metric.model.javaparser.visitor` (New):** The root package for all new components.
-   **`JavaParserClassVisitor` / `JavaParserMethodVisitor` (New):** Abstract base classes to ensure a consistent implementation pattern for all visitors.
-   **Concrete Visitor Classes (New):** A one-to-one mapping of new JavaParser visitors for each existing PSI visitor.
-   **Unit Tests (New):** A parallel test hierarchy under `src/test` to validate each new visitor's logic against sample code snippets.

### 3.3. Detailed Action Plan / Phases
This plan is structured to tackle visitors in increasing order of complexity, allowing for incremental progress and addressing dependencies logically.

#### Phase 1: Foundational Setup and Simple Visitors
-   **Objective(s):** Establish the necessary package and class structure. Implement the most straightforward visitors that rely on simple counting or structural analysis.
-   **Priority:** High

-   **Task 1.1: Create Package and Base Class Structure**
    -   **Rationale/Goal:** To create the foundational structure for all new visitors.
    -   **Estimated Effort (Optional):** S
    -   **Deliverable/Criteria for Completion:**
        -   The package `org.b333vv.metric.model.javaparser.visitor` and its `method` and `type` sub-packages are created.
        -   Abstract classes `JavaParserClassVisitor<T>` and `JavaParserMethodVisitor<T>` are created. They should extend `com.github.javaparser.ast.visitor.VoidVisitorAdapter<T>`.

-   **Task 1.2: Implement Simple Method-Level Visitors**
    -   **Rationale/Goal:** To implement visitors that perform simple counting operations on the method body, establishing a baseline for the implementation process.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:** The following visitors are implemented in the `method` package, with corresponding unit tests:
        -   `JavaParserNumberOfLoopsVisitor` (for `NOL`)
        -   `JavaParserLinesOfCodeVisitor` (for `LOC`)
        -   `JavaParserNumberOfParametersVisitor` (for `NOPM`)

-   **Task 1.3: Implement Simple Class-Level Visitors**
    -   **Rationale/Goal:** To implement visitors that count direct members of a class.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:** The following visitors are implemented in the `type` package, with corresponding unit tests:
        -   `JavaParserNumberOfMethodsVisitor` (for `NOM`)
        -   `JavaParserNumberOfAttributesVisitor` (for `NOA`)
        -   `JavaParserNumberOfPublicAttributesVisitor` (for `NOPA`)
        -   `JavaParserNumberOfAccessorMethodsVisitor` (for `NOAC`)

#### Phase 2: Visitors with Complex Internal Logic
-   **Objective(s):** Implement visitors that require more complex traversal of the AST within a single method or class body.
-   **Priority:** High

-   **Task 2.1: Implement Complexity Visitors**
    -   **Rationale/Goal:** To implement visitors that analyze control flow and nesting depth. These are complex but self-contained within a method body.
    -   **Estimated Effort (Optional):** L
    -   **Deliverable/Criteria for Completion:** The following visitors are implemented in the `method` package, with corresponding unit tests:
        -   `JavaParserMcCabeCyclomaticComplexityVisitor` (for `CC`)
        -   `JavaParserCognitiveComplexityVisitor` (for `CCM`)
        -   `JavaParserConditionNestingDepthVisitor` (for `CND`)
        -   `JavaParserLoopNestingDepthVisitor` (for `LND`)
        -   `JavaParserMaximumNestingDepthVisitor` (for `MND`)

-   **Task 2.2: Implement Halstead Visitors**
    -   **Rationale/Goal:** To implement the full suite of Halstead metrics, which involves detailed token analysis of operators and operands.
    -   **Estimated Effort (Optional):** L
    -   **Deliverable/Criteria for Completion:**
        -   A `JavaParserHalsteadMethodVisitor` is created to calculate all method-level Halstead metrics (`HVL`, `HD`, `HL`, `HEF`, `HVC`, `HER`).
        -   A `JavaParserHalsteadClassVisitor` is created to calculate all class-level Halstead metrics (`CHVL`, `CHD`, etc.).
        -   Comprehensive unit tests are created for both.

#### Phase 3: Visitors Requiring Type Resolution
-   **Objective(s):** Implement the most complex visitors, which depend on resolving types and symbols across the entire project.
-   **Priority:** Medium
-   **Dependency:** This phase is dependent on the completion of **Task 2.2: Implement and Configure JavaParser Type Solver** from the parent plan (`docs/javaparser/phase2.md`).

-   **Task 3.1: Implement Inheritance-Based Visitors**
    -   **Rationale/Goal:** To implement visitors that require navigating the class hierarchy.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:** The following visitors are implemented in the `type` package, with corresponding unit tests that use a configured `TypeSolver`:
        -   `JavaParserDepthOfInheritanceTreeVisitor` (for `DIT`)
        -   `JavaParserNumberOfChildrenVisitor` (for `NOC`)
        -   `JavaParserNumberOfOverriddenMethodsVisitor` (for `NOOM`)
        -   `JavaParserNumberOfAddedMethodsVisitor` (for `NOAM`)

-   **Task 3.2: Implement Coupling and Cohesion Visitors**
    -   **Rationale/Goal:** To implement the most complex metrics that rely heavily on accurate type resolution for method calls and field accesses.
    -   **Estimated Effort (Optional):** L
    -   **Deliverable/Criteria for Completion:** The following visitors are implemented, with corresponding unit tests that use a configured `TypeSolver`:
        -   `JavaParserCouplingBetweenObjectsVisitor` (for `CBO`)
        -   `JavaParserResponseForClassVisitor` (for `RFC`)
        -   `JavaParserLackOfCohesionOfMethodsVisitor` (for `LCOM`)
        -   `JavaParserTightClassCohesionVisitor` (for `TCC`)
        -   `JavaParserAccessToForeignDataVisitor` (for `ATFD`)
        -   `JavaParserDataAbstractionCouplingVisitor` (for `DAC`)
        -   `JavaParserMessagePassingCouplingVisitor` (for `MPC`)
        -   `JavaParserLocalityOfAttributeAccessesVisitor` (for `LAA`)
        -   `JavaParserForeignDataProvidersVisitor` (for `FDP`)

## 4. Key Considerations & Risk Mitigation
### 4.1. Technical Risks & Challenges
-   **Type Resolution Complexity:** Accurately calculating coupling and inheritance metrics is highly dependent on a correctly configured JavaParser `TypeSolver`.
    -   **Mitigation:** The implementation of visitors in Phase 3 must be tightly coordinated with the implementation of the Type Solver (Task 2.2 of the parent plan). Initial versions of these visitors can be developed using simplified test cases while the full project-aware solver is being built.
-   **Logic Parity with PSI:** The JavaParser AST may represent certain Java constructs differently than the PSI tree, which could lead to minor, legitimate discrepancies in metric values.
    -   **Mitigation:** The primary goal is to correctly re-implement the metric's definition (from `docs/metric/*/*.md`) using the JavaParser API. Unit tests should focus on validating this logic, not on achieving a bit-for-bit identical output with the PSI version in all edge cases. Any significant, unexplained discrepancies should be investigated as potential bugs.
-   **Performance:** JavaParser's type solving can be memory and CPU intensive.
    -   **Mitigation:** Visitor implementations should be mindful of performance. The overall performance management, however, is the responsibility of the `JavaParserCalculationStrategy` which will manage the lifecycle of the parser and solver.

### 4.2. Dependencies
-   **Internal:** Phase 3 is strictly dependent on the completion of the JavaParser Type Solver (Task 2.2 from `docs/javaparser/phase2.md`).
-   **External:** This plan requires the `javaparser-symbol-solver-core` dependency to be correctly configured in the `build.gradle.kts` file.

## 5. Success Metrics / Validation Criteria
-   A corresponding JavaParser visitor class exists for every PSI visitor class in the `org.b333vv.metric.model.visitor` package.
-   Each new visitor class is accompanied by a suite of unit tests that validate its calculation logic against various code samples.
-   The project's overall code coverage remains high, with the new `org.b333vv.metric.model.javaparser.visitor` package meeting or exceeding the project's quality standards.
-   A manual validation run on a sample project shows that the `javaParserValue` is being populated with reasonable values that align with the metric definitions.

## 6. Assumptions Made
-   The `Metric.java` data model has been successfully refactored to include the `setJavaParserValue()` method.
-   The `JavaParserCalculationStrategy` will be responsible for instantiating and running the visitors created in this plan.
-   The logic and definitions provided in the `docs/metric/*/*.md` files are the source of truth for metric calculations.

## 7. Open Questions / Areas for Further Investigation
-   What is the definitive strategy for handling files that JavaParser fails to parse? (Initial Assumption: Log an error to `MetricsConsole`, set all corresponding `javaParserValue`s to `Value.UNDEFINED`, and continue the process without crashing). This needs to be finalized in Task 2.4 of the parent plan.