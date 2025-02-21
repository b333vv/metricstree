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
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.model.metric.MetricType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.lang.Long.MAX_VALUE;

public class EditMetricProfileItemDialog extends DialogWrapper {
    private final JPanel panel;
    private final JSpinner minValue;
    private final JSpinner maxValue;
    private JCheckBox minValueNotSet;
    private JCheckBox maxValueNotSet;
    private final FitnessFunctionItem item;

    public EditMetricProfileItemDialog(Project project, FitnessFunctionItem item) {
        super(project, false);
        setTitle("Edit Metric Profile Item");

        this.item = item;

        panel = new JPanel(new GridBagLayout());
        minValueNotSet = new JCheckBox("Min Value:", item.getMinLongValue() > 0L || item.getMinDoubleValue() > 0.00);
        maxValueNotSet = new JCheckBox("Max Value:", (item.getMaxLongValue() != MAX_VALUE) && (item.getMaxLongValue() != 0L) ||
                (item.getMaxDoubleValue() != MAX_VALUE) && (item.getMaxDoubleValue() > 0.00));
        JLabel metricTypeNameLabel = new JLabel("Metric Type:");
        JLabel metricTypeName = new JLabel(MetricType.valueOf(item.getName()).description() + " [" + item.getName() + "]");
        minValue = new JSpinner();
        maxValue = new JSpinner();
        SpinnerNumberModel minValueModel;
        SpinnerNumberModel maxValueModel;
        if (this.item.isLong()) {
            minValueModel = new SpinnerNumberModel(Long.valueOf(this.item.getMinLongValue()),
                    Long.valueOf(0), Long.valueOf(MAX_VALUE), Long.valueOf(1));
            maxValueModel = new SpinnerNumberModel(Long.valueOf(this.item.getMaxLongValue()),
                    Long.valueOf(0), Long.valueOf(MAX_VALUE), Long.valueOf(1));
        } else {
            minValueModel = new SpinnerNumberModel(Double.valueOf(this.item.getMinDoubleValue()),
                    Double.valueOf(0.00), Double.valueOf(MAX_VALUE), Double.valueOf(0.01));
            maxValueModel = new SpinnerNumberModel(Double.valueOf(this.item.getMaxDoubleValue()),
                    Double.valueOf(0.00), Double.valueOf(MAX_VALUE), Double.valueOf(0.01));
        }
        minValue.setModel(minValueModel);
        maxValue.setModel(maxValueModel);

        if (!maxValueNotSet.isSelected()) {
            maxValue.setValue(item.isLong() ? 0L : 0.00);
            maxValue.setEnabled(false);
            maxValue.updateUI();
        }

        if (!minValueNotSet.isSelected()) {
            minValue.setValue(item.isLong() ? 0L : 0.00);
            minValue.setEnabled(false);
            minValue.updateUI();
        }

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        panel.add(metricTypeNameLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(metricTypeName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
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
                if (item.isLong()) {
                    item.setLong(true);
                    if (!maxValueNotSet.isSelected()) {
                        item.setMaxLongValue(MAX_VALUE);
                    } else {
                        item.setMaxLongValue((Long) maxValue.getModel().getValue());
                    }
                    Object value = minValue.getModel().getValue();
                    if (value instanceof Double) {
                        item.setMinLongValue(((Double) minValue.getModel().getValue()).longValue());
                    } else {
                        item.setMinLongValue((Long) minValue.getModel().getValue());
                    }
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
                    minValue.setValue(item.isLong() ? 0L : 0.0);
                }
            }
        });

        maxValueNotSet.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                maxValue.setEnabled(maxValueNotSet.isSelected());
                maxValue.updateUI();
                if (!maxValueNotSet.isSelected()) {
                    maxValue.setValue(item.isLong() ? 0L : 0.0);
                }
            }
        });

        init();
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
