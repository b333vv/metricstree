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
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.BasicMetricsRange;
import org.b333vv.metric.model.metric.value.DerivativeMetricsRange;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.method.JavaMethodVisitor;
import org.b333vv.metric.model.visitor.type.JavaClassVisitor;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;
import org.b333vv.metric.ui.settings.other.OtherSettings;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangeStub;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangeStub;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesSettings;

import java.util.Set;
import java.util.stream.Stream;

import static org.b333vv.metric.model.metric.MetricType.*;

public final class MetricsService {

    private MetricsService(Project project) {
    }

    public static Range getRangeForMetric(MetricType type) {
        BasicMetricsValidRangeStub basicStub = MetricsUtils.getForProject(BasicMetricsValidRangesSettings.class)
                .getControlledMetrics().get(type.name());
        if (basicStub != null) {
            return BasicMetricsRange.of(Value.of(basicStub.getRegularBound()),
                    Value.of(basicStub.getHighBound()), Value.of(basicStub.getVeryHighBound()));
        }
        DerivativeMetricsValidRangeStub derivativeStub = MetricsUtils.getForProject(DerivativeMetricsValidRangesSettings.class)
                .getControlledMetrics().get(type.name());
        if (derivativeStub != null) {
            return DerivativeMetricsRange.of(Value.of(derivativeStub.getMinValue()),
                    Value.of(derivativeStub.getMaxValue()));
        }
        return BasicMetricsRange.UNDEFINED;
    }

    public static Stream<JavaRecursiveElementVisitor> classVisitorsForClassMetricsTree() {
        return MetricsUtils.getForProject(ClassMetricsTreeSettings.class).getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaClassVisitor);
    }

    public static Stream<JavaRecursiveElementVisitor> methodsVisitorsForClassMetricsTree() {
        return MetricsUtils.getForProject(ClassMetricsTreeSettings.class).getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaMethodVisitor);
    }

    public static boolean isControlValidRanges() {
        return MetricsUtils.getForProject(BasicMetricsValidRangesSettings.class)
                .isControlValidRanges();
    }

    public static boolean isProjectMetricsStampStored() {
        return MetricsUtils.getForProject(OtherSettings.class)
                .isProjectMetricsStampStored();
    }

    public static boolean isShowClassMetricsTree() {
        return MetricsUtils.getForProject(ClassMetricsTreeSettings.class).isShowClassMetricsTree();
    }

    public static void setShowClassMetricsTree(boolean showClassMetricsTree) {
        MetricsUtils.getForProject(ClassMetricsTreeSettings.class).setShowClassMetricsTree(showClassMetricsTree);
        MetricsUtils.getCurrentProject()
                .getMessageBus().syncPublisher(MetricsEventListener.TOPIC).showClassMetricsTree(showClassMetricsTree);
    }

    public static Set<MetricType> getDeferredMetricTypes() {
        return Set.of(CBO, ATFD, TCC, LAA, FDP, NOAV, CINT, CDISP, MND, NOPA, NOAC, WOC);
//        return Set.of(CBO);
    }

    public static boolean isLongValueMetricType(MetricType metricType) {
        Set<MetricType> doubleValueMetricTypes = Set.of(TCC, I, A, D, MHF, AHF, MIF, AIF, CF, PF, LAA, CDISP, WOC);
        return !doubleValueMetricTypes.contains(metricType);
    }
}
