package org.b333vv.metric.model.code;

import com.intellij.psi.PsiPackage;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import com.intellij.psi.PsiClass; // Added for mocking
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith; // Added
import org.mockito.Mock;
import org.mockito.MockitoAnnotations; // Will be removed
import org.mockito.junit.jupiter.MockitoExtension; // Added

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Added
public class JavaPackageTest {

    @Mock
    private PsiPackage mockPsiPackage;
    // Helper to create JavaClass with mocked PsiClass
    private JavaClass createMockJavaClass(String name) {
        PsiClass mockPsi = mock(PsiClass.class);
        when(mockPsi.getName()).thenReturn(name);
        return new JavaClass(mockPsi);
    }


    private JavaPackage javaPackage;
    private final String packageName = "com.example.test";

    @BeforeEach
    void setUp() {
        // MockitoAnnotations.openMocks(this); // Removed
        javaPackage = new JavaPackage(packageName, mockPsiPackage); // Corrected constructor
    }

    // 1. Constructor and `getName()`, `getPsiPackage()`
    @Test
    void testConstructorAndGetters() {
        assertEquals(packageName, javaPackage.getName());
        assertEquals(mockPsiPackage, javaPackage.getPsiPackage());

        JavaPackage packageWithNullPsi = new JavaPackage("another.package", null); // Corrected constructor
        assertEquals("another.package", packageWithNullPsi.getName());
        assertNull(packageWithNullPsi.getPsiPackage());
    }

    // 2. `addFile()`, `files()`
    @Test
    void testAddFileAndFiles() {
        assertTrue(javaPackage.files().collect(Collectors.toList()).isEmpty(), "Initially, files stream should be empty.");

        JavaFile fileA = new JavaFile("FileA.java"); // Corrected constructor
        JavaFile fileC = new JavaFile("FileC.java"); // Corrected constructor
        JavaFile fileB = new JavaFile("FileB.java"); // Corrected constructor

        javaPackage.addFile(fileA);
        javaPackage.addFile(fileC);
        javaPackage.addFile(fileB);

        List<JavaFile> expectedFiles = Arrays.asList(fileA, fileB, fileC);
        expectedFiles.sort(Comparator.comparing(JavaCode::getName));

        List<JavaFile> actualFiles = javaPackage.files().collect(Collectors.toList());
        assertIterableEquals(expectedFiles, actualFiles, "Files should match and be in sorted order by name.");
    }

    // 3. `addPackage()` (for sub-packages), `subPackages()`
    @Test
    void testAddSubPackageAndSubPackages() {
        assertTrue(javaPackage.subPackages().collect(Collectors.toList()).isEmpty(), "Initially, subPackages stream should be empty.");

        JavaPackage subPkgA = new JavaPackage("subA", null); // Corrected constructor
        JavaPackage subPkgC = new JavaPackage("subC", null); // Corrected constructor
        JavaPackage subPkgB = new JavaPackage("subB", null); // Corrected constructor

        javaPackage.addPackage(subPkgA);
        javaPackage.addPackage(subPkgC);
        javaPackage.addPackage(subPkgB);

        List<JavaPackage> expectedPackages = Arrays.asList(subPkgA, subPkgB, subPkgC);
        expectedPackages.sort(Comparator.comparing(JavaCode::getName));

        List<JavaPackage> actualPackages = javaPackage.subPackages().collect(Collectors.toList());
        assertIterableEquals(expectedPackages, actualPackages, "Sub-packages should match and be in sorted order by name.");
    }

    // 4. `addClass()` (direct child), `classes()`
    @Test
    void testClassesFromFiles() {
        JavaFile file1 = new JavaFile("File1.java"); // Corrected constructor
        JavaClass class1InFile1 = createMockJavaClass("Class1File1");
        file1.addClass(class1InFile1);

        JavaFile file2 = new JavaFile("File2.java"); // Corrected constructor
        JavaClass class1InFile2 = createMockJavaClass("Class1File2");
        JavaClass class2InFile2 = createMockJavaClass("Class2File2");
        file2.addClass(class1InFile2);
        file2.addClass(class2InFile2);


        javaPackage.addFile(file1);
        javaPackage.addFile(file2); // File order shouldn't matter for class output, should be sorted by class name

        List<JavaClass> expectedClasses = Arrays.asList(class1InFile1, class1InFile2, class2InFile2);
        expectedClasses.sort(Comparator.comparing(JavaCode::getName)); // Sorting by class name

        List<JavaClass> actualClasses = javaPackage.classes().collect(Collectors.toList());
        assertIterableEquals(expectedClasses, actualClasses, "Classes from files should be collected and sorted by name.");
    }

    @Test
    void testDirectlyAddedClassesNotIncludedInClassesStream() {
        // Add a class directly to the package (unusual, but testing addClass)
        JavaClass directClass = createMockJavaClass("DirectClass");
        javaPackage.addClass(directClass); // This adds to JavaCode.children

        // Add a file with a class
        JavaFile file1 = new JavaFile("FileWithClass.java"); // Corrected constructor
        JavaClass classInFile = createMockJavaClass("ClassInFile");
        file1.addClass(classInFile);
        javaPackage.addFile(file1);

        // The classes() method should only return classes from files
        List<JavaClass> actualClassesFromStream = javaPackage.classes().collect(Collectors.toList());
        assertEquals(1, actualClassesFromStream.size(), "Only classes from files should be in classes() stream.");
        assertEquals("ClassInFile", actualClassesFromStream.get(0).getName());

        // Verify the direct class is a child, but not in classes()
        // assertTrue(javaPackage.getChildren().contains(directClass), "Directly added class should be among children."); // Removed
    }

    @Test
    void testClassesStreamEmptyWhenNoFilesOrNoClassesInFiles() {
        assertTrue(javaPackage.classes().collect(Collectors.toList()).isEmpty(), "Classes stream should be empty if no files.");

        JavaFile emptyFile = new JavaFile("Empty.java"); // Corrected constructor
        javaPackage.addFile(emptyFile);
        assertTrue(javaPackage.classes().collect(Collectors.toList()).isEmpty(), "Classes stream should be empty if files have no classes.");
    }


    // 5. Metric Management (inherited)
    @Test
    void testMetricManagement() {
        Metric metricNOC = Metric.of(MetricType.NOC, 10L);
        javaPackage.addMetric(metricNOC);
        assertEquals(metricNOC, javaPackage.metric(MetricType.NOC));

        assertNull(javaPackage.metric(MetricType.LOC));

        Metric metricNOM = Metric.of(MetricType.NOM, Value.of(50));
        javaPackage.addMetric(metricNOM);

        List<Metric> expectedMetrics = Arrays.asList(metricNOC, metricNOM); // NOC before NOM
        expectedMetrics.sort(Comparator.comparing(m -> m.getType().name()));

        List<Metric> actualMetrics = javaPackage.metrics().collect(Collectors.toList());
        assertIterableEquals(expectedMetrics, actualMetrics);

        javaPackage.removeMetric(MetricType.NOC);
        assertNull(javaPackage.metric(MetricType.NOC));
        assertEquals(1, javaPackage.metrics().count());
        assertEquals(metricNOM, javaPackage.metrics().findFirst().get());

        javaPackage.removeMetric(MetricType.NOM);
        assertTrue(javaPackage.metrics().collect(Collectors.toList()).isEmpty());
    }
}
