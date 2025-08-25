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
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.*;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;
import org.b333vv.metric.ui.settings.other.OtherSettings;
import org.b333vv.metric.ui.settings.other.CalculationEngine;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangeStub;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangeStub;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesSettings;

import java.util.Set;
import java.util.stream.Stream;

import static org.b333vv.metric.model.metric.MetricType.*;

@Service(Service.Level.PROJECT)
public final class SettingsService {

    private final Project project;

    public SettingsService(Project project) {
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

    public CalculationEngine getCalculationEngine() {
        return this.project.getService(OtherSettings.class).getCalculationEngine();
    }

    public boolean isShowClassMetricsTree() {

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

    // Facade getter methods
    public BasicMetricsValidRangesSettings getBasicMetricsSettings() {
        return project.getService(BasicMetricsValidRangesSettings.class);
    }

    public DerivativeMetricsValidRangesSettings getDerivativeMetricsSettings() {
        return project.getService(DerivativeMetricsValidRangesSettings.class);
    }

    public ClassMetricsTreeSettings getClassMetricsTreeSettings() {
        return project.getService(ClassMetricsTreeSettings.class);
    }

    public OtherSettings getOtherSettings() {
        return project.getService(OtherSettings.class);
    }

    public org.b333vv.metric.ui.settings.fitnessfunction.ClassLevelFitnessFunctions getClassLevelFitnessFunctions() {
        return project.getService(org.b333vv.metric.ui.settings.fitnessfunction.ClassLevelFitnessFunctions.class);
    }

    public org.b333vv.metric.ui.settings.fitnessfunction.PackageLevelFitnessFunctions getPackageLevelFitnessFunctions() {
        return project.getService(org.b333vv.metric.ui.settings.fitnessfunction.PackageLevelFitnessFunctions.class);
    }
}
