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

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettingsPanel;
import org.b333vv.metric.ui.settings.composition.ProjectMetricsTreeSettings;
import org.b333vv.metric.ui.settings.composition.ProjectMetricsTreeSettingsPanel;
import org.b333vv.metric.ui.settings.ranges.MetricsValidRangesPanel;
import org.b333vv.metric.ui.settings.ranges.MetricsValidRangesSettings;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel {
    private final JPanel root;
    private final MetricsValidRangesPanel metricsValidRangesPanel;
    private final ClassMetricsTreeSettingsPanel classMetricsTreeSettingsPanel;
    private final ProjectMetricsTreeSettingsPanel projectMetricsTreeSettingsPanel;
    private final Project project;

    public SettingsPanel(Project project) {
        this.project = project;
        root = new JPanel(new BorderLayout());
        JBTabbedPane tabs = new JBTabbedPane();

        MetricsValidRangesSettings metricsValidRangesSettings =
                MetricsUtils.get(project, MetricsValidRangesSettings.class);
        ClassMetricsTreeSettings classMetricsTreeSettings =
                MetricsUtils.get(project, ClassMetricsTreeSettings.class);
        ProjectMetricsTreeSettings projectMetricsTreeSettings =
                MetricsUtils.get(project, ProjectMetricsTreeSettings.class);

        metricsValidRangesPanel = new MetricsValidRangesPanel(project, metricsValidRangesSettings);
        classMetricsTreeSettingsPanel = new ClassMetricsTreeSettingsPanel(classMetricsTreeSettings);
        projectMetricsTreeSettingsPanel = new ProjectMetricsTreeSettingsPanel(projectMetricsTreeSettings);

        tabs.insertTab("Metrics Valid Values", null, metricsValidRangesPanel.getComponent(),
                "Configure valid values ", 0);
        tabs.insertTab("Class Metrics Tree Composition", null, classMetricsTreeSettingsPanel.getComponent(),
                "Configure class metrics tree composition", 1);
        tabs.insertTab("Project Metrics Tree Composition", null, projectMetricsTreeSettingsPanel.getComponent(),
                "Configure project metrics tree composition", 2);

        root.add(tabs, BorderLayout.CENTER);
    }

    public Project getProject() {
        return project;
    }

    public JComponent getRootPane() {
        return root;
    }

    public boolean isModified(MetricsValidRangesSettings metricsValidRangesSettings) {
        return metricsValidRangesPanel.isModified(metricsValidRangesSettings);
    }

    public boolean isModified(ClassMetricsTreeSettings classMetricsTreeSettings) {
        return classMetricsTreeSettingsPanel.isModified(classMetricsTreeSettings);
    }

    public boolean isModified(ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        return projectMetricsTreeSettingsPanel.isModified(projectMetricsTreeSettings);
    }

    public void save(MetricsValidRangesSettings metricsValidRangesSettings) {
        metricsValidRangesPanel.save(metricsValidRangesSettings);
    }

    public void save(ClassMetricsTreeSettings classMetricsTreeSettings) {
        classMetricsTreeSettingsPanel.save(classMetricsTreeSettings);
    }

    public void save(ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        projectMetricsTreeSettingsPanel.save(projectMetricsTreeSettings);
    }
}
