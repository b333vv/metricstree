package org.b333vv.metric.model.code;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.JavaClassVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JavaClassTest {

    @Mock
    private PsiClass mockPsiClass;
    @Mock
    private PsiClass mockPsiClass2;
    @Mock
    private JavaCode mockParent; // Assuming JavaFile or another JavaClass, not critical for most tests

    private JavaClass javaClass;
    private final String className = "TestClass";
    private final int startLine = 1;
    private final int endLine = 100;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockPsiClass.getName()).thenReturn(className);
        javaClass = new JavaClass(mockPsiClass, mockParent, startLine, endLine);
    }

    // 1. Constructor and `getName()`
    @Test
    void testConstructorAndGetName() {
        assertEquals(className, javaClass.getName(), "Class name should be correctly set from PsiClass.");
    }

    @Test
    void testConstructorWithNullPsiClassName() {
        // Objects.requireNonNull(psiClass.getName(), ...) in JavaClass constructor implies this shouldn't happen
        // or PsiClass.getName() itself has a @NotNull contract usually.
        // If psiClass.getName() could be null and not caught by Objects.requireNonNull,
        // then the behavior of JavaClass.getName() would depend on JavaCode.name default or handling.
        // For now, assuming PsiClass.getName() is expected to be non-null by contract or by Psi...
        when(mockPsiClass.getName()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> new JavaClass(mockPsiClass, mockParent, startLine, endLine),
                "Constructor should throw NullPointerException if psiClass.getName() is null due to Objects.requireNonNull.");
    }


    // 2. `getPsiClass()`
    @Test
    void testGetPsiClass() {
        assertEquals(mockPsiClass, javaClass.getPsiClass(), "getPsiClass() should return the PsiClass instance passed to constructor.");
    }

    // 3. `addMethod()` and `methods()`
    @Test
    void testAddMethodAndMethods() {
        assertTrue(javaClass.methods().collect(Collectors.toList()).isEmpty(), "Initially, methods stream should be empty.");

        JavaMethod methodA = mock(JavaMethod.class);
        when(methodA.getName()).thenReturn("methodA");
        JavaMethod methodC = mock(JavaMethod.class);
        when(methodC.getName()).thenReturn("methodC");
        JavaMethod methodB = mock(JavaMethod.class);
        when(methodB.getName()).thenReturn("methodB");

        javaClass.addMethod(methodA);
        javaClass.addMethod(methodC);
        javaClass.addMethod(methodB);

        List<JavaMethod> expectedMethods = Arrays.asList(methodA, methodB, methodC);
        // Sorting by name for comparison
        expectedMethods.sort(Comparator.comparing(JavaMethod::getName));


        List<JavaMethod> actualMethods = javaClass.methods().collect(Collectors.toList());

        assertEquals(expectedMethods.size(), actualMethods.size(), "Number of methods should match.");
        assertIterableEquals(expectedMethods, actualMethods, "Methods should match and be in sorted order by name.");
    }


    // 4. `addClass()` (for inner classes) and `innerClasses()`
    @Test
    void testAddInnerClassAndInnerClasses() {
        assertTrue(javaClass.innerClasses().collect(Collectors.toList()).isEmpty(), "Initially, innerClasses stream should be empty.");

        JavaClass innerA = mock(JavaClass.class);
        when(innerA.getName()).thenReturn("InnerA");
        JavaClass innerC = mock(JavaClass.class);
        when(innerC.getName()).thenReturn("InnerC");
        JavaClass innerB = mock(JavaClass.class);
        when(innerB.getName()).thenReturn("InnerB");

        javaClass.addClass(innerA); // addClass is used for inner classes
        javaClass.addClass(innerC);
        javaClass.addClass(innerB);

        List<JavaClass> expectedInnerClasses = Arrays.asList(innerA, innerB, innerC);
        expectedInnerClasses.sort(Comparator.comparing(JavaClass::getName));

        List<JavaClass> actualInnerClasses = javaClass.innerClasses().collect(Collectors.toList());

        assertEquals(expectedInnerClasses.size(), actualInnerClasses.size(), "Number of inner classes should match.");
        assertIterableEquals(expectedInnerClasses, actualInnerClasses, "Inner classes should match and be in sorted order by name.");
    }

    // 5. `equals()` and `hashCode()`
    @Test
    void testEqualsAndHashCode() {
        // javaClass is (mockPsiClass, className)
        JavaClass javaClassSame = new JavaClass(mockPsiClass, mockParent, startLine, endLine); // Same PsiClass, same name

        when(mockPsiClass2.getName()).thenReturn(className); // Different PsiClass, same name
        JavaClass javaClassDifferentPsi = new JavaClass(mockPsiClass2, mockParent, startLine, endLine);

        when(mockPsiClass2.getName()).thenReturn("AnotherName"); // Different PsiClass, different name
        JavaClass javaClassDifferentPsiAndName = new JavaClass(mockPsiClass2, mockParent, startLine, endLine);

        // Reflexivity
        assertEquals(javaClass, javaClass);

        // Symmetry & same PsiClass
        assertEquals(javaClass, javaClassSame);
        assertEquals(javaClassSame, javaClass);
        assertEquals(javaClass.hashCode(), javaClassSame.hashCode(), "Hashcodes for same PsiClass should be equal.");

        // Different PsiClass, even if name might be the same from stub
        assertNotEquals(javaClass, javaClassDifferentPsi, "Different PsiClass instances should make JavaClass instances not equal.");
        // Hashcodes for different PsiClass may or may not be different, not strictly asserted.

        assertNotEquals(javaClass, javaClassDifferentPsiAndName);

        // Test with null
        assertNotEquals(null, javaClass);

        // Test with different object type
        assertNotEquals("TestClass", javaClass);
    }

    // 6. `accept()` method
    @Test
    void testAcceptMethodWithJavaClassVisitor() {
        JavaClassVisitor mockVisitor = mock(JavaClassVisitor.class);
        javaClass.accept(mockVisitor);
        verify(mockVisitor, times(1)).visitJavaClass(javaClass);
        verifyNoMoreInteractions(mockVisitor); // Ensure no other visit methods are called
    }

    @Test
    void testAcceptMethodWithGenericPsiElementVisitor() {
        PsiElementVisitor genericVisitor = mock(PsiElementVisitor.class);
        javaClass.accept(genericVisitor);
        // A generic PsiElementVisitor should not have its specific visitJavaClass (if it had one) called by JavaCode.accept
        // JavaCode.accept(PsiElementVisitor) calls visitor.visitElement(this.psiClass)
        // So, we expect visitElement on the underlying psiClass to be called.
        verify(genericVisitor, times(1)).visitElement(mockPsiClass);
        verifyNoMoreInteractions(genericVisitor);
    }


    // 7. Metric Management (inherited)
    @Test
    void testMetricManagement() {
        // Add
        Metric metricLOC = Metric.of(MetricType.LOC, 100L);
        javaClass.addMetric(metricLOC);
        assertEquals(metricLOC, javaClass.metric(MetricType.LOC));

        // Get non-existent
        assertNull(javaClass.metric(MetricType.WMC));

        // Get all
        Metric metricLCOM = Metric.of(MetricType.LCOM, Value.of(5));
        javaClass.addMetric(metricLCOM);
        List<Metric> expectedMetrics = Arrays.asList(metricLCOM, metricLOC); // LCOM before LOC when sorted by type name
        expectedMetrics.sort(Comparator.comparing(m -> m.getType().name()));

        List<Metric> actualMetrics = javaClass.metrics().collect(Collectors.toList());
        assertIterableEquals(expectedMetrics, actualMetrics, "Metrics list should be sorted by type name.");

        // Remove
        javaClass.removeMetric(MetricType.LOC);
        assertNull(javaClass.metric(MetricType.LOC));
        assertEquals(1, javaClass.metrics().count());
        assertEquals(metricLCOM, javaClass.metrics().findFirst().get());

        // Get all empty
        javaClass.removeMetric(MetricType.LCOM);
        assertTrue(javaClass.metrics().collect(Collectors.toList()).isEmpty());
    }
}
