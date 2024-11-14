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
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.ui.settings.ConfigurationPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NORTHWEST;

public class MetricProfilePanel implements ConfigurationPanel<MetricProfileSettings1> {
    private static final String EMPTY_LABEL = "No metrics profiles configured";
    private final Project project;
    private JPanel panel;
    private MetricProfileTable metricProfileTable;

    public MetricProfilePanel(Project project, MetricProfileSettings1 settings) {
        this.project = project;
        createUIComponents(settings);
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(MetricProfileSettings1 settings) {
        return !metricProfileTable.getProfiles().equals(settings.getProfiles());
    }

    @Override
    public void save(MetricProfileSettings1 settings) {
        settings.setProfiles(metricProfileTable.getProfiles());
    }

    @Override
    public void load(MetricProfileSettings1 settings) {
        metricProfileTable.setProfiles(settings.getProfiles());
    }

    private boolean profileNameIsDuplicated(String key) {
        return metricProfileTable.getProfiles().containsKey(key);
    }

    private void createUIComponents(MetricProfileSettings1 settings) {

        Function<Map.Entry<String, List<MetricProfileItem>>,
                        Map.Entry<String, List<MetricProfileItem>>> onEdit = value -> {
            EditMetricProfileDialog dialog = new EditMetricProfileDialog(project, value, (key) -> false);
            if (dialog.showAndGet() && dialog.getMetricProfile() != null) {
                return dialog.getMetricProfile();
            }
            return null;
        };

        Supplier<Map.Entry<String, List<MetricProfileItem>>> onAdd = () -> {
            EditMetricProfileDialog dialog = new EditMetricProfileDialog(project, null, this::profileNameIsDuplicated);
            if (dialog.showAndGet() && dialog.getMetricProfile() != null) {
                return dialog.getMetricProfile();
            }
            return null;
        };

        metricProfileTable = new MetricProfileTable(onEdit, onAdd);
        metricProfileTable.setProfiles(settings.getProfiles());

        panel = new JPanel(new GridBagLayout());

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        panel.add(metricProfileTable.getComponent(), new GridBagConstraints(0, 1, 4, 2,
                1.0, 1.0, NORTHWEST, BOTH, insets, 40, 40));
    }
}
