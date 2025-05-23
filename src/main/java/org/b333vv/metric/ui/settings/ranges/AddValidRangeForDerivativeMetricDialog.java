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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Objects;

import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;

public class AddValidRangeForDerivativeMetricDialog extends DialogWrapper {
    private final JPanel panel;
    private final ComboBox metricsAllowableValuesRangeStubCombo;
    private final JSpinner minValue;
    private final JSpinner maxValue;
    private DerivativeMetricsValidRangeStub metricsAllowableValueRangeStub = null;

    public AddValidRangeForDerivativeMetricDialog(Project project) {
        super(project, false);
        setTitle("Add Range For Metric");

        DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings = project.getService(DerivativeMetricsValidRangesSettings.class);
        List<DerivativeMetricsValidRangeStub> uncontrolledMetrics = derivativeMetricsValidRangesSettings.getUnControlledMetricsList();
        metricsAllowableValuesRangeStubCombo = new ComboBox(uncontrolledMetrics.toArray());

        metricsAllowableValuesRangeStubCombo.addItemListener(arg -> {
            metricsAllowableValueRangeStub = (DerivativeMetricsValidRangeStub)
                    metricsAllowableValuesRangeStubCombo.getSelectedItem();
            setMetricsAllowableValueRangeStub(Objects.requireNonNull(metricsAllowableValueRangeStub));
        });

        panel = new JPanel(new GridBagLayout());
        JLabel minValueLabel = new JLabel("Minimum Valid Value");
        JLabel maxValueLabel = new JLabel("Maximum Valid Value");
        JLabel comboBoxLabel = new JLabel("Metric");
        minValue = new JSpinner();
        maxValue = new JSpinner();
        setMetricsAllowableValueRangeStub((DerivativeMetricsValidRangeStub)
                Objects.requireNonNull(metricsAllowableValuesRangeStubCombo.getSelectedItem()));

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        panel.add(comboBoxLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(metricsAllowableValuesRangeStubCombo, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(minValueLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(minValue, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(maxValueLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(maxValue, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));


        myOKAction = new OkAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                metricsAllowableValueRangeStub.setMinValue((Double) minValue.getModel().getValue());
                metricsAllowableValueRangeStub.setMaxValue((Double) maxValue.getModel().getValue());
                if (metricsAllowableValueRangeStub.getMaxValue() >= metricsAllowableValueRangeStub.getMinValue()) {
                    derivativeMetricsValidRangesSettings.removeFromUnControlledMetrics(metricsAllowableValueRangeStub.getName());
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

    public DerivativeMetricsValidRangeStub getMetricsAllowableValueRangeStub() {
        return metricsAllowableValueRangeStub;
    }

    public void setMetricsAllowableValueRangeStub(DerivativeMetricsValidRangeStub metricsAllowableValueRangeStub) {
        this.metricsAllowableValueRangeStub = metricsAllowableValueRangeStub;
        SpinnerNumberModel minModel = new SpinnerNumberModel(Double.valueOf(this.metricsAllowableValueRangeStub.getMinValue()),
                Double.valueOf(0.00), Double.valueOf(1000000.00), Double.valueOf(0.01));
        SpinnerNumberModel maxModel = new SpinnerNumberModel(Double.valueOf(this.metricsAllowableValueRangeStub.getMaxValue()),
                Double.valueOf(0.00), Double.valueOf(1000000.00), Double.valueOf(0.01));
        minValue.setModel(minModel);
        maxValue.setModel(maxModel);
    }
}
