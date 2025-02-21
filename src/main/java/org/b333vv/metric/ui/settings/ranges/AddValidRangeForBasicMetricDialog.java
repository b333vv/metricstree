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

public class AddValidRangeForBasicMetricDialog extends DialogWrapper {
    private final JPanel panel;
    private final ComboBox metricsAllowableValuesRangeStubCombo;
    private final JSpinner regularBound;
    private final JSpinner highBound;
    private final JSpinner veryHighBound;
    private BasicMetricsValidRangeStub metricsAllowableValueRangeStub = null;

    public AddValidRangeForBasicMetricDialog(Project project) {
        super(project, false);
        setTitle("Add Range For Basic Metric");

        BasicMetricsValidRangesSettings basicMetricsValidRangesSettings = MetricsUtils.get(project, BasicMetricsValidRangesSettings.class);
        List<BasicMetricsValidRangeStub> uncontrolledMetrics = basicMetricsValidRangesSettings.getUnControlledMetricsList();
        metricsAllowableValuesRangeStubCombo = new ComboBox(uncontrolledMetrics.toArray());

        metricsAllowableValuesRangeStubCombo.addItemListener(arg -> {
            metricsAllowableValueRangeStub = (BasicMetricsValidRangeStub)
                    metricsAllowableValuesRangeStubCombo.getSelectedItem();
            setMetricsAllowableValueRangeStub(Objects.requireNonNull(metricsAllowableValueRangeStub));
        });

        panel = new JPanel(new GridBagLayout());
        JLabel regularBoundLabel = new JLabel("Regular Range Upper Bound");
        JLabel highBoundLabel = new JLabel("High Range Upper Bound");
        JLabel veryHighBoundLabel = new JLabel("Very-high Range Upper Bound");
        JLabel comboBoxLabel = new JLabel("Metric");
        regularBound = new JSpinner();
        highBound = new JSpinner();
        veryHighBound = new JSpinner();
        setMetricsAllowableValueRangeStub((BasicMetricsValidRangeStub)
                Objects.requireNonNull(metricsAllowableValuesRangeStubCombo.getSelectedItem()));

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        panel.add(comboBoxLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(metricsAllowableValuesRangeStubCombo, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(regularBoundLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(regularBound, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(highBoundLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(highBound, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(veryHighBoundLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(veryHighBound, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));


        myOKAction = new OkAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                metricsAllowableValueRangeStub.setRegularBound((Long) regularBound.getModel().getValue());
                metricsAllowableValueRangeStub.setHighBound((Long) highBound.getModel().getValue());
                metricsAllowableValueRangeStub.setVeryHighBound((Long) veryHighBound.getModel().getValue());
                if (metricsAllowableValueRangeStub.getHighBound() >= metricsAllowableValueRangeStub.getRegularBound()
                        && metricsAllowableValueRangeStub.getVeryHighBound() >= metricsAllowableValueRangeStub.getHighBound()) {
                    basicMetricsValidRangesSettings.removeFromUnControlledMetrics(metricsAllowableValueRangeStub.getName());
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

    public BasicMetricsValidRangeStub getMetricsAllowableValueRangeStub() {
        return metricsAllowableValueRangeStub;
    }

    public void setMetricsAllowableValueRangeStub(BasicMetricsValidRangeStub metricsAllowableValueRangeStub) {
        this.metricsAllowableValueRangeStub = metricsAllowableValueRangeStub;
        SpinnerNumberModel regularBoundModel = new SpinnerNumberModel(Long.valueOf(
                this.metricsAllowableValueRangeStub.getRegularBound()), Long.valueOf(0), Long.valueOf(1000000), Long.valueOf(1));
        SpinnerNumberModel highBoundModel = new SpinnerNumberModel(Long.valueOf(
                this.metricsAllowableValueRangeStub.getHighBound()), Long.valueOf(0), Long.valueOf(1000000), Long.valueOf(1));
        SpinnerNumberModel veryHighBoundModel = new SpinnerNumberModel(Long.valueOf(
                this.metricsAllowableValueRangeStub.getVeryHighBound()), Long.valueOf(0), Long.valueOf(1000000), Long.valueOf(1));
        regularBound.setModel(regularBoundModel);
        highBound.setModel(highBoundModel);
        veryHighBound.setModel(veryHighBoundModel);
    }
}
