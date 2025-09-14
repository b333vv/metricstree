package org.b333vv.metric.verification.kotlin;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.MethodElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.builder.PsiCalculationStrategy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KotlinAllMethodMetricsIntegrationTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles(
                "kotlin/MethodMetrics.kt"
        );
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testAllMethodMetricsAreCalculatedForKotlin() {
        PsiCalculationStrategy strategy = new PsiCalculationStrategy();
        ProjectElement pe = strategy.calculate(getProject(), new EmptyProgressIndicator());

        List<ClassElement> classes = pe.allClasses().collect(Collectors.toList());
        ClassElement methodMetrics = classes.stream()
                .filter(c -> "MethodMetrics".equals(c.getName()))
                .findFirst().orElse(null);
        assertNotNull("Expected class MethodMetrics", methodMetrics);

        MethodElement any = methodMetrics.methods().findFirst().orElse(null);
        assertNotNull(any);

        // Check presence of all method-level metrics supported
        Set<MetricType> required = Set.of(
                MetricType.CND, MetricType.LND, MetricType.CC, MetricType.NOL,
                MetricType.LOC, MetricType.NOPM, MetricType.LAA, MetricType.FDP,
                MetricType.NOAV, MetricType.MND, MetricType.CINT, MetricType.CDISP,
                MetricType.HVL, MetricType.HD, MetricType.HL, MetricType.HEF,
                MetricType.HVC, MetricType.HER, MetricType.CCM
        );
        for (MetricType mt : required) {
            assertNotNull("Expected method metric: " + mt, any.metric(mt));
        }
    }
}
