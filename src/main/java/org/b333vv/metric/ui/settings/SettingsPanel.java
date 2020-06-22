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
import org.b333vv.metric.ui.settings.profile.MetricProfilePanel;
import org.b333vv.metric.ui.settings.profile.MetricProfileSettings;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesPanel;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesPanel;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesSettings;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel {
    private final JPanel root;
    private final BasicMetricsValidRangesPanel basicMetricsValidRangesPanel;
    private final DerivativeMetricsValidRangesPanel derivativeMetricsValidRangesPanel;
    private final ClassMetricsTreeSettingsPanel classMetricsTreeSettingsPanel;
    private final ProjectMetricsTreeSettingsPanel projectMetricsTreeSettingsPanel;
    private final MetricProfilePanel metricProfilePanel;
    private final Project project;

    public SettingsPanel(Project project) {
        this.project = project;
        root = new JPanel(new BorderLayout());
        JBTabbedPane tabs = new JBTabbedPane();

        BasicMetricsValidRangesSettings basicMetricsValidRangesSettings =
                MetricsUtils.get(project, BasicMetricsValidRangesSettings.class);
        DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings =
                MetricsUtils.get(project, DerivativeMetricsValidRangesSettings.class);
        ClassMetricsTreeSettings classMetricsTreeSettings =
                MetricsUtils.get(project, ClassMetricsTreeSettings.class);
        ProjectMetricsTreeSettings projectMetricsTreeSettings =
                MetricsUtils.get(project, ProjectMetricsTreeSettings.class);
        MetricProfileSettings metricProfileSettings =
                MetricsUtils.get(project, MetricProfileSettings.class);

        basicMetricsValidRangesPanel = new BasicMetricsValidRangesPanel(project, basicMetricsValidRangesSettings);
        derivativeMetricsValidRangesPanel = new DerivativeMetricsValidRangesPanel(project, derivativeMetricsValidRangesSettings);
        classMetricsTreeSettingsPanel = new ClassMetricsTreeSettingsPanel(classMetricsTreeSettings);
        projectMetricsTreeSettingsPanel = new ProjectMetricsTreeSettingsPanel(projectMetricsTreeSettings);
        metricProfilePanel = new MetricProfilePanel(project, metricProfileSettings);

        tabs.insertTab("Basic Metrics Valid Values", null, basicMetricsValidRangesPanel.getComponent(),
                "Configure valid values for basic metrics", 0);
        tabs.insertTab("Derivative Metrics Valid Values", null, derivativeMetricsValidRangesPanel.getComponent(),
                "Configure valid values for derivative metrics", 1);
        tabs.insertTab("Class Metrics Tree Composition", null, classMetricsTreeSettingsPanel.getComponent(),
                "Configure class metrics tree composition", 2);
        tabs.insertTab("Project Metrics Tree Composition", null, projectMetricsTreeSettingsPanel.getComponent(),
                "Configure project metrics tree composition", 3);
        tabs.insertTab("Anti-Patterns", null, metricProfilePanel.getComponent(),
                "Configure anti-patterns", 4);

        root.add(tabs, BorderLayout.CENTER);
    }

    public Project getProject() {
        return project;
    }

    public JComponent getRootPane() {
        return root;
    }

    public boolean isModified(BasicMetricsValidRangesSettings basicMetricsValidRangesSettings) {
        return basicMetricsValidRangesPanel.isModified(basicMetricsValidRangesSettings);
    }

    public boolean isModified(DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings) {
        return derivativeMetricsValidRangesPanel.isModified(derivativeMetricsValidRangesSettings);
    }

    public boolean isModified(ClassMetricsTreeSettings classMetricsTreeSettings) {
        return classMetricsTreeSettingsPanel.isModified(classMetricsTreeSettings);
    }

    public boolean isModified(ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        return projectMetricsTreeSettingsPanel.isModified(projectMetricsTreeSettings);
    }

    public boolean isModified(MetricProfileSettings metricProfileSettings) {
        return metricProfilePanel.isModified(metricProfileSettings);
    }

    public void save(BasicMetricsValidRangesSettings basicMetricsValidRangesSettings) {
        basicMetricsValidRangesPanel.save(basicMetricsValidRangesSettings);
    }

    public void save(DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings) {
        derivativeMetricsValidRangesPanel.save(derivativeMetricsValidRangesSettings);
    }

    public void save(ClassMetricsTreeSettings classMetricsTreeSettings) {
        classMetricsTreeSettingsPanel.save(classMetricsTreeSettings);
    }

    public void save(ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        projectMetricsTreeSettingsPanel.save(projectMetricsTreeSettings);
    }

    public void save(MetricProfileSettings metricProfileSettings) {
        metricProfilePanel.save(metricProfileSettings);
    }
}
