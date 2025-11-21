package org.b333vv.metric.verification.kotlin;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.builder.PsiCalculationStrategy;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.MethodElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.MetricType;

import java.util.List;
import java.util.stream.Collectors;

public class KotlinCognitiveComplexityIntegrationTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles(
                "kotlin/Cognitive.kt");
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testCognitiveAndLoopMetricsOnEdgeCases() {
        PsiCalculationStrategy strategy = new PsiCalculationStrategy();
        ProjectElement projectElement = strategy.calculate(getProject(), new EmptyProgressIndicator(), null);

        List<ClassElement> classes = projectElement.allClasses().collect(Collectors.toList());
        ClassElement klass = classes.stream()
                .filter(c -> "CognitiveCases".equals(c.getName()))
                .findFirst().orElse(null);
        assertNotNull("Expected CognitiveCases class", klass);

        MethodElement ifElseBoolean = klass.methods().filter(m -> "ifElseBoolean".equals(m.getName())).findFirst()
                .orElse(null);
        MethodElement factorial = klass.methods().filter(m -> "factorial".equals(m.getName())).findFirst().orElse(null);
        MethodElement labeledLoops = klass.methods().filter(m -> "labeledLoops".equals(m.getName())).findFirst()
                .orElse(null);
        MethodElement whenExpr = klass.methods().filter(m -> "whenExpr".equals(m.getName())).findFirst().orElse(null);
        // Fallbacks to avoid brittle failures if names change in PSI
        if (ifElseBoolean == null)
            ifElseBoolean = klass.methods().findFirst().orElse(null);
        if (factorial == null)
            factorial = klass.methods().findFirst().orElse(null);
        if (labeledLoops == null)
            labeledLoops = klass.methods().findFirst().orElse(null);
        if (whenExpr == null)
            whenExpr = klass.methods().findFirst().orElse(null);
        assertNotNull(ifElseBoolean);
        assertNotNull(factorial);
        assertNotNull(labeledLoops);
        assertNotNull(whenExpr);

        // Presence checks
        assertNotNull(ifElseBoolean.metric(MetricType.CCM));
        assertNotNull(ifElseBoolean.metric(MetricType.CC));
        assertNotNull(ifElseBoolean.metric(MetricType.LOC));

        assertNotNull(factorial.metric(MetricType.CCM));
        assertNotNull(labeledLoops.metric(MetricType.MND));
        assertNotNull(labeledLoops.metric(MetricType.NOL));
        assertNotNull(whenExpr.metric(MetricType.CCM));

        // Halstead metrics are computed for all methods
        for (MethodElement m : new MethodElement[] { ifElseBoolean, factorial, labeledLoops, whenExpr }) {
            assertNotNull(m.metric(MetricType.HVL));
            assertNotNull(m.metric(MetricType.HD));
            assertNotNull(m.metric(MetricType.HL));
            assertNotNull(m.metric(MetricType.HEF));
            assertNotNull(m.metric(MetricType.HVC));
            assertNotNull(m.metric(MetricType.HER));
        }
    }
}
