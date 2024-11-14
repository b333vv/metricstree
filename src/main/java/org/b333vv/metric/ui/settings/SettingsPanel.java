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
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings1;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettingsPanel;
import org.b333vv.metric.ui.settings.other.OtherSettings1;
import org.b333vv.metric.ui.settings.other.OtherSettingsPanel;
import org.b333vv.metric.ui.settings.profile.MetricProfilePanel;
import org.b333vv.metric.ui.settings.profile.MetricProfileSettings1;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesPanel;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings1;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesPanel;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesSettings1;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel {
    private final JPanel root;
    private final BasicMetricsValidRangesPanel basicMetricsValidRangesPanel;
    private final DerivativeMetricsValidRangesPanel derivativeMetricsValidRangesPanel;
    private final ClassMetricsTreeSettingsPanel classMetricsTreeSettingsPanel;
    private final MetricProfilePanel metricProfilePanel;
    private final OtherSettingsPanel otherSettingsPanel;
    private final Project project;

    public SettingsPanel(Project project) {
        this.project = project;
        root = new JPanel(new BorderLayout());
        JBTabbedPane tabs = new JBTabbedPane();

//        BasicMetricsValidRangesSettings basicMetricsValidRangesSettings =
//                MetricsUtils.get(project, BasicMetricsValidRangesSettings.class);
        BasicMetricsValidRangesSettings1 basicMetricsValidRangesSettings1 =
                project.getService(BasicMetricsValidRangesSettings1.class);
        DerivativeMetricsValidRangesSettings1 derivativeMetricsValidRangesSettings1 =
                project.getService(DerivativeMetricsValidRangesSettings1.class);
//        DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings =
//                MetricsUtils.get(project, DerivativeMetricsValidRangesSettings.class);
//        ClassMetricsTreeSettings classMetricsTreeSettings =
//                MetricsUtils.get(project, ClassMetricsTreeSettings.class);

        ClassMetricsTreeSettings1 classMetricsTreeSettings1 =
                project.getService(ClassMetricsTreeSettings1.class);

//        MetricProfileSettings metricProfileSettings =
//                MetricsUtils.get(project, MetricProfileSettings.class);
        MetricProfileSettings1 metricProfileSettings1 =
                project.getService(MetricProfileSettings1.class);
        OtherSettings1 otherSettings1 =
                project.getService(OtherSettings1.class);
//      OtherSettings otherSettings =
//                MetricsUtils.get(project, OtherSettings.class);

        basicMetricsValidRangesPanel = new BasicMetricsValidRangesPanel(project, basicMetricsValidRangesSettings1);
        derivativeMetricsValidRangesPanel = new DerivativeMetricsValidRangesPanel(project, derivativeMetricsValidRangesSettings1);
        classMetricsTreeSettingsPanel = new ClassMetricsTreeSettingsPanel(classMetricsTreeSettings1);
        metricProfilePanel = new MetricProfilePanel(project, metricProfileSettings1);
        otherSettingsPanel = new OtherSettingsPanel(project, otherSettings1);

        tabs.insertTab("Basic Metrics Valid Values", null, basicMetricsValidRangesPanel.getComponent(),
                "Configure valid values for basic metrics", 0);
        tabs.insertTab("Derivative Metrics Valid Values", null, derivativeMetricsValidRangesPanel.getComponent(),
                "Configure valid values for derivative metrics", 1);
        tabs.insertTab("Class Metrics Tree Composition", null, classMetricsTreeSettingsPanel.getComponent(),
                "Configure class metrics tree composition", 2);
        tabs.insertTab("Metrics Profiles", null, metricProfilePanel.getComponent(),
                "Configure metric profiles", 3);
        tabs.insertTab("Other Settings", null, otherSettingsPanel.getComponent(),
                "Other settings", 4);

        root.add(tabs, BorderLayout.CENTER);
    }

    public Project getProject() {
        return project;
    }

    public JComponent getRootPane() {
        return root;
    }

    public boolean isModified(BasicMetricsValidRangesSettings1 basicMetricsValidRangesSettings1) {
        return basicMetricsValidRangesPanel.isModified(basicMetricsValidRangesSettings1);
    }

    public boolean isModified(DerivativeMetricsValidRangesSettings1 derivativeMetricsValidRangesSettings1) {
        return derivativeMetricsValidRangesPanel.isModified(derivativeMetricsValidRangesSettings1);
    }

    public boolean isModified(ClassMetricsTreeSettings1 classMetricsTreeSettings1) {
        return classMetricsTreeSettingsPanel.isModified(classMetricsTreeSettings1);
    }


    public boolean isModified(MetricProfileSettings1 metricProfileSettings1) {
        return metricProfilePanel.isModified(metricProfileSettings1);
    }

    public boolean isModified(OtherSettings1 otherSettings1) {
        return otherSettingsPanel.isModified(otherSettings1);
    }

    public void save(BasicMetricsValidRangesSettings1 basicMetricsValidRangesSettings1) {
        basicMetricsValidRangesPanel.save(basicMetricsValidRangesSettings1);
    }

    public void save(DerivativeMetricsValidRangesSettings1 derivativeMetricsValidRangesSettings1) {
        derivativeMetricsValidRangesPanel.save(derivativeMetricsValidRangesSettings1);
    }

    public void save(ClassMetricsTreeSettings1 classMetricsTreeSettings1) {
        classMetricsTreeSettingsPanel.save(classMetricsTreeSettings1);
    }

    public void save(MetricProfileSettings1 metricProfileSettings1) {
        metricProfilePanel.save(metricProfileSettings1);
    }

    public void save(OtherSettings1 otherSettings1) {
        otherSettingsPanel.save(otherSettings1);
    }
}
