package org.b333vv.metric.model.code;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import com.intellij.psi.PsiClass; // Added for mocking
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// Mockito imports needed for createMockJavaClass helper
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class JavaFileTest {

    private JavaFile javaFile;
    private final String fileName = "TestFile.java";

    // Helper to create JavaClass with mocked PsiClass
    private JavaClass createMockJavaClass(String name) {
        PsiClass mockPsi = mock(PsiClass.class);
        when(mockPsi.getName()).thenReturn(name);
        return new JavaClass(mockPsi);
    }

    @BeforeEach
    void setUp() {
        javaFile = new JavaFile(fileName); // Corrected constructor
    }

    // 1. Constructor and `getName()`
    @Test
    void testConstructorAndGetName() {
        assertEquals(fileName, javaFile.getName(), "File name should be correctly set and retrieved.");
    }

    // 2. `addClass()` and `classes()`
    @Test
    void testAddClassAndClasses() {
        // Test with no classes
        assertTrue(javaFile.classes().collect(Collectors.toList()).isEmpty(), "Initially, classes stream should be empty.");

        // Add classes
        JavaClass classA = createMockJavaClass("ClassA");
        JavaClass classC = createMockJavaClass("ClassC");
        JavaClass classB = createMockJavaClass("ClassB");

        javaFile.addClass(classA);
        javaFile.addClass(classC);
        javaFile.addClass(classB);

        List<JavaClass> expectedClasses = Arrays.asList(classA, classB, classC);
        expectedClasses.sort(Comparator.comparing(JavaCode::getName)); // Ensure expected list is sorted by name

        List<JavaClass> actualClasses = javaFile.classes().collect(Collectors.toList());

        assertEquals(expectedClasses.size(), actualClasses.size(), "Number of classes should match.");
        for (int i = 0; i < expectedClasses.size(); i++) {
            assertEquals(expectedClasses.get(i).getName(), actualClasses.get(i).getName(),
                         "Class names should match and be in sorted order.");
        }
    }

    @Test
    void testClassesReturnsEmptyStreamWhenNoClassesAdded() {
        assertTrue(javaFile.classes().findAny().isEmpty(), "classes() should return an empty stream if no classes are added.");
    }

    // 3. Metric Management (inherited from JavaCode)
    @Test
    void testAddAndGetMetric() {
        Metric metric1 = Metric.of(MetricType.LOC, 100L);
        javaFile.addMetric(metric1);

        assertEquals(metric1, javaFile.metric(MetricType.LOC), "Retrieved metric should be the one added.");
    }

    @Test
    void testGetMetricNotPresent() {
        assertNull(javaFile.metric(MetricType.LCOM), "Querying a non-existent metric should return null.");
    }

    @Test
    void testGetAllMetrics() {
        Metric metricLOC = Metric.of(MetricType.LOC, 150L);
        Metric metricLCOM = Metric.of(MetricType.LCOM, 5L);
        // Add in non-alphabetical order of type to test sorting
        javaFile.addMetric(metricLOC);
        javaFile.addMetric(metricLCOM);


        List<Metric> expectedMetrics = Arrays.asList(metricLCOM, metricLOC); // Sorted by MetricType name
        // Sort expectedMetrics by MetricType name for reliable comparison
         expectedMetrics.sort(Comparator.comparing(m -> m.getType().name()));


        List<Metric> actualMetrics = javaFile.metrics().collect(Collectors.toList());

        assertEquals(expectedMetrics.size(), actualMetrics.size(), "Number of metrics should match.");
        for (int i = 0; i < expectedMetrics.size(); i++) {
            assertEquals(expectedMetrics.get(i), actualMetrics.get(i),
                         "Metrics should match and be in sorted order by type name.");
        }
    }

    @Test
    void testGetAllMetricsWhenEmpty() {
        assertTrue(javaFile.metrics().collect(Collectors.toList()).isEmpty(), "metrics() stream should be empty when no metrics are added.");
    }

    @Test
    void testRemoveMetric() {
        Metric metricWMC = Metric.of(MetricType.WMC, 20L);
        javaFile.addMetric(metricWMC);
        assertEquals(metricWMC, javaFile.metric(MetricType.WMC), "Metric should be present before removal.");

        javaFile.removeMetric(MetricType.WMC);
        assertNull(javaFile.metric(MetricType.WMC), "Metric should be null after removal.");
        assertTrue(javaFile.metrics().collect(Collectors.toList()).isEmpty(),
                   "Metrics stream should be empty after removing the only metric.");

        // Test removing a non-existent metric (should not throw error)
        javaFile.removeMetric(MetricType.RFC); // Should do nothing
        assertTrue(javaFile.metrics().collect(Collectors.toList()).isEmpty(),
                   "Metrics stream should still be empty after attempting to remove a non-existent metric.");
    }
}
