package org.b333vv.metric.model.code;

import com.intellij.psi.PsiClass;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith; // Added
import org.mockito.Mock;
import org.mockito.MockitoAnnotations; // Will be removed by logic below
import org.mockito.junit.jupiter.MockitoExtension; // Added

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Added
public class JavaProjectTest {

    private JavaProject javaProject;
    private final String projectName = "TestProject";

    @Mock
    private PsiClass mockPsiClass1;
    @Mock
    private PsiClass mockPsiClass2;

    @BeforeEach
    void setUp() {
        // MockitoAnnotations.openMocks(this); // Removed
        javaProject = new JavaProject(projectName); // Corrected constructor
    }

    // 1. Constructor and `getName()`
    @Test
    void testConstructorAndGetName() {
        assertEquals(projectName, javaProject.getName());
    }

    // 2. `addPackage()` and `packages()` (top-level packages)
    @Test
    void testAddPackageAndPackages() {
        assertTrue(javaProject.packages().collect(Collectors.toList()).isEmpty(), "Initially, packages() stream should be empty.");

        // PsiPackage mocks for JavaPackage constructor if needed, or null if PsiPackage is optional for the test's purpose
        JavaPackage pkgA = new JavaPackage("com.a", null); // Corrected constructor
        JavaPackage pkgC = new JavaPackage("com.c", null); // Corrected constructor
        JavaPackage pkgB = new JavaPackage("com.b", null); // Corrected constructor

        javaProject.addPackage(pkgA); // This adds to JavaCode.children, used by packages()
        javaProject.addPackage(pkgC);
        javaProject.addPackage(pkgB);

        List<JavaPackage> expectedPackages = Arrays.asList(pkgA, pkgB, pkgC);
        expectedPackages.sort(Comparator.comparing(JavaCode::getName));

        List<JavaPackage> actualPackages = javaProject.packages().collect(Collectors.toList());
        assertIterableEquals(expectedPackages, actualPackages, "Top-level packages should match and be in sorted order by name.");
    }

    // 3. `putToAllPackages()`, `getFromAllPackages()`, `removeFromAllPackages()`, `allPackagesIsEmpty()`, `allPackages()`
    @Test
    void testGlobalPackageManagement() {
        assertTrue(javaProject.allPackagesIsEmpty(), "allPackagesIsEmpty should be true initially.");
        assertTrue(javaProject.allPackages().collect(Collectors.toList()).isEmpty(), "allPackages() stream should be empty initially.");

        JavaPackage pkg1 = new JavaPackage("com.example.one", null); // Corrected constructor
        JavaPackage pkg2 = new JavaPackage("com.example.two", null); // Corrected constructor

        javaProject.putToAllPackages("com.example.one", pkg1);
        javaProject.putToAllPackages("com.example.two", pkg2);

        assertFalse(javaProject.allPackagesIsEmpty(), "allPackagesIsEmpty should be false after adding packages.");
        assertEquals(pkg1, javaProject.getFromAllPackages("com.example.one"));
        assertEquals(pkg2, javaProject.getFromAllPackages("com.example.two"));
        assertNull(javaProject.getFromAllPackages("com.example.nonexistent"));

        Set<JavaPackage> expectedAllPackages = new HashSet<>(Arrays.asList(pkg1, pkg2));
        Set<JavaPackage> actualAllPackages = javaProject.allPackages().collect(Collectors.toSet());
        assertEquals(expectedAllPackages, actualAllPackages, "allPackages() should contain all globally added packages.");

        javaProject.removeFromAllPackages("com.example.one");
        assertNull(javaProject.getFromAllPackages("com.example.one"));
        assertFalse(javaProject.allPackagesIsEmpty()); // pkg2 should still be there
        assertEquals(1, javaProject.allPackages().count());

        javaProject.removeFromAllPackages("com.example.two");
        assertNull(javaProject.getFromAllPackages("com.example.two"));
        assertTrue(javaProject.allPackagesIsEmpty(), "allPackagesIsEmpty should be true after removing all packages.");
        assertTrue(javaProject.allPackages().collect(Collectors.toList()).isEmpty(), "allPackages() stream should be empty after removing all.");
    }

    // 4. `addToAllClasses()`, `allClasses()` (flat collection of all classes)
    @Test
    void testGlobalClassManagement() {
        assertTrue(javaProject.allClasses().collect(Collectors.toList()).isEmpty(), "Initially, allClasses() stream should be empty.");

        when(mockPsiClass1.getName()).thenReturn("ClassA");
        JavaClass classA = new JavaClass(mockPsiClass1); // Corrected constructor

        when(mockPsiClass2.getName()).thenReturn("ClassB");
        JavaClass classB = new JavaClass(mockPsiClass2); // Corrected constructor

        javaProject.addToAllClasses(classA);
        javaProject.addToAllClasses(classB);

        Set<JavaClass> expectedClasses = new HashSet<>(Arrays.asList(classA, classB));
        Set<JavaClass> actualClasses = javaProject.allClasses().collect(Collectors.toSet());
        assertEquals(expectedClasses, actualClasses, "allClasses() should contain all globally added classes.");

        // Test clearing or removing (if such functionality existed directly)
        // For now, just testing adding and getting all.
        // If there was a removeAllClasses or similar, it would be tested here.
        // Let's test that adding the same class again doesn't duplicate if it's a Set based collection internally.
        javaProject.addToAllClasses(classA);
        assertEquals(2, javaProject.allClasses().count(), "Adding same class instance again should not increase count if backed by a Set.");
    }

    @Test
    void testAllClassesEmpty() {
         assertTrue(javaProject.allClasses().collect(Collectors.toList()).isEmpty(), "allClasses() stream should be empty when no classes are added.");
    }


    // 5. Metric Management (inherited)
    @Test
    void testMetricManagement() {
        Metric metricMHF = Metric.of(MetricType.MHF, 1000L); // Corrected: NOF -> MHF
        javaProject.addMetric(metricMHF);
        assertEquals(metricMHF, javaProject.metric(MetricType.MHF));

        assertNull(javaProject.metric(MetricType.LOC));

        Metric metricNOL = Metric.of(MetricType.NOL, Value.of(50000L)); // Number of Lines
        javaProject.addMetric(metricNOL);

        List<Metric> expectedMetrics = Arrays.asList(metricMHF, metricNOL); // MHF before NOL (depends on enum name order)
        expectedMetrics.sort(Comparator.comparing(m -> m.getType().name()));

        List<Metric> actualMetrics = javaProject.metrics().collect(Collectors.toList());
        assertIterableEquals(expectedMetrics, actualMetrics);

        javaProject.removeMetric(MetricType.MHF);
        assertNull(javaProject.metric(MetricType.MHF));
        assertEquals(1, javaProject.metrics().count());
        assertEquals(metricNOL, javaProject.metrics().findFirst().get());

        javaProject.removeMetric(MetricType.NOL);
        assertTrue(javaProject.metrics().collect(Collectors.toList()).isEmpty());
    }
}
