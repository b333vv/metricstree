package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.testFramework.ServiceContainerUtil; // Added
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.service.TaskQueueService;
import org.b333vv.metric.task.ProjectTreeTask;
import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith; // Removed
// import org.mockito.ArgumentCaptor; // No longer needed
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations; // Added
import org.mockito.Spy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

// @ExtendWith(MockitoExtension.class) // Removed
public class CalculateProjectMetricsActionTest extends BasePlatformTestCase {

    @Mock
    private MetricsEventListener mockMetricsEventListener;

    @Spy
    private TaskQueueService spyTaskQueueService;

    private CalculateProjectMetricsAction action;
    private AnActionEvent event;

    @Override
    protected void setUp() throws Exception {
        super.setUp(); // Handles BasePlatformTestCase setup
        MockitoAnnotations.openMocks(this); // Initialize @Mock fields like mockMetricsEventListener

        // Register the spy as the service instance
        ServiceContainerUtil.replaceService(getProject(), TaskQueueService.class, spyTaskQueueService, getTestRootDisposable());

        getProject().getMessageBus().connect(getTestRootDisposable())
                .subscribe(MetricsEventListener.TOPIC, mockMetricsEventListener);

        action = new CalculateProjectMetricsAction(); // Action initialization
        event = new TestActionEvent();

        // Мокаем асинхронное выполнение очереди задач, чтобы избежать Already disposed
        doAnswer(invocation -> null)
                .when(spyTaskQueueService).queue(any(ProjectTreeTask.class));
    }

    @Test
    public void testUpdateLogic_ActionEnabledInitially() { // Renamed and simplified
        // The spyTaskQueueService initially has an empty queue and isProcessing is false by default.
        // The action will use this spied instance.
        action.update(event);
        assertTrue("Action should be enabled when task queue is initially empty and not processing.", event.getPresentation().isEnabled());
    }

    @Test
    public void testActionPerformed_SchedulesTaskAndClearsTree() {
        action.update(event);
        assertTrue("Action should be enabled before performing.", event.getPresentation().isEnabled());

        // Perform the action
        action.actionPerformed(event);

        Mockito.verify(mockMetricsEventListener, Mockito.times(1)).clearProjectMetricsTree();
        Mockito.verify(spyTaskQueueService, Mockito.times(1)).queue(any(ProjectTreeTask.class));
    }
}
