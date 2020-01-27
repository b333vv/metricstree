package org.jacoquev.ui.info;

import com.intellij.icons.AllIcons;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.value.Range;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.URL;

import static java.awt.GridBagConstraints.*;

public class MetricsDescriptionPanel {
    JEditorPane jEditorPane;
    private JTextPane metricDescription;
    private JPanel rightMetricPanel;
    private JLabel allowableRangeLabel;
    private JLabel allowableRangeValue;
    private JLabel currentValueLabel;
    private JLabel currentValue;

    public MetricsDescriptionPanel() {
        rightMetricPanel = new JPanel(new GridBagLayout());
        allowableRangeLabel = new JLabel("Allowable value range:");
        allowableRangeValue = new JLabel();
        currentValueLabel = new JLabel("Calculated metrics value:");
        currentValue = new JLabel();
        jEditorPane = new JEditorPane();
        jEditorPane.setEditable(false);

        JScrollPane scrollableMetricDescriptionPanel = ScrollPaneFactory.createScrollPane(
                jEditorPane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableMetricDescriptionPanel.getVerticalScrollBar().setUnitIncrement(10);

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        rightMetricPanel.add(currentValueLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        rightMetricPanel.add(currentValue, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        rightMetricPanel.add(allowableRangeLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        rightMetricPanel.add(allowableRangeValue, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        rightMetricPanel.add(scrollableMetricDescriptionPanel, new GridBagConstraints(0, 2, 4, 2, 1.0, 1.0,
                NORTHWEST, BOTH, insets, 40, 40));
    }

    public JComponent getPanel() {
        return rightMetricPanel;
    }

    public void setMetric(Metric metric) {
        Border b = IdeBorderFactory.createTitledBorder(metric.getDescription());
        rightMetricPanel.setBorder(b);
        allowableRangeValue.setText(metric.getRange().toString());
        currentValue.setText(metric.getValue().toString());

        try {
            URL url = MetricsDescriptionPanel.class.getResource(metric.getDescriptionUrl());
            jEditorPane.setPage(url);
        } catch (Exception e) {
            jEditorPane.setContentType("text/html");
            jEditorPane.setText("<html>Page not found.</html>");
        }

        if (!metric.hasAllowableValue()) {
            currentValue.setIcon(AllIcons.General.BalloonError);
        } else if (metric.getRange() == Range.UNDEFINED_RANGE) {
            currentValue.setIcon(AllIcons.General.BalloonWarning);
        } else {
            currentValue.setIcon(AllIcons.Actions.Commit);
        }
    }

    public void clear() {
        allowableRangeValue.setText("");
        currentValue.setText("");
        currentValue.setIcon(null);
    }
}
