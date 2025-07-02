# Refactoring Plan: Phase 3 - Final Cleanup and Architectural Solidification

## 1. Executive Summary & Goals
This document provides a unified, actionable plan for Phase 3 of the MetricsTree plugin refactoring. It merges the implementation specifications from `Phase 3 Spec.md` with the validation and testing strategies from `Phase 3 Test.md`. The primary objective of this phase is to eliminate the remaining technical debt from the initial architecture, ensuring the new service-oriented patterns are applied consistently across the entire codebase.

- **Goal 1:** Completely remove the deprecated `MetricsUtils` and `MetricTaskCache` classes from the project.
- **Goal 2:** Ensure all components, especially UI panels and helper classes, exclusively use the new services for state management, task execution, and data caching.
- **Goal 3:** Solidify the event-driven UI pattern, ensuring UI components are "dumb" renderers that react to `MessageBus` events rather than containing business logic.

## 2. Current Situation Analysis
Following the completion of Phases 1 and 2, the new service layer (`UIStateService`, `TaskQueueService`, `CacheService`, `SettingsService`, `CalculationService`) is operational and used by the primary UI actions. However, the legacy classes `MetricsUtils` and `MetricTaskCache` still exist, marked as `@Deprecated`. They are still referenced by numerous secondary components (e.g., UI panels, tree builders, metric visitors) that were not the focus of the initial refactoring phases. This residual technical debt prevents the full realization of a decoupled architecture and poses a risk of new code accidentally depending on the old, incorrect patterns.

## 3. Proposed Solution / Refactoring Strategy
### 3.1. High-Level Design / Architectural Overview
This phase is a systematic "search and destroy" mission. The strategy is to methodically find every remaining usage of the deprecated classes and refactor them to use the appropriate new service. This will be followed by a comprehensive review of the UI layer to enforce a clean, event-driven pattern, ensuring the final architecture is robust and consistent. All components will interact through the well-defined service layer or the platform's message bus, with no dependencies on the legacy classes.

### 3.2. Key Components / Modules
- **`EditorUtils` (New):** A new, focused utility class to house genuinely static, stateless helper methods related to PSI and Editor manipulation, extracted from `MetricsUtils`.
- **All other components:** Will be refactored to remove dependencies on the deprecated classes.

### 3.3. Detailed Action Plan / Phases
This plan constitutes a single, focused phase. The tasks integrate implementation and testing steps to ensure continuous validation.

- **Phase 3.1: Legacy Code Elimination & Utility Refactoring**
    - **Objective(s):** To completely remove the legacy God Objects and their associated technical debt, migrating their remaining valid logic to appropriate new homes.
    - **Priority:** High

    - **Task 3.1.1: Refactor All `MetricsUtils` Usages**
        - **Rationale/Goal:** To eliminate the primary God Object by migrating its varied responsibilities to the correct services or new, focused utility classes.
        - **Estimated Effort (Optional):** L
        - **Implementation Steps:**
            1.  Use the IDE's "Find Usages" feature on the `MetricsUtils` class to identify all remaining call sites.
            2.  Categorize and refactor each usage:
                - **UI State Access:** Calls like `MetricsUtils.isProjectAutoScrollable()` must be replaced with calls to `project.getService(UIStateService.class)`.
                - **Filter Access:** The `MetricsTreeFilter` objects should be moved into and managed by the `UIStateService`. All calls like `MetricsUtils.getClassMetricsTreeFilter()` must be refactored to use the service.
                - **PSI/Editor Helpers:** Truly static, stateless helpers like `getSelectedPsiJavaFile`, `openInEditor`, and `isElementInSelectedFile` should be moved to a new, focused utility class: `org.b333vv.metric.util.EditorUtils`.
                - **Generic Helpers:** The `sortByValueReversed` method should be moved to a new utility class, e.g., `org.b333vv.metric.util.CollectionUtils`.
            3.  After all usages are refactored, delete the `MetricsUtils.java` file. The project must compile successfully.
        - **Deliverable/Criteria for Completion:**
            - The `MetricsUtils.java` file is deleted.
            - The new `EditorUtils` class exists and contains the relevant static helpers.
            - The `MetricsUtilsTest.java` file is renamed to `EditorUtilsTest.java`, and its tests are adapted to validate the new utility class.
            - The project compiles and all existing tests pass.

    - **Task 3.1.2: Refactor All `MetricTaskCache` Usages**
        - **Rationale/Goal:** To finalize the removal of the overloaded service, ensuring all caching and task management goes through the new, dedicated services.
        - **Estimated Effort (Optional):** M
        - **Implementation Steps:**
            1.  Use "Find Usages" on the `MetricTaskCache` class.
            2.  Refactor any remaining code that calls `project.getService(MetricTaskCache.class)` to instead call `project.getService(CacheService.class)` or `project.getService(TaskQueueService.class)` as appropriate.
            3.  Pay special attention to `CouplingBetweenObjectsVisitor`, which gets the `DependenciesBuilder` from the cache. It must be updated to use `CacheService`.
            4.  After all usages are refactored, delete the `MetricTaskCache.java` file. The project must compile successfully.
        - **Deliverable/Criteria for Completion:**
            - The `MetricTaskCache.java` file is deleted.
            - The project compiles and all existing tests pass.

