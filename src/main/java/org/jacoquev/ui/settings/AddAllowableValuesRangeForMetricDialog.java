package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;

public class AddAllowableValuesRangeForMetricDialog extends DialogWrapper {
    private JPanel panel;
    private ComboBox metricsAllowableValuesRangeStubCombo;
    private JLabel minValueLabel;
    private JLabel maxValueLabel;
    private JLabel comboBoxLabel;
    private JSpinner minValue;
    private JSpinner maxValue;
    private MetricsAllowableValuesRangeStub metricsAllowableValueRangeStub = null;
    private boolean spinnerIsDouble;


    public AddAllowableValuesRangeForMetricDialog(Project project) {
        super(project, false);
        setTitle("Add Allowable Values Range For Metric");

        MetricsAllowableValuesRangesSettings metricsAllowableValuesRangesSettings = MetricsUtils.get(project, MetricsAllowableValuesRangesSettings.class);
        List<MetricsAllowableValuesRangeStub> uncontrolledMetrics = metricsAllowableValuesRangesSettings.getUnControlledMetricsList();
        metricsAllowableValuesRangeStubCombo = new ComboBox(uncontrolledMetrics.toArray());

        metricsAllowableValuesRangeStubCombo.addItemListener(arg -> {
            metricsAllowableValueRangeStub = (MetricsAllowableValuesRangeStub)
                    metricsAllowableValuesRangeStubCombo.getSelectedItem();
            setMetricsAllowableValueRangeStub(metricsAllowableValueRangeStub);
        });

        panel = new JPanel(new GridBagLayout());
        minValueLabel = new JLabel("Minimum Allowed Value");
        maxValueLabel = new JLabel("Maximum Allowed Value");
        comboBoxLabel = new JLabel("Metric");
        minValue = new JSpinner();
        maxValue = new JSpinner();
        setMetricsAllowableValueRangeStub((MetricsAllowableValuesRangeStub)
                metricsAllowableValuesRangeStubCombo.getSelectedItem());

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
                if (spinnerIsDouble) {
                    metricsAllowableValueRangeStub.setMinDoubleValue((Double) minValue.getModel().getValue());
                    metricsAllowableValueRangeStub.setMaxDoubleValue((Double) maxValue.getModel().getValue());
                    if (metricsAllowableValueRangeStub.getMaxDoubleValue() >= metricsAllowableValueRangeStub.getMinDoubleValue()) {
                        metricsAllowableValuesRangesSettings.removeFromUnControlledMetrics(metricsAllowableValueRangeStub.getName());
                        super.actionPerformed(e);
                        dispose();
                    }
                } else {
                    metricsAllowableValueRangeStub.setMinLongValue((Long) minValue.getModel().getValue());
                    metricsAllowableValueRangeStub.setMaxLongValue((Long) maxValue.getModel().getValue());
                    if (metricsAllowableValueRangeStub.getMaxLongValue() >= metricsAllowableValueRangeStub.getMinLongValue()) {
                        metricsAllowableValuesRangesSettings.removeFromUnControlledMetrics(metricsAllowableValueRangeStub.getName());
                        super.actionPerformed(e);
                        dispose();
                    }
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

    public MetricsAllowableValuesRangeStub getMetricsAllowableValueRangeStub() {
        return metricsAllowableValueRangeStub;
    }

    public void setMetricsAllowableValueRangeStub(MetricsAllowableValuesRangeStub metricsAllowableValueRangeStub) {
        this.metricsAllowableValueRangeStub = metricsAllowableValueRangeStub;
        SpinnerNumberModel minModel;
        SpinnerNumberModel maxModel;
        if (metricsAllowableValueRangeStub.isDoubleValue()) {
            spinnerIsDouble = true;
            minModel = new SpinnerNumberModel(Double.valueOf(this.metricsAllowableValueRangeStub.getMinDoubleValue()),
                    Double.valueOf(0.00), Double.valueOf(1000000.00), Double.valueOf(0.01));
            maxModel = new SpinnerNumberModel(Double.valueOf(this.metricsAllowableValueRangeStub.getMaxDoubleValue()),
                    Double.valueOf(0.00), Double.valueOf(1000000.00), Double.valueOf(0.01));
        } else {
            spinnerIsDouble = false;
            minModel = new SpinnerNumberModel(Long.valueOf(this.metricsAllowableValueRangeStub.getMinLongValue()),
                    Long.valueOf(0), Long.valueOf(1000000), Long.valueOf(1));
            maxModel = new SpinnerNumberModel(Long.valueOf(this.metricsAllowableValueRangeStub.getMaxLongValue()),
                    Long.valueOf(0), Long.valueOf(1000000), Long.valueOf(1));
        }
        minValue.setModel(minModel);
        maxValue.setModel(maxModel);
    }
}
