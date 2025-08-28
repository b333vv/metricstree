# Refactoring Plan: Enable JavaParser Values for Missing Class Metrics

## 1. Executive Summary & Goals
This plan addresses a bug where several class-level metrics, despite being calculated via both PSI and JavaParser, do not display their JavaParser-calculated values in the UI. The system is designed to show both values in the format `METRIC: PSI_VALUE (JAVAPARSER_VALUE)`.

-   **Primary Objective:** Ensure all specified class metrics (CCC, CHVL, CHD, CHL, CHEF, CHVC, CHER, NOC, NOOM, NOAM, CMI) correctly display their JavaParser-calculated values.
-   **Key Goals:**
    1.  Identify and fix the root cause for each missing metric value.
    2.  Implement the necessary logic for calculating derivative metrics (CCC, CMI) using JavaParser-based values.
    3.  Add verification and testing to ensure the correctness of all affected metrics and prevent future regressions.

## 2. Current Situation Analysis
The application architecture, as described in `docs/javaparser/abstract-plan-javaparser-as-addition.md`, supports a dual-engine metric calculation strategy. The `PsiCalculationStrategy` first builds a complete model with PSI-based values. Then, if enabled, the `JavaParserCalculationStrategy` augments this model by populating the `javaParserValue` field for each metric. The UI components (`MetricNode`, `MetricsSummaryTable`) are already configured to display this second value if it's present.

### Key Pain Points / Root Cause Analysis
The issue stems from two distinct problems within the `JavaParserCalculationStrategy`:

1.  **Omission of Visitor Registration:** Several existing JavaParser visitors are not registered in the strategy's execution list. Specifically, the visitors for Halstead metrics (`JavaParserHalsteadClassVisitor`), Number of Overridden Methods (`JavaParserNumberOfOverriddenMethodsVisitor`), and Number of Added Methods (`JavaParserNumberOfAddedMethodsVisitor`) are implemented but never called.
2.  **Lack of Derivative Metric Calculation:** The metrics `CMI` (Class Maintainability Index) and `CCC` (Class Cognitive Complexity) are derivative, meaning they are calculated using the results of other base metrics (e.g., `WMC`, `LOC`, `CHVL`, `CCM`). The current `JavaParserCalculationStrategy` does not include the logic to perform these secondary calculations after the base JavaParser metrics have been computed.
3.  **Potential Bug in `NOC`:** The `NOC` (Number of Children) metric is reported as missing, but its visitor appears to be correctly handled in the strategy. This requires specific investigation to confirm if it's a bug in the visitor's logic or an issue with its dependency on the `allClassDeclarations` list.

## 3. Proposed Solution / Refactoring Strategy
### 3.1. High-Level Design / Architectural Overview
The solution involves targeted modifications to `JavaParserCalculationStrategy.java` to correctly integrate all required metric calculations. We will not change the overall architecture but will complete the implementation as originally intended. The strategy is to:
1.  Register the existing but unused visitors.
2.  Add a post-processing step within the strategy to calculate derivative metrics from JavaParser-based inputs.
3.  Verify and, if necessary, fix the implementation of the `NOC` visitor.

### 3.2. Key Components / Modules
-   **`org.b333vv.metric.builder.JavaParserCalculationStrategy`**: This is the central component to be modified. We will add visitor registrations and new methods for derivative calculations here.
-   **`org.b333vv.metric.model.visitor.type.JavaParser*`**: The visitors for Halstead, NOOM, and NOAM will be added to the execution flow. The `JavaParserNumberOfChildrenVisitor` will be reviewed and tested.
-   **`org.b333vv.metric.builder.ModelBuilder`**: This class will serve as a reference for porting the derivative metric calculation logic (for CMI and CCC) to the JavaParser strategy.

### 3.3. Detailed Action Plan / Phases
The work is divided into logical, sequential phases.

#### Phase 1: Register Missing Metric Visitors
-   **Objective(s):** Enable the calculation of Halstead metrics, NOOM, and NOAM using the JavaParser engine.
-   **Priority:** High

