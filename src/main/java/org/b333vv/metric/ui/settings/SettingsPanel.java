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
import org.b333vv.metric.ui.settings.fitnessfunction.*;
import org.b333vv.metric.ui.settings.other.OtherSettings;
import org.b333vv.metric.ui.settings.other.OtherSettingsPanel;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesPanel;
import org.b333vv.metric.util.SettingsService;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesPanel;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesSettings;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SettingsPanel {
    private final JPanel root;
    private final BasicMetricsValidRangesPanel basicMetricsValidRangesPanel;
    private final DerivativeMetricsValidRangesPanel derivativeMetricsValidRangesPanel;
    private final ClassMetricsTreeSettingsPanel classMetricsTreeSettingsPanel;
    private final ClassFitnessFunctionPanel classFitnessFunctionPanel;
    private final PackageFitnessFunctionPanel packageFitnessFunctionPanel;
    private final OtherSettingsPanel otherSettingsPanel;
    private final Project project;

    public SettingsPanel(Project project) {
        this.project = project;
        root = new JPanel(new BorderLayout());
        JBTabbedPane tabs = new JBTabbedPane();

        BasicMetricsValidRangesSettings basicMetricsValidRangesSettings =
                project.getService(SettingsService.class).getBasicMetricsSettings();
        DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings =
                project.getService(SettingsService.class).getDerivativeMetricsSettings();
        ClassMetricsTreeSettings classMetricsTreeSettings =
                project.getService(SettingsService.class).getClassMetricsTreeSettings();
        ClassLevelFitnessFunctions classLevelFitnessFunctions =
                project.getService(SettingsService.class).getClassLevelFitnessFunctions();
        PackageLevelFitnessFunctions packageLevelFitnessFunctions =
                project.getService(SettingsService.class).getPackageLevelFitnessFunctions();
        OtherSettings otherSettings =
                project.getService(SettingsService.class).getOtherSettings();

        basicMetricsValidRangesPanel = new BasicMetricsValidRangesPanel(project, basicMetricsValidRangesSettings);
        derivativeMetricsValidRangesPanel = new DerivativeMetricsValidRangesPanel(project, derivativeMetricsValidRangesSettings);
        classMetricsTreeSettingsPanel = new ClassMetricsTreeSettingsPanel(classMetricsTreeSettings);
        classFitnessFunctionPanel = new ClassFitnessFunctionPanel(project, classLevelFitnessFunctions);
        packageFitnessFunctionPanel = new PackageFitnessFunctionPanel(project, packageLevelFitnessFunctions);
        otherSettingsPanel = new OtherSettingsPanel(project, otherSettings);

        tabs.insertTab("Basic Metrics Valid Values", null, basicMetricsValidRangesPanel.getComponent(),
                "Configure valid values for basic metrics", 0);
        tabs.insertTab("Derivative Metrics Valid Values", null, derivativeMetricsValidRangesPanel.getComponent(),
                "Configure valid values for derivative metrics", 1);
        tabs.insertTab("Class Metrics Tree Composition", null, classMetricsTreeSettingsPanel.getComponent(),
                "Configure class metrics tree composition", 2);
        tabs.insertTab("Class Level Fitness Functions", null, classFitnessFunctionPanel.getComponent(),
                "Configure class level fitness functions", 3);
        tabs.insertTab("Package Level Fitness Functions", null, packageFitnessFunctionPanel.getComponent(),
                "Configure package level fitness functions", 4);
        tabs.insertTab("Other Settings", null, otherSettingsPanel.getComponent(),
                "Other settings", 5);

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


    public boolean isModified(ClassLevelFitnessFunctions classLevelFitnessFunctions) {
        return classFitnessFunctionPanel.isModified(classLevelFitnessFunctions);
    }

    public boolean isModified(PackageLevelFitnessFunctions packageLevelFitnessFunctions) {
        return packageFitnessFunctionPanel.isModified(packageLevelFitnessFunctions);
    }

    public boolean isModified(OtherSettings otherSettings) {
        return otherSettingsPanel.isModified(otherSettings);
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

    public void save(ClassLevelFitnessFunctions classLevelFitnessFunctions) {

        classFitnessFunctionPanel.save(classLevelFitnessFunctions);
    }

    public void save(PackageLevelFitnessFunctions packageLevelFitnessFunctions) {
        packageFitnessFunctionPanel.save(packageLevelFitnessFunctions);
    }

    public void save(OtherSettings otherSettings) {
        otherSettingsPanel.save(otherSettings);
    }
}
