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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.b333vv.metric.exec.MetricsEventListener;
import org.b333vv.metric.model.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.BasicMetricsRange;
import org.b333vv.metric.model.metric.value.DerivativeMetricsRange;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.method.JavaMethodVisitor;
import org.b333vv.metric.model.visitor.type.JavaClassVisitor;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;
import org.b333vv.metric.ui.settings.composition.ProjectMetricsTreeSettings;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangeStub;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangeStub;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesSettings;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;

import javax.swing.tree.DefaultTreeModel;
import java.util.Set;
import java.util.stream.Stream;

import static org.b333vv.metric.model.metric.MetricType.*;

public final class MetricsService {
    private DependenciesBuilder dependenciesBuilder;

    private CachedValue<DefaultTreeModel> projectTree;


    private MetricsService() {
        // Utility class
    }

    public static MetricsService instance() {
        return ServiceManager.getService(MetricsUtils.getCurrentProject(), MetricsService.class);
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

    public static Stream<JavaRecursiveElementVisitor> classVisitorsForProjectMetricsTree() {
        return MetricsUtils.getForProject(ProjectMetricsTreeSettings.class).getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
//                .filter(m -> (!getDeferredMetricTypes().contains(m.getType())))
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaClassVisitor);
    }

    public static Stream<JavaRecursiveElementVisitor> methodsVisitorsForProjectMetricsTree() {
        return MetricsUtils.getForProject(ProjectMetricsTreeSettings.class).getMetricsList().stream()
                .filter(MetricsTreeSettingsStub::isNeedToConsider)
                .map(m -> m.getType().visitor())
                .filter(m -> m instanceof JavaMethodVisitor);
    }

    public static boolean isNeedToConsiderProjectMetrics() {
        return MetricsUtils.getForProject(ProjectMetricsTreeSettings.class).isNeedToConsiderProjectMetrics();
    }

    public static boolean isNeedToConsiderPackageMetrics() {
        return MetricsUtils.getForProject(ProjectMetricsTreeSettings.class).isNeedToConsiderPackageMetrics();
    }

    public static boolean isControlValidRanges() {
        return MetricsUtils.getForProject(BasicMetricsValidRangesSettings.class)
                .isControlValidRanges();
    }

    public static boolean isShowClassMetricsTree() {
       return MetricsUtils.getForProject(ClassMetricsTreeSettings.class).isShowClassMetricsTree();
    }

    public static void setShowClassMetricsTree(boolean showClassMetricsTree) {
        MetricsUtils.getForProject(ClassMetricsTreeSettings.class).setShowClassMetricsTree(showClassMetricsTree);
        MetricsUtils.getCurrentProject()
                .getMessageBus().syncPublisher(MetricsEventListener.TOPIC).showClassMetricsTree(showClassMetricsTree);
    }

    public static void setDependenciesBuilder(DependenciesBuilder dependenciesBuilder) {
        MetricsService.instance().dependenciesBuilder = dependenciesBuilder;
    }
    public static DependenciesBuilder getDependenciesBuilder() {
        return MetricsService.instance().dependenciesBuilder;
    }

    public static Set<MetricType> getDeferredMetricTypes() {
        return Set.of(CBO, ATFD, TCC, LAA, FDP, NOAV, CINT, CDISP, MND, NOPA, NOAC, WOC);
//        return Set.of(CBO);
    }

    public static boolean isLongValueMetricType(MetricType metricType) {
        Set<MetricType> doubleValueMetricTypes = Set.of(TCC, I, A, D, MHF, AHF, MIF, AIF, CF, PF, LAA, CDISP, WOC);
        return !doubleValueMetricTypes.contains(metricType);
    }

    public static DefaultTreeModel getProjectTree(JavaProject javaProject) {
        final CachedValuesManager manager = CachedValuesManager.getManager(MetricsUtils.getCurrentProject());
        final Object[] dependencies = {PsiModificationTracker.MODIFICATION_COUNT, ModificationTracker.EVER_CHANGED,
                ProjectRootManager.getInstance(MetricsUtils.getCurrentProject())};
        if (instance().projectTree == null || instance().projectTree.getValue() == null) {
            MetricsUtils.getConsole().info("Building cache");
            instance().projectTree = manager.createCachedValue(new CachedValueProvider<DefaultTreeModel>() {
                public Result<DefaultTreeModel> compute() {
                    ProjectMetricTreeBuilder projectMetricTreeBuilder = new ProjectMetricTreeBuilder(javaProject);
                    DefaultTreeModel metricsTreeModel = projectMetricTreeBuilder.createMetricTreeModel();
                    return Result.create(metricsTreeModel, dependencies);
                }
            }, false);
        } else {
            MetricsUtils.getConsole().info("Getting from cache");
        }
//        project.getUserData()
        return instance().projectTree.getValue();
    }
}
