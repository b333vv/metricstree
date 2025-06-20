package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.util.MetricsUtils;

// No Mockito needed for this test, so no @ExtendWith(MockitoExtension.class)

public class SetProjectAutoScrollableActionTest extends BasePlatformTestCase {

    private AnActionEvent event;
    private SetProjectAutoScrollableAction action;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Set default state before each test
        MetricsUtils.setProjectAutoScrollable(true);
        action = new SetProjectAutoScrollableAction();
        // Create a TestActionEvent that provides the project
        DataContext dataContext = dataId -> CommonDataKeys.PROJECT.is(dataId) ? getProject() : null;
        event = TestActionEvent.createTestEvent(dataContext);
    }

    public void testIsSelectedReflectsMetricsUtilsState() {
        MetricsUtils.setProjectAutoScrollable(true);
        assertTrue("Action should be selected when MetricsUtils.isProjectAutoScrollable is true.", action.isSelected(event));

        MetricsUtils.setProjectAutoScrollable(false);
        assertFalse("Action should not be selected when MetricsUtils.isProjectAutoScrollable is false.", action.isSelected(event));
    }

    public void testSetSelectedUpdatesMetricsUtilsState() {
        // Initial state set to true in setUp()
        assertTrue(MetricsUtils.isProjectAutoScrollable(), "Initial MetricsUtils state should be true.");
        assertTrue(action.isSelected(event), "Action should initially be selected.");

        action.setSelected(event, false);
        assertFalse(MetricsUtils.isProjectAutoScrollable(), "MetricsUtils state should be false after setSelected(false).");
        assertFalse(action.isSelected(event), "Action should not be selected after setSelected(false).");

        action.setSelected(event, true);
        assertTrue(MetricsUtils.isProjectAutoScrollable(), "MetricsUtils state should be true after setSelected(true).");
        assertTrue(action.isSelected(event), "Action should be selected after setSelected(true).");
    }

    public void testActionPerformedTogglesState() {
        // Initial state set to true in setUp()
        assertTrue(MetricsUtils.isProjectAutoScrollable(), "Initial MetricsUtils state should be true.");
        boolean initialActionSelectedState = action.isSelected(event);
        assertTrue(initialActionSelectedState, "Action should be selected initially.");

        // First toggle: true -> false
        action.actionPerformed(event);
        assertFalse(MetricsUtils.isProjectAutoScrollable(), "MetricsUtils state should be false after first actionPerformed.");
        assertFalse(action.isSelected(event), "Action should not be selected after first actionPerformed.");

        // Second toggle: false -> true
        action.actionPerformed(event);
        assertTrue(MetricsUtils.isProjectAutoScrollable(), "MetricsUtils state should be true after second actionPerformed.");
        assertTrue(action.isSelected(event), "Action should be selected after second actionPerformed.");
    }
}
