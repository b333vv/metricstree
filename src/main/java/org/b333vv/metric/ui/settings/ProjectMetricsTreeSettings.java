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

package org.b333vv.metric.ui.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@State(name = "ProjectMetricsTreeSettings", storages = {@Storage("project-metrics-tree.xml")})
public final class ProjectMetricsTreeSettings implements PersistentStateComponent<ProjectMetricsTreeSettings>, BaseComponent {

    private static final String CLASS_LEVEL = "Class level";
    private static final String METHOD_LEVEL = "Method level";
    private static final String CHIDAMBER_KEMERER_METRICS_SET = "Chidamber-Kemerer metrics set";
    private static final String LORENZ_KIDD_METRICS_SET = "Lorenz-Kidd metrics set";
    private static final String LI_HENRY_METRICS_SET = "Li-Henry metrics set";

    private final List<MetricsTreeSettingsStub> projectTreeMetrics = new ArrayList<>();
    private boolean needToConsiderProjectMetrics;
    private boolean needToConsiderPackageMetrics;

    public ProjectMetricsTreeSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {
        needToConsiderProjectMetrics = true;
        needToConsiderPackageMetrics = true;

        //Chidamber-Kemerer metrics set
        projectTreeMetrics.add(new MetricsTreeSettingsStub("WMC", "Weighted Methods Per Class",
                CLASS_LEVEL, CHIDAMBER_KEMERER_METRICS_SET, true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("DIT", "Depth Of Inheritance Tree",
                CLASS_LEVEL, CHIDAMBER_KEMERER_METRICS_SET, true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOC", "Number Of Children",
                CLASS_LEVEL, CHIDAMBER_KEMERER_METRICS_SET, true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("RFC", "Response For A Class",
                CLASS_LEVEL, CHIDAMBER_KEMERER_METRICS_SET, true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("LCOM", "Lack Of Cohesion In Methods",
                CLASS_LEVEL, CHIDAMBER_KEMERER_METRICS_SET, true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("CBO", "Coupling Between Objects",
                CLASS_LEVEL, CHIDAMBER_KEMERER_METRICS_SET, true));

        //Lorenz-Kidd metrics set
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOA", "Number Of Attributes",
                CLASS_LEVEL, LORENZ_KIDD_METRICS_SET, true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOO", "Number Of Operations",
                CLASS_LEVEL, LORENZ_KIDD_METRICS_SET, true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOAM", "Number Of Added Methods",
                CLASS_LEVEL, LORENZ_KIDD_METRICS_SET, true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOOM", "Number of Overridden Methods",
                CLASS_LEVEL, LORENZ_KIDD_METRICS_SET, true));

        //Li-Henry metrics set
        projectTreeMetrics.add(new MetricsTreeSettingsStub("SIZE2", "Number Of Attributes And Methods",
                CLASS_LEVEL, LI_HENRY_METRICS_SET, true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOM", "Number Of Methods",
                CLASS_LEVEL, LI_HENRY_METRICS_SET, true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("MPC", "Message Passing Coupling",
                CLASS_LEVEL, LI_HENRY_METRICS_SET, true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("DAC", "Data Abstraction Coupling",
                CLASS_LEVEL, LI_HENRY_METRICS_SET, true));

        //Methods metrics set
        projectTreeMetrics.add(new MetricsTreeSettingsStub("CND", "Condition Nesting Depth",
                METHOD_LEVEL, "", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("LOC", "Lines Of Code",
                METHOD_LEVEL, "", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("LND", "Loop Nesting Depth",
                METHOD_LEVEL, "", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("CC", "McCabe Cyclomatic Complexity",
                METHOD_LEVEL, "", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOL", "Number Of Loops",
                METHOD_LEVEL, "", true));
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

    @NotNull
    @Override
    public String getComponentName() {
        return "ProjectMetricsTreeSettings";
    }

    public List<MetricsTreeSettingsStub> getMetricsList() {
        Comparator<MetricsTreeSettingsStub> compareByLevelAndName = Comparator
                .comparing(MetricsTreeSettingsStub::getLevel)
                .thenComparing(MetricsTreeSettingsStub::getName);
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
