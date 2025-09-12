# Detailed Plan: Kotlin Support - Phase 2 (Visitor Implementation)

## 1. Executive Summary & Goals
- **Primary Objective:** To implement the core logic for calculating all existing class-level and method-level metrics for Kotlin source code by creating a complete suite of Kotlin-specific PSI visitors.
- **Key Goals:**
    1. Establish the necessary project dependencies and package structure to support Kotlin code analysis.
    2. Develop a parallel set of Kotlin PSI visitors that mirror the semantic intent of the existing Java visitors.
    3. Create a solid, testable foundation of metric calculation logic for Kotlin, preparing for integration into the main calculation pipeline in Phase 3.

## 2. Current Situation Analysis
Phase 1 of the Kotlin support plan is complete. The core domain model (e.g., `ProjectElement`, `ClassElement`, `MethodElement`) has been successfully refactored to be language-agnostic.

The project currently possesses a comprehensive suite of metric calculators for Java, implemented as visitors that extend `JavaRecursiveElementVisitor` and operate on the Java PSI tree. However, the project lacks any Kotlin-specific analysis logic, the required Kotlin PSI dependencies, and visitors capable of traversing the Kotlin PSI tree (`KtFile`, `KtClass`, etc.). This phase will bridge that gap.

## 3. Proposed Solution / Refactoring Strategy
### 3.1. High-Level Design / Architectural Overview
The strategy is to create a parallel visitor hierarchy specifically for Kotlin, leaving the existing Java implementation untouched. While the Java visitors operate on `PsiJavaFile` and its descendants, the new Kotlin visitors will operate on `org.jetbrains.kotlin.psi.KtFile` and its elements (e.g., `KtClass`, `KtNamedFunction`).

The logic within each new Kotlin visitor will replicate the semantic intent of its Java counterpart, adapting the calculation to Kotlin's unique syntax and idioms. This parallel structure ensures a clean separation of concerns and allows for independent testing and maintenance of the logic for each language.

### 3.2. Key Components / Modules
-   **`build.gradle.kts` (Modification):** The build script will be updated to include the necessary dependencies for Kotlin PSI analysis.
-   **`org.b333vv.metric.model.visitor.kotlin` (New Package):** A new root package for all Kotlin-specific visitors.
    -   **`...kotlin.type` (New Sub-package):** Will contain visitors for calculating class-level metrics (operating on `KtClass` or `KtObjectDeclaration`).
    -   **`...kotlin.method` (New Sub-package):** Will contain visitors for calculating method-level metrics (operating on `KtNamedFunction`, `KtSecondaryConstructor`, etc.).
-   **Kotlin Visitor Base Classes (New):** Abstract base classes like `KotlinClassVisitor` and `KotlinMethodVisitor` will be created to standardize the visitor pattern for Kotlin elements, mirroring the existing `JavaClassVisitor` and `JavaMethodVisitor`.

### 3.3. Detailed Action Plan / Phases
This phase is broken down into three logical sub-phases: setup, class-level visitor implementation, and method-level visitor implementation.

#### Phase 2.1: Project Setup and Dependencies
- **Objective(s):** Prepare the project environment for Kotlin code analysis.
- **Priority:** High

- **Task 2.1.1: Add Kotlin PSI Dependency**
    - **Rationale/Goal:** To gain access to the Kotlin PSI classes (`KtFile`, `KtClass`, etc.) required for building the visitors.
    - **Estimated Effort (Optional):** S
    - **Deliverable/Criteria for Completion:** The `build.gradle.kts` file is updated to include the `org.jetbrains.kotlin:kotlin-compiler-embeddable` dependency. The project successfully syncs with the new dependency.

- **Task 2.1.2: Create Kotlin Visitor Package Structure**
    - **Rationale/Goal:** To establish a clean, parallel directory structure for the new Kotlin visitors, mirroring the existing Java visitor structure.
    - **Estimated Effort (Optional):** S
    - **Deliverable/Criteria for Completion:** The following new packages are created:
        - `org.b333vv.metric.model.visitor.kotlin`
        - `org.b333vv.metric.model.visitor.kotlin.type`
        - `org.b333vv.metric.model.visitor.kotlin.method`

