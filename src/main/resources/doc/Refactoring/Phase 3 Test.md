Phase 3 Test

# Testing Plan: Phase 3 Refactoring Validation

## 1. Executive Summary & Goals
This document provides the testing strategy for Phase 3, the final cleanup stage of the MetricsTree plugin refactoring. The primary goal of this phase is to eliminate all remaining dependencies on the deprecated `MetricsUtils` and `MetricTaskCache` classes and to ensure the new service-oriented architecture is applied consistently, especially within the UI layer.

The testing approach for this phase is primarily one of **verification and hardening**. We will adapt existing tests to remove dependencies on legacy components and add new, focused tests to validate the purity of the UI layer.

- **Goal 1:** Verify that the complete removal of `MetricsUtils` and `MetricTaskCache` does not cause regressions.
- **Goal 2:** Ensure that UI components are truly decoupled and react correctly to `MessageBus` events.
- **Goal 3:** Confirm that all parts of the application now correctly use the new service layer for their dependencies.

## 2. Overall Testing Process

### 2.1. Pre-Refactoring Baseline
Before starting Phase 3 refactoring:
1.  **Execute Full Test Suite:** Run all existing tests from the successful completion of Phase 2. This ensures we start from a known-good state.
2.  **Static Analysis:** Use the IDE's "Find Usages" on `MetricsUtils` and `MetricTaskCache` to identify all remaining call sites that need to be refactored. This list will guide the refactoring effort.

### 2.2. Post-Refactoring Validation
After the Phase 3 refactoring is complete:
1.  **Compilation Check:** The most critical validation is that the project compiles successfully after deleting `MetricsUtils.java` and `MetricTaskCache.java`. This proves all static dependencies have been removed.
2.  **Adapt Tests:** Modify any remaining tests that still reference the legacy classes. This will primarily involve tests that were not the focus of Phases 1 and 2.
3.  **Develop New Tests:** Create new tests for UI components to verify their event-driven nature.
4.  **Execute Full Test Suite:** Run the complete, adapted test suite. A 100% pass rate is required.
5.  **Manual Regression Testing:** Conduct a thorough manual test of all major user workflows, as this phase touches many secondary UI components and utility functions.

## 3. Detailed Test Specifications per Task

### Task 3.1 & 3.2: Eliminate All Usages of `MetricsUtils` and `MetricTaskCache`

- **Objective:** Verify that the functionality previously provided by these classes is now correctly sourced from the new services or utility classes without any loss of function.

- **Existing Tests to Leverage:**
    - The entire existing test suite serves as a regression test. Any test that fails after refactoring a component it covers indicates a problem. For example, if `CouplingBetweenObjectsVisitor` is refactored to use `CacheService` and `CouplingBetweenObjectsVisitorTest` (if it existed) starts failing, the refactoring is incorrect.

- **New Tests to Develop:**
    - **Test Type:** Unit Test
    - **Class Name:** `EditorUtilsTest.java` (or similar name for the new utility class)
    - **Location:** `src/test/java/org/b333vv/metric/util/`
    - **Description:** A unit test for any genuinely static, stateless helper methods extracted from `MetricsUtils`.
    - **Test Cases:**
        - `testSortByValueReversed`: This test already exists in `MetricsUtilsTest` and should be moved here.
        - Any other static helpers (e.g., for string manipulation) should have corresponding unit tests.

- **Refactoring Test Strategy:**
    - **`MetricsUtilsTest.java`:** This test class will be renamed to `EditorUtilsTest.java` (or the name of the new utility class). Tests for methods that were moved to services will be deleted from this class, as their functionality is now tested via the service tests. Only tests for the remaining static methods will be kept.
    - **All other tests:** Search the entire test codebase for imports of `MetricsUtils` and `MetricTaskCache`. Each found usage must be refactored. For example, a test that previously used `MetricsUtils.getDumbService(project)` must be adapted to get the service directly from the test project fixture.

### Task 3.3: Review and Refactor UI Panels

- **Objective:** Verify that UI panels are "dumb" components that render data received from the `MessageBus` and do not contain business logic.

- **Existing Tests to Leverage:** None. The UI layer is largely untested at a granular level.

