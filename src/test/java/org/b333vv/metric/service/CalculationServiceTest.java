package org.b333vv.metric.service;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.task.ProjectTreeTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import com.intellij.openapi.progress.ProgressIndicator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.tree.DefaultTreeModel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.b333vv.metric.util.SettingsService;
import org.b333vv.metric.builder.ProjectTreeModelCalculator;

@ExtendWith(MockitoExtension.class)
public class CalculationServiceTest {

    private CalculationService calculationService;

    @Mock
    private Project mockProject;

    @Mock
    private CacheService mockCacheService;

    @Mock
    private TaskQueueService mockTaskQueueService;

    @Mock
    private SettingsService mockSettingsService;

    @Mock
    private ProgressIndicator mockProgressIndicator;

    @Captor
    private ArgumentCaptor<ProjectTreeTask> projectTreeTaskCaptor;

    @BeforeEach
    public void setUp() {
        when(mockProject.getService(CacheService.class)).thenReturn(mockCacheService);
        when(mockProject.getService(TaskQueueService.class)).thenReturn(mockTaskQueueService);
        when(mockProject.getService(SettingsService.class)).thenReturn(mockSettingsService);

        calculationService = new CalculationServiceImpl(mockProject);
    }

    @Test
    public void testCalculateProjectTree_cacheHit() {
        DefaultTreeModel mockTreeModel = mock(DefaultTreeModel.class);
        when(mockCacheService.getUserData(CacheService.PROJECT_TREE)).thenReturn(mockTreeModel);

        calculationService.calculateProjectTree();

        verify(mockCacheService, times(1)).getUserData(CacheService.PROJECT_TREE);
        verify(mockTaskQueueService, never()).queue(any(ProjectTreeTask.class));
    }

    @Test
    public void testCalculateProjectTree_cacheMiss() {
        when(mockCacheService.getUserData(CacheService.PROJECT_TREE)).thenReturn(null);

        try (MockedStatic<ProjectTreeModelCalculator> mockedCalculator = mockStatic(ProjectTreeModelCalculator.class)) {
            ProjectTreeModelCalculator mockCalculatorInstance = mock(ProjectTreeModelCalculator.class);
            DefaultTreeModel mockCalculatedTreeModel = mock(DefaultTreeModel.class);

            mockedCalculator.when(() -> new ProjectTreeModelCalculator(any())).thenReturn(mockCalculatorInstance);
            when(mockCalculatorInstance.calculate()).thenReturn(mockCalculatedTreeModel);

            calculationService.calculateProjectTree();

            verify(mockCacheService, times(1)).getUserData(CacheService.PROJECT_TREE);
            verify(mockTaskQueueService, times(1)).queue(projectTreeTaskCaptor.capture());

            ProjectTreeTask capturedTask = projectTreeTaskCaptor.getValue();
            capturedTask.run(mockProgressIndicator);
            capturedTask.onSuccess();

            verify(mockCacheService, times(1)).putUserData(eq(CacheService.PROJECT_TREE), eq(mockCalculatedTreeModel));
        }
    }
}
