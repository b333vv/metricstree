# Status Report & Completion Plan: Kotlin Visitor Implementation (docs/kotlin/phase2.md)

## 1. Executive Summary & Goals
This document provides a detailed status analysis of the plan outlined in `docs/kotlin/phase2.md`. The analysis is based on the provided file structure, which indicates the implementation progress of the specified tasks.

The plan in `docs/kotlin/phase2.md` is **partially complete**. All foundational setup and all class-level visitors have been implemented. However, a significant portion of the method-level visitors remains to be created.

The key goals of this new plan are:
-   **Achieve Feature Parity:** Implement the remaining Kotlin method-level metric visitors to match the existing Java functionality.
-   **Ensure Correctness:** Establish a testing framework to validate the new Kotlin visitors.
-   **Unblock Future Work:** Complete all Phase 2 tasks to enable the project to proceed to Phase 3 (Integration into Calculation Workflow) as defined in `docs/kotlin/main_plan.md`.

## 2. Current Situation Analysis
The file `docs/kotlin/phase2.md` details the second phase of the overall Kotlin support initiative described in `docs/kotlin/main_plan.md`. Its purpose is to create a complete suite of PSI visitors capable of calculating software metrics for Kotlin code.

An analysis of the current `src/main/java/org/b333vv/metric/model/visitor/kotlin/` directory reveals the following implementation status against the plan:

### 2.1. Completed Tasks (Phase 2.1 & 2.2)
The foundational setup and all class-level visitors have been implemented.
-   **(Task 2.1.1)** Kotlin PSI dependencies are present in `build.gradle.kts`.
-   **(Task 2.1.2)** The package structure `.../visitor/kotlin/{type, method}` exists.
-   **(Task 2.1.3)** Base classes `KotlinClassVisitor` and `KotlinMethodVisitor` have been created.
-   **(Task 2.2.1 - 2.2.4)** All planned class-level visitors exist:
    -   `KotlinWeightedMethodCountVisitor` (WMC)
    -   `KotlinCouplingBetweenObjectsVisitor` (CBO)
    -   `KotlinLackOfCohesionOfMethodsVisitor` (LCOM)
    -   `KotlinDepthOfInheritanceTreeVisitor` (DIT)
    -   `KotlinNumberOfChildrenVisitor` (NOC)
    -   `KotlinResponseForClassVisitor` (RFC)
    -   `KotlinNumberOfMethodsVisitor` (NOM)
    -   `KotlinNumberOfAttributesVisitor` (NOA)
    -   `KotlinTightClassCohesionVisitor` (TCC)
    -   `KotlinNonCommentingSourceStatementsVisitor` (NCSS)

### 2.2. Partially Completed Tasks (Phase 2.3)
The implementation of method-level visitors has begun but is incomplete.
-   **(Task 2.3.1)** `KotlinMcCabeCyclomaticComplexityVisitor` (CC) is implemented.
-   **(Task 2.3.2)** `KotlinLinesOfCodeVisitor` (LOC) is implemented.

### 2.3. Remaining Tasks (Phase 2.3.3)
The following method-level visitors, specified in the plan, have **not yet been implemented**:
-   `KotlinConditionNestingDepthVisitor` (CND)
-   `KotlinLoopNestingDepthVisitor` (LND)
-   `KotlinNumberOfParametersVisitor` (NOPM)
-   `KotlinLocalityOfAttributeAccessesVisitor` (LAA)
-   `KotlinForeignDataProvidersVisitor` (FDP)
-   `KotlinNumberOfAccessedVariablesVisitor` (NOAV)

## 3. Proposed Solution / Completion Strategy
The following action plan details the steps required to complete the work outlined in `docs/kotlin/phase2.md`. The strategy is to implement the remaining visitors and create corresponding tests to ensure their correctness.

### 3.1. High-Level Design
The implementation of the remaining visitors should follow the established pattern:
1.  Create a new class in the `.../visitor/kotlin/method/` package.
2.  The class should extend `KotlinMethodVisitor`.
3.  The logic should mirror the semantic intent of the corresponding Java visitor in `.../visitor/method/`, adapting as necessary for Kotlin's PSI structure and language features.
4.  New test cases should be added to `testData/kotlin/` to cover Kotlin-specific idioms relevant to each metric.

### 3.2. Detailed Action Plan
#### Phase 1: Implement Remaining Method-Level Visitors
-   **Objective(s):** Achieve full feature parity for method-level metrics between Java and Kotlin implementations.
-   **Priority:** High

-   **Task 1.1: Implement `KotlinConditionNestingDepthVisitor` (CND)**
    -   **Rationale/Goal:** Measure the maximum nesting of conditional statements (`if`, `when`, ternary operator `?:`) in Kotlin functions.
    -   **Estimated Effort (Optional):** S
    -   **Deliverable/Criteria for Completion:** `KotlinConditionNestingDepthVisitor.java` file is created and implements the CND logic.

