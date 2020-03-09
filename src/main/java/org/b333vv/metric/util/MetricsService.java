/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.method.*;
import org.b333vv.metric.model.visitor.type.*;
import org.b333vv.metric.ui.settings.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class MetricsService {
    private static MetricsValidRangesSettings metricsValidRangesSettings;
    private static ClassMetricsTreeSettings classMetricsTreeSettings;
    private static ProjectMetricsTreeSettings projectMetricsTreeSettings;
    private static final Map<String, JavaRecursiveElementVisitor> visitors = new HashMap<>();
    private static final Map<String, JavaRecursiveElementVisitor> deferredVisitors = new HashMap<>();
    private static boolean showClassMetricsTree;
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
        visitors.put("NOM", new NumberOfMethodsVisitor());
        visitors.put("MPC", new MessagePassingCouplingVisitor());
        visitors.put("DAC", new DataAbstractionCouplingVisitor());
        visitors.put("LOC", new LinesOfCodeVisitor());
        visitors.put("CND", new ConditionNestingDepthVisitor());
        visitors.put("LND", new LoopNestingDepthVisitor());
        visitors.put("CC", new McCabeCyclomaticComplexityVisitor());
        visitors.put("NOL", new NumberOfLoopsVisitor());
        visitors.put("FIN", new FanInVisitor());
        visitors.put("FOUT", new FanOutVisitor());
        deferredVisitors.put("CBO", new CouplingBetweenObjectsVisitor());
    }

    private MetricsService() {
        // Utility class
    }

    public static void init(Project project) {
        metricsValidRangesSettings = MetricsUtils.get(project, MetricsValidRangesSettings.class);
        classMetricsTreeSettings = MetricsUtils.get(project, ClassMetricsTreeSettings.class);
        showClassMetricsTree = classMetricsTreeSettings.isShowClassMetricsTree();
        projectMetricsTreeSettings = MetricsUtils.get(project, ProjectMetricsTreeSettings.class);
    }

    public static Range getRangeForMetric(String metricName) {
        MetricsValidRangeStub metricsAllowableValueRangeStub =
                metricsValidRangesSettings.getControlledMetrics().get(metricName);
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
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> visitors.get(m.getName()))
                .filter(m -> m instanceof JavaClassVisitor);
    }

    public static Stream<JavaRecursiveElementVisitor> getJavaMethodVisitorsForClassMetricsTree() {
        return classMetricsTreeSettings.getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> visitors.get(m.getName()))
                .filter(m -> m instanceof JavaMethodVisitor);
    }

    public static Stream<JavaRecursiveElementVisitor> getJavaClassVisitorsForProjectMetricsTree() {
        return projectMetricsTreeSettings.getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> visitors.get(m.getName()))
                .filter(m -> m instanceof JavaClassVisitor);
    }

    public static Stream<JavaRecursiveElementVisitor> getDeferredJavaClassVisitorsForProjectMetricsTree() {
        return projectMetricsTreeSettings.getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> deferredVisitors.get(m.getName()))
                .filter(m -> m instanceof JavaClassVisitor);
    }

    public static Stream<JavaRecursiveElementVisitor> getJavaMethodVisitorsForProjectMetricsTree() {
        return projectMetricsTreeSettings.getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> visitors.get(m.getName()))
                .filter(m -> m instanceof JavaMethodVisitor);
    }

    public static boolean isNeedToConsiderProjectMetrics() {
        return projectMetricsTreeSettings.isNeedToConsiderProjectMetrics();
    }

    public static boolean isNeedToConsiderPackageMetrics() {
        return projectMetricsTreeSettings.isNeedToConsiderPackageMetrics();
    }

    public static boolean isControlValidRanges() {
        return metricsValidRangesSettings.isControlValidRanges();
    }

    public static boolean isShowClassMetricsTree() { return showClassMetricsTree; }

    public static void setShowClassMetricsTree(boolean showClassMetricsTree) {
        MetricsService.showClassMetricsTree = showClassMetricsTree;
        classMetricsTreeSettings.setShowClassMetricsTree(showClassMetricsTree);
        if (!showClassMetricsTree) {
            MetricsUtils.getClassMetricsPanel().clear();
        } else {
            MetricsUtils.getClassMetricsPanel().createUIComponents();
            MetricsUtils.getClassMetricsPanel().refresh();
        }
    }
}
