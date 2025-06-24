package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.service.UIStateService;

// No Mockito needed for this test, so no @ExtendWith(MockitoExtension.class)

public class SetProjectAutoScrollableActionTest extends BasePlatformTestCase {

    private AnActionEvent event;
    private SetProjectAutoScrollableAction action;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Set default state before each test
        Project project = getProject();
        project.getService(UIStateService.class).setProjectAutoScrollable(true);
        action = new SetProjectAutoScrollableAction();
        // Create a TestActionEvent with project
        event = new TestActionEvent();
        ((TestActionEvent) event).getPresentation().putClientProperty(CommonDataKeys.PROJECT.getName(), project);
    }

    public void testIsSelectedReflectsUIStateServiceState() {
        Project project = getProject();
        UIStateService uiStateService = project.getService(UIStateService.class);
        
        uiStateService.setProjectAutoScrollable(true);
        assertTrue("Action should be selected when UIStateService.isProjectAutoScrollable is true.", action.isSelected(event));

        uiStateService.setProjectAutoScrollable(false);
        assertFalse("Action should not be selected when UIStateService.isProjectAutoScrollable is false.", action.isSelected(event));
    }

    public void testSetSelectedUpdatesUIStateServiceState() {
        Project project = getProject();
        UIStateService uiStateService = project.getService(UIStateService.class);
        
        // Initial state set to true in setUp()
        assertTrue("Initial UIStateService state should be true (from setUp).", uiStateService.isProjectAutoScrollable());
        assertTrue("Action should initially be selected (from setUp).", action.isSelected(event));

        action.setSelected(event, false);
        assertFalse("UIStateService state should be false after setSelected(false).", uiStateService.isProjectAutoScrollable());
        assertFalse("Action should not be selected after setSelected(false).", action.isSelected(event));

        action.setSelected(event, true);
        assertTrue("UIStateService state should be true after setSelected(true).", uiStateService.isProjectAutoScrollable());
        assertTrue("Action should be selected after setSelected(true).", action.isSelected(event));
    }

    public void testActionPerformedTogglesState() {
        Project project = getProject();
        UIStateService uiStateService = project.getService(UIStateService.class);
        
        // Initial state set to true in setUp()
        assertTrue("Initial UIStateService state should be true (from setUp).", uiStateService.isProjectAutoScrollable());
        boolean initialActionSelectedState = action.isSelected(event);
        assertTrue("Action should be selected initially (from setUp).", initialActionSelectedState);

        // First toggle: true -> false
        action.actionPerformed(event);
        assertFalse("UIStateService state should be false after first actionPerformed.", uiStateService.isProjectAutoScrollable());
        assertFalse("Action should not be selected after first actionPerformed.", action.isSelected(event));

        // Second toggle: false -> true
        action.actionPerformed(event);
        assertTrue("UIStateService state should be true after second actionPerformed.", uiStateService.isProjectAutoScrollable());
        assertTrue("Action should be selected after second actionPerformed.", action.isSelected(event));
    }
}
