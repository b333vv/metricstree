package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.service.CalculationService;
import org.b333vv.metric.service.TaskQueueService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class CalculateProjectMetricsActionTest extends BasePlatformTestCase {

    @Mock
    private Project mockProject;

    @Mock
    private CalculationService mockCalculationService;

    @Mock
    private TaskQueueService mockTaskQueueService;

    private CalculateProjectMetricsAction action;
    private AnActionEvent event;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.openMocks(this);

        when(mockProject.getService(CalculationService.class)).thenReturn(mockCalculationService);
        when(mockProject.getService(TaskQueueService.class)).thenReturn(mockTaskQueueService);
        when(mockTaskQueueService.isQueueEmpty()).thenReturn(true);

        DataContext dataContext = new DataContext() {
            @Override
            public Object getData(String dataId) {
                if (CommonDataKeys.PROJECT.getName().equals(dataId)) {
                    return mockProject;
                }
                return null;
            }
        };

        action = new CalculateProjectMetricsAction();
        event = new TestActionEvent(dataContext);
    }

    @Test
    public void testUpdateLogic_ActionEnabledInitially() {
        action.update(event);
        assertTrue(event.getPresentation().isEnabled());
    }

    @Test
    public void testActionPerformed_CallsCalculateProjectTree() {
        action.update(event);
        assertTrue(event.getPresentation().isEnabled());

        action.actionPerformed(event);

        Mockito.verify(mockCalculationService, Mockito.times(1)).calculateProjectTree();
    }
}
