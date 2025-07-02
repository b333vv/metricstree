# Detailed Plan: UI Layer Hardening and Validation

## 1. Executive Summary & Goals
This document provides a detailed, actionable plan for implementing and validating **Task 3.2.1** of the Phase 3 refactoring. The primary objective is to create a robust suite of integration tests for the main UI panels (`ProjectMetricsPanel`, `ClassMetricsPanel`) to formally verify their event-driven architecture.

While the UI panels have been refactored to listen to `MessageBus` events, this behavior is not currently covered by automated tests. This plan addresses that gap, ensuring the UI layer is truly decoupled, predictable, and resilient to future regressions.

- **Goal 1:** Create integration tests for `ProjectMetricsPanel` that validate its response to `MessageBus` events.
- **Goal 2:** Create integration tests for `ClassMetricsPanel` that validate its response to `MessageBus` events.
- **Goal 3:** Establish a testing pattern that can be applied to other complex UI components in the future.

## 2. Current Situation Analysis
The UI panels in `org.b333vv.metric.ui.tool` have been refactored to contain an inner `MetricsEventListener` class. They are intended to be "dumb" components that render data provided via the `MessageBus`. However, this crucial architectural pattern lacks automated validation. Without tests, there is a risk that:
- Future changes could inadvertently re-introduce business logic or direct data fetching into the UI layer.
- Regressions in the event-handling logic could go unnoticed.
- The contract between the `CalculationService` (which publishes events) and the UI panels (which consume them) is not formally enforced.

## 3. Proposed Solution / Refactoring Strategy
### 3.1. High-Level Design / Architectural Overview
The testing strategy is to use the `BasePlatformTestCase` provided by the IntelliJ test framework. This gives us a lightweight, headless IDE environment with a real `Project` instance, a `MessageBus`, and the ability to instantiate UI components.

The test workflow for each panel will be:
1.  **Setup:** In the test's `setUp` method, instantiate the target UI panel.
2.  **Act:** In each test method, get the project's `MessageBus` and publish a specific event (e.g., `projectMetricsTreeIsReady`).
3.  **Dispatch:** Ensure the UI event queue is processed, allowing the panel's listener to react.
4.  **Assert:** Inspect the internal state of the UI panel's components (e.g., the `JTree`'s model) to verify it has been updated correctly.

### 3.2. Key Components / Modules
- **`ProjectMetricsPanelTest.java`**: A new integration test class for `ProjectMetricsPanel`.
- **`ClassMetricsPanelTest.java`**: A new integration test class for `ClassMetricsPanel`.
- **Test Fixtures**: The tests will use mock objects (e.g., a mock `DefaultTreeModel`) as event payloads to isolate the test from the actual calculation logic.

### 3.3. Detailed Action Plan / Phases
This plan is broken down into two phases, one for each major UI panel.

#### **Phase 1: Integration Testing for `ProjectMetricsPanel`**
- **Objective(s):** Verify that `ProjectMetricsPanel` correctly initializes, clears its view, and updates its content in response to `MessageBus` events.
- **Priority:** High

- **Task 1.1: Setup `ProjectMetricsPanelTest`**
    - **Rationale/Goal:** Create the test class and fixture setup required to test the panel.
    - **Estimated Effort (Optional):** S
    - **Deliverable/Criteria for Completion:**
        1.  Create a new file: `src/integration-test/java/org/b333vv/metric/ui/tool/ProjectMetricsPanelTest.java`.
        2.  The class should extend `com.intellij.testFramework.fixtures.BasePlatformTestCase`.
        3.  Implement the `setUp()` method to initialize an instance of `ProjectMetricsPanel`:
            ```java
            public class ProjectMetricsPanelTest extends BasePlatformTestCase {
                private ProjectMetricsPanel panel;

                @Override
                protected void setUp() throws Exception {
                    super.setUp();
                    // The panel needs to be created on the EDT
                    UIUtil.invokeAndWaitIfNeeded(() -> {
                        panel = ProjectMetricsPanel.newInstance(getProject());
                    });
                }
                // ... tests will go here
            }
            ```
        4.  **Note:** To inspect the internal state of the panel (e.g., its `MetricsTree`), it may be necessary to add a package-private getter method to `MetricsTreePanel`, like `getMetricsTree()`.

