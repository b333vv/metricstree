package org.b333vv.metric.builder;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.task.MetricTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.tree.DefaultTreeModel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProjectTreeModelCalculatorTest {

    @Mock
    private Project mockProject;

    @Mock
    private MetricTaskManager mockMetricTaskManager;

    @Mock
    private JavaProject mockJavaProject;

    private ProjectTreeModelCalculator calculator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockProject.getService(MetricTaskManager.class)).thenReturn(mockMetricTaskManager);
        when(mockMetricTaskManager.getProjectModel(any(ProgressIndicator.class))).thenReturn(mockJavaProject);
        calculator = new ProjectTreeModelCalculator(mockProject);
    }

    @Test
    public void testCalculate() {
        DefaultTreeModel treeModel = calculator.calculate();
        assertNotNull(treeModel);
        assertNotNull(treeModel.getRoot());
    }
}
