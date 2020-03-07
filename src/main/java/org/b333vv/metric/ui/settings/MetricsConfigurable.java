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

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nullable;
import javax.swing.*;

public class MetricsConfigurable implements Configurable, Configurable.NoMargin, Configurable.NoScroll {
    private final Project project;
    private final MetricsValidRangesSettings metricsValidRangesSettings;
    private final ClassMetricsTreeSettings classMetricsTreeSettings;
    private final ProjectMetricsTreeSettings projectMetricsTreeSettings;

    private SettingsPanel panel;

    public MetricsConfigurable(Project project) {
        this.project = project;
        this.metricsValidRangesSettings = project.getComponent(MetricsValidRangesSettings.class);
        this.classMetricsTreeSettings = project.getComponent(ClassMetricsTreeSettings.class);
        this.projectMetricsTreeSettings = project.getComponent(ProjectMetricsTreeSettings.class);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "MetricsTree";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (panel == null) {
            panel = new SettingsPanel(project);
        }
        return panel.getRootPane();
    }

    @Override
    public boolean isModified() {
        return panel != null && (panel.isModified(metricsValidRangesSettings)
                || panel.isModified(classMetricsTreeSettings)
                || panel.isModified(projectMetricsTreeSettings));
    }

    @Override
    public void apply() {
        if (panel != null) {
            panel.save(metricsValidRangesSettings);
            metricsValidRangesSettings.clearTemporaryControlledMetrics();
            metricsValidRangesSettings.clearTemporaryUnControlledMetrics();
            panel.save(classMetricsTreeSettings);
            panel.save(projectMetricsTreeSettings);
        }
    }

    @Override
    public void reset() {
        metricsValidRangesSettings.returnAllToUnControlledMetrics();
        metricsValidRangesSettings.returnAllToControlledMetrics();
    }

    @Override
    public void disposeUIResources() {}
}
