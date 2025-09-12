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

package org.b333vv.metric.ui.treemap.builder;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.treemap.model.ColorProvider;
import org.b333vv.metric.ui.treemap.model.Rectangle;
import org.b333vv.metric.ui.treemap.model.TreeModel;
import org.b333vv.metric.util.SettingsService;


import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricTypeColorProvider implements ColorProvider<CodeElement, Color> {
    private static final Color UNDEFINED = new JBColor(new Color(0x979797), new Color(0x979797));
    private static final Color REGULAR = new JBColor(new Color(0x499C54), new Color(0x499C54));
    private static final Color HIGH = new JBColor(new Color(0xf9c784), new Color(0xf9c784));
    private static final Color VERY_HIGH = new JBColor(new Color(0xfc7a1e), new Color(0xfc7a1e));
    private static final Color EXTREME = new JBColor(new Color(0xf24c00), new Color(0xf24c00));

    private final MetricType metricType;
    private final Project project;

    public MetricTypeColorProvider(MetricType metricType, Project project) {
        this.project = project;
        this.metricType = metricType;
    }

    @Override
    public Color getColor(TreeModel<Rectangle<CodeElement>> model, Rectangle<CodeElement> rectangle) {
        if (rectangle.getNode() instanceof ClassElement) {
            Map<MetricType, Metric> metrics = rectangle.getNode().metrics().collect(Collectors.toMap(Metric::getType, m -> m));
            if (!metrics.containsKey(metricType)) {
                return UNDEFINED;
            }

            Value value = rectangle.getNode().metric(metricType).getPsiValue();

            if (project.getService(SettingsService.class).getRangeForMetric(metricType).getRangeType(value) == RangeType.UNDEFINED) {
                return UNDEFINED;
            }
            if (project.getService(SettingsService.class).getRangeForMetric(metricType).getRangeType(value) == RangeType.REGULAR) {
                return REGULAR;
            }
            if (project.getService(SettingsService.class).getRangeForMetric(metricType).getRangeType(value) == RangeType.HIGH) {
                return HIGH;
            }
            if (project.getService(SettingsService.class).getRangeForMetric(metricType).getRangeType(value) == RangeType.VERY_HIGH) {
                return VERY_HIGH;
            }
            if (project.getService(SettingsService.class).getRangeForMetric(metricType).getRangeType(value) == RangeType.EXTREME) {
                return EXTREME;
            }
        }
        return REGULAR;
    }
}
