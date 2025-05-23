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
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.Function;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;

import static java.awt.GridBagConstraints.*;

public class EditMetricProfileDialog extends DialogWrapper {
    private final JPanel panel;
    private final Map.Entry<String, List<FitnessFunctionItem>> profile;
    private final MetricProfileItemTable metricProfileItemTable;
    private final JTextField ffName;
    private final JTextField ffDescription;
    private final Set<MetricLevel> metricLevelSet;

    public EditMetricProfileDialog(Project project, Map.Entry<String, List<FitnessFunctionItem>> profile,
                                   String profileDescription,
                                   Function<String, Boolean> profileNameIsDuplicated,
                                   Set<MetricLevel> metricLevelSet) {
        super(project, false);
        this.metricLevelSet = metricLevelSet;
        setTitle("Edit Fitness Function");

        this.profile = Objects.requireNonNullElseGet(profile, () -> Map.entry("Edit Fitness Function", new ArrayList<>()));

        metricProfileItemTable = new MetricProfileItemTable(project, this::onProfileItemAdd, this::onProfileItemEdit,
                this::onProfileItemRemove, this::getPossibleMetricTypes);
        metricProfileItemTable.setProfileItems(this.profile.getValue());

        panel = new JPanel(new GridBagLayout());

        JLabel ffLabel = new JLabel("Fitness Function");
        JLabel descriptionLabel = new JLabel("Description");
        if (profile == null) {
            ffName = new JFormattedTextField("");
            ffDescription = new JFormattedTextField("");
        } else {
            ffName = new JFormattedTextField(profile.getKey());
            ffName.setEditable(false);
            ffDescription = new JFormattedTextField(profileDescription);
        }

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        panel.add(ffLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 10, 0));
        panel.add(ffName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 10, 0));
        panel.add(descriptionLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 10, 0));
        panel.add(ffDescription, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 10, 0));
        panel.add(metricProfileItemTable.getComponent(), new GridBagConstraints(0, 2, 4, 2, 1.0, 1.0,
                NORTHWEST, BOTH, insets, 40, 40));

        myOKAction = new DialogWrapper.OkAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    if (!(profileNameIsDuplicated.fun(ffName.getText())
                            || ffName.getText().isBlank())) {
                        super.actionPerformed(e);
                        dispose();
                    }
            }
        };
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }

    public Map.Entry<String, List<FitnessFunctionItem>> getMetricProfile() {
        return Map.entry(ffName.getText(), new ArrayList<>(profile.getValue()));
    }

    private void onProfileItemAdd(FitnessFunctionItem profileItem) {
        profile.getValue().add(profileItem);
    }

    private void onProfileItemEdit(FitnessFunctionItem profileItem) {
        profile.getValue().removeIf(item -> item.getName().equals(profileItem.getName()));
        profile.getValue().add(profileItem);
    }

    private void onProfileItemRemove(FitnessFunctionItem profileItem) {
        profile.getValue().remove(profileItem);
    }

    private ArrayList<String> getPossibleMetricTypes() {
        ArrayList<String> metricTypes = new ArrayList<>();
        for (MetricType metricType : MetricType.values()) {
            if (metricLevelSet.contains(metricType.level())) {
                metricTypes.add(metricType.name());
            }
        }
        List<FitnessFunctionItem> fitnessFunctionItems = profile.getValue();
        if (fitnessFunctionItems != null && !fitnessFunctionItems.isEmpty()) {
            for (FitnessFunctionItem item : fitnessFunctionItems) {
                metricTypes.removeIf(metricTypeName -> item.getName().equals(metricTypeName));
            }
        }
        return metricTypes;
    }

    public Map.Entry<String, String> getProfileDescription() {
        return new Map.Entry<>() {
            @Override
            public String getKey() {
                return ffName.getText();
            }

            @Override
            public String getValue() {
                return ffDescription.getText();
            }

            @Override
            public String setValue(String value) {
                return "";
            }
        };
    }
}
