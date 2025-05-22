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
import org.b333vv.metric.model.metric.MetricSet;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.URL;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NORTHWEST;

public class MetricsSetDescriptionPanel {
    private final JEditorPane metricDescription;
    private final JPanel rightMetricPanel;
    private final Project project;

    public MetricsSetDescriptionPanel(Project project) {
        this.project = project;
        rightMetricPanel = new JPanel(new GridBagLayout());
        metricDescription = new JEditorPane();
        metricDescription.setContentType("text/html");
        metricDescription.setEditable(false);

        JScrollPane scrollableMetricDescriptionPanel = ScrollPaneFactory.createScrollPane(
                metricDescription,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableMetricDescriptionPanel.getVerticalScrollBar().setUnitIncrement(10);

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        rightMetricPanel.add(scrollableMetricDescriptionPanel, new GridBagConstraints(0, 3, 4, 2, 1.0, 1.0,
                NORTHWEST, BOTH, insets, 40, 40));
    }

    public JComponent getPanel() {
        return rightMetricPanel;
    }

    public void setMetric(MetricSet metricsSet) {
        Border b = IdeBorderFactory.createTitledBorder(metricsSet.set());
        rightMetricPanel.setBorder(b);
        showDescription(metricsSet.url());
    }

    private void showDescription(String stringUrl) {
        try {
            URL url = MetricsSetDescriptionPanel.class.getResource(stringUrl);
            metricDescription.setPage(url);
        } catch (Exception e) {
            this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(e.getMessage());
//            MetricsUtils.getConsole().error(e.getMessage());
            metricDescription.setContentType("text/html");
            metricDescription.setText("<html>Page not found.</html>");
        }
    }
}
