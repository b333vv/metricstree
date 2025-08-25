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

import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricSet;
import org.b333vv.metric.model.metric.MetricType;
import icons.MetricsIcons;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.util.SettingsService;

import javax.swing.*;
import javax.swing.border.Border;
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
    private final Project project;

    public MetricsDescriptionPanel(Project project) {
        this.project = project;
        rightMetricPanel = new JPanel(new GridBagLayout());
        JLabel allowableRangeLabel = new JLabel("Regular Range:");
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
        Border b = IdeBorderFactory.createTitledBorder(metric.getType().description());
        rightMetricPanel.setBorder(b);
        metricLevel.setText(metric.getType().level().level());
        metricSet.setText(metric.getType().set().set());
        if (metric.getType().set() == MetricSet.MOOD) {
            allowableRangeValue.setText(this.project.getService(SettingsService.class).getRangeForMetric(metric.getType()).percentageFormat());
            currentValue.setText(metric.getPsiValue().percentageFormat());
        } else {
            allowableRangeValue.setText(this.project.getService(SettingsService.class).getRangeForMetric(metric.getType()).toString());
            currentValue.setText(metric.getPsiValue().toString());
        }

        showDescription(metric.getType().url());

        if (this.project.getService(SettingsService.class).isControlValidRanges()) {
            if (this.project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.REGULAR) {
                currentValue.setIcon(MetricsIcons.REGULAR_COLOR);
            }
            if (this.project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.HIGH) {
                currentValue.setIcon(MetricsIcons.HIGH_COLOR);
            }
            if (this.project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.VERY_HIGH) {
                currentValue.setIcon(MetricsIcons.VERY_HIGH_COLOR);
            }
            if (this.project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.EXTREME) {
                currentValue.setIcon(MetricsIcons.EXTREME_COLOR);
            }
            if (this.project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.UNDEFINED) {
                currentValue.setIcon(MetricsIcons.NOT_TRACKED);
            }
        } else {
            allowableRangeValue.setText("");
        }
    }

    public void setMetric(MetricType metricType) {
        Border b = IdeBorderFactory.createTitledBorder(metricType.description());
        rightMetricPanel.setBorder(b);

        metricLevel.setText(metricType.level().level());
        metricSet.setText(metricType.set().set());

        showDescription(metricType.url());
        allowableRangeValue.setText("");
    }

    public void setMetric(MetricSet metricsSet) {
        Border b = IdeBorderFactory.createTitledBorder(metricsSet.set());
        rightMetricPanel.setBorder(b);

        metricLevel.setText(metricsSet.level().level());
        metricSet.setText(metricsSet.set());

        showDescription(metricsSet.url());
        allowableRangeValue.setText("");
    }

    private void showDescription(String stringUrl) {
        try {
            URL url = MetricsDescriptionPanel.class.getResource(stringUrl);
            metricDescription.setPage(url);
        } catch (Exception e) {
            this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(e.getMessage());

            metricDescription.setContentType("text/html");
            metricDescription.setText("<html>Page not found.</html>");
        }
    }

    public void clear() {
        allowableRangeValue.setText("");
        currentValue.setText("");
        currentValue.setIcon(null);
    }
}
