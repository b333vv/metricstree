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

package org.b333vv.metric.ui.settings.fitnessfunction;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.ui.settings.ConfigurationPanel;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NORTHWEST;

public class ClassFitnessFunctionPanel implements ConfigurationPanel<ClassLevelFitnessFunctions> {
    private static final String EMPTY_LABEL = "No customized fitness functions";
    private final Project project;
    private JPanel panel;
    private MetricProfileTable metricProfileTable;
    private final Map<String, String> profilesDescriptions;
    private final Map<String, List<FitnessFunctionItem>> unmodifiableProfiles;

    public ClassFitnessFunctionPanel(Project project, ClassLevelFitnessFunctions settings) {
        this.project = project;
        profilesDescriptions = settings.getProfilesDescription();
        unmodifiableProfiles = Collections.unmodifiableMap(settings.getProfiles());
        createUIComponents(settings);
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(ClassLevelFitnessFunctions settings) {
        return !metricProfileTable.getProfiles().equals(unmodifiableProfiles)
                || !profilesDescriptions.equals(settings.getProfilesDescription());
    }

    @Override
    public void save(ClassLevelFitnessFunctions settings) {
        settings.setProfiles(metricProfileTable.getProfiles());
        settings.setProfilesDescription(profilesDescriptions);
    }

    @Override
    public void load(ClassLevelFitnessFunctions settings) {
        metricProfileTable.setProfiles(settings.getProfiles());
        profilesDescriptions.putAll(settings.getProfilesDescription());
    }

    private boolean profileNameIsDuplicated(String key) {
        return metricProfileTable.getProfiles().containsKey(key);
    }

    private void createUIComponents(ClassLevelFitnessFunctions settings) {

        var metricLevelSet = new HashSet<MetricLevel>();
        metricLevelSet.add(MetricLevel.CLASS);
        metricLevelSet.add(MetricLevel.METHOD);

        Function<Map.Entry<String, List<FitnessFunctionItem>>,
                        Map.Entry<String, List<FitnessFunctionItem>>> onEdit = value -> {
            EditMetricProfileDialog dialog = new EditMetricProfileDialog(project, value,
                    profilesDescriptions.get(value.getKey()), (key) -> false, metricLevelSet);
            if (dialog.showAndGet() && dialog.getMetricProfile() != null) {
                profilesDescriptions.put(dialog.getProfileDescription().getKey(), dialog.getProfileDescription().getValue());
                return dialog.getMetricProfile();
            }
            return null;
        };

        Supplier<Map.Entry<String, List<FitnessFunctionItem>>> onAdd = () -> {
            EditMetricProfileDialog dialog = new EditMetricProfileDialog(project, null, null, this::profileNameIsDuplicated, metricLevelSet);
            if (dialog.showAndGet() && dialog.getMetricProfile() != null) {
                profilesDescriptions.put(dialog.getProfileDescription().getKey(), dialog.getProfileDescription().getValue());
                return dialog.getMetricProfile();
            }
            return null;
        };

        metricProfileTable = new MetricProfileTable(onEdit, onAdd);
        metricProfileTable.setProfiles(settings.getProfiles());

        panel = new JPanel(new GridBagLayout());

        JBInsets insets = JBUI.insets(2);

        panel.add(metricProfileTable.getComponent(), new GridBagConstraints(0, 1, 4, 2,
                1.0, 1.0, NORTHWEST, BOTH, insets, 40, 40));
    }
}