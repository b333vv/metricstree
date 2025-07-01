# Refactoring/Design Plan: Phase 2, Task 1.4 - Visualization Logic Isolation

## 1. Executive Summary & Goals
This document provides a detailed specification for the refactoring of all visualization-related `*Task` classes, as outlined in the broader Phase 2 plan. The primary objective is to systematically apply the established service-oriented pattern to every chart and treemap generation workflow. This involves extracting the core data preparation and chart-building logic from the `*Task` classes into dedicated, testable `*Calculator` components.

- **Goal 1:** Decouple all chart and treemap generation logic from the IntelliJ Platform's background execution framework.
- **Goal 2:** Expand the `CalculationService` to provide a complete, high-level API for all visualization-related actions.
- **Goal 3:** Simplify all corresponding UI `AnAction` classes to delegate their requests to the `CalculationService`.
- **Goal 4:** Establish a clear testing strategy for each new `*Calculator` component to ensure correctness and prevent regressions.

## 2. Current Situation Analysis
While the primary `ProjectTree` workflow has been refactored, a significant number of `*Task` classes related to generating charts and treemaps still contain business logic. These tasks are directly instantiated by UI actions, maintaining a tight coupling between the UI and implementation details. This makes the core logic difficult to unit-test and the overall architecture inconsistent.

## 3. Proposed Solution / Refactoring Strategy
The strategy is to methodically refactor each visualization task, following the pattern established in the initial part of Phase 2. For each task, we will:
1.  **Extract** the core logic into a new `*Calculator` or `*Builder` class in the `org.b333vv.metric.builder` package.
2.  **Refactor** the original `*Task` class to be a thin wrapper that executes the new calculator.
3.  **Extend** the `CalculationService` with a new method to orchestrate this workflow.
4.  **Refactor** the corresponding `AnAction` to use the new service method.
5.  **Specify** a new unit test for the extracted calculator logic.

### 3.1. Detailed Action Plan
This plan details the refactoring for each remaining visualization task.

---

#### 3.1.1. Refactor `PieChartTask`
- **Target Task:** `org.b333vv.metric.task.PieChartTask`
- **New Calculator:** `org.b333vv.metric.builder.PieChartDataCalculator`
- **Logic to Extract:** The `calculate()` method will take a `JavaProject` and a `Project` as input. It will use `ClassesByMetricsValuesDistributor` and `MetricPieChartBuilder` to produce a `List<MetricPieChartBuilder.PieChartStructure>`.
- **New `CalculationService` Method:**
    ```java
    void calculatePieChart();
    ```
- **Action to Refactor:** `org.b333vv.metric.actions.BuildMetricsPieChartAction`
- **Testing Strategy:** Create `PieChartDataCalculatorTest.java` to verify that, given a `JavaProject`, the calculator produces the correct number of pie chart structures with the expected data.

---

#### 3.1.2. Refactor `CategoryChartTask`
- **Target Task:** `org.b333vv.metric.task.CategoryChartTask`
- **New Calculator:** `org.b333vv.metric.builder.CategoryChartDataCalculator`
- **Logic to Extract:** The `calculate()` method will take a `JavaProject` and a `Project` as input. It will use `ClassesByMetricsValuesCounter` and `MetricCategoryChartBuilder` to produce a `CategoryChart`.
- **New `CalculationService` Method:**
    ```java
    void calculateCategoryChart();
    ```
- **Action to Refactor:** `org.b333vv.metric.actions.BuildMetricsCategoryChartAction`
- **Testing Strategy:** Create `CategoryChartDataCalculatorTest.java` to verify that the generated `CategoryChart` contains the correct series and data points for a given input model.

---

#### 3.1.3. Refactor `XyChartTask`
- **Target Task:** `org.b333vv.metric.task.XyChartTask`
- **New Calculator:** `org.b333vv.metric.builder.XyChartDataCalculator`
- **Logic to Extract:** The `calculate()` method will take a `JavaProject` and a `Project` as input. It will use `ProjectMetricXYChartDataBuilder` and `ProjectMetricXYChartBuilder` to produce an `XYChart`.
- **New `CalculationService` Method:**
    ```java
    void calculateXyChart();
    ```
- **Action to Refactor:** `org.b333vv.metric.actions.BuildProjectMetricXYChartAction`
- **Testing Strategy:** Create `XyChartDataCalculatorTest.java` to validate that the `XYChart` is correctly configured with the main sequence and data points from the input project model.

---

#### 3.1.4. Refactor `ProfilesBoxChartTask`
- **Target Task:** `org.b333vv.metric.task.ProfilesBoxChartTask`
- **New Calculator:** `org.b333vv.metric.builder.ProfileBoxChartDataCalculator`
- **Logic to Extract:** The `calculate()` method will take a `Map<FitnessFunction, Set<JavaClass>>` as input. It will use `ProfileBoxChartBuilder` to produce a `List<ProfileBoxChartBuilder.BoxChartStructure>`.
- **New `CalculationService` Method:**
    ```java
    void calculateProfileBoxCharts();
    ```
- **Action to Refactor:** `org.b333vv.metric.actions.BuildProfileBoxChartAction`
- **Testing Strategy:** Create `ProfileBoxChartDataCalculatorTest.java`. This test will provide a mock distribution of classes by fitness function and assert that the resulting list of box chart structures is correctly generated.

---

#### 3.1.5. Refactor `ProfilesCategoryChartTask`
- **Target Task:** `org.b333vv.metric.task.ProfilesCategoryChartTask`
- **New Calculator:** `org.b333vv.metric.builder.ProfileCategoryChartDataCalculator`
- **Logic to Extract:** The `calculate()` method will take a `Map<FitnessFunction, Set<JavaClass>>` as input. It will use `ProfileCategoryChartBuilder` to produce a `CategoryChart`.
- **New `CalculationService` Method:**
    ```java
    void calculateProfileCategoryChart();
    ```
