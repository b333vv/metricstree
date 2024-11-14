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

package org.b333vv.metric.ui.settings.composition;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.util.MetricsService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@State(
        name = "ClassMetricsTreeSettings",
        storages = {@Storage("class-metrics-tree.xml")}
)
public final class ClassMetricsTreeSettings1 implements PersistentStateComponent<ClassMetricsTreeSettings1> {
    private final List<MetricsTreeSettingsStub> classTreeMetrics = new ArrayList<>();
    private boolean showClassMetricsTree;

    public ClassMetricsTreeSettings1() {
        loadInitialValues();
    }

    private void loadInitialValues() {
        showClassMetricsTree = true;
        for (MetricType type : MetricType.values()) {
            if (!MetricsService.getDeferredMetricTypes().contains(type) && (type.level() == MetricLevel.CLASS || type.level() == MetricLevel.METHOD)) {
                classTreeMetrics.add(new MetricsTreeSettingsStub(type, true));
            }
        }
    }

    public void setClassTreeMetrics(List<MetricsTreeSettingsStub> metrics) {
        this.classTreeMetrics.clear();
        this.classTreeMetrics.addAll(metrics);
    }

    @Override
    public synchronized ClassMetricsTreeSettings1 getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull ClassMetricsTreeSettings1 state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public List<MetricsTreeSettingsStub> getMetricsList() {
        Comparator<MetricsTreeSettingsStub> compareByType = Comparator
                .comparing(MetricsTreeSettingsStub::getType);
        return classTreeMetrics.stream()
                .sorted(compareByType)
                .collect(Collectors.toList());
    }


    public void setShowClassMetricsTree(boolean showClassMetricsTree) {
        this.showClassMetricsTree = showClassMetricsTree;
    }

    public boolean isShowClassMetricsTree() {
        return showClassMetricsTree;
    }
}
