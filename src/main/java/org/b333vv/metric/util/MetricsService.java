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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.JavaRecursiveElementVisitor;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.*;
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

@Service(Service.Level.PROJECT)
public final class MetricsService {

    private final Project project;

    public MetricsService(Project project) {
        this.project = project;
    }

    public Range getRangeForMetric(MetricType type) {
        BasicMetricsValidRangeStub basicStub = this.project.getService(BasicMetricsValidRangesSettings.class)
                .getControlledMetrics().get(type.name());
        if (basicStub != null) {
            return BasicMetricsRange.of(Value.of(basicStub.getRegularBound()),
                    Value.of(basicStub.getHighBound()), Value.of(basicStub.getVeryHighBound()));
        }
        DerivativeMetricsValidRangeStub derivativeStub = this.project.getService(DerivativeMetricsValidRangesSettings.class)
                .getControlledMetrics().get(type.name());
        if (derivativeStub != null) {
            return DerivativeMetricsRange.of(Value.of(derivativeStub.getMinValue()),
                    Value.of(derivativeStub.getMaxValue()));
        }
        return BasicMetricsRange.UNDEFINED;
    }

    public RangeType getRangeTypeByMetricTypeAndValue(MetricType type, Value value) {
        return getRangeForMetric(type).getRangeType(value);
    }

    public boolean isNotRegularValue(MetricType type, Value value) {
        return getRangeTypeByMetricTypeAndValue(type, value) != RangeType.UNDEFINED
                && getRangeTypeByMetricTypeAndValue(type, value) != RangeType.REGULAR;
    }

    public Stream<JavaRecursiveElementVisitor> classVisitorsForClassMetricsTree() {
//        return project.getService(ClassMetricsTreeSettings.class).getMetricsList().stream()
//                .filter(MetricsTreeSettingsStub::isNeedToConsider)
//                .map(m -> m.getType().visitor())
//                .filter(m -> m instanceof JavaClassVisitor);

        return this.project.getService(ClassMetricsTreeSettings.class).getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaClassVisitor);
    }

    public Stream<JavaRecursiveElementVisitor> methodsVisitorsForClassMetricsTree() {
        return this.project.getService(ClassMetricsTreeSettings.class).getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaMethodVisitor);

//        return project.getService(ClassMetricsTreeSettings.class).getMetricsList().stream()
//        return project.getService(ClassMetricsTreeSettings.class).getMetricsList().stream()
//                .filter(MetricsTreeSettingsStub::isNeedToConsider)
//                .map(m -> m.getType().visitor())
//                .filter(m -> m instanceof JavaMethodVisitor);
    }

    public boolean isControlValidRanges() {
//        return project.getService(BasicMetricsValidRangesSettings.class)
//                .isControlValidRanges();
        return this.project.getService(BasicMetricsValidRangesSettings.class)
                .isControlValidRanges();
    }

    public boolean isProjectMetricsStampStored() {
//        return project.getService(OtherSettings.class)
//                .isProjectMetricsStampStored();
        return this.project.getService(OtherSettings.class)
                .isProjectMetricsStampStored();
    }

    public boolean isShowClassMetricsTree() {
////        return MetricsUtils.getForProject(ClassMetricsTreeSettings.class).isShowClassMetricsTree();
////        return project.getService(ClassMetricsTreeSettings.class).isShowClassMetricsTree();
        return this.project.getService(ClassMetricsTreeSettings.class).isShowClassMetricsTree();
    }

    public void setShowClassMetricsTree(boolean showClassMetricsTree) {
//        project.getService(ClassMetricsTreeSettings.class).setShowClassMetricsTree(showClassMetricsTree);
        this.project.getService(ClassMetricsTreeSettings.class).setShowClassMetricsTree(showClassMetricsTree);
        this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).showClassMetricsTree(showClassMetricsTree);
    }

    public Set<MetricType> getDeferredMetricTypes() {
//        return Set.of(CBO, ATFD, TCC, LAA, FDP, NOAV, CINT, CDISP, MND, NOPA, NOAC, WOC);
//        return Set.of(CBO);
        return Set.of();
    }


    public boolean isLongValueMetricType(MetricType metricType) {
        Set<MetricType> doubleValueMetricTypes = Set.of(TCC, I, A, D, MHF, AHF, MIF, AIF, CF, PF, LAA, CDISP, WOC, CCC, CCM);
        return !doubleValueMetricTypes.contains(metricType);
    }
}