- **New Tests to Develop (Post-Refactoring):**
    - **Test Type:** Integration Test
    - **Class Name:** `ProjectMetricsPanelTest.java`
    - **Location:** `src/integration-test/java/org/b333vv/metric/ui/tool/`
    - **Description:** An integration test using `BasePlatformTestCase` to verify the panel's event-driven behavior.
    - **Test Cases:**
        - `testPanelIsClearedOnClearEvent`:
            1.  Initialize the `ProjectMetricsPanel`.
            2.  Manually populate it with some data (e.g., by firing a `projectMetricsTreeIsReady` event with a dummy model).
            3.  Assert that the panel is not empty.
            4.  Fire a `clearProjectMetricsTree` event on the project's `MessageBus`.
            5.  Assert that the panel's components (e.g., the `MetricsTree` model) are now empty or reset to their initial state.
        - `testPanelUpdatesOnMetricsReadyEvent`:
            1.  Initialize the `ProjectMetricsPanel`.
            2.  Create a mock `DefaultTreeModel`.
            3.  Fire a `projectMetricsTreeIsReady` event on the `MessageBus`, passing the mock model.
            4.  Assert that the `MetricsTree` inside the panel now has the mock model set.
    - **Test Type:** Integration Test
    - **Class Name:** `ClassMetricsPanelTest.java`
    - **Location:** `src/integration-test/java/org/b333vv/metric/ui/tool/`
    - **Description:** Similar to the above, but for the `ClassMetricsPanel`.
    - **Test Cases:**
        - `testPanelUpdatesOnClassMetricsEvolutionCalculated`:
            1.  Initialize the `ClassMetricsPanel`.
            2.  Create a mock `DefaultTreeModel` for evolution data.
            3.  Fire a `classMetricsValuesEvolutionCalculated` event.
            4.  Assert that the panel's `MetricsTree` is updated with the evolution model.
        - `testPanelRefreshesOnRefreshEvent`:
            1.  This is more complex. It would require spying on the `ClassMetricsPanel`'s `update` method.
            2.  Initialize the panel.
            3.  Fire a `refreshClassMetricsTree` event.
            4.  Verify that the `update` method on the panel's spy was called.

### Task 3.4: Final Code Review and Full Test Suite Execution

- **Objective:** Final verification of the entire refactoring effort.

- **Testing Activities:**
    - **Automated Regression:** Execute the full test suite (`test`, `integrationTest`, `e2eTest`) on a clean build. This is the most critical step to catch any regressions introduced during the cleanup phase.
    - **Manual Sanity Checks:**
        1.  **Settings Workflow:** Open the settings dialog (`MetricsConfigurable`). Change a value on each tab (e.g., a metric range, a fitness function, an "other" setting). Click "Apply". Close and reopen the settings dialog. Verify that all changes were persisted correctly.
        2.  **Class Metrics Workflow:** Open a Java file. Verify the class metrics tree appears. Use the "Refresh" button and confirm the tree reloads. Use the "Filter" button and confirm the filter popup appears.
        3.  **Project Metrics Workflow:** Click "Calculate Project Metrics". Verify the project tree appears. Click on different nodes (project, package, class, method, metric) and confirm the details panels on the right update correctly.
        4.  **Fitness Function Workflow:** Navigate to the "Metric Fitness Functions" tab. Click "Build Class Level Fitness Function". Verify the list of profiles appears. Click on a profile and verify the list of classes updates. Click on a class and verify the metrics table updates. Repeat for the package-level functions.
        5.  **Log Workflow:** Perform a calculation that is known to log information. Navigate to the "Log" tab and verify the messages appear. Use the "Clear All" button and confirm the log is cleared.

## 4. Summary of Test Artifacts

| Phase Stage       | Test Type         | Test Class Name (Example)      | Purpose                                                                                             |
| ----------------- | ----------------- | ------------------------------ | --------------------------------------------------------------------------------------------------- |
| **Pre-Refactor**  | N/A               | N/A                            | Focus is on running the existing suite from Phase 2 to establish a green baseline.                  |
| **Post-Refactor** | Unit Test (Moved) | `EditorUtilsTest.java`         | Verify stateless helper methods extracted from `MetricsUtils`.                                      |
| **Post-Refactor** | Integration Test  | `ProjectMetricsPanelTest.java` | Verify the panel correctly responds to `MessageBus` events for data updates and clearing.           |
| **Post-Refactor** | Integration Test  | `ClassMetricsPanelTest.java`   | Verify the panel correctly responds to `MessageBus` events for data updates and clearing.           |
| **Post-Refactor** | All (Adapted)     | Entire Test Suite              | Serve as a comprehensive regression suite to ensure no functionality was broken during the cleanup. |