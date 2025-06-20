package org.b333vv.metric.model.code;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.java.PsiMethodImpl; // A concrete class for mocking if needed, or just PsiMethod
import com.intellij.psi.util.MethodSignature;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.JavaMethodVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JavaMethodTest {

    @Mock
    private PsiMethod mockPsiMethod;
    @Mock
    private PsiMethod mockPsiMethod2;
    @Mock
    private JavaClass mockJavaClass; // Parent class
    @Mock
    private MethodSignature mockMethodSignature;

    private JavaMethod javaMethod;
    private final String methodName = "testMethod";
    private final int startLine = 5;
    private final int endLine = 25;

    @BeforeEach
    void setUp() {
        // MockitoAnnotations.openMocks(this); // Removed

        // Common setup for PsiMethod needed by JavaMethod constructor via JavaMethod.signature()
        when(mockPsiMethod.getName()).thenReturn(methodName);
        when(mockPsiMethod.getSignature(PsiSubstitutor.EMPTY)).thenReturn(mockMethodSignature);
        when(mockMethodSignature.getParameterTypes()).thenReturn(PsiType.EMPTY_ARRAY); // Default to no params

        // javaMethod instance for some tests, others will create their own
        javaMethod = new JavaMethod(mockPsiMethod, mockJavaClass, startLine, endLine);
    }

    // 1. `signature()` static method
    @Test
    void testSignatureStaticMethodNoParams() {
        // Setup already done in @BeforeEach for mockPsiMethod
        String expectedSignature = methodName + "()";
        assertEquals(expectedSignature, JavaMethod.signature(mockPsiMethod));
    }

    @Test
    void testSignatureStaticMethodWithParams() {
        PsiType mockParamType1 = mock(PsiType.class);
        PsiType mockParamType2 = mock(PsiType.class);
        when(mockParamType1.getPresentableText()).thenReturn("String");
        when(mockParamType2.getPresentableText()).thenReturn("int");
        when(mockMethodSignature.getParameterTypes()).thenReturn(new PsiType[]{mockParamType1, mockParamType2});

        String expectedSignature = methodName + "(String, int)";
        assertEquals(expectedSignature, JavaMethod.signature(mockPsiMethod));
    }

    @Test
    void testSignatureStaticMethodWithSingleParam() {
        PsiType mockParamType = mock(PsiType.class);
        when(mockParamType.getPresentableText()).thenReturn("boolean");
        when(mockMethodSignature.getParameterTypes()).thenReturn(new PsiType[]{mockParamType});

        String expectedSignature = methodName + "(boolean)";
        assertEquals(expectedSignature, JavaMethod.signature(mockPsiMethod));
    }


    // 2. Constructor, `getName()`, `getPsiMethod()`, `getJavaClass()`
    @Test
    void testConstructorAndGetters() {
        // javaMethod is constructed in @BeforeEach with no params signature
        String expectedSignature = methodName + "()";
        assertEquals(expectedSignature, javaMethod.getName(), "getName() should return the method signature.");
        assertEquals(mockPsiMethod, javaMethod.getPsiMethod(), "getPsiMethod() should return the PsiMethod instance.");
        assertEquals(mockJavaClass, javaMethod.getJavaClass(), "getJavaClass() should return the parent JavaClass instance.");
    }

    // 3. `equals()` and `hashCode()`
    @Test
    void testEqualsAndHashCode() {
        // javaMethod uses mockPsiMethod, methodName + "()"
        JavaMethod javaMethodSamePsi = new JavaMethod(mockPsiMethod, mockJavaClass, startLine, endLine);

        // Different PsiMethod, but we'll mock it to produce the same signature
        when(mockPsiMethod2.getName()).thenReturn(methodName);
        MethodSignature mockMethodSignature2 = mock(MethodSignature.class);
        when(mockMethodSignature2.getParameterTypes()).thenReturn(PsiType.EMPTY_ARRAY);
        when(mockPsiMethod2.getSignature(PsiSubstitutor.EMPTY)).thenReturn(mockMethodSignature2);
        JavaMethod javaMethodDifferentPsiSameSig = new JavaMethod(mockPsiMethod2, mockJavaClass, startLine, endLine);

        // Different PsiMethod, different signature (name)
        when(mockPsiMethod2.getName()).thenReturn("anotherMethod");
        // Signature will be anotherMethod()
        JavaMethod javaMethodDifferentPsiDifferentSig = new JavaMethod(mockPsiMethod2, mockJavaClass, startLine, endLine);

        // Reflexivity
        assertEquals(javaMethod, javaMethod);

        // Symmetry: same PsiMethod instance implies equals
        assertEquals(javaMethod, javaMethodSamePsi);
        assertEquals(javaMethodSamePsi, javaMethod);
        assertEquals(javaMethod.hashCode(), javaMethodSamePsi.hashCode());

        // Different PsiMethod instances should not be equal, even if signature is the same
        assertNotEquals(javaMethod, javaMethodDifferentPsiSameSig, "Different PsiMethod instances should make objects not equal.");
        // Hashcodes may or may not be different here, not strictly asserting.

        assertNotEquals(javaMethod, javaMethodDifferentPsiDifferentSig);

        // Test with null
        assertNotEquals(null, javaMethod);

        // Test with different object type
        assertNotEquals(methodName + "()", javaMethod);
    }

    // 4. `accept()` method
    @Test
    void testAcceptMethodWithJavaMethodVisitor() {
        JavaMethodVisitor mockVisitor = mock(JavaMethodVisitor.class);
        javaMethod.accept(mockVisitor);
        verify(mockVisitor, times(1)).visitJavaMethod(javaMethod);
        verifyNoMoreInteractions(mockVisitor);
    }

    @Test
    void testAcceptMethodWithGenericPsiElementVisitor() {
        PsiElementVisitor genericVisitor = mock(PsiElementVisitor.class);
        javaMethod.accept(genericVisitor);
        // JavaCode.accept(PsiElementVisitor) calls visitor.visitElement(this.psiElement)
        // For JavaMethod, this.psiElement is the PsiMethod instance.
        verify(genericVisitor, times(1)).visitElement(mockPsiMethod);
        verifyNoMoreInteractions(genericVisitor);
    }

    // 5. Metric Management (inherited)
    @Test
    void testMetricManagement() {
        Metric metricCYCLO = Metric.of(MetricType.CYCLO, 5L);
        javaMethod.addMetric(metricCYCLO);
        assertEquals(metricCYCLO, javaMethod.metric(MetricType.CYCLO));

        assertNull(javaMethod.metric(MetricType.LOC)); // Non-existent

        Metric metricNBD = Metric.of(MetricType.NBD, Value.of(3));
        javaMethod.addMetric(metricNBD);
        List<Metric> expectedMetrics = Arrays.asList(metricCYCLO, metricNBD); // CYCLO before NBD by type name
        expectedMetrics.sort(Comparator.comparing(m -> m.getType().name()));

        List<Metric> actualMetrics = javaMethod.metrics().collect(Collectors.toList());
        assertIterableEquals(expectedMetrics, actualMetrics);

        javaMethod.removeMetric(MetricType.CYCLO);
        assertNull(javaMethod.metric(MetricType.CYCLO));
        assertEquals(1, javaMethod.metrics().count());
        assertEquals(metricNBD, javaMethod.metrics().findFirst().get());

        javaMethod.removeMetric(MetricType.NBD);
        assertTrue(javaMethod.metrics().collect(Collectors.toList()).isEmpty());
    }
}
