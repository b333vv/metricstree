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
import org.b333vv.metric.exec.MetricsEventListener;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.method.JavaMethodVisitor;
import org.b333vv.metric.model.visitor.type.JavaClassVisitor;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;
import org.b333vv.metric.ui.settings.composition.ProjectMetricsTreeSettings;
import org.b333vv.metric.ui.settings.ranges.MetricsValidRangeStub;
import org.b333vv.metric.ui.settings.ranges.MetricsValidRangesSettings;

import java.util.stream.Stream;

import static org.b333vv.metric.model.metric.MetricType.CBO;

public class MetricsService {
    private static MetricsValidRangesSettings metricsValidRangesSettings;
    private static ClassMetricsTreeSettings classMetricsTreeSettings;
    private static ProjectMetricsTreeSettings projectMetricsTreeSettings;
    private static boolean showClassMetricsTree;
    private static Project project;

    private MetricsService() {
        // Utility class
    }

    public static void init(Project project) {
        metricsValidRangesSettings = MetricsUtils.get(project, MetricsValidRangesSettings.class);
        classMetricsTreeSettings = MetricsUtils.get(project, ClassMetricsTreeSettings.class);
        showClassMetricsTree = classMetricsTreeSettings.isShowClassMetricsTree();
        projectMetricsTreeSettings = MetricsUtils.get(project, ProjectMetricsTreeSettings.class);
        MetricsService.project = project;
    }

    public static Range getRangeForMetric(MetricType type) {
        MetricsValidRangeStub metricsAllowableValueRangeStub =
                metricsValidRangesSettings.getMetricsAllowableValueRangeStub(type);
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
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaClassVisitor);
    }

    public static Stream<JavaRecursiveElementVisitor> getJavaMethodVisitorsForClassMetricsTree() {
        return classMetricsTreeSettings.getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaMethodVisitor);
    }

    public static Stream<JavaRecursiveElementVisitor> getJavaClassVisitorsForProjectMetricsTree() {
        return projectMetricsTreeSettings.getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .filter(m -> m.getType() != CBO)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaClassVisitor);
    }

    public static Stream<JavaRecursiveElementVisitor> getDeferredJavaClassVisitorsForProjectMetricsTree() {
        return projectMetricsTreeSettings.getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .filter(m -> m.getType() == CBO)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaClassVisitor);
    }

    public static Stream<JavaRecursiveElementVisitor> getJavaMethodVisitorsForProjectMetricsTree() {
        return projectMetricsTreeSettings.getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> m.getType().visitor())
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
        project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).showClassMetricsTree(showClassMetricsTree);
    }
}
