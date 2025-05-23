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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.util.MetricsService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.Supplier;

import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.lang.Long.MAX_VALUE;

public class AddMetricProfileItemDialog extends DialogWrapper {
    private final JPanel panel;
    private final ComboBox metricType;
    private final JSpinner minValue;
    private final JSpinner maxValue;
    private JCheckBox minValueNotSet;
    private JCheckBox maxValueNotSet;
    private boolean isLong = true;
    private FitnessFunctionItem item = new FitnessFunctionItem();

    public AddMetricProfileItemDialog(Project project, Supplier<ArrayList<String>> getMetricTypeList) {
        super(project, false);
        setTitle("Add Metric Profile Item");

        metricType = new ComboBox(getMetricTypeList.get().toArray());

        metricType.addItemListener(arg -> {
            String metricTypeName = (String) metricType.getSelectedItem();

            boolean isLongFromCombo = project.getService(MetricsService.class).isLongValueMetricType(MetricType.valueOf(metricTypeName));
            if (isLongFromCombo != isLong) {
                isLong = isLongFromCombo;
                resetSpinnerModels();
            }
        });

        panel = new JPanel(new GridBagLayout());
        JLabel comboBoxLabel = new JLabel("Metric Type:");
        minValueNotSet = new JCheckBox("Min Value:", false);
        maxValueNotSet = new JCheckBox("Max Value:", false);
        minValue = new JSpinner();
        maxValue = new JSpinner();

        SpinnerNumberModel minValueModel = new SpinnerNumberModel(Long.valueOf(0),
                Long.valueOf(0), Long.valueOf(MAX_VALUE), Long.valueOf(1));
        SpinnerNumberModel maxValueModel = new SpinnerNumberModel(Long.valueOf(0),
                Long.valueOf(0), Long.valueOf(MAX_VALUE), Long.valueOf(1));
        minValue.setModel(minValueModel);
        maxValue.setModel(maxValueModel);
        minValue.setEnabled(false);
        maxValue.setEnabled(false);

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        panel.add(comboBoxLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(metricType, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(minValueNotSet, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(minValue, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(maxValueNotSet, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(maxValue, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));

        myOKAction = new OkAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!minValueNotSet.isSelected() && !maxValueNotSet.isSelected()) {
                    return;
                }
                if (isLong) {
                    item.setLong(true);
                    if (!maxValueNotSet.isSelected()) {
                        item.setMaxLongValue(MAX_VALUE);
                    } else {
                        item.setMaxLongValue((Long) maxValue.getModel().getValue());
                    }
                    item.setName((String) metricType.getSelectedItem());
                    item.setMinLongValue((Long) minValue.getModel().getValue());
                    if (item.getMaxLongValue() >= item.getMinLongValue()) {
                        super.actionPerformed(e);
                        dispose();
                    }
                } else {
                    item.setLong(false);
                    if (!maxValueNotSet.isSelected()) {
                        item.setMaxDoubleValue(MAX_VALUE);
                    } else {
                        double value = Math.round(((Double) maxValue.getModel().getValue()) * 100.0) / 100.0;
                        item.setMaxDoubleValue(value);
                    }
                    item.setName((String) metricType.getSelectedItem());
                    double value = Math.round(((Double) minValue.getModel().getValue()) * 100.0) / 100.0;
                    item.setMinDoubleValue(value);
                    if (item.getMaxDoubleValue() >= item.getMinDoubleValue()) {
                        super.actionPerformed(e);
                        dispose();
                    }
                }
            }
        };

        minValueNotSet.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                minValue.setEnabled(minValueNotSet.isSelected());
                minValue.updateUI();
                if (!minValueNotSet.isSelected()) {
                    minValue.setValue(isLong ? 0L : 0.0);
                }
            }
        });

        maxValueNotSet.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                maxValue.setEnabled(maxValueNotSet.isSelected());
                maxValue.updateUI();
                if (!maxValueNotSet.isSelected()) {
                    maxValue.setValue(isLong ? 0L : 0.0);
                }
            }
        });
        init();
    }

    private void resetSpinnerModels() {
        SpinnerNumberModel minValueModel;
        SpinnerNumberModel maxValueModel;
        if (isLong) {
            minValueModel = new SpinnerNumberModel(Long.valueOf(0), Long.valueOf(0),
                    Long.valueOf(MAX_VALUE), Long.valueOf(1));
            maxValueModel = new SpinnerNumberModel(Long.valueOf(0), Long.valueOf(0),
                    Long.valueOf(MAX_VALUE), Long.valueOf(1));
        } else {
            minValueModel = new SpinnerNumberModel(Double.valueOf(0.00), Double.valueOf(0.00),
                    Double.valueOf(MAX_VALUE), Double.valueOf(0.01));
            maxValueModel = new SpinnerNumberModel(Double.valueOf(0.00),
                    Double.valueOf(0.00), Double.valueOf(MAX_VALUE), Double.valueOf(0.01));
        }
        minValue.setModel(minValueModel);
        maxValue.setModel(maxValueModel);
        minValue.setEnabled(false);
        maxValue.setEnabled(false);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }

    public FitnessFunctionItem getMetricProfileItem() {
        return item;
    }
}
