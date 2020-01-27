package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.jacoquev.util.MetricsSettings;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;

import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;

public class EditMetricsDialog extends DialogWrapper {
    private JPanel panel;
    private JLabel minValueLabel;
    private JLabel maxValueLabel;
    private JSpinner minValue;
    private JSpinner maxValue;
    private MetricsSettings.MetricStub metricStub = null;
    private boolean spinnerIsDouble;


    public EditMetricsDialog(Project project) {
        super(project, false);
        setTitle("Edit metrics settings");

        panel = new JPanel(new GridBagLayout());
        minValueLabel = new JLabel("Minimum of allowed value:");
        maxValueLabel = new JLabel("Maximum of allowed value:");
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
                    metricStub.setMinDoubleValue((Double) minValue.getModel().getValue());
                    metricStub.setMaxDoubleValue((Double) maxValue.getModel().getValue());
                    if (metricStub.getMaxDoubleValue() >= metricStub.getMinDoubleValue()) {
                        super.actionPerformed(e);
                        dispose();
                    }
                } else {
                    metricStub.setMinLongValue((Long) minValue.getModel().getValue());
                    metricStub.setMaxLongValue((Long) maxValue.getModel().getValue());
                    if (metricStub.getMaxLongValue() >= metricStub.getMinLongValue()) {
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

    public MetricsSettings.MetricStub getMetricStub() {
        return metricStub;
    }

    public void setMetricStub(MetricsSettings.MetricStub metricStub) {
        this.metricStub = metricStub;
        Border b = IdeBorderFactory.createTitledBorder(this.metricStub.getDescription());
        panel.setBorder(b);
        SpinnerNumberModel minModel;
        SpinnerNumberModel maxModel;
        if (metricStub.isDoubleValue()) {
            spinnerIsDouble = true;
            minModel = new SpinnerNumberModel(Double.valueOf(this.metricStub.getMinDoubleValue()),
                    Double.valueOf(0.00), Double.valueOf(1000000.00), Double.valueOf(0.01));
            maxModel = new SpinnerNumberModel(Double.valueOf(this.metricStub.getMaxDoubleValue()),
                    Double.valueOf(0.00), Double.valueOf(1000000.00), Double.valueOf(0.01));
        } else {
            spinnerIsDouble = false;
            minModel = new SpinnerNumberModel(Long.valueOf(this.metricStub.getMinLongValue()),
                    Long.valueOf(0), Long.valueOf(1000000), Long.valueOf(1));
            maxModel = new SpinnerNumberModel(Long.valueOf(this.metricStub.getMaxLongValue()),
                    Long.valueOf(0), Long.valueOf(1000000), Long.valueOf(1));
        }
        minValue.setModel(minModel);
        maxValue.setModel(maxModel);
    }
}
