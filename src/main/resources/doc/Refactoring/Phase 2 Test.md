Phase 2 Test

# Testing Plan: Phase 2 Refactoring Validation

## 1. Executive Summary & Goals
This document outlines the testing strategy to ensure the correctness and stability of the Phase 2 refactoring. The goal of Phase 2 is to decouple the core metric calculation logic from the IntelliJ Platform's execution framework by introducing a `CalculationService` and extracting business logic from `*Task` classes into pure `*Calculator` components.

This testing plan follows the same two-stage process as Phase 1:
1.  **Pre-Refactoring:** Establish a baseline by creating new tests against the current architecture to capture the behavior of the `*Task` classes.
2.  **Post-Refactoring:** Adapt the new and existing tests to validate the `CalculationService` and the new `*Calculator` components, ensuring no functional regressions.

The primary objective is to verify that the end-to-end calculation process produces the same results, even though the internal orchestration has been completely redesigned.

## 2. Overall Testing Process

### 2.1. Pre-Refactoring Baseline
Before any Phase 2 refactoring begins:
1.  **Execute Full Test Suite:** Run all existing tests from Phase 1 to ensure the starting point is stable and green.
2.  **Develop New Tests (as specified below):** Create the new integration tests for the `*Task` classes. These tests will validate the current, logic-filled `run()` methods, establishing a behavioral contract.
3.  **Commit Baseline Tests:** Commit these new tests to version control before committing any refactoring code.

### 2.2. Post-Refactoring Validation
After the Phase 2 refactoring is complete:
1.  **Adapt Tests:**
    -   The new integration tests for `*Task` classes will be repurposed to become unit tests for the new `*Calculator` classes.
    -   Existing E2E tests for `AnAction` classes will be simplified to verify interaction with the `CalculationService` mock.
2.  **Develop New Tests:** Create new integration tests for the `CalculationService` itself to verify its orchestration logic.
3.  **Execute Full Test Suite:** Run the complete, adapted test suite. A 100% pass rate is required.
4.  **Manual Verification:** Perform manual checks of the primary UI actions (e.g., "Calculate Project Metrics", "Build Pie Chart") to confirm they still trigger background processing and update the UI correctly.

## 3. Detailed Test Specifications per Task

### Task 2.1: Create `CalculationService`

- **Objective:** Verify that the `CalculationService` correctly orchestrates caching, task queuing, and result publication.

- **Existing Tests to Leverage:** None directly. This is a new component.

- **New Tests to Develop (Post-Refactoring):**
    - **Test Type:** Integration Test
    - **Class Name:** `CalculationServiceTest.java`
    - **Location:** `src/integration-test/java/org/b333vv/metric/service/`
    - **Description:** An integration test using `BasePlatformTestCase` to test the service's interaction with other mocked services.
    - **Test Cases:**
        - `testCalculateProjectTree_usesCache`:
            1.  Mock `CacheService` to return a pre-defined `DefaultTreeModel`.
            2.  Mock `TaskQueueService` and `MessageBus`.
            3.  Call `calculationService.calculateProjectTree()`.
            4.  Verify that `taskQueueService.queue()` was **not** called.
            5.  Verify that `messageBus.syncPublisher(...).projectMetricsTreeIsReady()` **was** called with the cached model.
        - `testCalculateProjectTree_queuesTaskWhenCacheIsEmpty`:
            1.  Mock `CacheService` to return `null`.
            2.  Mock `TaskQueueService`.
            3.  Call `calculationService.calculateProjectTree()`.
            4.  Verify that `taskQueueService.queue()` **was** called with an instance of `ProjectTreeTask`.
        - `testCalculateProjectTree_onSuccess_updatesCache`:
            1.  This is a more advanced test. It requires a way to capture the `onSuccess` runnable from the task passed to the `TaskQueueService`.
            2.  Mock `TaskQueueService` using an `ArgumentCaptor` to capture the `ProjectTreeTask`.
            3.  Call `calculationService.calculateProjectTree()`.
            4.  Extract the captured task, manually create a result model, and invoke the task's `onSuccess()` logic (which may need to be made accessible for testing).
            5.  Verify that `cacheService.putUserData()` was called with the correct key and the result model.

