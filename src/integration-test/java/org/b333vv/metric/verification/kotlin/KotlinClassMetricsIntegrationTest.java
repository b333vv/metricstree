package org.b333vv.metric.verification.kotlin;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.builder.PsiCalculationStrategy;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.MetricType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KotlinClassMetricsIntegrationTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles(
                "kotlin/Cohesion.kt",
                "kotlin/Base.kt",
                "kotlin/Derived.kt",
                "kotlin/RfcSample.kt",
                "kotlin/MethodMetrics.kt");
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testClassLevelMetricsPresenceForKotlin() {
        PsiCalculationStrategy strategy = new PsiCalculationStrategy();
        ProjectElement projectElement = strategy.calculate(getProject(), new EmptyProgressIndicator(), null);

        List<ClassElement> classes = projectElement.allClasses().collect(Collectors.toList());
        // Just ensure we found at least one class from test data
        ClassElement any = classes.stream().filter(c -> c.getName() != null).findFirst().orElse(null);
        assertNotNull(any);

        // Verify a broad set of class-level metrics are available (values may vary by
        // implementation)
        Set<MetricType> required = Set.of(
                MetricType.WMC, MetricType.NOM, MetricType.NCSS,
                MetricType.DIT, MetricType.RFC, MetricType.LCOM, MetricType.NOC,
                MetricType.NOA, MetricType.NOO, MetricType.NOOM, MetricType.NOAM, MetricType.SIZE2,
                MetricType.CBO, MetricType.MPC, MetricType.DAC,
                MetricType.ATFD, MetricType.NOPA, MetricType.NOAC, MetricType.WOC,
                MetricType.TCC);
        for (MetricType type : required) {
            // Some metrics can be null when not applicable to a specific class; check
            // presence across any class
            boolean presentSomewhere = classes.stream().anyMatch(c -> c.metric(type) != null);
            assertTrue("Expected presence of class metric: " + type, presentSomewhere);
        }
    }
}