- **Phase 3.2: UI Layer Hardening**
    - **Objective(s):** To enforce a strict separation of concerns where UI panels are "dumb" components that only render data and delegate user actions.
    - **Priority:** High

    - **Task 3.2.1: Refactor UI Panels to be Purely Event-Driven**
        - **Rationale/Goal:** To ensure UI components are decoupled from the data-providing logic, making them simpler and more predictable.
        - **Estimated Effort (Optional):** M
        - **Implementation Steps:**
            1.  Review all classes in `org.b333vv.metric.ui.tool` and `org.b333vv.metric.ui.info`.
            2.  **Identify and remove business logic:** Any logic that makes decisions based on settings or data should be moved out of the UI class.
            3.  **Ensure event-driven updates:** Panels must not proactively fetch data (e.g., by calling `CacheService`). They must implement `MetricsEventListener` and update their state only when an event like `projectMetricsTreeIsReady()` is received from the `MessageBus`.
            4.  **Verify action delegation:** Ensure all user actions (button clicks, etc.) that result in a calculation are handled by `AnAction` classes, which then call the `CalculationService`.
        - **Deliverable/Criteria for Completion:**
            - UI panels are stateless (or have minimal view-only state) and are updated exclusively via `MessageBus` events.
            - New integration tests are created for `ProjectMetricsPanel` and `ClassMetricsPanel` to verify their event-driven behavior. These tests should:
                - Fire a `clearProjectMetricsTree` event and assert that the panel's components are cleared.
                - Fire a `projectMetricsTreeIsReady` event with a mock model and assert that the panel's `MetricsTree` is updated with that model.

- **Phase 3.3: Final Verification**
    - **Objective(s):** To perform a final, comprehensive validation of the entire refactoring effort.
    - **Priority:** High

    - **Task 3.3.1: Full Automated Regression Testing**
        - **Rationale/Goal:** To catch any regressions from the widespread changes made during the cleanup phase.
        - **Estimated Effort (Optional):** M
        - **Implementation Steps:**
            1.  Execute the entire automated test suite, including unit, integration, and E2E tests.
        - **Deliverable/Criteria for Completion:**
            - 100% pass rate on the full test suite on a clean build.

    - **Task 3.3.2: Comprehensive Manual Testing**
        - **Rationale/Goal:** To verify UI behavior and end-to-end workflows that are difficult or impossible to automate.
        - **Estimated Effort (Optional):** M
        - **Implementation Steps:**
            1.  Follow the manual testing checklist below.
        - **Deliverable/Criteria for Completion:**
            - Successful completion of all manual test cases.
            - **Manual Test Checklist:**
                - [ ] **Settings Workflow:**
                    - [ ] Open the settings dialog (`MetricsConfigurable`).
                    - [ ] Change a value on the "Basic Metrics" tab.
                    - [ ] Change a value on the "Class Level Fitness Functions" tab.
                    - [ ] Change a value on the "Other Settings" tab.
                    - [ ] Click "Apply". Close and reopen the settings dialog. Verify all changes were persisted.
                - [ ] **Class Metrics Workflow:**
                    - [ ] Open a Java file. Verify the class metrics tree appears.
                    - [ ] Click the "Refresh" button and confirm the tree reloads.
                    - [ ] Click the "Filter" button and confirm the filter popup appears.
                - [ ] **Project Metrics Workflow:**
                    - [ ] Click "Calculate Project Metrics". Verify the project tree appears.
                    - [ ] Click on a project node, a package node, a class node, and a metric node. Confirm the details panel on the right updates correctly for each selection.
                - [ ] **Fitness Function Workflow:**
                    - [ ] Navigate to the "Metric Fitness Functions" tab.
                    - [ ] Click "Build Class Level Fitness Function". Verify the list of profiles appears.
                    - [ ] Click on a profile and verify the list of classes updates.
                    - [ ] Click on a class and verify the metrics table updates.
                - [ ] **Log Workflow:**
                    - [ ] Perform a calculation. Navigate to the "Log" tab and verify messages appear.
                    - [ ] Click the "Clear All" button and confirm the log is cleared.

## 4. Key Considerations & Risk Mitigation
- **Missed Dependencies:** A static usage of a deprecated method might be missed by automated tools. The final step of deleting the class files (`MetricsUtils.java`, `MetricTaskCache.java`) is the ultimate verification. A compilation failure will pinpoint any missed reference.
- **Behavioral Changes:** Refactoring UI components to be purely event-driven might subtly alter behavior related to timing or state updates. Thorough manual testing of the UI is crucial to mitigate this risk.

## 5. Success Metrics / Validation Criteria
- **Primary Metric:** The `MetricsUtils.java` and `MetricTaskCache.java` files are successfully deleted from the source tree.
- **Verification:** The project compiles, all automated tests pass, and all user-facing features are confirmed to be working correctly through the manual testing checklist.
- **Qualitative Assessment:** A code review confirms that the new service-oriented architecture is applied consistently and that the UI layer is properly decoupled.

## 6. Assumptions Made
- Phases 1 and 2 of the refactoring have been successfully completed.
- The new services (`UIStateService`, `TaskQueueService`, `CacheService`, `SettingsService`, `CalculationService`) are stable and correctly implement the required functionality.
- The existing test suite provides adequate coverage to detect major regressions.

## 7. Open Questions / Areas for Further Investigation
- Are there any other "utility" or "manager" classes in the project that exhibit God Object characteristics and should be considered for a future refactoring phase? A post-mortem review after Phase 3 would be beneficial.
- Can the test suite be improved to reduce reliance on `BasePlatformTestCase` for logic that is now framework-independent?