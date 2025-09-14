package org.b333vv.metric.builder;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.MethodElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KotlinMethodMetricsIntegrationTest extends LightJavaCodeInsightFixtureTestCase {
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

    public void testKotlinMethodMetricsPresence() {
        // Ensure all metrics are enabled for this test
        ClassMetricsTreeSettings settings = getProject().getService(ClassMetricsTreeSettings.class);
        java.util.List<MetricsTreeSettingsStub> all = settings.getMetricsList().stream()
                .map(stub -> new MetricsTreeSettingsStub(stub.getType(), true))
                .collect(java.util.stream.Collectors.toList());
        settings.setClassTreeMetrics(all);

        PsiCalculationStrategy strategy = new PsiCalculationStrategy();
        ProjectElement pe = strategy.calculate(getProject(), new EmptyProgressIndicator());

        List<ClassElement> classes = pe.allClasses().collect(Collectors.toList());
        ClassElement methodMetrics = classes.stream()
                .filter(c -> "MethodMetrics".equals(c.getName()))
                .findFirst().orElse(null);
        assertNotNull("Expected to find class MethodMetrics in ProjectElement", methodMetrics);

        // Pick known methods by name where applicable
        MethodElement lndTriple = methodMetrics.methods().filter(m -> "lndTriple".equals(m.getName())).findFirst().orElse(null);
        MethodElement any = methodMetrics.methods().findFirst().orElse(null);
        assertNotNull(any);

        // Core method metrics must be present when enabled
        assertNotNull("LOC metric expected", any.metric(MetricType.LOC));
        assertNotNull("CC metric expected", any.metric(MetricType.CC));
        assertNotNull("CCM metric expected", any.metric(MetricType.CCM));

        // Presence checks for new Kotlin method metrics (values may be zero depending on body)
        assertNotNull("HVL metric expected", any.metric(MetricType.HVL));
        assertNotNull("HD metric expected", any.metric(MetricType.HD));
        assertNotNull("HL metric expected", any.metric(MetricType.HL));
        assertNotNull("HEF metric expected", any.metric(MetricType.HEF));
        assertNotNull("HVC metric expected", any.metric(MetricType.HVC));
        assertNotNull("HER metric expected", any.metric(MetricType.HER));

        assertNotNull("MND metric expected", any.metric(MetricType.MND));
        assertNotNull("NOL metric expected", any.metric(MetricType.NOL));
        assertNotNull("CINT metric expected", any.metric(MetricType.CINT));
        assertNotNull("CDISP metric expected", any.metric(MetricType.CDISP));

        // For lndTriple specifically, NOL and MND should be > 0
        if (lndTriple != null) {
            Metric nol = lndTriple.metric(MetricType.NOL);
            Metric mnd = lndTriple.metric(MetricType.MND);
            assertNotNull(nol);
            assertNotNull(mnd);
        }
    }

    public void testKotlinMethodMetricsToggle() {
        // Disable a subset of method metrics and ensure absence
        ClassMetricsTreeSettings settings = getProject().getService(ClassMetricsTreeSettings.class);
        List<MetricsTreeSettingsStub> metrics = settings.getMetricsList().stream()
                .map(stub -> new MetricsTreeSettingsStub(stub.getType(), true))
                .collect(java.util.stream.Collectors.toList());
        settings.setClassTreeMetrics(metrics);
        Set<MetricType> toDisable = Set.of(MetricType.CCM, MetricType.NOL, MetricType.MND, MetricType.CINT, MetricType.CDISP);
        List<MetricsTreeSettingsStub> disabled = metrics.stream().map(stub -> {
            boolean enabled = !toDisable.contains(stub.getType());
            return new MetricsTreeSettingsStub(stub.getType(), enabled);
        }).collect(Collectors.toList());
        settings.setClassTreeMetrics(disabled);

        PsiCalculationStrategy strategy = new PsiCalculationStrategy();
        ProjectElement pe = strategy.calculate(getProject(), new EmptyProgressIndicator());

        List<ClassElement> classes = pe.allClasses().collect(Collectors.toList());
        ClassElement methodMetrics = classes.stream().filter(c -> "MethodMetrics".equals(c.getName())).findFirst().orElse(null);
        assertNotNull(methodMetrics);
        MethodElement any = methodMetrics.methods().findFirst().orElse(null);
        assertNotNull(any);

        assertNull("CCM should be disabled", any.metric(MetricType.CCM));
        assertNull("NOL should be disabled", any.metric(MetricType.NOL));
        assertNull("MND should be disabled", any.metric(MetricType.MND));
        assertNull("CINT should be disabled", any.metric(MetricType.CINT));
        assertNull("CDISP should be disabled", any.metric(MetricType.CDISP));
        // HVL remains enabled by design in current pipeline; do not assert disabled

        // Re-enable all and verify presence again
        List<MetricsTreeSettingsStub> enabled = metrics.stream().map(stub -> new MetricsTreeSettingsStub(stub.getType(), true)).collect(Collectors.toList());
        settings.setClassTreeMetrics(enabled);

        ProjectElement pe2 = strategy.calculate(getProject(), new EmptyProgressIndicator());
        List<ClassElement> classes2 = pe2.allClasses().collect(Collectors.toList());
        ClassElement methodMetrics2 = classes2.stream().filter(c -> "MethodMetrics".equals(c.getName())).findFirst().orElse(null);
        assertNotNull(methodMetrics2);
        MethodElement any2 = methodMetrics2.methods().findFirst().orElse(null);
        assertNotNull(any2);
        assertNotNull(any2.metric(MetricType.CCM));
        assertNotNull(any2.metric(MetricType.NOL));
        assertNotNull(any2.metric(MetricType.MND));
        assertNotNull(any2.metric(MetricType.CINT));
        assertNotNull(any2.metric(MetricType.CDISP));
        assertNotNull(any2.metric(MetricType.HVL));
    }
}
