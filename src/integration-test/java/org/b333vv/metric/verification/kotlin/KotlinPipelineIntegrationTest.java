package org.b333vv.metric.verification.kotlin;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.builder.PsiCalculationStrategy;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.MethodElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;

import java.util.List;
import java.util.stream.Collectors;

public class KotlinPipelineIntegrationTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles(
                "kotlin/MethodMetrics.kt");
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testPipelineCalculatesKotlinClassAndMethodMetrics() {
        PsiCalculationStrategy strategy = new PsiCalculationStrategy();
        ProjectElement pe = strategy.calculate(getProject(), new EmptyProgressIndicator(), null);

        // Find Kotlin class from test data
        List<ClassElement> classes = pe.allClasses().collect(Collectors.toList());
        ClassElement methodMetrics = classes.stream()
                .filter(c -> "MethodMetrics".equals(c.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull("Expected to find class MethodMetrics in ProjectElement", methodMetrics);

        // Assert some class-level metrics are present (values may vary by
        // implementation)
        assertNotNull("WMC metric expected", methodMetrics.metric(MetricType.WMC));
        assertNotNull("NOM metric expected", methodMetrics.metric(MetricType.NOM));
        assertNotNull("NCSS metric expected", methodMetrics.metric(MetricType.NCSS));

        // Assert some method-level metrics are present for known method
        MethodElement any = methodMetrics.methods().findFirst().orElse(null);
        assertNotNull("Expected at least one method in MethodMetrics", any);
        Metric cc = any.metric(MetricType.CC);
        Metric loc = any.metric(MetricType.LOC);
        assertNotNull("CC metric expected on some method", cc);
        assertNotNull("LOC metric expected on some method", loc);
    }

    public void testSettingsTogglesAffectKotlinMetrics() {
        // Disable NCSS (class-level) and CC (method-level), run pipeline, verify
        // metrics are absent
        ClassMetricsTreeSettings settings = getProject().getService(ClassMetricsTreeSettings.class);
        java.util.List<MetricsTreeSettingsStub> metrics = settings.getMetricsList();
        java.util.List<MetricsTreeSettingsStub> disabled = metrics.stream().map(stub -> {
            if (stub.getType() == MetricType.NCSS || stub.getType() == MetricType.CC) {
                MetricsTreeSettingsStub copy = new MetricsTreeSettingsStub(stub.getType(), false);
                return copy;
            }
            return new MetricsTreeSettingsStub(stub.getType(), stub.isNeedToConsider());
        }).collect(java.util.stream.Collectors.toList());
        settings.setClassTreeMetrics(disabled);

        PsiCalculationStrategy strategy = new PsiCalculationStrategy();
        ProjectElement pe = strategy.calculate(getProject(), new EmptyProgressIndicator(), null);

        java.util.List<ClassElement> classes = pe.allClasses().collect(java.util.stream.Collectors.toList());
        ClassElement methodMetrics = classes.stream().filter(c -> "MethodMetrics".equals(c.getName())).findFirst()
                .orElse(null);
        assertNotNull(methodMetrics);
        // NCSS should be disabled
        assertNull("NCSS should be disabled and absent", methodMetrics.metric(MetricType.NCSS));
        // Pick any method and check CC is disabled
        MethodElement any = methodMetrics.methods().findFirst().orElse(null);
        assertNotNull(any);
        assertNull("CC should be disabled and absent on method", any.metric(MetricType.CC));

        // Re-enable NCSS and CC and verify presence again
        java.util.List<MetricsTreeSettingsStub> enabled = metrics.stream()
                .map(stub -> new MetricsTreeSettingsStub(stub.getType(), true))
                .collect(java.util.stream.Collectors.toList());
        settings.setClassTreeMetrics(enabled);

        ProjectElement pe2 = strategy.calculate(getProject(), new EmptyProgressIndicator(), null);
        java.util.List<ClassElement> classes2 = pe2.allClasses().collect(java.util.stream.Collectors.toList());
        ClassElement methodMetrics2 = classes2.stream().filter(c -> "MethodMetrics".equals(c.getName())).findFirst()
                .orElse(null);
        assertNotNull(methodMetrics2);
        assertNotNull("NCSS should be present after enabling", methodMetrics2.metric(MetricType.NCSS));
        MethodElement any2 = methodMetrics2.methods().findFirst().orElse(null);
        assertNotNull(any2);
        assertNotNull("CC should be present after enabling", any2.metric(MetricType.CC));
    }
}
