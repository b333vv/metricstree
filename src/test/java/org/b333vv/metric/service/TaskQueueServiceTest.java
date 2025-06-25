package org.b333vv.metric.service;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskQueueServiceTest {

    @Mock
    private ProgressManager mockProgressManager;
    @Mock
    private Application mockApplication;
    @Mock
    private Task.Backgroundable mockTask;

    private MockedStatic<ProgressManager> progressManagerMockedStatic;
    private MockedStatic<ApplicationManager> applicationManagerMockedStatic;

    private TaskQueueService service;

    @BeforeEach
    void setUp() {
        service = new TaskQueueService();
        progressManagerMockedStatic = mockStatic(ProgressManager.class);
        progressManagerMockedStatic.when(ProgressManager::getInstance).thenReturn(mockProgressManager);

        applicationManagerMockedStatic = mockStatic(ApplicationManager.class);
        applicationManagerMockedStatic.when(ApplicationManager::getApplication).thenReturn(mockApplication);
    }

    @AfterEach
    void tearDown() {
        progressManagerMockedStatic.close();
        applicationManagerMockedStatic.close();
    }

    @Test
    void testQueueIsEmptyInitially() {
        assertTrue(service.isQueueEmpty());
    }

    @Test
    void testQueueIsNotEmptyAfterQueueing() {
        doNothing().when(mockApplication).invokeLater(any(Runnable.class), any(ModalityState.class));

        service.queue(mockTask);

        assertFalse(service.isQueueEmpty(), "Queue should not be empty after queueing a task and before it completes.");
    }

    @Test
    void testProcessNextTaskRunsTask() {
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doNothing().when(mockApplication).invokeLater(runnableCaptor.capture(), any(ModalityState.class));

        service.queue(mockTask);

        runnableCaptor.getValue().run();

        verify(mockProgressManager).run(mockTask);

        assertTrue(service.isQueueEmpty(), "Queue should be empty after task is processed.");
    }
}
