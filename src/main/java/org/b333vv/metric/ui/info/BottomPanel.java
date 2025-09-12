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

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricSet;
import org.b333vv.metric.model.metric.MetricType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class BottomPanel {
    private final JPanel panel;
    private JTextPane metricDescription;

    public BottomPanel() {
        panel = new JPanel(new BorderLayout());
        panel.setBorder(IdeBorderFactory.createBorder(SideBorder.LEFT));
        clear();
        show();
    }

    public void clear() {
        metricDescription = null;
        panel.removeAll();
        JComponent titleComp = new JLabel("", SwingConstants.LEFT);
        panel.add(titleComp, BorderLayout.CENTER);
        panel.revalidate();
    }

    public void show() {
        panel.setVisible(true);
    }

    public JComponent getPanel() {
        return panel;
    }

    public void setData(@NotNull Metric metric) {
        updateDescription("Metric: "
                + metric.getType().description()
                + " [" + metric.getType().name()
                + " = "
                + (metric.getType().set() == MetricSet.MOOD
                        ? metric.getPsiValue().percentageFormat()
                        : metric.getFormattedValue())
                + "]");
    }

    public void setData(@NotNull MetricType metricType) {
        updateDescription("Metric: "
                + metricType.description()
                + " [" + metricType.name()
                + "]");
    }

    public void setData(@NotNull ProjectElement javaProject) {
        updateDescription("Project: " + javaProject.getName());
    }

    public void setData(@NotNull PackageElement javaPackage) {
        updateDescription("Package: " + javaPackage.getName());
    }

    public void setData(@NotNull FileElement javaFile) {
        updateDescription("File: " + javaFile.getName());
    }

    public void setData(@NotNull ClassElement javaClass) {
        updateDescription("Class: " + javaClass.getName());
    }

    public void setData(@NotNull MethodElement javaMethod) {
        updateDescription("Method: " + javaMethod.getName());
    }

    public void setData(String text) {
        updateDescription(text);
    }

    private void updateDescription(String text) {
        panel.removeAll();
        metricDescription = new JTextPane();
        metricDescription.setText(text);
        panel.add(metricDescription, BorderLayout.CENTER);
        panel.revalidate();
    }
}
