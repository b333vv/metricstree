package org.jacoquev.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import org.jacoquev.model.metric.value.Range;
import org.jacoquev.model.metric.value.Value;
import org.jacoquev.model.visitor.method.*;
import org.jacoquev.model.visitor.type.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class MetricsService {
    private static MetricsAllowableValueRanges metricsAllowableValueRanges;
    private static ClassMetricsTreeSettings classMetricsTreeSettings;
    private static Map<String, JavaRecursiveElementVisitor> visitors = new HashMap<>();
    static {
        visitors.put("NOAM", new NumberOfAddedMethodsVisitor());
        visitors.put("LCOM", new LackOfCohesionOfMethodsVisitor());
        visitors.put("DIT", new DepthOfInheritanceTreeVisitor());
        visitors.put("NOA", new NumberOfAttributesVisitor());
        visitors.put("NOC", new NumberOfChildrenVisitor());
        visitors.put("NOO", new NumberOfOperationsVisitor());
        visitors.put("NOOM", new NumberOfOverriddenMethodsVisitor());
        visitors.put("RFC", new ResponseForClassVisitor());
        visitors.put("WMC", new WeightedMethodCountVisitor());
        visitors.put("SIZE2", new NumberOfAttributesAndMethodsVisitor());
        visitors.put("CBO", new CouplingBetweenObjectsVisitor());
        visitors.put("LOC", new LinesOfCodeVisitor());
        visitors.put("CND", new ConditionNestingDepthVisitor());
        visitors.put("LND", new LoopNestingDepthVisitor());
        visitors.put("CC", new McCabeCyclomaticComplexityVisitor());
        visitors.put("NOL", new NumberOfLoopsVisitor());
        visitors.put("FANIN", new FanInVisitor());
        visitors.put("FANOUT", new FanOutVisitor());
    }

    private MetricsService() {
        // Utility class
    }

    public static void setMetricsAllowableValueRanges(Project project) {
        metricsAllowableValueRanges = MetricsUtils.get(project, MetricsAllowableValueRanges.class);
        classMetricsTreeSettings = MetricsUtils.get(project, ClassMetricsTreeSettings.class);
    }

    public static Range getRangeForMetric(String metricName) {
        MetricsAllowableValueRanges.MetricsAllowableValueRangeStub metricsAllowableValueRangeStub =
                metricsAllowableValueRanges.getMetrics().get(metricName);
        if (metricsAllowableValueRangeStub == null) {
            return Range.UNDEFINED;
        }
        if (metricsAllowableValueRangeStub.isDoubleValue()) {
            return Range.of(Value.of(metricsAllowableValueRangeStub.getMinDoubleValue()), Value.of(metricsAllowableValueRangeStub.getMaxDoubleValue()));
        } else {
            return Range.of(Value.of(metricsAllowableValueRangeStub.getMinLongValue()), Value.of(metricsAllowableValueRangeStub.getMaxLongValue()));
        }
    }

    public static Stream<JavaRecursiveElementVisitor> getJavaClassVisitorsForClassMetricsTree() {
        return classMetricsTreeSettings.getMetricsList().stream()
                .filter(ClassMetricsTreeSettings.ClassMetricsTreeStub::isNeedToConsider)
                .map(m -> visitors.get(m.getName()))
                .filter(m -> m instanceof JavaClassVisitor);
    }

    public static Stream<JavaRecursiveElementVisitor> getJavaMethodVisitorsForClassMetricsTree() {
        return classMetricsTreeSettings.getMetricsList().stream()
                .filter(ClassMetricsTreeSettings.ClassMetricsTreeStub::isNeedToConsider)
                .map(m -> visitors.get(m.getName()))
                .filter(m -> m instanceof JavaMethodVisitor);
    }
}
