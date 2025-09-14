# Design Plan: Achieve Kotlin Visitor Feature Parity

## 1. Executive Summary & Goals
This plan addresses the feature gap between Java and Kotlin metric analysis in the MetricsTree plugin. A review of the codebase reveals that while a solid foundation for Kotlin analysis exists, a significant number of metric visitors have been implemented for Java but not for Kotlin.

-   **Primary Objective:** Achieve full feature parity by implementing the missing Kotlin metric visitors.
-   **Key Goals:**
    1.  Create Kotlin-equivalent visitors for all missing class-level metrics.
    2.  Create Kotlin-equivalent visitors for all missing method-level metrics.
    3.  Ensure the correctness of new visitors through dedicated verification tests.

## 2. Current Situation Analysis
The project has a parallel visitor structure for Java and Kotlin. However, the implementation for Kotlin is incomplete.

-   **Java Visitors (Implemented):**
    -   **Class-Level:** ATFD, CBO, DAC, DIT, Halstead (CHVL, etc.), LCOM, MPC, NCSS, NOA, NOAC, NOAM, NOC, NOM, NOO, NOOM, NOPA, RFC, SIZE2, TCC, WMC, WOC.
    -   **Method-Level:** CCM, CND, CDISP, CINT, FDP, Halstead (HVL, etc.), LAA, LOC, LND, MND, CC, NOAV, NOL, NOPM.

-   **Kotlin Visitors (Implemented):**
    -   **Class-Level:** CBO, DIT, LCOM, NCSS, NOA, NOC, NOM, RFC, TCC, WMC.
    -   **Method-Level:** CND, FDP, LAA, LOC, LND, CC, NOAV, NOPM.

-   **Identified Gap:** The following metrics have Java visitors but lack corresponding Kotlin implementations:
    -   **Missing Class-Level Visitors:**
        -   `ATFD` (Access To Foreign Data)
        -   `DAC` (Data Abstraction Coupling)
        -   `Halstead` (CHVL, CHD, CHL, CHEF, CHVC, CHER)
        -   `MPC` (Message Passing Coupling)
        -   `NOAC` (Number Of Accessor Methods)
        -   `NOAM` (Number Of Added Methods)
        -   `NOO` (Number Of Operations)
        -   `NOOM` (Number Of Overridden Methods)
        -   `NOPA` (Number Of Public Attributes)
        -   `SIZE2` (Number Of Attributes And Methods)
        -   `WOC` (Weight Of A Class)
    -   **Missing Method-Level Visitors:**
        -   `CCM` (Cognitive Complexity)
        -   `CDISP` (Coupling Dispersion)
        -   `CINT` (Coupling Intensity)
        -   `Halstead` (HVL, HD, HL, HEF, HVC, HER)
        -   `MND` (Maximum Nesting Depth)
        -   `NOL` (Number Of Loops)

## 3. Proposed Solution / Refactoring Strategy
### 3.1. High-Level Design / Architectural Overview
The strategy is to continue the established parallel design pattern. For each missing Java visitor, a corresponding Kotlin visitor will be created in the `org.b333vv.metric.model.visitor.kotlin` package. These new visitors will extend `KotlinClassVisitor` or `KotlinMethodVisitor` and will traverse the Kotlin PSI (`KtTreeVisitorVoid`) to implement the metric's logic, adapting it for Kotlin's syntax and semantics.

### 3.2. Key Components / Modules
The new components will be the Kotlin visitor classes themselves, placed in the following packages:
-   `org.b333vv.metric.model.visitor.kotlin.type` (for class-level metrics)
-   `org.b333vv.metric.model.visitor.kotlin.method` (for method-level metrics)

### 3.3. Detailed Action Plan / Phases
#### Phase 1: Implement Missing Class-Level Visitors
-   **Objective(s):** Implement all missing class-level metric visitors for Kotlin.
-   **Priority:** High
-   **Task 1.1: Implement `KotlinAccessToForeignDataVisitor` (ATFD)**
    -   **Rationale/Goal:** Measure coupling based on access to external class data.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:** A new `KotlinAccessToForeignDataVisitor.java` file is created that correctly calculates the ATFD metric for Kotlin classes.
-   **Task 1.2: Implement `KotlinDataAbstractionCouplingVisitor` (DAC)**
    -   **Rationale/Goal:** Measure coupling based on attribute types.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:** A new `KotlinDataAbstractionCouplingVisitor.java` file is created.
-   **Task 1.3: Implement `KotlinHalsteadClassVisitor` (Halstead Suite)**
    -   **Rationale/Goal:** Implement Halstead complexity metrics for Kotlin classes.
    -   **Estimated Effort (Optional):** L
    -   **Deliverable/Criteria for Completion:** A new `KotlinHalsteadClassVisitor.java` file is created that calculates CHVL, CHD, etc.
-   **Task 1.4: Implement `KotlinMessagePassingCouplingVisitor` (MPC)**
    -   **Rationale/Goal:** Measure coupling based on method calls.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:** A new `KotlinMessagePassingCouplingVisitor.java` file is created.
