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

package org.b333vv.metric.ui.settings.profile;

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
    private final Map.Entry<String, List<MetricProfileItem>> profile;
    private final MetricProfileItemTable metricProfileItemTable;
    private final JTextField profileName;

    public EditMetricProfileDialog(Project project, Map.Entry<String, List<MetricProfileItem>> profile, Function<String, Boolean> profileNameIsDuplicated) {
        super(project, false);
        setTitle("Edit Anti-Pattern Metric Profile");

        this.profile = Objects.requireNonNullElseGet(profile, () -> Map.entry("Metric Profile Name", new ArrayList<>()));

        metricProfileItemTable = new MetricProfileItemTable(this::onProfileItemAdd, this::onProfileItemEdit,
                this::onProfileItemRemove, this::getPossibleMetricTypes);
        metricProfileItemTable.setProfileItems(this.profile.getValue());

        panel = new JPanel(new GridBagLayout());
        JLabel profileNameLabel = new JLabel("Anti-Pattern Name");
        if (profile == null) {
            profileName = new JFormattedTextField("");
        } else {
            profileName = new JFormattedTextField(profile.getKey());
            profileName.setEditable(false);
        }

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        panel.add(profileNameLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 10, 0));
        panel.add(profileName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 10, 0));
        panel.add(metricProfileItemTable.getComponent(), new GridBagConstraints(0, 1, 4, 2, 1.0, 1.0,
                NORTHWEST, BOTH, insets, 40, 40));

        myOKAction = new DialogWrapper.OkAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    if (!(profileNameIsDuplicated.fun(profileName.getText())
                            || profileName.getText().isBlank())) {
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

    public Map.Entry<String, List<MetricProfileItem>> getMetricProfile() {
        return Map.entry(profileName.getText(), new ArrayList<>(profile.getValue()));
    }

    private void onProfileItemAdd(MetricProfileItem profileItem) {
        profile.getValue().add(profileItem);
    }

    private void onProfileItemEdit(MetricProfileItem profileItem) {
        profile.getValue().removeIf(item -> item.getName().equals(profileItem.getName()));
        profile.getValue().add(profileItem);
    }

    private void onProfileItemRemove(MetricProfileItem profileItem) {
        profile.getValue().remove(profileItem);
    }

    private ArrayList<String> getPossibleMetricTypes() {
        ArrayList<String> metricTypes = new ArrayList<>();
        for (MetricType metricType : MetricType.values()) {
            if (metricType.level() == MetricLevel.CLASS || metricType.level() == MetricLevel.METHOD) {
                metricTypes.add(metricType.name());
            }
        }
        List<MetricProfileItem> metricProfileItems = profile.getValue();
        if (metricProfileItems != null && !metricProfileItems.isEmpty()) {
            for (MetricProfileItem item : metricProfileItems) {
                metricTypes.removeIf(metricTypeName -> item.getName().equals(metricTypeName));
            }
        }
        return metricTypes;
    }
}
