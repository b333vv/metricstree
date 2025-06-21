package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
// import com.intellij.openapi.project.ex.ProjectEx; // No longer directly used
import com.intellij.testFramework.ServiceContainerUtil; // Added import
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.ui.log.MetricsConsole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

// It's an E2E test, but if it uses Mockito for the service, MockitoExtension is useful.
@ExtendWith(MockitoExtension.class)
public class CleanConsoleActionTest extends BasePlatformTestCase {

    @Mock
    private MetricsConsole mockMetricsConsole;

    @Test
    public void testActionPerformed() {
        Project project = getProject(); // Use getProject() from BasePlatformTestCase directly

        // Register mock MetricsConsole as a project service
        ServiceContainerUtil.replaceService(project, MetricsConsole.class, mockMetricsConsole, getTestRootDisposable());

        // Instantiate the action
        CleanConsoleAction action = new CleanConsoleAction();

        // Create a test AnActionEvent
        AnActionEvent event = TestActionEvent.createTestEvent(); // Simplified creation

        // Call actionPerformed
        action.actionPerformed(event);

        // Verify that mockMetricsConsole.clear() was called
        Mockito.verify(mockMetricsConsole, Mockito.times(1)).clear();
    }
}