- **Task 1.2: Implement `testPanelIsClearedOnClearEvent`**
    - **Rationale/Goal:** To verify that the panel correctly resets its state when a `clearProjectMetricsTree` event is published.
    - **Estimated Effort (Optional):** M
    - **Deliverable/Criteria for Completion:**
        1.  A new test method `testPanelIsClearedOnClearEvent` is created.
        2.  **Test Logic:**
            ```java
            // 1. Arrange: Populate the panel with some initial data
            DefaultTreeModel initialModel = new DefaultTreeModel(new DefaultMutableTreeNode("Initial State"));
            getProject().getMessageBus().syncPublisher(MetricsEventListener.TOPIC).projectMetricsTreeIsReady(initialModel);
            UIUtil.dispatchAllInvocationEvents(); // Ensure listener has processed the event
            assertNotNull(panel.getMetricsTree().getModel(), "Panel should have a model after initial population.");
            assertNotEquals(0, panel.getMetricsTree().getRowCount(), "Tree should have rows initially.");

            // 2. Act: Publish the clear event
            getProject().getMessageBus().syncPublisher(MetricsEventListener.TOPIC).clearProjectMetricsTree();
            UIUtil.dispatchAllInvocationEvents(); // Process the clear event

            // 3. Assert: Verify the panel is now empty
            assertNull(panel.getMetricsTree().getModel().getRoot(), "Tree root should be null after clear event.");
            ```

- **Task 1.3: Implement `testPanelUpdatesOnMetricsReadyEvent`**
    - **Rationale/Goal:** To verify the primary function of the panel: displaying a new metrics tree when the `projectMetricsTreeIsReady` event is published.
    - **Estimated Effort (Optional):** M
    - **Deliverable/Criteria for Completion:**
        1.  A new test method `testPanelUpdatesOnMetricsReadyEvent` is created.
        2.  **Test Logic:**
            ```java
            // 1. Arrange: Create a mock tree model to use as the event payload
            DefaultMutableTreeNode mockRoot = new DefaultMutableTreeNode("Mock Root");
            mockRoot.add(new DefaultMutableTreeNode("Child 1"));
            DefaultTreeModel mockModel = new DefaultTreeModel(mockRoot);

            // 2. Act: Publish the event
            getProject().getMessageBus().syncPublisher(MetricsEventListener.TOPIC).projectMetricsTreeIsReady(mockModel);
            UIUtil.dispatchAllInvocationEvents(); // Ensure listener has processed the event

            // 3. Assert: Verify the panel's tree now uses the mock model
            assertNotNull(panel.getMetricsTree().getModel(), "Panel tree model should not be null.");
            assertEquals(mockRoot, panel.getMetricsTree().getModel().getRoot(), "Tree root should be the one from the event.");
            assertEquals(1, panel.getMetricsTree().getRowCount(), "Tree should display the new model's rows.");
            ```

#### **Phase 2: Integration Testing for `ClassMetricsPanel`**
- **Objective(s):** Verify that `ClassMetricsPanel` correctly responds to its specific set of events, such as a new file being selected or evolution data being calculated.
- **Priority:** High

- **Task 2.1: Setup `ClassMetricsPanelTest`**
    - **Rationale/Goal:** Create the test class and fixture for `ClassMetricsPanel`.
    - **Estimated Effort (Optional):** S
    - **Deliverable/Criteria for Completion:**
        1.  Create a new file: `src/integration-test/java/org/b333vv/metric/ui/tool/ClassMetricsPanelTest.java`.
        2.  The class should extend `BasePlatformTestCase`.
        3.  The `setUp()` method should initialize `ClassMetricsPanel`.

