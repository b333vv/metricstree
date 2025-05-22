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

import com.intellij.ui.JBColor;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.treemap.model.ColorProvider;
import org.b333vv.metric.ui.treemap.model.Rectangle;
import org.b333vv.metric.ui.treemap.model.TreeModel;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;

import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricTypeColorProvider implements ColorProvider<JavaCode, Color> {
    private static final Color UNDEFINED = new JBColor(new Color(0x979797), new Color(0x979797));
    private static final Color REGULAR = new JBColor(new Color(0x499C54), new Color(0x499C54));
    private static final Color HIGH = new JBColor(new Color(0xf9c784), new Color(0xf9c784));
    private static final Color VERY_HIGH = new JBColor(new Color(0xfc7a1e), new Color(0xfc7a1e));
    private static final Color EXTREME = new JBColor(new Color(0xf24c00), new Color(0xf24c00));

    private final MetricType metricType;

    public MetricTypeColorProvider(MetricType metricType) {
        this.metricType = metricType;
    }

    @Override
    public Color getColor(TreeModel<Rectangle<JavaCode>> model, Rectangle<JavaCode> rectangle) {
        if (rectangle.getNode() instanceof JavaClass) {
            Map<MetricType, Metric> metrics = rectangle.getNode().metrics().collect(Collectors.toMap(Metric::getType, m -> m));
            if (!metrics.containsKey(metricType)) {
                MetricsUtils.getConsole().debug("Metric " + metricType + " is not calculated for " + rectangle.getNode().getName());
                return UNDEFINED;
            }

            Value value = rectangle.getNode().metric(metricType).getValue();

            if (project.getService(MetricsService.class).getRangeForMetric(metricType).getRangeType(value) == RangeType.UNDEFINED) {
                return UNDEFINED;
            }
            if (project.getService(MetricsService.class).getRangeForMetric(metricType).getRangeType(value) == RangeType.REGULAR) {
                return REGULAR;
            }
            if (project.getService(MetricsService.class).getRangeForMetric(metricType).getRangeType(value) == RangeType.HIGH) {
                return HIGH;
            }
            if (project.getService(MetricsService.class).getRangeForMetric(metricType).getRangeType(value) == RangeType.VERY_HIGH) {
                return VERY_HIGH;
            }
            if (project.getService(MetricsService.class).getRangeForMetric(metricType).getRangeType(value) == RangeType.EXTREME) {
                return EXTREME;
            }
        }
        return REGULAR;
    }
}
