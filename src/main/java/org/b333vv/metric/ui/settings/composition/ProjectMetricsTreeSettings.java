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
import org.b333vv.metric.model.metric.MetricType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@State(name = "ProjectMetricsTreeSettings", storages = {@Storage("project-metrics-tree.xml")})
public final class ProjectMetricsTreeSettings implements PersistentStateComponent<ProjectMetricsTreeSettings> {

    private final List<MetricsTreeSettingsStub> projectTreeMetrics = new ArrayList<>();
    private boolean needToConsiderProjectMetrics;
    private boolean needToConsiderPackageMetrics;

    public ProjectMetricsTreeSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {
        needToConsiderProjectMetrics = true;
        needToConsiderPackageMetrics = true;
        for (MetricType type : MetricType.values()) {
            projectTreeMetrics.add(new MetricsTreeSettingsStub(type, true));
        }
    }

    public List<MetricsTreeSettingsStub> getProjectTreeMetrics() {
        return new ArrayList<>(projectTreeMetrics);
    }

    public void setProjectTreeMetrics(List<MetricsTreeSettingsStub> metrics) {
        this.projectTreeMetrics.clear();
        this.projectTreeMetrics.addAll(metrics);
    }

    @Override
    public synchronized ProjectMetricsTreeSettings getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull ProjectMetricsTreeSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public List<MetricsTreeSettingsStub> getMetricsList() {
        Comparator<MetricsTreeSettingsStub> compareByLevelAndName = Comparator
                .comparing(MetricsTreeSettingsStub::getType);
        return projectTreeMetrics.stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

    public boolean isNeedToConsiderProjectMetrics() {
        return needToConsiderProjectMetrics;
    }

    public void setNeedToConsiderProjectMetrics(boolean needToConsiderProjectMetrics) {
        this.needToConsiderProjectMetrics = needToConsiderProjectMetrics;
    }

    public boolean isNeedToConsiderPackageMetrics() {
        return needToConsiderPackageMetrics;
    }

    public void setNeedToConsiderPackageMetrics(boolean needToConsiderPackageMetrics) {
        this.needToConsiderPackageMetrics = needToConsiderPackageMetrics;
    }
}