### Task 2.2 & 2.5: Refactor `*Task` classes and Extract `*Calculator`s

- **Objective:** Ensure the business logic extracted from `*Task` classes into new `*Calculator` components remains correct.

- **Existing Tests to Leverage:**
    - `ClassModelBuilderTest.java`, `JavaFileModelBuilderIntegrationTest.java`, `ProjectModelBuilderIntegrationTest.java`: These tests already validate the core model-building logic that many calculators will depend on.

- **New Tests to Develop (Pre-Refactoring):**
    - **Test Type:** Integration Test
    - **Class Name:** `ProjectTreeTaskTest.java` (and similar for other tasks like `PieChartTaskTest`, etc.)
    - **Location:** `src/integration-test/java/org/b333vv/metric/task/`
    - **Description:** An integration test that runs a specific task and validates its output. This captures the current behavior.
    - **Test Cases (Example for `ProjectTreeTaskTest`):**
        - `testRun_buildsCorrectTreeModel`:
            1.  Set up a test project with `myFixture`.
            2.  Instantiate and run the `ProjectTreeTask` directly (bypassing the queue for the test).
            3.  Retrieve the resulting `DefaultTreeModel` from the `MetricTaskCache`.
            4.  Assert that the root node of the tree is a `ProjectNode` with the correct name.
            5.  Assert that the number of child nodes corresponds to the expected packages/metrics.

- **Refactoring Test Strategy:**
    - **`ProjectTreeTaskTest.java` -> `ProjectTreeModelCalculatorTest.java`:**
        - **Before:** The test runs the `ProjectTreeTask` and checks the result in the cache.
        - **After:** The test will be renamed and moved to `src/test/java/org/b333vv/metric/builder/`. It will become a **unit test**. It will no longer instantiate a `Task`. Instead, it will instantiate the new `ProjectTreeModelCalculator`, call its `calculate()` method, and directly assert on the returned `DefaultTreeModel`. This removes the dependency on the IntelliJ test framework for this core logic.

### Task 2.4 & 2.5: Refactor `AnAction` Classes

- **Objective:** Verify that UI actions correctly delegate to the `CalculationService` and that their `update` logic functions correctly with the new `TaskQueueService`.

- **Existing Tests to Leverage:**
    - `CalculateProjectMetricsActionTest.java`: This E2E test is the primary validation vehicle.

- **New Tests to Develop:** None required. The focus is on adapting existing tests.

- **Refactoring Test Strategy:**
    - **`CalculateProjectMetricsActionTest.java`:**
        - **Before:** The test spies on `MetricTaskCache` to verify that `runTask` is called.
        - **After:** The test will be simplified. It will now mock the `CalculationService`.
            1.  Use `ServiceContainerUtil.replaceService` to inject a mock `CalculationService` into the test project.
            2.  In `testActionPerformed_triggersCalculationService`, call `action.actionPerformed(event)`.
            3.  Verify that `mockCalculationService.calculateProjectTree()` was called exactly once. This is a much cleaner and more direct test of the action's responsibility.
            4.  The `update` method test will be adapted to use a mock `TaskQueueService` to control the return value of `isQueueEmpty()` and verify the action's enabled/disabled state.

## 4. Summary of Test Artifacts

| Phase Stage       | Test Type         | Test Class Name (Example)                 | Purpose                                                                                             |
|-------------------|-------------------|-------------------------------------------|-----------------------------------------------------------------------------------------------------|
| **Pre-Refactor**  | Integration Test  | `ProjectTreeTaskTest.java`                | Capture the current behavior of a logic-filled `Task` class to create a contract for the refactoring. |
| **Post-Refactor** | Unit Test         | `ProjectTreeModelCalculatorTest.java`     | (Adapted from above) Verify the pure business logic of the new calculator POJO, without IDE dependencies. |
| **Post-Refactor** | Integration Test  | `CalculationServiceTest.java`             | Verify the new service correctly orchestrates caching, task queuing, and result publication.          |
| **Post-Refactor** | E2E Test (Adapted) | `CalculateProjectMetricsActionTest.java`  | Verify the UI action correctly calls the `CalculationService` and its `update` method works.        |