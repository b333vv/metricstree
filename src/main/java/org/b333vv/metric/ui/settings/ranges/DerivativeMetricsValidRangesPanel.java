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

package org.b333vv.metric.ui.settings.ranges;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.ui.settings.ConfigurationPanel;
import org.b333vv.metric.util.MetricsService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.awt.GridBagConstraints.*;

public class DerivativeMetricsValidRangesPanel implements ConfigurationPanel<DerivativeMetricsValidRangesSettings> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private final Project project;
    private JPanel panel;
    private DerivativeMetricsValidRangesTable derivativeMetricsValidRangesTable;

    public DerivativeMetricsValidRangesPanel(Project project, DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings) {
        this.project = project;
        createUIComponents(derivativeMetricsValidRangesSettings);
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(DerivativeMetricsValidRangesSettings settings) {
        List<DerivativeMetricsValidRangeStub> rows = settings.getControlledMetricsList();
        return !rows.equals(derivativeMetricsValidRangesTable.get());
    }

    @Override
    public void save(DerivativeMetricsValidRangesSettings settings) {
        Map<String, DerivativeMetricsValidRangeStub> newMetricsMap = derivativeMetricsValidRangesTable.get()
                .stream()
                .collect(Collectors.toMap(DerivativeMetricsValidRangeStub::getName, x -> x));
        settings.setControlledMetrics(newMetricsMap);
    }

    @Override
    public void load(DerivativeMetricsValidRangesSettings settings) {
        derivativeMetricsValidRangesTable.set(settings.getControlledMetricsList());
    }

    private void createUIComponents(DerivativeMetricsValidRangesSettings settings) {

        Function<DerivativeMetricsValidRangeStub, DerivativeMetricsValidRangeStub> onEdit = value -> {
            EditValidRangeForDerivativeMetricDialog dialog = new EditValidRangeForDerivativeMetricDialog(project);
            dialog.setMetricsAllowableValueRangeStub(value);
            if (dialog.showAndGet() && dialog.getMetricsAllowableValueRangeStub() != null) {
                return dialog.getMetricsAllowableValueRangeStub();
            }
            return null;
        };

        Supplier<DerivativeMetricsValidRangeStub> onAdd = () -> {
            if (!settings.getUnControlledMetricsList().isEmpty()) {
                AddValidRangeForDerivativeMetricDialog dialog = new AddValidRangeForDerivativeMetricDialog(project);
                if (dialog.showAndGet() && dialog.getMetricsAllowableValueRangeStub() != null) {
                    return dialog.getMetricsAllowableValueRangeStub();
                } else {
                    return null;
                }
            }
            return null;
        };

        derivativeMetricsValidRangesTable = new DerivativeMetricsValidRangesTable(EMPTY_LABEL, onEdit, onAdd, project);

        panel = new JPanel(new GridBagLayout());

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        JPanel tablePanel = derivativeMetricsValidRangesTable.getComponent();

        panel.add(tablePanel, new GridBagConstraints(0, 1, 4, 2, 1.0, 1.0,
                NORTHWEST, BOTH, insets, 40, 40));

        derivativeMetricsValidRangesTable.enableComponents(tablePanel, project.getService(MetricsService.class).isControlValidRanges());
    }
}