- **Action to Refactor:** `org.b333vv.metric.actions.BuildProfilesCategoryChartAction`
- **Testing Strategy:** Create `ProfileCategoryChartDataCalculatorTest.java` to verify that the generated `CategoryChart` correctly represents the number of classes per profile.

---

#### 3.1.6. Refactor `ProfilesHeatMapChartTask`
- **Target Task:** `org.b333vv.metric.task.ProfilesHeatMapChartTask`
- **New Calculator:** `org.b333vv.metric.builder.ProfileHeatMapDataCalculator`
- **Logic to Extract:** The `calculate()` method will take a `Map<FitnessFunction, Set<JavaClass>>` as input. It will use `ProfileHeatMapChartBuilder` to produce a `HeatMapChart`.
- **New `CalculationService` Method:**
    ```java
    void calculateProfileHeatMapChart();
    ```
- **Action to Refactor:** `org.b333vv.metric.actions.BuildProfileHeatMapChartAction`
- **Testing Strategy:** Create `ProfileHeatMapDataCalculatorTest.java` to validate that the heat map data correctly reflects the Jaccard index between class sets of different profiles.

---

#### 3.1.7. Refactor `ProfilesRadarChartTask`
- **Target Task:** `org.b333vv.metric.task.ProfilesRadarChartTask`
- **New Calculator:** `org.b333vv.metric.builder.ProfileRadarDataCalculator`
- **Logic to Extract:** The `calculate()` method will take a `Map<FitnessFunction, Set<JavaClass>>` and a `Project` as input. It will use `ProfileRadarChartBuilder` to produce a `List<ProfileRadarChartBuilder.RadarChartStructure>`.
- **New `CalculationService` Method:**
    ```java
    void calculateProfileRadarCharts();
    ```
- **Action to Refactor:** `org.b333vv.metric.actions.BuildProfileRadarChartAction`
- **Testing Strategy:** Create `ProfileRadarDataCalculatorTest.java` to verify that the radar chart data correctly calculates the proportion of invalid metrics for each profile.

---

#### 3.1.8. Refactor `MetricTreeMapTask`
- **Target Task:** `org.b333vv.metric.task.MetricTreeMapTask`
- **New Calculator:** `org.b333vv.metric.builder.MetricTreeMapModelCalculator`
- **Logic to Extract:** The `calculate()` method will take a `JavaProject` as input. It will use `TreeMapBuilder` to produce a fully configured `MetricTreeMap<JavaCode>`.
- **New `CalculationService` Method:**
    ```java
    void calculateMetricTreeMap();
    ```
- **Action to Refactor:** `org.b333vv.metric.actions.BuildMetricTreeMapAction`
- **Testing Strategy:** Create `MetricTreeMapModelCalculatorTest.java` to validate that the `MetricTreeMap` is correctly constructed and configured based on the input project model.

---

#### 3.1.9. Refactor `ProfileTreeMapTask`
- **Target Task:** `org.b333vv.metric.task.ProfileTreeMapTask`
- **New Calculator:** `org.b333vv.metric.builder.ProfileTreeMapModelCalculator`
- **Logic to Extract:** The `calculate()` method will take a `JavaProject` as input. It will use `TreeMapBuilder` to produce a fully configured `MetricTreeMap<JavaCode>` for profile visualization.
- **New `CalculationService` Method:**
    ```java
    void calculateProfileTreeMap();
    ```
- **Action to Refactor:** `org.b333vv.metric.actions.BuildProfileTreeMapAction`
- **Testing Strategy:** Create `ProfileTreeMapModelCalculatorTest.java` to validate the treemap construction for profile analysis.

---

#### 3.1.10. Refactor `ProjectMetricsHistoryXyChartTask`
- **Target Task:** `org.b333vv.metric.task.ProjectMetricsHistoryXyChartTask`
- **New Calculator:** `org.b333vv.metric.builder.ProjectHistoryChartDataCalculator`
- **Logic to Extract:** The `calculate()` method will take a `Project` as input. It will use `ProjectMetricsSet2Json` to parse stored snapshots and `ProjectMetricsHistoryXYChartBuilder` to produce an `XYChart`.
- **New `CalculationService` Method:**
    ```java
    void calculateProjectMetricsHistoryChart();
    ```
- **Action to Refactor:** `org.b333vv.metric.actions.BuildProjectMetricsHistoryXYChartAction`
- **Testing Strategy:** Create `ProjectHistoryChartDataCalculatorTest.java`. This test will require mocking the file system to provide dummy JSON snapshot files and will assert that the `XYChart` is generated with the correct data series.

## 4. Key Considerations & Risk Mitigation
- **Dependencies Between Calculators:** Some calculators will depend on the output of others (e.g., chart calculators depend on fitness function distributions). The `CalculationService` must manage this dependency chain, ensuring prerequisite data is available in the cache or calculated first.
- **UI Threading:** Chart objects are Swing components and must be handled on the EDT. The `onSuccess` callback of the background tasks must ensure that any interaction with the generated chart (e.g., adding it to a panel) is wrapped in `SwingUtilities.invokeLater`.

## 5. Success Metrics / Validation Criteria
- All `*...ChartTask` and `*...TreeMapTask` classes are refactored into simple wrappers or removed in favor of a generic task.
- The `CalculationService` contains a comprehensive set of methods for triggering all visualization-related workflows.
- All corresponding `AnAction` classes are simplified to call the `CalculationService`.
- New unit tests exist for each extracted `*Calculator` class, providing robust test coverage for the core logic.
- The plugin's visualization features remain fully functional from a user's perspective.

