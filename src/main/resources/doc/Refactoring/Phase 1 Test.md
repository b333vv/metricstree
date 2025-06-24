Phase 1 Test

# Testing Plan: Phase 1 Refactoring Validation

## 1. Executive Summary & Goals
This document outlines the testing strategy to ensure the correctness and stability of the Phase 1 refactoring. The goal of Phase 1 is to decompose the `MetricsUtils` and `MetricTaskCache` classes into a new layer of single-responsibility services.

This testing plan is designed to be executed in two stages:
1.  **Pre-Refactoring:** Establish a baseline by running existing tests and creating new ones against the current architecture to validate both the application's behavior and the tests themselves.
2.  **Post-Refactoring:** Adapt and re-run all tests against the new service-based architecture to verify that no regressions have been introduced.

The primary objective is to guarantee that the external behavior of the plugin remains unchanged after the internal architecture has been significantly improved.

## 2. Overall Testing Process

### 2.1. Pre-Refactoring Baseline
Before any code is refactored, the following steps must be completed:
1.  **Execute Full Test Suite:** Run the entire existing test suite (`test`, `integrationTest`, `e2eTest`) to ensure a clean, green baseline.
2.  **Develop New Tests (as specified below):** Create the new unit and integration tests outlined in this plan. These tests will initially target the legacy components (`MetricsUtils`, `MetricTaskCache`). This ensures the tests accurately capture the behavior that needs to be preserved.
3.  **Commit Baseline Tests:** Commit the new tests to version control before committing any refactoring code. This provides a clear, testable contract that the refactored code must fulfill.

### 2.2. Post-Refactoring Validation
After the Phase 1 refactoring is complete:
1.  **Adapt Tests:** Modify the existing and newly created tests to target the new services (`UIStateService`, `TaskQueueService`, etc.) instead of the legacy classes.
2.  **Execute Full Test Suite:** Run the complete, adapted test suite. A 100% pass rate is required to validate the success of the refactoring.
3.  **Manual Verification:** Perform a brief manual check of the UI actions that were refactored (e.g., the "Auto Scroll" and "Calculate Project Metrics" buttons) to confirm their functionality in the running IDE.

## 3. Detailed Test Specifications per Task

### Task 1.1: Create `UIStateService`

- **Objective:** Verify that UI state management (e.g., auto-scroll toggles) works identically after being moved from `MetricsUtils` to `UIStateService`.

- **Existing Tests to Leverage:**
    - `SetProjectAutoScrollableActionTest.java`: This E2E test already covers the required user-facing behavior.

- **New Tests to Develop:**
    - **Test Type:** Unit Test
    - **Class Name:** `UIStateServiceTest.java`
    - **Location:** `src/test/java/org/b333vv/metric/service/`
    - **Description:** A standard JUnit test for the `UIStateService` POJO.
    - **Test Cases:**
        - Verify that the default value for each state property (e.g., `isProjectAutoScrollable`) is correct upon service instantiation.
        - For each property, create a test that calls the setter and then asserts that the getter returns the new value.
        - Test toggling boolean values from `true` to `false` and vice-versa.

- **Refactoring Test Strategy:**
    - **`SetProjectAutoScrollableActionTest.java`:**
        - **Before:** The test directly manipulates and asserts against static fields/methods in `MetricsUtils`.
        - **After:** The test will be modified to get the `UIStateService` instance from the test project (`getProject().getService(UIStateService.class)`). All assertions and state manipulations will be performed on this service instance instead of `MetricsUtils`. The test's logic and assertions about the action's behavior will remain the same.

### Task 1.2: Create `TaskQueueService`

- **Objective:** Ensure the background task queuing and execution mechanism is preserved after being extracted from `MetricTaskCache`.

- **Existing Tests to Leverage:**
    - `CalculateProjectMetricsActionTest.java`: This E2E test already verifies that a task is queued when the action is performed.

- **New Tests to Develop:**
    - **Test Type:** Unit Test
    - **Class Name:** `TaskQueueServiceTest.java`
    - **Location:** `src/test/java/org/b333vv/metric/service/`
    - **Description:** A JUnit test for the `TaskQueueService`. This will require mocking IntelliJ Platform components.
    - **Test Cases:**
        - `testQueueIsEmptyInitially`: Assert that `isQueueEmpty()` returns `true` for a new service instance.
        - `testQueueIsNotEmptyAfterQueueing`: Call `queue(mockTask)` and assert that `isQueueEmpty()` now returns `false`.
        - `testProcessNextTaskRunsTask`: Mock `ProgressManager` and `ApplicationManager`. Call `queue(mockTask)` and verify that `ProgressManager.getInstance().run(task)` is eventually called. This is a more complex test that verifies the core execution logic.

