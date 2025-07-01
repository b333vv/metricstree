package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SortedClassesTreeModelCalculatorTest {

    private Project mockProject;
    private JavaProject mockJavaProject;
    private SortedClassesTreeModelCalculator calculator;

    @BeforeEach
    void setUp() {
        mockProject = mock(Project.class);
        mockJavaProject = mock(JavaProject.class);
        calculator = new SortedClassesTreeModelCalculator();
    }

    @Test
    void calculateReturnsDefaultTreeModel() {
        DefaultTreeModel model = calculator.calculate(mockJavaProject, mockProject);
        assertNotNull(model);
        assertTrue(model.getRoot() instanceof DefaultMutableTreeNode);
    }

    @Test
    void calculateWithClassesAndMetrics() {
        PsiClass mockPsiClassA = mock(PsiClass.class);
        when(mockPsiClassA.getName()).thenReturn("ClassA");
        JavaClass classA = new JavaClass(mockPsiClassA);

        PsiClass mockPsiClassB = mock(PsiClass.class);
        when(mockPsiClassB.getName()).thenReturn("ClassB");
        JavaClass classB = new JavaClass(mockPsiClassB);

        Metric metricA = Metric.of(MetricType.ATFD, Value.of(10.0));
        Metric metricB = Metric.of(MetricType.ATFD, Value.of(5.0));

        classA.addMetric(metricA);
        classB.addMetric(metricB);

        when(mockJavaProject.allClasses()).thenReturn(java.util.stream.Stream.of(classA, classB));

        DefaultTreeModel model = calculator.calculate(mockJavaProject, mockProject);
        assertNotNull(model);

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        assertEquals("Classes", root.getUserObject());
        assertEquals(2, root.getChildCount());

        DefaultMutableTreeNode child1 = (DefaultMutableTreeNode) root.getChildAt(0);
        DefaultMutableTreeNode child2 = (DefaultMutableTreeNode) root.getChildAt(1);

        // Assuming the builder sorts in descending order of metric value
        assertEquals("ClassA (ATFD: 10.0)", child1.getUserObject());
        assertEquals("ClassB (ATFD: 5.0)", child2.getUserObject());
    }

    @Test
    void calculateWithNoClasses() {
        when(mockJavaProject.allClasses()).thenReturn(java.util.stream.Stream.of());

        DefaultTreeModel model = calculator.calculate(mockJavaProject, mockProject);
        assertNotNull(model);

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        assertEquals("Classes", root.getUserObject());
        assertEquals(0, root.getChildCount());
    }
}