-   **Task 1.1: Update `JavaParserCalculationStrategy` with missing visitors**
    -   **Rationale/Goal:** To activate the existing but unused JavaParser visitors for several metrics.
    -   **Estimated Effort (Optional):** S
    -   **Deliverable/Criteria for Completion:**
        -   In `JavaParserCalculationStrategy.java`, locate the `classVisitors` list initialization.
        -   Add the following visitors to the list:
            -   `new JavaParserHalsteadClassVisitor()`
            -   `new JavaParserNumberOfOverriddenMethodsVisitor()`
            -   `new JavaParserNumberOfAddedMethodsVisitor()`
        -   After this change, the metrics `CHVL`, `CHD`, `CHL`, `CHEF`, `CHVC`, `CHER`, `NOOM`, and `NOAM` should correctly display their JavaParser values in the UI.

#### Phase 2: Implement Derivative Metric Calculation
-   **Objective(s):** Implement the logic to calculate CMI and CCC using JavaParser-based values.
-   **Priority:** High
-   **Dependencies:** Phase 1 must be complete, as the CMI calculation depends on the JavaParser-based `CHVL` value.

-   **Task 2.1: Add a post-processing method for derivative metrics**
    -   **Rationale/Goal:** To create a dedicated method within `JavaParserCalculationStrategy` to calculate metrics that depend on the results of other JavaParser visitors.
    -   **Estimated Effort (Optional):** S
    -   **Deliverable/Criteria for Completion:**
        -   Create a new private method: `private void calculateDerivativeClassMetrics(JavaClass javaClass)`.

-   **Task 2.2: Implement JavaParser-based CMI calculation**
    -   **Rationale/Goal:** To calculate the Class Maintainability Index using JavaParser-based inputs.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:**
        -   Inside `calculateDerivativeClassMetrics`, adapt the logic from `ModelBuilder.addMaintainabilityIndexForClass`.
        -   The logic must retrieve the `javaParserValue` from the following metrics:
            -   `CHVL` from the `javaClass` object.
            -   Sum of `CC` from all `JavaMethod` children of the `javaClass`.
            -   Sum of `LOC` from all `JavaMethod` children of the `javaClass`.
        -   Calculate the CMI value using these inputs.
        -   Retrieve the `CMI` metric object from `javaClass` and set its `javaParserValue`. Handle cases where base metric values are `UNDEFINED`.

-   **Task 2.3: Implement JavaParser-based CCC calculation**
    -   **Rationale/Goal:** To calculate the Class Cognitive Complexity using JavaParser-based inputs.
    -   **Estimated Effort (Optional):** S
    -   **Deliverable/Criteria for Completion:**
        -   Inside `calculateDerivativeClassMetrics`, adapt the logic from `ModelBuilder.addCognitiveComplexityForClass`.
        -   The logic must sum the `javaParserValue` of the `CCM` metric from all `JavaMethod` children of the `javaClass`.
        -   Retrieve the `CCC` metric object from `javaClass` and set its `javaParserValue`.

-   **Task 2.4: Integrate the derivative calculation into the main loop**
    -   **Rationale/Goal:** To ensure the derivative metrics are calculated for every class after its base metrics have been processed.
    -   **Estimated Effort (Optional):** S
    -   **Deliverable/Criteria for Completion:**
        -   In the `augment` method of `JavaParserCalculationStrategy`, inside the main loop that iterates over `javaProject.allClasses()`, add a call to `calculateDerivativeClassMetrics(javaClass)` at the end of the loop body.

#### Phase 3: Verification and Testing
-   **Objective(s):** Verify the `NOC` metric implementation and add comprehensive tests for all fixed metrics.
-   **Priority:** Medium

