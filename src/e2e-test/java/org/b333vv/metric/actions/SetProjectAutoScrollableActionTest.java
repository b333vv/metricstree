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
        // Create a TestActionEvent
        event = new TestActionEvent(); // Corrected instantiation
    }

    public void testIsSelectedReflectsMetricsUtilsState() {
        MetricsUtils.setProjectAutoScrollable(true);
        assertTrue("Action should be selected when MetricsUtils.isProjectAutoScrollable is true.", action.isSelected(event));

        MetricsUtils.setProjectAutoScrollable(false);
        assertFalse("Action should not be selected when MetricsUtils.isProjectAutoScrollable is false.", action.isSelected(event));
    }

    public void testSetSelectedUpdatesMetricsUtilsState() {
        // Initial state set to true in setUp()
        assertTrue("Initial MetricsUtils state should be true (from setUp).", MetricsUtils.isProjectAutoScrollable());
        assertTrue("Action should initially be selected (from setUp).", action.isSelected(event));

        action.setSelected(event, false);
        assertFalse("MetricsUtils state should be false after setSelected(false).", MetricsUtils.isProjectAutoScrollable());
        assertFalse("Action should not be selected after setSelected(false).", action.isSelected(event));

        action.setSelected(event, true);
        assertTrue("MetricsUtils state should be true after setSelected(true).", MetricsUtils.isProjectAutoScrollable());
        assertTrue("Action should be selected after setSelected(true).", action.isSelected(event));
    }

    public void testActionPerformedTogglesState() {
        // Initial state set to true in setUp()
        assertTrue("Initial MetricsUtils state should be true (from setUp).", MetricsUtils.isProjectAutoScrollable());
        boolean initialActionSelectedState = action.isSelected(event);
        assertTrue("Action should be selected initially (from setUp).", initialActionSelectedState);

        // First toggle: true -> false
        action.actionPerformed(event);
        assertFalse("MetricsUtils state should be false after first actionPerformed.", MetricsUtils.isProjectAutoScrollable());
        assertFalse("Action should not be selected after first actionPerformed.", action.isSelected(event));

        // Second toggle: false -> true
        action.actionPerformed(event);
        assertTrue("MetricsUtils state should be true after second actionPerformed.", MetricsUtils.isProjectAutoScrollable());
        assertTrue("Action should be selected after second actionPerformed.", action.isSelected(event));
    }
}
