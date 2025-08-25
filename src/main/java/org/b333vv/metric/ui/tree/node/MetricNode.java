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

package org.b333vv.metric.ui.tree.node;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.scale.JBUIScale;
import icons.MetricsIcons;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricSet;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.tree.CompositeIcon;
import org.b333vv.metric.ui.tree.TreeCellRenderer;
import org.b333vv.metric.util.SettingsService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MetricNode extends AbstractNode {

    protected final Metric metric;
    protected final Icon icon;
    private final Project project;

    public MetricNode(Metric metric, Icon icon, Project project) {
        this.project = project;
        this.metric = metric;
        this.icon = icon;
    }

    public MetricNode(Metric metric, Project project) {
        this.metric = metric;
        this.project = project;
        this.icon = AllIcons.Nodes.Artifact;
    }

    protected Icon getIcon() {
        return icon;
    }

    protected String getMetricName() {
        return metric.getType().description() + ": ";
    }

    @NotNull
    private String getMetricValue() {
        if (metric.getType().set() == MetricSet.MOOD) {
            return metric.getPsiValue().percentageFormat();
        } else {
            return metric.getFormattedValue();
        }
    }

    public Metric getMetric() {
        return metric;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        int gap = JBUIScale.isUsrHiDPI() ? 8 : 4;
        renderer.append(getMetricName());
        if (project.getService(SettingsService.class).isControlValidRanges()) {
            if (metric.getPsiValue() == Value.UNDEFINED) {
                renderer.setIconToolTip("This metric was not calculated");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(), MetricsIcons.NA));
                renderer.append(getMetricValue());
            } else if (project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.VERY_HIGH) {
                renderer.setIconToolTip("This metric has very-high value");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(), MetricsIcons.VERY_HIGH_COLOR));
                renderer.append(getMetricValue(), SimpleTextAttributes.ERROR_ATTRIBUTES);
            } else if (project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.EXTREME) {
                renderer.setIconToolTip("This metric has extreme value");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(), MetricsIcons.EXTREME_COLOR));
                renderer.append(getMetricValue(), SimpleTextAttributes.ERROR_ATTRIBUTES);
            } else if (project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.HIGH) {
                renderer.setIconToolTip("This metric has high value");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(), MetricsIcons.HIGH_COLOR));
                renderer.append(getMetricValue());
            } else if (project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.REGULAR) {
                renderer.setIconToolTip("This metric has regular value");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(), MetricsIcons.REGULAR_COLOR));
                renderer.append(getMetricValue());
            } else if (project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.UNDEFINED) {
                renderer.setIconToolTip("The desired value range is not set for this metric");
                renderer.setIcon(new CompositeIcon(CompositeIcon.Axis.X_AXIS, gap, getIcon(), MetricsIcons.NOT_TRACKED));
                renderer.append(getMetricValue());
            }
        } else {
            if (metric.getPsiValue() == Value.UNDEFINED) {
                renderer.setIconToolTip("This metric was not calculated");
                renderer.setIcon(MetricsIcons.NA);
            } else {
                renderer.setIcon(getIcon());
            }
            renderer.append(getMetricValue());
        }
    }
}
