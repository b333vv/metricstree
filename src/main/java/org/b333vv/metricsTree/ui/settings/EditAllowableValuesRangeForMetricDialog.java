package org.b333vv.metricsTree.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;

import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;

public class EditAllowableValuesRangeForMetricDialog extends DialogWrapper {
    private JPanel panel;
    private JLabel minValueLabel;
    private JLabel maxValueLabel;
    private JSpinner minValue;
    private JSpinner maxValue;
    private MetricsAllowableValuesRangeStub metricsAllowableValueRangeStub = null;
    private boolean spinnerIsDouble;


    public EditAllowableValuesRangeForMetricDialog(Project project) {
        super(project, false);
        setTitle("Edit Allowable Values Range For Metric");

        panel = new JPanel(new GridBagLayout());
        minValueLabel = new JLabel("Minimum Allowed Value");
        maxValueLabel = new JLabel("Maximum Allowed Value");
        minValue = new JSpinner();
        maxValue = new JSpinner();

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        panel.add(minValueLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(minValue, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(maxValueLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        panel.add(maxValue, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));


        myOKAction = new OkAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (spinnerIsDouble) {
                    metricsAllowableValueRangeStub.setMinDoubleValue((Double) minValue.getModel().getValue());
                    metricsAllowableValueRangeStub.setMaxDoubleValue((Double) maxValue.getModel().getValue());
                    if (metricsAllowableValueRangeStub.getMaxDoubleValue() >= metricsAllowableValueRangeStub.getMinDoubleValue()) {
                        super.actionPerformed(e);
                        dispose();
                    }
                } else {
                    metricsAllowableValueRangeStub.setMinLongValue((Long) minValue.getModel().getValue());
                    metricsAllowableValueRangeStub.setMaxLongValue((Long) maxValue.getModel().getValue());
                    if (metricsAllowableValueRangeStub.getMaxLongValue() >= metricsAllowableValueRangeStub.getMinLongValue()) {
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
        Border b = IdeBorderFactory.createTitledBorder(this.metricsAllowableValueRangeStub.getDescription());
        panel.setBorder(b);
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