-   **Task 1.5: Implement Remaining Class-Level Visitors**
    -   **Rationale/Goal:** Achieve full feature parity for all remaining class-level metrics.
    -   **Estimated Effort (Optional):** L
    -   **Deliverable/Criteria for Completion:** The following visitor classes are created and implemented: `KotlinNumberOfAccessorMethodsVisitor` (NOAC), `KotlinNumberOfAddedMethodsVisitor` (NOAM), `KotlinNumberOfOperationsVisitor` (NOO), `KotlinNumberOfOverriddenMethodsVisitor` (NOOM), `KotlinNumberOfPublicAttributesVisitor` (NOPA), `KotlinNumberOfAttributesAndMethodsVisitor` (SIZE2), and `KotlinWeightOfAClassVisitor` (WOC).

#### Phase 2: Implement Missing Method-Level Visitors
-   **Objective(s):** Implement all missing method-level metric visitors for Kotlin.
-   **Priority:** High
-   **Task 2.1: Implement `KotlinCognitiveComplexityVisitor` (CCM)**
    -   **Rationale/Goal:** Provide cognitive complexity analysis for Kotlin functions.
    -   **Estimated Effort (Optional):** L
    -   **Deliverable/Criteria for Completion:** A new `KotlinCognitiveComplexityVisitor.java` file is created that correctly calculates CCM.
-   **Task 2.2: Implement `KotlinHalsteadMethodVisitor` (Halstead Suite)**
    -   **Rationale/Goal:** Implement Halstead complexity metrics for Kotlin functions.
    -   **Estimated Effort (Optional):** L
    -   **Deliverable/Criteria for Completion:** A new `KotlinHalsteadMethodVisitor.java` file is created that calculates HVL, HD, etc.
-   **Task 2.3: Implement Remaining Method-Level Visitors**
    -   **Rationale/Goal:** Achieve full feature parity for all remaining method-level metrics.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:** The following visitor classes are created and implemented: `KotlinCouplingDispersionVisitor` (CDISP), `KotlinCouplingIntensityVisitor` (CINT), `KotlinMaximumNestingDepthVisitor` (MND), and `KotlinNumberOfLoopsVisitor` (NOL).

#### Phase 3: Verification and Testing
-   **Objective(s):** Ensure the functional correctness of all newly implemented Kotlin visitors.
-   **Priority:** High
-   **Task 3.1: Create/Update Kotlin Test Cases**
    -   **Rationale/Goal:** Provide verifiable Kotlin code samples that exercise language features relevant to the new metrics.
    -   **Estimated Effort (Optional):** L
    -   **Deliverable/Criteria for Completion:** New `.kt` files are added to `metric-verification-data-kotlin/` covering features like properties, accessors, companion objects, extension functions, etc.
-   **Task 3.2: Update Integration Tests**
    -   **Rationale/Goal:** Write automated tests to assert the correctness of the new Kotlin metric visitors.
    -   **Estimated Effort (Optional):** L
    -   **Deliverable/Criteria for Completion:** The `KotlinClassVisitorsTest` and `KotlinMethodVisitorsTest` classes are updated with new test methods for each implemented visitor, asserting correct values against the test data. All tests pass.

## 4. Key Considerations & Risk Mitigation
### 4.1. Technical Risks & Challenges
-   **Semantic Mapping of Metrics:** Many metrics were designed with Java's object model in mind. Translating them to Kotlin requires careful consideration of features like properties vs. fields, companion objects vs. static members, and extension functions.
    -   **Mitigation:** Each visitor implementation must start with a clear, documented interpretation of how the metric applies to Kotlin. These decisions should be reflected in code comments and validated with specific test cases.
-   **Kotlin PSI Complexity:** The Kotlin PSI can be more complex than Java's. Correctly identifying all relevant nodes for a given metric will require careful implementation and testing.
    -   **Mitigation:** Leverage IntelliJ's "PSI Viewer" tool during development. Develop each visitor in tandem with its corresponding test cases to ensure correctness from the start.

### 4.2. Dependencies
-   The implementation of class-level and method-level visitors (Phases 1 and 2) can proceed in parallel.
-   The verification phase (Phase 3) is dependent on the completion of the visitor implementations.

## 5. Success Metrics / Validation Criteria
-   All missing visitor classes listed in section 2 are implemented for Kotlin.
-   The integration test suites are updated to cover all new Kotlin visitors.
-   All existing and new tests for both Java and Kotlin pass successfully, indicating no regressions and correct new functionality.
-   The plugin can calculate and display all metrics for both Java and Kotlin files without errors.

## 6. Assumptions Made
-   The existing set of metrics is conceptually applicable to Kotlin code, even if the implementation requires adaptation to Kotlin-specific language features.
-   The IntelliJ Platform provides stable and reliable APIs for analyzing the Kotlin PSI.

## 7. Open Questions / Areas for Further Investigation
-   **NOAC (Number of Accessor Methods):** How should this be defined for Kotlin properties, which have implicit getters/setters? Should it count properties with custom `get()` or `set()` implementations?
-   **NOAM/NOOM (Added/Overridden Methods):** How do these metrics apply to Kotlin properties that override parent properties?
-   **Halstead Metrics:** How should Kotlin-specific operators and language constructs (e.g., `when`, `..`, `?:`, string templates) be classified as Halstead operators or operands? A consistent classification scheme must be defined and documented.