-   **Task 3.1: Investigate and verify `JavaParserNumberOfChildrenVisitor` (NOC)**
    -   **Rationale/Goal:** To confirm if the `NOC` metric calculation is functioning correctly, as its visitor is already part of the strategy.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:**
        -   Create a new integration test in `src/test/java/org/b333vv/metric/model/javaparser/visitor/type/` specifically for `NOC`.
        -   The test should parse a multi-file project with a clear inheritance hierarchy (e.g., using the existing `testData/inheritance` files).
        -   Assert that the `NOC` value calculated for a parent class via `JavaParserNumberOfChildrenVisitor` is correct.
        -   If a bug is found, fix it within the visitor.

-   **Task 3.2: Add integration tests for newly enabled metrics**
    -   **Rationale/Goal:** To ensure the correctness of Halstead, NOOM, and NOAM metrics and prevent regressions.
    -   **Estimated Effort (Optional):** L
    -   **Deliverable/Criteria for Completion:**
        -   In `JavaParserHalsteadClassVisitorTest.java`, add assertions for all class-level Halstead metrics (`CHVL`, `CHD`, etc.).
        -   In `JavaParserInheritanceVisitorsTest.java`, add assertions for `NOOM` and `NOAM`.
        -   These tests must validate the values calculated by the JavaParser visitors.

-   **Task 3.3: Add integration tests for derivative metrics**
    -   **Rationale/Goal:** To validate the new calculation logic for CMI and CCC.
    -   **Estimated Effort (Optional):** M
    -   **Deliverable/Criteria for Completion:**
        -   Create a new integration test that runs the full `JavaParserCalculationStrategy`.
        -   The test should use a sample class with known base metric values (or values that can be easily calculated).
        -   Assert that the final `javaParserValue` for `CMI` and `CCC` on the `JavaClass` object matches the expected calculated value.

## 4. Key Considerations & Risk Mitigation
### 4.1. Technical Risks & Challenges
-   **Performance:** Adding more calculations will marginally increase the analysis time when the JavaParser engine is selected. This is an expected and acceptable trade-off for feature completeness.
-   **Accuracy of Derivative Metrics:** The calculation for CMI and CCC depends on the accuracy of the underlying base metrics (CC, LOC, CHVL, CCM). The testing in Phase 3 is critical to mitigate this risk.
-   **Type Resolution in JavaParser:** The accuracy of some metrics (like NOC) heavily depends on JavaParser's ability to resolve types correctly across the project. The test for `NOC` must use a multi-file setup to validate this properly.

### 4.2. Dependencies
-   Phase 2 is dependent on the completion of Phase 1, as CMI requires the `CHVL` (Halstead Volume) metric.
-   Phase 3 (Testing) should be performed after the corresponding implementation in Phase 1 or 2 is complete.

## 5. Success Metrics / Validation Criteria
-   **Primary Success Metric:** When the JavaParser engine is enabled, all metrics listed in the user task (`CCC`, `CHVL`, `CHD`, `CHL`, `CHEF`, `CHVC`, `CHER`, `NOC`, `NOOM`, `NOAM`, `CMI`) display a value in parentheses next to the PSI value in both the metrics tree and the summary table.
-   **Secondary Success Metric:** All new and updated integration tests pass, confirming the correctness of the calculated values.

## 6. Assumptions Made
-   The existing, but unregistered, JavaParser visitors (`JavaParserHalsteadClassVisitor`, etc.) are functionally correct and only need to be added to the processing pipeline.
-   The formulas for derivative metrics `CMI` and `CCC` used in the PSI-based `ModelBuilder` are correct and can be directly adapted for use with JavaParser-based inputs.
-   The existing UI components will correctly display the `javaParserValue` once it is populated in the `Metric` object, requiring no UI changes.

## 7. Open Questions / Areas for Further Investigation
-   What is the expected behavior if a base metric required for a derivative calculation (e.g., `CHVL` for `CMI`) fails to compute and has a value of `UNDEFINED`?
    -   **Proposed Action:** The derivative metric should also result in `UNDEFINED`. The calculation logic must include checks for this.