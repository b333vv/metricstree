package org.b333vv.metric.builder;

import com.intellij.psi.PsiClass;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JavaParserDerivativeMetricsIntegrationTest {

    @Mock
    private PsiClass mockPsiClass;
    @Mock
    private JavaMethod mockMethodA;
    @Mock
    private JavaMethod mockMethodB;

    private JavaClass javaClass;
    private JavaParserCalculationStrategy strategy;

    @BeforeEach
    void setUp() {
        when(mockPsiClass.getName()).thenReturn("TestClass");
        javaClass = new JavaClass(mockPsiClass);
        strategy = new JavaParserCalculationStrategy();

        // Add derivative metrics to the class, initially with UNDEFINED values
        javaClass.addMetric(Metric.of(MetricType.CCC, Value.UNDEFINED));
        javaClass.addMetric(Metric.of(MetricType.CMI, Value.UNDEFINED));

        // Add base metrics that CMI depends on
        javaClass.addMetric(Metric.of(MetricType.CHVL, Value.UNDEFINED));

        // Configure mocks for methods
        when(mockMethodA.metric(MetricType.CCM)).thenReturn(Metric.of(MetricType.CCM, Value.UNDEFINED));
        when(mockMethodA.metric(MetricType.CC)).thenReturn(Metric.of(MetricType.CC, Value.UNDEFINED));
        when(mockMethodA.metric(MetricType.LOC)).thenReturn(Metric.of(MetricType.LOC, Value.UNDEFINED));

        when(mockMethodB.metric(MetricType.CCM)).thenReturn(Metric.of(MetricType.CCM, Value.UNDEFINED));
        when(mockMethodB.metric(MetricType.CC)).thenReturn(Metric.of(MetricType.CC, Value.UNDEFINED));
        when(mockMethodB.metric(MetricType.LOC)).thenReturn(Metric.of(MetricType.LOC, Value.UNDEFINED));

        javaClass.addMethod(mockMethodA);
        javaClass.addMethod(mockMethodB);
    }

    @Test
    void testCalculateDerivativeClassMetrics() throws Exception {
        // Set JavaParser values for base metrics
        javaClass.metric(MetricType.CHVL).setJavaParserValue(Value.of(50.0));
        mockMethodA.metric(MetricType.CCM).setJavaParserValue(Value.of(1L));
        mockMethodA.metric(MetricType.CC).setJavaParserValue(Value.of(1L));
        mockMethodA.metric(MetricType.LOC).setJavaParserValue(Value.of(3L));
        mockMethodB.metric(MetricType.CCM).setJavaParserValue(Value.of(2L));
        mockMethodB.metric(MetricType.CC).setJavaParserValue(Value.of(2L));
        mockMethodB.metric(MetricType.LOC).setJavaParserValue(Value.of(5L));

        // Use reflection to call the private method
        Method method = JavaParserCalculationStrategy.class.getDeclaredMethod("calculateDerivativeClassMetrics", JavaClass.class);
        method.setAccessible(true);
        method.invoke(strategy, javaClass);

        // Assert CCC
        Value cccValue = javaClass.metric(MetricType.CCC).getJavaParserValue();
        assertEquals(3L, cccValue.longValue(), "CCC should be the sum of method CCMs (1 + 2)");

        // Assert CMI
        // CMI = MAX(0,(171 - 5.2 * ln(50.0) - 0.23 * (3) - 16.2 * ln(8))*100 / 171)
        // CMI = MAX(0,(171 - 20.34 - 0.69 - 33.69)*100 / 171)
        // CMI = MAX(0,(116.28)*100 / 171) = 67.99
        Value cmiValue = javaClass.metric(MetricType.CMI).getJavaParserValue();
        assertEquals(67.99, cmiValue.doubleValue(), 0.01, "CMI calculation is incorrect");
    }
}
