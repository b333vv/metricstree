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

import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.scale.JBUIScale;
import icons.MetricsIcons;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.ui.tree.CompositeIcon;
import org.b333vv.metric.ui.tree.TreeCellRenderer;
import org.b333vv.metric.util.MetricsService;

import javax.swing.*;

public class MetricTypeNode extends AbstractNode {

    private final MetricType metricType;

    public MetricTypeNode(MetricType metricType) {
        this.metricType = metricType;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    protected Icon getIcon() {
        return MetricsIcons.CLASS_METRIC;
    }

    protected String getMetricTypeName() {
        return metricType.description();
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        renderer.setIcon(getIcon());
        renderer.append(getMetricTypeName());
        renderer.append(": " + MetricsService.getRangeForMetric(metricType).toString());
        renderer.append(" [" + this.children.size() + " classes]", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES);
    }
}