-   **Task 1.2: Implement `KotlinLoopNestingDepthVisitor` (LND)**
    -   **Rationale/Goal:** Measure the maximum nesting of loop structures (`for`, `while`, `do-while`) in Kotlin functions.
    -   **Estimated Effort (Optional):** S
    -   **Deliverable/Criteria for Completion:** `KotlinLoopNestingDepthVisitor.java` file is created and implements the LND logic.

-   **Task 1.3: Implement `KotlinNumberOfParametersVisitor` (NOPM)**
    -   **Rationale/Goal:** Count the number of parameters in Kotlin functions and constructors.
    -   **Estimated Effort (Optional):** S
    -   **Deliverable/Criteria for Completion:** `KotlinNumberOfParametersVisitor.java` file is created and implements the NOPM logic.

-   **Task 1.4: Implement `KotlinLocalityOfAttributeAccessesVisitor` (LAA)**
    -   **Rationale/Goal:** Measure the ratio of own-class property accesses to total property accesses within a function.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:** `KotlinLocalityOfAttributeAccessesVisitor.java` file is created. The implementation correctly distinguishes between local and foreign property access.

-   **Task 1.5: Implement `KotlinForeignDataProvidersVisitor` (FDP)**
    -   **Rationale/Goal:** Count the number of foreign classes whose properties are accessed within a function.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:** `KotlinForeignDataProvidersVisitor.java` file is created and implements the FDP logic.

-   **Task 1.6: Implement `KotlinNumberOfAccessedVariablesVisitor` (NOAV)**
    -   **Rationale/Goal:** Count the number of unique variables (local, parameter, or property) accessed within a function.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:** `KotlinNumberOfAccessedVariablesVisitor.java` file is created and implements the NOAV logic.

#### Phase 2: Verification and Testing
-   **Objective(s):** Ensure the functional correctness of all newly implemented Kotlin method-level visitors.
-   **Priority:** High

-   **Task 2.1: Create Kotlin Test Data for Method Metrics**
    -   **Rationale/Goal:** To provide a set of verifiable Kotlin code samples that exercise various language features relevant to the new metrics.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:** New `.kt` files are added to `testData/kotlin/` containing functions with nested loops, complex conditional expressions, property access patterns, etc.

-   **Task 2.2: Create `KotlinMethodVisitorsTest.java` Integration Test**
    -   **Rationale/Goal:** To write automated tests that assert the correctness of the new Kotlin metric visitors against the manually verified test data.
    -   **Estimated Effort (Optional):** L
    -   **Deliverable/Criteria for Completion:** A new test class `KotlinMethodVisitorsTest.java` is created in the `integration-test` source set. It contains specific test methods for CND, LND, NOPM, LAA, FDP, and NOAV, asserting their calculated values are correct.

## 4. Key Considerations & Risk Mitigation
### 4.1. Technical Risks & Challenges
-   **Semantic Mapping of Metrics:** Some Java-centric metrics (e.g., LAA, FDP) may require careful interpretation to correctly handle Kotlin-specific features like extension functions, properties with custom getters/setters, and delegated properties.
    -   **Mitigation:** Each implementation task must begin with a brief analysis of how the metric's formal definition applies to Kotlin. Decisions on handling these features should be documented in code comments and validated with specific test cases.
-   **Testing Complexity:** The correctness of the visitors depends on having a comprehensive set of test cases.
    -   **Mitigation:** The test data creation (Task 2.1) is a critical prerequisite. Each new visitor implementation should be developed in tandem with its corresponding test cases to ensure coverage.

### 4.2. Dependencies
-   The completion of this plan is a prerequisite for **Phase 3: Integration into Calculation Workflow** as defined in `docs/kotlin/main_plan.md`. The project cannot proceed with integrating Kotlin analysis until all visitors are implemented.

## 5. Success Metrics / Validation Criteria
-   All tasks listed in `docs/kotlin/phase2.md` are marked as complete.
-   The `src/main/java/.../visitor/kotlin/method` directory contains implementations for all planned method-level visitors.
-   The new `KotlinMethodVisitorsTest.java` integration test suite passes successfully.
-   The project is ready to begin Phase 3 of the Kotlin support plan.

## 6. Assumptions Made
-   The existing, implemented Kotlin visitors (`KotlinMcCabeCyclomaticComplexityVisitor`, `KotlinLinesOfCodeVisitor`, and all class-level visitors) are functionally correct and serve as a valid pattern for the new implementations.
-   The definitions of the remaining metrics are conceptually applicable to Kotlin code, even if the implementation requires adaptation to Kotlin-specific language features.

## 7. Open Questions / Areas for Further Investigation
-   **LAA/FDP and Extension Functions:** How should property access within an extension function be treated? Does it count as "local" to the extended class or "foreign"? A clear rule must be defined. (Suggestion: Treat it as foreign, as the code resides outside the extended class).
-   **NOAV and Destructuring Declarations:** How should destructuring declarations (e.g., `val (a, b) = pair`) be counted by the `KotlinNumberOfAccessedVariablesVisitor`? (Suggestion: Each variable `a` and `b` should count as a distinct accessed variable).