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

package org.b333vv.metric.ui.info;

import com.intellij.icons.AllIcons;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.Sets;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.util.MetricsService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.net.URL;

import static java.awt.GridBagConstraints.*;

public class MetricsDescriptionPanel {
    private final JEditorPane metricDescription;
    private final JPanel rightMetricPanel;
    private final JLabel allowableRangeValue;
    private final JLabel currentValue;
    private final JLabel metricLevel;
    private final JLabel metricSet;

    public MetricsDescriptionPanel() {
        rightMetricPanel = new JPanel(new GridBagLayout());
        JLabel allowableRangeLabel = new JLabel("Valid Range:");
        allowableRangeValue = new JLabel();
        JLabel currentValueLabel = new JLabel("Calculated Metrics Value:");
        currentValue = new JLabel();
        metricLevel = new JLabel();
        metricSet = new JLabel();
        JLabel metricLevelLabel = new JLabel("Metrics Level:");
        JLabel metricSetLabel = new JLabel("Metrics Set:");

        metricDescription = new JEditorPane();
        metricDescription.setContentType("text/html");
        metricDescription.setEditable(false);

//        if (UIUtil.isUnderDarcula()) {
//            HTMLEditorKit kit = new HTMLEditorKit();
//            StyleSheet styleSheet = new StyleSheet();
//            styleSheet.addRule("body { background-color: #303030 }");
//            styleSheet.addRule("body { color: #D3D3D3; }");
//            kit.getStyleSheet().addStyleSheet(styleSheet);
//            metricDescription.setEditorKit(kit);
//        }

        JScrollPane scrollableMetricDescriptionPanel = ScrollPaneFactory.createScrollPane(
                metricDescription,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableMetricDescriptionPanel.getVerticalScrollBar().setUnitIncrement(10);

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        rightMetricPanel.add(currentValueLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        rightMetricPanel.add(currentValue, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));


        rightMetricPanel.add(allowableRangeLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        rightMetricPanel.add(allowableRangeValue, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));

        rightMetricPanel.add(metricLevelLabel, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        rightMetricPanel.add(metricLevel, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));

        rightMetricPanel.add(metricSetLabel, new GridBagConstraints(2, 1, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
        rightMetricPanel.add(metricSet, new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));

        rightMetricPanel.add(scrollableMetricDescriptionPanel, new GridBagConstraints(0, 3, 4, 2, 1.0, 1.0,
                NORTHWEST, BOTH, insets, 40, 40));
    }

    public JComponent getPanel() {
        return rightMetricPanel;
    }

    public void setMetric(Metric metric) {
        Border b = IdeBorderFactory.createTitledBorder(metric.getDescription());
        rightMetricPanel.setBorder(b);
        if (Sets.inMoodMetricsSet(metric.getName())) {
            allowableRangeValue.setText(metric.getRange().percentageFormat());
            currentValue.setText(metric.getValue().percentageFormat());
            metricLevel.setText("Project level");
            metricSet.setText("MOOD metrics set");
        } else  if (Sets.inRobertMartinMetricsSet(metric.getName())) {
            allowableRangeValue.setText(metric.getRange().toString());
            currentValue.setText(metric.getValue().toString());
            metricLevel.setText("Package level");
            metricSet.setText("Robert C. Martin metrics set");
        } else  if (Sets.inChidamberKemererMetricsSet(metric.getName())) {
            allowableRangeValue.setText(metric.getRange().toString());
            currentValue.setText(metric.getValue().toString());
            metricLevel.setText("Class level");
            metricSet.setText("Chidamber-Kemerer metrics set");
        } else  if (Sets.inLorenzKiddMetricsSet(metric.getName())) {
            allowableRangeValue.setText(metric.getRange().toString());
            currentValue.setText(metric.getValue().toString());
            metricLevel.setText("Class level");
            metricSet.setText("Lorenz-Kidd metrics set");
        } else  if (Sets.inLiHenryMetricsSet(metric.getName())) {
            allowableRangeValue.setText(metric.getRange().toString());
            currentValue.setText(metric.getValue().toString());
            metricLevel.setText("Class level");
            metricSet.setText("Li-Henry metrics set");
        } else {
            allowableRangeValue.setText(metric.getRange().toString());
            currentValue.setText(metric.getValue().toString());
            metricLevel.setText("Method level");
            metricSet.setText("-");
        }

        try {
            URL url = MetricsDescriptionPanel.class.getResource(metric.getDescriptionUrl());
            metricDescription.setPage(url);
        } catch (Exception e) {
            metricDescription.setContentType("text/html");
            metricDescription.setText("<html>Page not found.</html>");
        }

        if (MetricsService.isControlValidRanges()) {
            if (!metric.hasAllowableValue()) {
                currentValue.setIcon(AllIcons.General.BalloonError);
            } else if (metric.getRange() == Range.UNDEFINED) {
                currentValue.setIcon(AllIcons.General.BalloonWarning);
            } else {
                currentValue.setIcon(AllIcons.Actions.Commit);
            }
        } else {
            allowableRangeValue.setText("");
        }
    }

    public void clear() {
        allowableRangeValue.setText("");
        currentValue.setText("");
        currentValue.setIcon(null);
    }
}
