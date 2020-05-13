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

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.tree.node.*;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collectors.*;

public class MetricsValuesViolatorsTreeBuilder {

    @Nullable
    public DefaultTreeModel createMetricTreeModel(JavaProject javaProject) {
        Map<MetricType, Map<JavaClass, Metric>> classesByMetricTypes = javaProject.allClasses().flatMap(
                inner -> inner.metrics()
                        .filter(m -> m.getRange() != Range.UNDEFINED)
                        .collect(groupingBy(Metric::getType, groupingBy(i -> inner)))
                        .entrySet()
                        .stream())
                .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, new MetricsValuesViolatorsCollector())));

        ProjectNode projectNode = new ProjectNode(javaProject, "classes that violate recommended metric values");
        DefaultTreeModel model = new DefaultTreeModel(projectNode);
        model.setRoot(projectNode);

        classesByMetricTypes.forEach((key, value) -> {
            if (value.values().stream().anyMatch(m -> !m.hasAllowableValue())) {
                MetricTypeNode metricTypeNode = new MetricTypeNode(key);
                projectNode.add(metricTypeNode);
                value.forEach((k, v) -> {
                    if (!v.hasAllowableValue() && v.getValue() != Value.UNDEFINED) {
                        ViolatorClassNode violatorClassNode = new ViolatorClassNode(k, v);
                        metricTypeNode.add(violatorClassNode);
                    }
                });
            }
        });

        return model;
    }

    private static class MetricsValuesViolatorsCollector
            implements Collector<Map<JavaClass, List<Metric>>, Map<JavaClass, Metric>, Map<JavaClass, Metric>>
    {

        @Override
        public Supplier<Map<JavaClass, Metric>> supplier() {
            return HashMap::new;
        }

        @Override
        public BiConsumer<Map<JavaClass, Metric>, Map<JavaClass, List<Metric>>> accumulator() {
            return (map, val) -> {
                    Map.Entry<JavaClass, List<Metric>> entry = val.entrySet().iterator().next();
                    JavaClass javaClass = entry.getKey();
                    Metric metric = entry.getValue().get(0);
                    map.put(javaClass, metric);
            };
        }

        @Override
        public Function<Map<JavaClass, Metric>, Map<JavaClass, Metric>> finisher() {
            return MetricsUtils::sortByValueReversed;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of();
        }

        @Override
        public BinaryOperator<Map<JavaClass, Metric>> combiner() {
            return (map1, map2) -> {
                map2.forEach(map1::put);
                return map1;
            };
        }
    }
}