- **Task 2.1.3: Create Kotlin Visitor Base Classes**
    - **Rationale/Goal:** To create a standardized, reusable foundation for all Kotlin visitors, simplifying their implementation and integration.
    - **Estimated Effort (Optional):** S
    - **Deliverable/Criteria for Completion:**
        - An abstract `KotlinClassVisitor` is created. It will be visited by a `ClassElement` and will, in turn, visit the underlying `KtClass` element.
        - An abstract `KotlinMethodVisitor` is created for `MethodElement` and `KtNamedFunction`.

#### Phase 2.2: Implement Class-Level Kotlin Visitors
- **Objective(s):** Implement the calculation logic for all class-level metrics on Kotlin classes.
- **Priority:** High

- **Task 2.2.1: Implement `KotlinWeightedMethodCountVisitor` (WMC)**
    - **Rationale/Goal:** Calculate the sum of cyclomatic complexities for all functions and constructors in a Kotlin class. This is a foundational complexity metric.
    - **Estimated Effort (Optional):** M
    - **Deliverable/Criteria for Completion:** A `KotlinWeightedMethodCountVisitor` class is created. It traverses all `KtNamedFunction`, `KtSecondaryConstructor`, and `KtPrimaryConstructor` elements within a `KtClass` and sums their cyclomatic complexities (which requires the completion of Task 2.3.1).
    - **Key Considerations:** Must correctly handle functions in the primary class body, companion objects, and other nested objects.

- **Task 2.2.2: Implement `KotlinCouplingBetweenObjectsVisitor` (CBO)**
    - **Rationale/Goal:** Measure the number of other classes a Kotlin class is coupled to. This is a critical but complex metric.
    - **Estimated Effort (Optional):** L
    - **Deliverable/Criteria for Completion:** A `KotlinCouplingBetweenObjectsVisitor` is created. It must identify unique class dependencies from property types, function parameters/return types, local variables, generic type arguments, super-types, and receiver types of extension functions.
    - **Key Considerations:** This task heavily depends on the Kotlin PSI's type resolution capabilities. Special attention must be paid to resolving types from both project sources and external libraries.

- **Task 2.2.3: Implement `KotlinLackOfCohesionOfMethodsVisitor` (LCOM)**
    - **Rationale/Goal:** Measure the cohesion of a Kotlin class based on shared property access among its functions.
    - **Estimated Effort (Optional):** L
    - **Deliverable/Criteria for Completion:** A `KotlinLackOfCohesionOfMethodsVisitor` is created. It must identify all instance properties and then, for each function, determine which properties it accesses. It will then build a connectivity graph to count the number of disconnected components.
    - **Key Considerations:** Must differentiate between instance properties and companion object/top-level properties. The definition of "method" must be adapted to include all relevant functions.

- **Task 2.2.4: Implement Remaining Class-Level Visitors**
    - **Rationale/Goal:** To achieve full feature parity with the Java implementation for all class-level metrics.
    - **Estimated Effort (Optional):** L
    - **Deliverable/Criteria for Completion:** Create Kotlin equivalents for all other visitors in `...visitor.type`, including but not limited to: `DIT`, `NOC`, `RFC`, `NOM`, `NOA`, `TCC`, `NCSS`. Each implementation must be accompanied by unit/integration tests.

#### Phase 2.3: Implement Method-Level Kotlin Visitors
- **Objective(s):** Implement the calculation logic for all method-level metrics on Kotlin functions.
- **Priority:** High

- **Task 2.3.1: Implement `KotlinMcCabeCyclomaticComplexityVisitor` (CC)**
    - **Rationale/Goal:** This is a foundational metric required by WMC (Task 2.2.1) and is a key indicator of method-level complexity.
    - **Estimated Effort (Optional):** M
    - **Deliverable/Criteria for Completion:** A visitor that operates on `KtNamedFunction` and constructors. It must correctly increment complexity for Kotlin's control flow structures.
    - **Key Considerations:** A clear mapping must be defined and implemented for: `if` expressions, `when` expressions (with and without arguments), `for` and `while` loops, `try-catch` blocks, the elvis operator (`?:`) when used with `return` or `throw`, safe calls (`?.`), and boolean operators (`&&`, `||`).

