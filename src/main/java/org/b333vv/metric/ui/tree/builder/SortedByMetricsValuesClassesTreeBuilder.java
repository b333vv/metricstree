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

package org.b333vv.metric.ui.tree.builder;

import com.intellij.openapi.project.Project;
import icons.MetricsIcons;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.ui.tree.node.GrouppingNode;
import org.b333vv.metric.ui.tree.node.MetricTypeNode;
import org.b333vv.metric.ui.tree.node.ProjectNode;
import org.b333vv.metric.ui.tree.node.SortedByMetricsValueClassNode;
import org.b333vv.metric.util.SettingsService;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import java.util.Map;

import static org.b333vv.metric.builder.ClassesByMetricsValuesDistributor.classesByMetricsValuesDistribution;

public class SortedByMetricsValuesClassesTreeBuilder {

    @Nullable
    public DefaultTreeModel createMetricTreeModel(ProjectElement javaProject, Project project) {
        Map<MetricType, Map<ClassElement, Metric>> classesByMetricTypes = classesByMetricsValuesDistribution(javaProject, project);

        ProjectNode projectNode = new ProjectNode(javaProject, "class distribution by metric values", MetricsIcons.SORT_BY_VALUES);
        DefaultTreeModel model = new DefaultTreeModel(projectNode);
        model.setRoot(projectNode);

        classesByMetricTypes.forEach((key, value) -> {
            if (value.values().stream()
                    .anyMatch(m -> project.getService(SettingsService.class).getRangeForMetric(m.getType()).getRangeType(m.getPsiValue()) != RangeType.REGULAR)) {
                MetricTypeNode metricTypeNode = new MetricTypeNode(key);
                projectNode.add(metricTypeNode);
                GrouppingNode high = new GrouppingNode("high", MetricsIcons.HIGH_COLOR);
                GrouppingNode veryHigh = new GrouppingNode("very-high", MetricsIcons.VERY_HIGH_COLOR);
                GrouppingNode extreme = new GrouppingNode("extreme", MetricsIcons.EXTREME_COLOR);
                metricTypeNode.add(high);
                metricTypeNode.add(veryHigh);
                metricTypeNode.add(extreme);

                value.forEach((k, v) -> {
                    if (project.getService(SettingsService.class).getRangeForMetric(v.getType()).getRangeType(v.getPsiValue()) == RangeType.HIGH) {
                        SortedByMetricsValueClassNode sortedByMetricsValueClassNode = new SortedByMetricsValueClassNode(k, v);
                        high.add(sortedByMetricsValueClassNode);
                    }
                    if (project.getService(SettingsService.class).getRangeForMetric(v.getType()).getRangeType(v.getPsiValue()) == RangeType.VERY_HIGH) {
                        SortedByMetricsValueClassNode sortedByMetricsValueClassNode = new SortedByMetricsValueClassNode(k, v);
                        veryHigh.add(sortedByMetricsValueClassNode);
                    }
                    if (project.getService(SettingsService.class).getRangeForMetric(v.getType()).getRangeType(v.getPsiValue()) == RangeType.EXTREME) {
                        SortedByMetricsValueClassNode sortedByMetricsValueClassNode = new SortedByMetricsValueClassNode(k, v);
                        extreme.add(sortedByMetricsValueClassNode);
                    }
                });
            }
        });
        return model;
    }

}
