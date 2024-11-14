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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.awt.GridBagConstraints.*;

public class BasicMetricsValidRangesPanel implements ConfigurationPanel<BasicMetricsValidRangesSettings1> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private final Project project;
    private JPanel panel;
    private BasicMetricsValidRangesTable basicMetricsValidRangesTable;
    private JCheckBox controlValidRanges;

    public BasicMetricsValidRangesPanel(Project project, BasicMetricsValidRangesSettings1 basicMetricsValidRangesSettings1) {
        this.project = project;
        createUIComponents(basicMetricsValidRangesSettings1);
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(BasicMetricsValidRangesSettings1 settings) {
        List<BasicMetricsValidRangeStub> rows = settings.getControlledMetricsList();
        return !rows.equals(basicMetricsValidRangesTable.get())
                || settings.isControlValidRanges() != controlValidRanges.isSelected();
    }

    @Override
    public void save(BasicMetricsValidRangesSettings1 settings) {
        Map<String, BasicMetricsValidRangeStub> newMetricsMap = basicMetricsValidRangesTable.get()
                .stream()
                .collect(Collectors.toMap(BasicMetricsValidRangeStub::getName, x -> x));
        settings.setControlledMetrics(newMetricsMap);
        settings.setControlValidRanges(controlValidRanges.isSelected());
    }

    @Override
    public void load(BasicMetricsValidRangesSettings1 settings) {
        basicMetricsValidRangesTable.set(settings.getControlledMetricsList());
        controlValidRanges.setSelected(settings.isControlValidRanges());
    }

    private void createUIComponents(BasicMetricsValidRangesSettings1 basicMetricsValidRangesSettings1) {

        controlValidRanges = new JCheckBox("Take Basic Metrics Values Under Control",
                        basicMetricsValidRangesSettings1.isControlValidRanges());

        Function<BasicMetricsValidRangeStub, BasicMetricsValidRangeStub> onEdit = value -> {
            EditValidRangeForBasicMetricDialog dialog = new EditValidRangeForBasicMetricDialog(project);
            dialog.setMetricsAllowableValueRangeStub(value);
            if (dialog.showAndGet() && dialog.getMetricsAllowableValueRangeStub() != null) {
                return dialog.getMetricsAllowableValueRangeStub();
            }
            return null;
        };

        Supplier<BasicMetricsValidRangeStub> onAdd = () -> {
            if (!basicMetricsValidRangesSettings1.getUnControlledMetricsList().isEmpty()) {
                AddValidRangeForBasicMetricDialog dialog = new AddValidRangeForBasicMetricDialog(project);
                if (dialog.showAndGet() && dialog.getMetricsAllowableValueRangeStub() != null) {
                    return dialog.getMetricsAllowableValueRangeStub();
                } else {
                    return null;
                }
            }
            return null;
        };

        basicMetricsValidRangesTable = new BasicMetricsValidRangesTable(EMPTY_LABEL, onEdit, onAdd, project);

        panel = new JPanel(new GridBagLayout());

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        JPanel tablePanel = basicMetricsValidRangesTable.getComponent();

        panel.add(controlValidRanges, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));

        panel.add(tablePanel, new GridBagConstraints(0, 1, 4, 2, 1.0, 1.0,
                NORTHWEST, BOTH, insets, 40, 40));

        basicMetricsValidRangesTable.enableComponents(tablePanel, controlValidRanges.isSelected());

        controlValidRanges.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                basicMetricsValidRangesTable.enableComponents(tablePanel, controlValidRanges.isSelected());
            }
        });
    }
}
