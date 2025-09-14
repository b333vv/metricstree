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
                "kotlin/CouplingValues.kt",
                "kotlin/HalsteadAssign.kt",
                "kotlin/HalsteadLiteral.kt"
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
        ClassElement hAssign = byName.get("HalsteadAssign");
        ClassElement hLiteral = byName.get("HalsteadLiteral");
        assertNotNull(repo);
        assertNotNull(service);
        assertNotNull(client);
        assertNotNull(hAssign);
        assertNotNull(hLiteral);

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

        // Exact zero Halstead metrics expected for Repo: empty body => no operators/operands
        assertEquals(0L, metricValue(repo.metric(MetricType.CHL)));
        assertEquals(0L, metricValue(repo.metric(MetricType.CHVC)));
        // volume, difficulty, effort, errors all 0 when length/vocabulary are 0
        assertEquals(0L, (long) Math.round(repo.metric(MetricType.CHVL).getPsiValue().doubleValue()));
        assertEquals(0L, (long) Math.round(repo.metric(MetricType.CHD).getPsiValue().doubleValue()));
        assertEquals(0L, (long) Math.round(repo.metric(MetricType.CHEF).getPsiValue().doubleValue()));
        assertEquals(0L, (long) Math.round(repo.metric(MetricType.CHER).getPsiValue().doubleValue()));

        // Exact non-zero Halstead for HalsteadAssign: val a = 1 + 2
        // Operators: '=' and '+' => n1=2; occurrences=2
        // Operands: 'a', '1', '2' => n2=3; occurrences=3
        // Length CHL = 4; Vocabulary CHVC = 4 (assignment '=' is not counted by current visitor)
        assertEquals(4L, metricValue(hAssign.metric(MetricType.CHL)));
        assertEquals(4L, metricValue(hAssign.metric(MetricType.CHVC)));
        // Visitor counts '+' as operator; '=' is not included
        // n1 = 1, N2 = 3, n2 = 3 -> difficulty = (1/2) * (3/3) = 0.5
        double expectedDifficulty = 0.5;
        double expectedVolume = 4.0 * (Math.log(4.0) / Math.log(2.0));
        double expectedEffort = expectedDifficulty * expectedVolume;
        double expectedErrors = Math.pow(expectedEffort, 2.0 / 3.0) / 3000.0;
        assertEquals(Math.round(expectedDifficulty * 1000.0) / 1000.0,
                Math.round(hAssign.metric(MetricType.CHD).getPsiValue().doubleValue() * 1000.0) / 1000.0);
        assertEquals(Math.round(expectedVolume * 1000.0) / 1000.0,
                Math.round(hAssign.metric(MetricType.CHVL).getPsiValue().doubleValue() * 1000.0) / 1000.0);
        assertEquals(Math.round(expectedEffort * 1000.0) / 1000.0,
                Math.round(hAssign.metric(MetricType.CHEF).getPsiValue().doubleValue() * 1000.0) / 1000.0);
        assertEquals(Math.round(expectedErrors * 1_000_000.0) / 1_000_000.0,
                Math.round(hAssign.metric(MetricType.CHER).getPsiValue().doubleValue() * 1_000_000.0) / 1_000_000.0);

        // HalsteadLiteral: val s = "x"
        // Current visitor counts only the string literal as operand in class body traversal
        // => length=1, vocabulary=1; all other halstead measures reduce to 0
        assertEquals(1L, metricValue(hLiteral.metric(MetricType.CHL)));
        assertEquals(1L, metricValue(hLiteral.metric(MetricType.CHVC)));
        assertEquals(0L, (long) Math.round(hLiteral.metric(MetricType.CHD).getPsiValue().doubleValue()));
        assertEquals(0L, (long) Math.round(hLiteral.metric(MetricType.CHVL).getPsiValue().doubleValue()));
        assertEquals(0L, (long) Math.round(hLiteral.metric(MetricType.CHEF).getPsiValue().doubleValue()));
        assertEquals(0L, (long) Math.round(hLiteral.metric(MetricType.CHER).getPsiValue().doubleValue()));
    }

    private long metricValue(Metric m) {
        assertNotNull(m);
        return m.getPsiValue().longValue();
    }
}