- **Task 2.3.2: Implement `KotlinLinesOfCodeVisitor` (LOC)**
    - **Rationale/Goal:** Provide a basic size metric for Kotlin functions.
    - **Estimated Effort (Optional):** S
    - **Deliverable/Criteria for Completion:** A visitor that calculates the number of lines within a function's body.
    - **Key Considerations:** The implementation must decide how to consistently handle both block-body functions (`fun a() { ... }`) and expression-body functions (`fun a() = ...`).

- **Task 2.3.3: Implement Remaining Method-Level Visitors**
    - **Rationale/Goal:** To achieve full feature parity with the Java implementation for all method-level metrics.
    - **Estimated Effort (Optional):** L
    - **Deliverable/Criteria for Completion:** Create Kotlin equivalents for all other visitors in `...visitor.method`, including but not limited to: `CND`, `LND`, `NOPM`, `LAA`, `FDP`, `NOAV`. Each implementation must be accompanied by unit/integration tests.

## 4. Key Considerations & Risk Mitigation
### 4.1. Technical Risks & Challenges
-   **Semantic Mapping of Metrics:** Some Java-centric metrics may not have a direct one-to-one mapping to Kotlin's idiomatic features (e.g., how does LCOM apply to a file with top-level functions? How are extension functions handled in CBO?).
    -   **Mitigation:** For each complex visitor, the implementation should be preceded by a brief documented analysis of how the metric's definition will be interpreted for Kotlin. These decisions should be captured in code comments or a separate design note and reviewed by the team.
-   **Kotlin PSI Complexity:** The Kotlin PSI is different from Java's and can be more complex due to features like type inference, extension functions, and expression-based control flow.
    -   **Mitigation:** Allocate specific time for learning and experimentation. Utilize IntelliJ's "PSI Viewer" tool extensively during development. Begin with simpler metrics (e.g., NOM, LOC) to build familiarity before tackling more complex ones (CBO, LCOM).
-   **Testing Complexity:** Creating a robust test suite for Kotlin visitors will require creating a variety of Kotlin source files that cover all relevant language features.
    -   **Mitigation:** For each visitor, create a dedicated test file in `testData` that isolates the specific Kotlin constructs being measured. This will make tests clearer and easier to maintain.

### 4.2. Dependencies
-   The class-level visitor `KotlinWeightedMethodCountVisitor` (Task 2.2.1) is dependent on the completion of the method-level `KotlinMcCabeCyclomaticComplexityVisitor` (Task 2.3.1).
-   All tasks in Phase 2.2 and 2.3 are dependent on the successful completion of Phase 2.1.

## 5. Success Metrics / Validation Criteria
-   A complete suite of Kotlin visitors exists in the new package structure, mirroring the functionality of the Java visitors.
-   The project compiles successfully with the new Kotlin PSI dependency.
-   A new set of integration tests for Kotlin visitors is created and passes, validating that metrics are calculated correctly against a curated set of Kotlin code samples.
-   The successful completion of this phase provides all necessary logical components to proceed with Phase 3 (Integration into Calculation Workflow).

## 6. Assumptions Made
-   The development team has, or can acquire, the necessary knowledge of the Kotlin PSI API.
-   The existing metric definitions are conceptually applicable to Kotlin code, even if the implementation requires adaptation to Kotlin-specific language features.
-   The IntelliJ Platform and its bundled Kotlin plugin provide a stable and reliable API for analyzing Kotlin code.

## 7. Open Questions / Areas for Further Investigation
-   **Top-Level Functions:** How should metrics be calculated for top-level functions that are not part of a class? (Suggestion: Group them under a synthetic node representing the file, which can hold metrics like a class).
-   **Companion Objects:** How should functions and properties within a `companion object` be treated for class-level metrics like WMC and NOM? (Suggestion: Treat them as static members of the enclosing class for metric calculation purposes to maintain consistency with Java).
-   **Future Metrics:** Are there any Kotlin-specific metrics that should be considered for a future implementation (e.g., number of extension functions, sealed class hierarchy depth)? (This is out of scope for the current task but should be noted for future planning).