package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectEx;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.ui.console.MetricsConsole;
import org.junit.jupiter.api.Test; // Using JUnit 5 test annotation
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
        Project project = myFixture.getProject();

        // Register mock MetricsConsole as a project service
        // This is the tricky part. ProjectEx.replaceService is preferred.
        if (project instanceof ProjectEx) {
            ProjectEx projectEx = (ProjectEx) project;
            projectEx.replaceService(MetricsConsole.class, mockMetricsConsole, getTestRootDisposable());
        } else {
            // Fallback or error if ProjectEx is not available or replaceService fails.
            // For this test, we'll assume it works or the test might need to be adapted
            // if the environment doesn't support ProjectEx fully (e.g. in older IDE versions).
            // Another way could be to ensure the service is registered if not present,
            // but that's more for overriding default behavior if a real service existed.
            // Here, we want to inject a mock.
            // If this block is hit, the test might not run as intended unless another
            // service mocking mechanism for BasePlatformTestCase is found.
            System.err.println("Warning: Project is not an instance of ProjectEx. Mock service replacement might not work as expected.");
            // A more robust way if replaceService is unavailable might involve using PicoContainer directly,
            // or ensuring the test environment allows overriding getService, but that's more complex.
            // For now, this test relies on replaceService or manual verification if it fails.
            // One could also try:
            // Disposer.register(getTestRootDisposable(), () -> ServiceManager.getService(project, MetricsConsole.class)); // to ensure it's disposed
            // ((MutablePicoContainer) project.getPicoContainer()).unregisterComponent(MetricsConsole.class.getName());
            // ((MutablePicoContainer) project.getPicoContainer()).registerComponentInstance(MetricsConsole.class.getName(), mockMetricsConsole);
            // However, this is more intrusive. Let's stick to replaceService and document if it fails.
        }

        // Instantiate the action
        CleanConsoleAction action = new CleanConsoleAction();

        // Create a test AnActionEvent
        DataContext dataContext = dataId -> CommonDataKeys.PROJECT.is(dataId) ? project : null;
        AnActionEvent event = TestActionEvent.createTestEvent(dataContext);

        // Call actionPerformed
        action.actionPerformed(event);

        // Verify that mockMetricsConsole.clear() was called
        Mockito.verify(mockMetricsConsole, Mockito.times(1)).clear();
    }
}
