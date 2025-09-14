package org.b333vv.metric.verification.kotlin;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.builder.PsiCalculationStrategy;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KotlinExactCouplingAndHalsteadIntegrationTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles(
                "kotlin/CouplingValues.kt"
        );
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testExactCouplingAndRfcValues() {
        PsiCalculationStrategy strategy = new PsiCalculationStrategy();
        ProjectElement pe = strategy.calculate(getProject(), new EmptyProgressIndicator());

        List<ClassElement> classes = pe.allClasses().collect(Collectors.toList());
        Map<String, ClassElement> byName = classes.stream().filter(c -> c.getName() != null)
                .collect(Collectors.toMap(ClassElement::getName, Function.identity(), (a,b)->a));

        ClassElement repo = byName.get("Repo");
        ClassElement service = byName.get("Service");
        ClassElement client = byName.get("Client");
        assertNotNull(repo);
        assertNotNull(service);
        assertNotNull(client);

        // RFC expected per KotlinResponseForClassVisitor (declared functions + unique call names/arity)
        assertEquals(1L, metricValue(repo.metric(MetricType.RFC)));
        // Includes constructor calls in property initializers (Repo() / Service()) as call expressions
        assertEquals(4L, metricValue(service.metric(MetricType.RFC)));
        assertEquals(4L, metricValue(client.metric(MetricType.RFC)));

        // MPC expected per KotlinMessagePassingCouplingVisitor (count of call expressions inside class bodies)
        assertEquals(0L, metricValue(repo.metric(MetricType.MPC)));
        assertEquals(3L, metricValue(service.metric(MetricType.MPC)));
        assertEquals(3L, metricValue(client.metric(MetricType.MPC)));

        // CBO expected per KotlinCouplingBetweenObjectsVisitor (unique external types referenced)
        assertEquals(0L, metricValue(repo.metric(MetricType.CBO)));     // no external types
        assertEquals(1L, metricValue(service.metric(MetricType.CBO)));  // Repo
        assertEquals(1L, metricValue(client.metric(MetricType.CBO)));   // Service

        // Class-level Halstead should be computed and positive for non-trivial classes
        assertNotNull(service.metric(MetricType.CHVL));
        assertNotNull(service.metric(MetricType.CHD));
        assertNotNull(service.metric(MetricType.CHL));
        assertNotNull(service.metric(MetricType.CHEF));
        assertNotNull(service.metric(MetricType.CHVC));
        assertNotNull(service.metric(MetricType.CHER));
    }

    private long metricValue(Metric m) {
        assertNotNull(m);
        return m.getPsiValue().longValue();
    }
}