- **Refactoring Test Strategy:**
    - **`CalculateProjectMetricsActionTest.java`:**
        - **Before:** The test uses `Mockito.spy()` on the `MetricTaskCache` service to verify interactions.
        - **After:** The test will be modified to spy on the new `TaskQueueService` instead. The `ServiceContainerUtil.replaceService` call will be updated to replace `TaskQueueService.class`. The `Mockito.verify` calls will be updated to check for invocations on the `TaskQueueService` spy (e.g., `verify(spyTaskQueueService).queue(any())`).

### Task 1.3: Create `CacheService`

- **Objective:** Validate that the data caching and VFS-based invalidation logic remains correct after being moved to `CacheService`.

- **Existing Tests to Leverage:** None directly test caching logic, but many integration tests implicitly rely on it. Their continued passing will be a validation signal.

- **New Tests to Develop:**
    - **Test Type:** Unit Test
    - **Class Name:** `CacheServiceTest.java`
    - **Location:** `src/test/java/org/b333vv/metric/service/`
    - **Description:** A JUnit test for the `UserDataHolder` functionality.
    - **Test Cases:**
        - `testPutAndGetUserData`: Use a `Key<String>` to put a value into the cache and assert that `getUserData` returns the same value.
        - `testOverwriteUserData`: Put a value, then put a new value with the same key, and assert the new value is returned.
        - `testInvalidateUserData`: Put several values into the cache, call `invalidateUserData()`, and assert that `getUserData` now returns `null` for all keys.

    - **Test Type:** Integration Test
    - **Class Name:** `CacheInvalidationTest.java`
    - **Location:** `src/integration-test/java/org/b333vv/metric/service/`
    - **Description:** An integration test using `BasePlatformTestCase` to verify VFS listener functionality.
    - **Test Cases:**
        - `testCacheInvalidatedOnContentChange`:
            1. Get the `CacheService` instance from the test project.
            2. Create a dummy file using `myFixture.configureByText("Test.java", "class A {}")`.
            3. Put a value into the cache using the file's `VirtualFile` as part of the key or context.
            4. Modify the file's content using `myFixture.getEditor().getDocument().setText("class B {}")`.
            5. Trigger a VFS event save (`FileDocumentManager.getInstance().saveAllDocuments()`).
            6. Assert that the cached data for that file is now `null`.

- **Refactoring Test Strategy:**
    - All existing integration tests that implicitly use the cache (e.g., `ProjectModelBuilderIntegrationTest`) should be re-run without modification. Their continued success will validate that the refactored `CacheService` is being used correctly by the system.

### Task 1.4: Refactor `MetricsService` into `SettingsService`

- **Objective:** Ensure that all settings are still accessible and that utility logic is correctly relocated.

- **Existing Tests to Leverage:**
    - `MetricsServiceTest.java`: This unit test already covers the `isLongValueMetricType` logic.

- **New Tests to Develop:**
    - **Test Type:** Integration Test
    - **Class Name:** `SettingsServiceTest.java`
    - **Location:** `src/integration-test/java/org/b333vv/metric/service/`
    - **Description:** An integration test using `BasePlatformTestCase` to verify the `SettingsService` facade.
    - **Test Cases:**
        - `testGettersReturnNonNullSettings`: Get the `SettingsService` instance and call each getter (e.g., `getBasicMetricsSettings()`). Assert that the returned settings object is not null.
        - `testSettingsModificationIsPersisted`:
            1. Get the `SettingsService`.
            2. Get a specific settings component (e.g., `getOtherSettings()`).
            3. Change a value (e.g., `setProjectMetricsStampStored(false)`).
            4. Get a new instance of the `SettingsService` (simulating a different part of the app accessing it).
            5. Get the same settings component again and assert that the new value (`false`) is returned.

- **Refactoring Test Strategy:**
    - **`MetricsServiceTest.java`:**
        - **Before:** Tests the `isLongValueMetricType` method on an instance of `MetricsService`.
        - **After:** The test will be renamed to `MetricTypeUtilsTest.java` (or similar). It will be modified to test the new static utility method or the new method on the `MetricType` enum, depending on the chosen implementation. The test logic itself will remain identical.