package org.b333vv.metric.service;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.builder.MetricsBackgroundableTask;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import com.intellij.openapi.progress.ProgressIndicator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.DumbService;
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.builder.DependenciesCalculator;
import org.b333vv.metric.builder.ClassAndMethodsMetricsCalculator;
import org.b333vv.metric.builder.PackageMetricsSetCalculator;
import org.b333vv.metric.builder.ProjectMetricsSetCalculator;

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
    private ArgumentCaptor<MetricsBackgroundableTask> projectTreeTaskCaptor;

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
        verify(mockTaskQueueService, never()).queue(any(MetricsBackgroundableTask.class));
    }

    @Test
    public void testModelBuildingDependencyChain() {
        // Mock initial cache misses
        when(mockCacheService.getUserData(CacheService.DEPENDENCIES)).thenReturn(null);
        when(mockCacheService.getUserData(CacheService.CLASS_AND_METHODS_METRICS)).thenReturn(null);
        when(mockCacheService.getUserData(CacheService.PACKAGE_METRICS)).thenReturn(null);
        when(mockCacheService.getUserData(CacheService.PROJECT_METRICS)).thenReturn(null);

        // Mock AnalysisScope and DumbService
        try (MockedStatic<DumbService> mockedDumbService = mockStatic(DumbService.class);
             MockedStatic<AnalysisScope> mockedAnalysisScope = mockStatic(AnalysisScope.class)) {

            DumbService mockDumbService = mock(DumbService.class);
            doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(0);
                runnable.run(); // Directly run the runnable to simulate immediate execution
                return null;
            }).when(mockDumbService).runWhenSmart(any(Runnable.class));
            mockedDumbService.when(() -> DumbService.getInstance(any())).thenReturn(mockDumbService);

            AnalysisScope mockAnalysisScope = mock(AnalysisScope.class);
            mockedAnalysisScope.when(() -> new AnalysisScope(any(Project.class))).thenReturn(mockAnalysisScope);

            // Mock Calculators and their return values
            try (MockedStatic<DependenciesCalculator> mockedDependenciesCalculator = mockStatic(DependenciesCalculator.class);
                 MockedStatic<ClassAndMethodsMetricsCalculator> mockedClassAndMethodsMetricsCalculator = mockStatic(ClassAndMethodsMetricsCalculator.class);
                 MockedStatic<PackageMetricsSetCalculator> mockedPackageMetricsSetCalculator = mockStatic(PackageMetricsSetCalculator.class);
                 MockedStatic<ProjectMetricsSetCalculator> mockedProjectMetricsSetCalculator = mockStatic(ProjectMetricsSetCalculator.class)) {

                DependenciesCalculator mockDependenciesCalculator = mock(DependenciesCalculator.class);
                DependenciesBuilder mockDependenciesBuilder = mock(DependenciesBuilder.class);
                when(mockDependenciesCalculator.calculateDependencies()).thenReturn(mockDependenciesBuilder);
                mockedDependenciesCalculator.when(() -> new DependenciesCalculator(any(), any())).thenReturn(mockDependenciesCalculator);

                ClassAndMethodsMetricsCalculator mockClassAndMethodsMetricsCalculator = mock(ClassAndMethodsMetricsCalculator.class);
                JavaProject mockClassAndMethodsJavaProject = mock(JavaProject.class);
                when(mockClassAndMethodsMetricsCalculator.calculateMetrics()).thenReturn(mockClassAndMethodsJavaProject);
                mockedClassAndMethodsMetricsCalculator.when(() -> new ClassAndMethodsMetricsCalculator(any(), any())).thenReturn(mockClassAndMethodsMetricsCalculator);

                PackageMetricsSetCalculator mockPackageMetricsSetCalculator = mock(PackageMetricsSetCalculator.class);
                // calculate() returns void, so just verify interaction
                mockedPackageMetricsSetCalculator.when(() -> new PackageMetricsSetCalculator(any(), any(), any())).thenReturn(mockPackageMetricsSetCalculator);

                ProjectMetricsSetCalculator mockProjectMetricsSetCalculator = mock(ProjectMetricsSetCalculator.class);
                // calculate() returns void, so just verify interaction
                mockedProjectMetricsSetCalculator.when(() -> new ProjectMetricsSetCalculator(any(), any(), any())).thenReturn(mockProjectMetricsSetCalculator);

                // Call a public method that triggers the dependency chain
                calculationService.exportToXml("test.xml");

                // Verify that tasks are queued
                verify(mockTaskQueueService, times(1)).queue(projectTreeTaskCaptor.capture());
                MetricsBackgroundableTask capturedTask = projectTreeTaskCaptor.getValue();
                capturedTask.run(mockProgressIndicator); // Simulate task execution

                // Verify that each model is built and cached in order
                verify(mockDependenciesCalculator, times(1)).calculateDependencies();
                verify(mockCacheService, times(1)).putUserData(eq(CacheService.DEPENDENCIES), eq(mockDependenciesBuilder));

                verify(mockClassAndMethodsMetricsCalculator, times(1)).calculateMetrics();
                verify(mockCacheService, times(1)).putUserData(eq(CacheService.CLASS_AND_METHODS_METRICS), eq(mockClassAndMethodsJavaProject));

                verify(mockPackageMetricsSetCalculator, times(1)).calculate();
                verify(mockCacheService, times(1)).putUserData(eq(CacheService.PACKAGE_METRICS), any(JavaProject.class)); // PackageMetricsSetCalculator modifies the passed JavaProject

                verify(mockProjectMetricsSetCalculator, times(1)).calculate();
                verify(mockCacheService, times(1)).putUserData(eq(CacheService.PROJECT_METRICS), any(JavaProject.class)); // ProjectMetricsSetCalculator modifies the passed JavaProject
            }
        }
    }
}
