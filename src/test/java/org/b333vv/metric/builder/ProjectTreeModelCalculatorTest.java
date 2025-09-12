package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.ProjectElement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.b333vv.metric.service.CacheService;

import javax.swing.tree.DefaultTreeModel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProjectTreeModelCalculatorTest {

    @Mock
    private Project mockProject;

    @Mock
    private ProjectElement mockJavaProject;

    private ProjectTreeModelCalculator calculator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(mockProject.getService(CacheService.class).getProject()).thenReturn(mockJavaProject);
        calculator = new ProjectTreeModelCalculator(mockProject);
    }

    @Test
    public void testCalculate() {
        DefaultTreeModel treeModel = calculator.calculate();
        assertNotNull(treeModel);
        assertNotNull(treeModel.getRoot());
    }
}