- **Task 2.2: Implement `testPanelUpdatesOnClassMetricsEvolutionCalculated`**
    - **Rationale/Goal:** To validate that the panel correctly displays the metrics evolution tree when the corresponding event is fired.
    - **Estimated Effort (Optional):** M
    - **Deliverable/Criteria for Completion:**
        1.  A new test method `testPanelUpdatesOnClassMetricsEvolutionCalculated` is created.
        2.  **Test Logic:**
            ```java
            // 1. Arrange: Create a mock tree model for the evolution data
            DefaultMutableTreeNode evolutionRoot = new DefaultMutableTreeNode("Evolution Data");
            evolutionRoot.add(new DefaultMutableTreeNode("Commit 1"));
            DefaultTreeModel evolutionModel = new DefaultTreeModel(evolutionRoot);

            // 2. Act: Publish the event
            getProject().getMessageBus().syncPublisher(MetricsEventListener.TOPIC).classMetricsValuesEvolutionCalculated(evolutionModel);
            UIUtil.dispatchAllInvocationEvents();

            // 3. Assert: Verify the panel's tree now displays the evolution model
            assertEquals(evolutionRoot, panel.getMetricsTree().getModel().getRoot(), "Tree should display the evolution model root.");
            ```

- **Task 2.3: Implement `testPanelUpdatesOnNewFileSelection`**
    - **Rationale/Goal:** To verify that the panel correctly triggers a recalculation (or clears itself) when the user switches to a new Java file in the editor. This is a critical part of its behavior.
    - **Estimated Effort (Optional):** L
    - **Deliverable/Criteria for Completion:**
        1.  A new test method `testPanelUpdatesOnNewFileSelection` is created.
        2.  **Test Logic:**
            ```java
            // 1. Arrange: Configure two different Java files in the fixture
            PsiFile fileA = myFixture.configureByText("FileA.java", "class A {}");
            PsiFile fileB = myFixture.configureByText("FileB.java", "class B {}");

            // Spy on the panel to verify its update method is called
            ClassMetricsPanel panelSpy = spy(panel);
            // This might require making the panel instance accessible for spying.

            // 2. Act: Simulate the FileEditorManager sending a selection change event
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
            // This is a simplified representation; the actual event might need more setup.
            fileEditorManager.getEventPublisher().selectionChanged(
                new FileEditorManagerEvent(fileEditorManager, fileA.getVirtualFile(), null, fileB.getVirtualFile(), null)
            );
            UIUtil.dispatchAllInvocationEvents();

            // 3. Assert: Verify that the panel's update method was called with the new file
            verify(panelSpy, times(1)).update((PsiJavaFile) fileB);
            ```

## 4. Key Considerations & Risk Mitigation
- **Headless UI Testing:** Testing Swing components in a headless environment can be tricky. The use of `UIUtil.dispatchAllInvocationEvents()` is critical to ensure that listeners on the Event Dispatch Thread (EDT) have a chance to run before assertions are made.
- **Component Accessibility:** The internal components of the panels (e.g., `private MetricsTree metricsTree;`) must be accessible to the tests. If they are private, they should be changed to package-private or have package-private getters to facilitate testing without exposing them publicly.
- **Test Isolation:** Each test method should start with a clean panel state. The `setUp()` method should ensure a fresh instance of the panel is created for each test.

## 5. Success Metrics / Validation Criteria
- The new test classes `ProjectMetricsPanelTest.java` and `ClassMetricsPanelTest.java` are implemented in the `integration-test` source set.
- The tests successfully compile and pass as part of the full test suite.
- The tests explicitly assert the state of UI components *after* a `MessageBus` event has been published, confirming the event-driven contract.
- Code coverage for the listener logic within the UI panels is significantly increased.

## 6. Assumptions Made
- The `BasePlatformTestCase` provides a functional `MessageBus` that can be used to publish and receive events within the test environment.
- The UI panels can be instantiated and manipulated on the EDT within the test environment using `UIUtil.invokeAndWaitIfNeeded()`.

## 7. Open Questions / Areas for Further Investigation
- **Other Panels:** Should `ClassLevelFitnessFunctionPanel` and `PackageLevelFitnessFunctionPanel` also have similar integration tests?
    - **Recommendation:** Yes. After establishing the pattern with the first two panels, a follow-up task should be created to apply the same testing strategy to the fitness function panels, as they also rely heavily on event-driven updates.